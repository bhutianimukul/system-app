package app.gakseong.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The grace window and the break rule, which are the only two things in a session that can be got wrong. */
class SessionTest {

    private val start = 0L
    private fun session(minutes: Int = 45) = FocusState(startedAtMs = start, lengthMinutes = minutes)

    private fun run(state: FocusState, vararg steps: Pair<Long, String?>): FocusState =
        steps.fold(state) { s, (at, fg) -> step(s, at, fg) }

    // ── the grace window ─────────────────────────────────────────────────────

    @Test
    fun `a mis-tap does not cost the session`() {
        // Leaving and coming straight back is not a broken session. A rule that punishes it teaches people to
        // be afraid of their own phone rather than deliberate with it.
        val s = run(session(), 60_000L to "com.instagram.android", 65_000L to null)
        assertFalse(s.broken)
    }

    @Test
    fun `staying away past the grace window breaks it`() {
        val s = run(session(), 60_000L to "com.instagram.android", 60_000L + RETURN_GRACE_MS + 1 to "com.instagram.android")
        assertTrue(s.broken)
    }

    @Test
    fun `exactly at the grace boundary is still forgiven`() {
        val s = run(session(), 60_000L to "com.instagram.android", 60_000L + RETURN_GRACE_MS to "com.instagram.android")
        assertFalse("the boundary itself must not break", s.broken)
    }

    @Test
    fun `time away inside the grace window is not counted as held`() {
        // Otherwise leaving repeatedly for nine seconds buys a shorter session.
        val s = run(session(1), 10_000L to "com.instagram.android", 18_000L to null)
        assertEquals(8_000L, s.awayMs)
        val held = (status(s, 60_000L) as Status.Running).heldMs
        assertEquals(52_000L, held)
    }

    @Test
    fun `repeated short absences accumulate rather than being forgiven each time`() {
        var s = session(5)
        repeat(5) { i ->
            val base = (i + 1) * 60_000L
            s = step(s, base, "com.instagram.android")
            s = step(s, base + 9_000L, null)
        }
        assertFalse(s.broken)
        assertEquals(45_000L, s.awayMs)
    }

    // ── the dialer ───────────────────────────────────────────────────────────

    @Test
    fun `a phone call never breaks a session`() {
        // Same reason DND lets calls through: an app that stands between a fifteen-year-old and a phone call
        // for forty-five minutes is indefensible.
        ALWAYS_ALLOWED.forEach { dialer ->
            val s = run(session(), 60_000L to dialer, 20 * 60_000L to dialer, 21 * 60_000L to null)
            assertFalse("$dialer broke the session", s.broken)
        }
    }

    @Test
    fun `a call does not pause the clock either`() {
        val s = run(session(45), 60_000L to "com.android.dialer", 300_000L to null)
        assertEquals(0L, s.awayMs)
        assertEquals(300_000L, (status(s, 300_000L) as Status.Running).heldMs)
    }

    // ── status ───────────────────────────────────────────────────────────────

    @Test
    fun `a running session reports what is held and what is left`() {
        val s = status(session(45), 10 * 60_000L) as Status.Running
        assertEquals(10 * 60_000L, s.heldMs)
        assertEquals(35 * 60_000L, s.remainingMs)
    }

    @Test
    fun `being away shows a countdown rather than a failure`() {
        val s = step(session(45), 60_000L, "com.instagram.android")
        val g = status(s, 64_000L) as Status.Grace
        assertEquals(RETURN_GRACE_MS - 4_000L, g.graceLeftMs)
    }

    @Test
    fun `a session completes at its length and not before`() {
        assertTrue(status(session(45), 45 * 60_000L) is Status.Complete)
        assertTrue(status(session(45), 45 * 60_000L - 1) is Status.Running)
    }

    @Test
    fun `a broken session stays broken`() {
        val broken = session().copy(broken = true)
        assertEquals(broken, step(broken, 999_999L, null))
        assertTrue(status(broken, 999_999L) is Status.Broken)
    }

    @Test
    fun `a broken session holds no minutes`() {
        assertEquals(0, heldMinutes(session().copy(broken = true), 40 * 60_000L))
    }

    @Test
    fun `held minutes are whole minutes`() {
        assertEquals(20, heldMinutes(session(45), 20 * 60_000L + 59_000L))
    }

    @Test
    fun `held time never runs negative if the clock jumps backwards`() {
        // System.currentTimeMillis can move backwards on an NTP correction, and a negative held time would
        // read as a completed session once it was formatted.
        val s = status(session(45), start - 60_000L)
        assertEquals(0L, (s as Status.Running).heldMs)
    }
}
