package app.gakseong.quest

import app.gakseong.sense.HealthReading
import app.gakseong.sense.UsageReading
import gakseong.engine.Provability
import gakseong.engine.award
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VerifierTest {

    private fun usage(
        totalMs: Long = 0, perPackage: Map<String, Long> = emptyMap(),
        offMs: Long = 0, opened: Set<String> = emptySet(),
    ) = Readings(usage = UsageReading(totalMs, perPackage, offMs, opened, available = true))

    private fun health(steps: Long = 0, metres: Double = 0.0, sleep: Long = 0) =
        Readings(health = HealthReading(steps, metres, sleep, emptySet(), available = true))

    // ── the rule the type is there to enforce ────────────────────────────────

    @Test
    fun `a declared quest can never pay a sensor rate`() {
        // §6/§7: the System assigns aura and the provability tier is a property of how it is proven, so no bank
        // entry can hand a declared quest a sensor payout.
        assertEquals(Provability.DECLARED, Verifier.Declared("meal").provability)
        assertTrue(award(400, Verifier.Declared("meal").provability) < award(400, Provability.SENSOR))
    }

    @Test
    fun `the app marking its own homework is app-initiated, never sensor`() {
        assertEquals(Provability.APP_INITIATED, Verifier.ReadSession(20).provability)
        assertEquals(Provability.APP_INITIATED, Verifier.FocusSession(45).provability)
    }

    @Test
    fun `every sensor verifier is backed by a real system reading`() {
        listOf(
            Verifier.ScreenOffBlock(45), Verifier.TotalScreenTime(180), Verifier.AppBudget("insta", 90),
            Verifier.AppAbsent(setOf("insta"), 120), Verifier.Steps(6000), Verifier.Distance(1500),
            Verifier.Sleep(390),
        ).forEach { assertEquals("${it::class.simpleName}", Provability.SENSOR, it.provability) }
    }

    // ── unavailable is never a failure ───────────────────────────────────────

    @Test
    fun `an ungranted usage read never clears and never fails`() {
        val p = Verifier.ScreenOffBlock(45).evaluate(Readings())
        assertFalse(p.cleared)
        assertFalse(p.available)
        assertEquals("Usage access needed", p.label)
    }

    @Test
    fun `an ungranted health read never clears and never fails`() {
        val p = Verifier.Steps(6000).evaluate(Readings())
        assertFalse(p.cleared)
        assertFalse(p.available)
    }

    @Test
    fun `a budget verifier does not clear on an ungranted read`() {
        // A ceiling reads as "cleared while under". Without the availability guard, no grant would look like a
        // perfect day, which is the most dangerous direction for this bug to fail.
        val p = Verifier.TotalScreenTime(180).evaluate(Readings())
        assertFalse("no grant must not clear a budget", p.cleared)
    }

    // ── the verifiers themselves ─────────────────────────────────────────────

    @Test
    fun `screen off block clears at the target`() {
        assertTrue(Verifier.ScreenOffBlock(45).evaluate(usage(offMs = 45 * 60_000)).cleared)
        assertFalse(Verifier.ScreenOffBlock(45).evaluate(usage(offMs = 44 * 60_000)).cleared)
    }

    @Test
    fun `total screen time is a ceiling and clears while under it`() {
        assertTrue(Verifier.TotalScreenTime(180).evaluate(usage(totalMs = 179 * 60_000)).cleared)
        assertTrue(Verifier.TotalScreenTime(180).evaluate(usage(totalMs = 180 * 60_000)).cleared)
        assertFalse(Verifier.TotalScreenTime(180).evaluate(usage(totalMs = 181 * 60_000)).cleared)
    }

    @Test
    fun `an app budget only counts its own package`() {
        val r = usage(perPackage = mapOf("insta" to 100 * 60_000L, "reddit" to 5 * 60_000L))
        assertFalse(Verifier.AppBudget("insta", 90).evaluate(r).cleared)
        assertTrue(Verifier.AppBudget("reddit", 90).evaluate(r).cleared)
    }

    @Test
    fun `app absent breaks the moment one of the named packages is opened`() {
        val v = Verifier.AppAbsent(setOf("insta", "reddit"), 120)
        assertTrue(v.evaluate(usage(opened = setOf("maps"))).cleared)
        assertFalse(v.evaluate(usage(opened = setOf("maps", "reddit"))).cleared)
    }

    @Test
    fun `steps distance and sleep clear at their targets`() {
        assertTrue(Verifier.Steps(6000).evaluate(health(steps = 6000)).cleared)
        assertFalse(Verifier.Steps(6000).evaluate(health(steps = 5999)).cleared)
        assertTrue(Verifier.Distance(1500).evaluate(health(metres = 1500.0)).cleared)
        assertTrue(Verifier.Sleep(390).evaluate(health(sleep = 390)).cleared)
    }

    @Test
    fun `a declared quest never answered expires unclaimed`() {
        // Silence must not be the cheapest way to farm.
        val meal = Verifier.Declared("meal")
        assertFalse("never answered", meal.evaluate(Readings()).cleared)
        assertFalse("answered no", meal.evaluate(Readings(declared = mapOf("meal" to false))).cleared)
        assertTrue("answered yes", meal.evaluate(Readings(declared = mapOf("meal" to true))).cleared)
    }

    @Test
    fun `an unanswered declared quest says so rather than looking done`() {
        assertEquals("Answer when it expires", Verifier.Declared("meal").evaluate(Readings()).label)
    }

    @Test
    fun `a focus session that was never started is not cleared`() {
        assertEquals("Not started", Verifier.FocusSession(45).evaluate(Readings()).label)
        assertFalse(Verifier.FocusSession(45).evaluate(Readings()).cleared)
        assertTrue(Verifier.FocusSession(45).evaluate(Readings(focusMinutes = 45)).cleared)
    }

    @Test
    fun `the reader counts its own minutes and nothing else`() {
        assertTrue(Verifier.ReadSession(20).evaluate(Readings(readerMinutes = 20)).cleared)
        assertFalse(Verifier.ReadSession(20).evaluate(usage(totalMs = 60 * 60_000)).cleared)
    }

    @Test
    fun `call duration is minutes and never who`() {
        assertTrue(Verifier.CallDuration(10).evaluate(Readings(callSeconds = 600)).cleared)
        assertFalse(Verifier.CallDuration(10).evaluate(Readings(callSeconds = 599)).cleared)
    }

    @Test
    fun `the label a card shows is the value that decides the quest`() {
        // One computation, so what the user reads cannot disagree with what cleared.
        val p = Verifier.TotalScreenTime(180).evaluate(usage(totalMs = 41 * 60_000))
        assertEquals("41 min used", p.label)
        assertEquals(41L, p.current)
    }
}

