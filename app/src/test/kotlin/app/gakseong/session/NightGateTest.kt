package app.gakseong.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A window that crosses midnight is the only hard thing here, and the only way to be sure about it is to
 * enumerate. A negative span would read as instantly satisfied, which turns the night gate into free aura.
 */
class NightGateTest {

    private val minute = 60_000L
    private val day = 24 * 60 * minute

    @Test
    fun `the default window is five and a half hours`() {
        assertEquals(330, gateMinutes("00:30", "06:00"))
    }

    @Test
    fun `a window crossing midnight is measured forwards, not backwards`() {
        // 23:00 to 06:00 is seven hours. Subtracting gives −1020, which any `held >= target` check clears.
        assertEquals(7 * 60, gateMinutes("23:00", "06:00"))
        assertEquals(8 * 60, gateMinutes("22:00", "06:00"))
        assertEquals(60, gateMinutes("23:30", "00:30"))
    }

    @Test
    fun `a zero length window is not a window`() {
        assertNull(gateMinutes("06:00", "06:00"))
    }

    @Test
    fun `malformed times are refused rather than guessed at`() {
        listOf("", "6", "6:00:00", "25:00", "06:61", "ab:cd", "-1:00").forEach {
            assertNull("$it should not parse", minutesOfDay(it))
            assertNull("$it should not make a window", gateMinutes(it, "06:00"))
        }
    }

    @Test
    fun `inside the gate is inside it, on both sides of midnight`() {
        assertTrue(insideGate(minutesOfDay("02:00")!!, "00:30", "06:00"))
        assertFalse(insideGate(minutesOfDay("07:00")!!, "00:30", "06:00"))
        assertFalse(insideGate(minutesOfDay("00:15")!!, "00:30", "06:00"))

        // The crossing case: 23:30 and 01:00 are both inside a 23:00–06:00 gate.
        assertTrue(insideGate(minutesOfDay("23:30")!!, "23:00", "06:00"))
        assertTrue(insideGate(minutesOfDay("01:00")!!, "23:00", "06:00"))
        assertFalse(insideGate(minutesOfDay("12:00")!!, "23:00", "06:00"))
    }

    @Test
    fun `the boundaries belong to the night they open, not the one they close`() {
        assertTrue("the opening minute is inside", insideGate(minutesOfDay("00:30")!!, "00:30", "06:00"))
        assertFalse("the closing minute is outside", insideGate(minutesOfDay("06:00")!!, "00:30", "06:00"))
    }

    @Test
    fun `the gate is closed exactly when it is not open`() {
        (0 until 24 * 60).forEach { m ->
            assertEquals(!insideGate(m, "00:30", "06:00"), gateClosed(m, "00:30", "06:00"))
        }
    }

    @Test
    fun `a window inside one day starts on that day`() {
        val range = gateWindow(0L, "00:30", "06:00")!!
        assertEquals(30 * minute, range.first)
        assertEquals(360 * minute, range.last)
    }

    @Test
    fun `a window crossing midnight starts on the day before`() {
        // The naive version does startOfDay + 23:00, putting the query twenty-three hours into the wrong day.
        val range = gateWindow(day, "23:00", "06:00")!!
        assertEquals("should start at 23:00 the previous day", day - 60 * minute, range.first)
        assertEquals("should end at 06:00 today", day + 360 * minute, range.last)
    }

    @Test
    fun `the queried window is exactly as long as the gate says it is`() {
        listOf("00:30" to "06:00", "23:00" to "06:00", "22:30" to "07:15", "01:00" to "05:00").forEach { (s, e) ->
            val range = gateWindow(day, s, e)!!
            assertEquals("$s to $e", gateMinutes(s, e)!! * minute, range.last - range.first)
        }
    }

    @Test
    fun `a malformed window produces no query rather than a wrong one`() {
        assertNull(gateWindow(day, "nonsense", "06:00"))
    }
}
