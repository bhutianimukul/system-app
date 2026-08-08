package app.gakseong.sense

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The usage fold, exhaustively. This is the one piece of phase 04 an emulator cannot check: its own usage
 * history is thin, and the cases that matter are the ones where an event straddles the window edge.
 */
class FoldTest {

    private val start = 0L
    private val end = 60 * MIN

    private fun fg(pkg: String, at: Long) = UsageEvent(pkg, EventType.FOREGROUND, at)
    private fun bg(pkg: String, at: Long) = UsageEvent(pkg, EventType.BACKGROUND, at)
    private fun screenOff(at: Long) = UsageEvent("android", EventType.SCREEN_NON_INTERACTIVE, at)
    private fun screenOn(at: Long) = UsageEvent("android", EventType.SCREEN_INTERACTIVE, at)

    @Test
    fun `an empty stream reads as a clean window`() {
        val r = fold(emptyList(), start, end)
        assertEquals(0L, r.totalForegroundMs)
        assertEquals(0L, r.longestScreenOffMs)
        assertTrue(r.packagesOpened.isEmpty())
        assertTrue(r.available)
    }

    @Test
    fun `one session counts once`() {
        val r = fold(listOf(fg("insta", 5 * MIN), bg("insta", 20 * MIN)), start, end)
        assertEquals(15 * MIN, r.totalForegroundMs)
        assertEquals(15 * MIN, r.perPackageMs["insta"])
        assertEquals(setOf("insta"), r.packagesOpened)
    }

    @Test
    fun `two sessions of the same app add up`() {
        val r = fold(
            listOf(fg("insta", 0), bg("insta", 10 * MIN), fg("insta", 30 * MIN), bg("insta", 35 * MIN)),
            start, end,
        )
        assertEquals(15 * MIN, r.perPackageMs["insta"])
    }

    @Test
    fun `an app still in front at window close counts to the edge and no further`() {
        val r = fold(listOf(fg("insta", 50 * MIN)), start, end)
        assertEquals(10 * MIN, r.totalForegroundMs)
    }

    @Test
    fun `a session that began before the window only counts from the window start`() {
        val r = fold(listOf(fg("insta", -30 * MIN), bg("insta", 10 * MIN)), start, end)
        assertEquals(10 * MIN, r.totalForegroundMs)
    }

    @Test
    fun `switching apps without a background event closes the previous one`() {
        // Real streams drop BACKGROUND pairs regularly. Without this, the first app runs to the window edge.
        val r = fold(listOf(fg("insta", 0), fg("reddit", 10 * MIN), bg("reddit", 15 * MIN)), start, end)
        assertEquals(10 * MIN, r.perPackageMs["insta"])
        assertEquals(5 * MIN, r.perPackageMs["reddit"])
        assertEquals(15 * MIN, r.totalForegroundMs)
    }

    @Test
    fun `a stale background event for another app is ignored`() {
        val r = fold(listOf(fg("insta", 0), bg("reddit", 5 * MIN), bg("insta", 10 * MIN)), start, end)
        assertEquals(10 * MIN, r.perPackageMs["insta"])
        assertEquals(null, r.perPackageMs["reddit"])
    }

    @Test
    fun `the longest screen off block is the longest, not the last`() {
        val r = fold(
            listOf(
                screenOff(0), screenOn(45 * MIN),
                screenOff(50 * MIN), screenOn(55 * MIN),
            ),
            start, end,
        )
        assertEquals(45 * MIN, r.longestScreenOffMs)
    }

    @Test
    fun `a screen still off at window close counts to the edge`() {
        val r = fold(listOf(screenOff(20 * MIN)), start, end)
        assertEquals(40 * MIN, r.longestScreenOffMs)
    }

    @Test
    fun `a screen off block that began before the window is clipped to it`() {
        val r = fold(listOf(screenOff(-90 * MIN), screenOn(10 * MIN)), start, end)
        assertEquals(10 * MIN, r.longestScreenOffMs)
    }