/** The night gate, which is checked once after its window closes and never during. */
class NightGateVerifierTest {

    private val granted = Readings(
        usage = UsageReading(0L, emptyMap(), 0L, emptySet(), available = true),
    )

    @Test
    fun `an open window is pending, not failed`() {
        // §Verifiers: checked once after it closes. A query at 02:00 can only say "so far", and a gate that
        // reported halfway through would teach the user that leaving it early is free.
        val p = Verifier.NightGate(330).evaluate(granted.copy(nightGateMinutes = null))
        assertFalse(p.cleared)
        assertEquals("Pending · tonight", p.label)
    }

    @Test
    fun `a held gate clears`() {
        assertTrue(Verifier.NightGate(330).evaluate(granted.copy(nightGateMinutes = 330)).cleared)
        assertTrue(Verifier.NightGate(330).evaluate(granted.copy(nightGateMinutes = 400)).cleared)
    }

    @Test
    fun `a broken gate says when it broke rather than just failing`() {
        // Every penalty screen shows the road back, and a gate is no different: knowing it went at 04:10 is
        // what makes tomorrow's attempt different from tonight's.
        val p = Verifier.NightGate(330).evaluate(granted.copy(nightGateMinutes = 250))
        assertFalse(p.cleared)
        assertEquals("Broken after 4h 10m", p.label)
    }

    @Test
    fun `no usage grant is unavailable rather than broken`() {
        val p = Verifier.NightGate(330).evaluate(Readings(nightGateMinutes = 100))
        assertFalse(p.available)
        assertFalse(p.cleared)
    }

    @Test
    fun `the gate is sensor proven, so it can reach the top`() {
        assertEquals(Provability.SENSOR, Verifier.NightGate(330).provability)
    }
}