    @Test
    fun `an unknown screen state does not invent a block`() {
        // A window opening mid-sleep sees SCREEN_INTERACTIVE first. Assuming the screen was on before that
        // would be equally wrong, so nothing is claimed until an off event actually arrives.
        val r = fold(listOf(screenOn(10 * MIN)), start, end)
        assertEquals(0L, r.longestScreenOffMs)
    }

    @Test
    fun `the screen going off ends whatever was in front of it`() {
        val r = fold(listOf(fg("insta", 0), screenOff(10 * MIN), screenOn(20 * MIN)), start, end)
        assertEquals(10 * MIN, r.perPackageMs["insta"])
        assertEquals(10 * MIN, r.longestScreenOffMs)
    }

    @Test
    fun `reader time is carved out of the total but kept per package`() {
        // §24: time in the reader counts as reading, not as screen time, in every threshold that measures it.
        val r = fold(
            listOf(fg("insta", 0), bg("insta", 10 * MIN), fg("app.gakseong", 20 * MIN), bg("app.gakseong", 40 * MIN)),
            start, end, carveOut = setOf("app.gakseong"),
        )
        assertEquals(10 * MIN, r.totalForegroundMs)
        assertEquals(20 * MIN, r.perPackageMs["app.gakseong"])
    }

    @Test
    fun `a running app carved out during a raid window does not eat the day's budget`() {
        // §24's harder case: Strava holds the foreground for the whole run.
        val r = fold(listOf(fg("com.strava", 0), bg("com.strava", 55 * MIN)), start, end, carveOut = setOf("com.strava"))
        assertEquals(0L, r.totalForegroundMs)
    }

    @Test
    fun `events after the window are not counted`() {
        val r = fold(listOf(fg("insta", 70 * MIN), bg("insta", 80 * MIN)), start, end)
        assertEquals(0L, r.totalForegroundMs)
        assertTrue(r.packagesOpened.isEmpty())
    }

    @Test
    fun `an out of order stream folds the same as a sorted one`() {
        val sorted = listOf(fg("insta", 0), bg("insta", 10 * MIN), fg("reddit", 20 * MIN), bg("reddit", 25 * MIN))
        assertEquals(fold(sorted, start, end), fold(sorted.reversed(), start, end))
    }

    @Test
    fun `a zero length window is legal and empty`() {
        assertEquals(0L, fold(listOf(fg("insta", 0)), 0L, 0L).totalForegroundMs)
    }

    @Test
    fun `an inverted window is rejected rather than silently returning zero`() {
        // Silently returning zero here would read as "a clean day" and quietly clear a threshold.
        val threw = runCatching { fold(emptyList(), end, start) }.isFailure
        assertTrue("an inverted window must throw", threw)
    }

    @Test
    fun `unavailable is distinguishable from a clean window`() {
        assertFalse(UsageReading.Unavailable.available)
        assertTrue(fold(emptyList(), start, end).available)
    }

    private companion object {
        const val MIN = 60_000L
    }
}

/** The two the emulator caught that nineteen unit tests did not. */
class FoldCeilingTest {

    private fun fg(pkg: String, at: Long) = UsageEvent(pkg, EventType.FOREGROUND, at)

    @Test
    fun `an unclosed session is capped rather than running across an idle gap`() {
        // A stream that drops BACKGROUND on a device whose screen never sleeps. This produced 231 hours.
        val day = 24 * 60 * 60 * 1000L
        val r = fold(listOf(fg("insta", 0)), 0L, 10 * day)
        assertEquals(MAX_SESSION_MS, r.totalForegroundMs)
    }

    @Test
    fun `a session shorter than the cap is untouched`() {
        val r = fold(listOf(fg("insta", 0), UsageEvent("insta", EventType.BACKGROUND, 90 * 60_000L)), 0L, 24 * 3_600_000L)
        assertEquals(90 * 60_000L, r.totalForegroundMs)
    }

    @Test
    fun `activity stopped closes a session when background never arrived`() {
        val r = fold(
            listOf(fg("insta", 0), UsageEvent("insta", EventType.ACTIVITY_STOPPED, 20 * 60_000L)),
            0L, 60 * 60_000L,
        )
        assertEquals(20 * 60_000L, r.totalForegroundMs)
    }
}
