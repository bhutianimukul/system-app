package app.gakseong.quest

import app.gakseong.data.Bonus
import app.gakseong.data.HunterSnapshot
import app.gakseong.data.SystemState
import app.gakseong.data.Today
import app.gakseong.sense.HealthReading
import app.gakseong.sense.UsageReading
import gakseong.engine.Balance
import gakseong.engine.Provability
import gakseong.engine.Rank
import gakseong.engine.award
import gakseong.engine.bandFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettleTest {

    private val everything = Readings(
        usage = UsageReading(0L, emptyMap(), 12 * 60 * 60_000L, emptySet(), available = true),
        health = HealthReading(20_000, 20_000.0, 600, setOf("com.strava"), available = true),
        readerMinutes = 120,
        focusMinutes = 120,
        declared = BANK.map { it.id }.associateWith { true },
        callSeconds = 3600,
        locationCheckedIn = true,
        nightGateMinutes = 8 * 60,
    )

    private fun stateOn(date: String, quests: List<app.gakseong.data.QuestInstance>) = SystemState(
        hunter = HunterSnapshot(rankOrdinal = 0),
        today = Today(date = date, quests = quests),
    )

    // ── the safety property ──────────────────────────────────────────────────

    @Test
    fun `a perfect day cannot exceed the daily cap in rank credit`() {
        // §3: the cap is a safety property, not balance. It bounds how much the app can reward in one day, and
        // there are fifteen-year-olds in a fitness-adjacent app.
        val quests = BANK.map { it.instance(everything) }
        val after = settle(stateOn("2026-08-08", quests), everything, "2026-08-09")
        val cap = bandFor(Rank(0)).cap

        assertTrue("every quest cleared", quests.all { it.state == "DONE" })
        assertTrue("rank credit ${after.history.first().rankCredit} exceeds cap $cap",
            after.history.first().rankCredit <= cap)
    }

    @Test
    fun `everything above the ceiling becomes overflow and buys no standing`() {
        val quests = BANK.map { it.instance(everything) }
        val after = settle(stateOn("2026-08-08", quests), everything, "2026-08-09")
        val row = after.history.first()
        assertTrue("a full bank should overflow at E-III", row.overflow > 0)
        assertEquals(row.aura, row.rankCredit + row.overflow)
    }

    @Test
    fun `a bonus raises the ceiling and nothing else`() {
        val quests = BANK.map { it.instance(everything) }
        val plain = settle(stateOn("2026-08-08", quests), everything, "2026-08-09").history.first()
        val bonused = settle(
            stateOn("2026-08-08", quests).copy(
                today = Today("2026-08-08", quests, bonus = Bonus("x", "y", 450, 0L)),
            ),
            everything, "2026-08-09",
        ).history.first()

        assertEquals("the aura earned is the same", plain.aura, bonused.aura)
        assertTrue("the ceiling moved", bonused.rankCredit > plain.rankCredit)
    }

    // ── provability, applied exactly once ────────────────────────────────────

    @Test
    fun `aura is awarded through the engine, never at the base rate`() {
        val declared = BANK.first { it.verifier is Verifier.Declared }
        val total = auraSoFar(listOf(declared.instance(everything)), everything)
        assertEquals(award(declared.baseAura, Provability.DECLARED), total)
        assertNotEquals(declared.baseAura, total)
    }

    @Test
    fun `a declared day pays roughly a third of a sensor day`() {
        // §Economy: 120–180 against 400–450. Equal pay would make self-reporting the rational way to farm.
        val declared = award(450, Provability.DECLARED)
        val sensor = award(450, Provability.SENSOR)
        assertTrue("$declared should be near a third of $sensor", declared < sensor / 2)
    }

    // ── idempotence, which is what makes a 15-minute worker safe ─────────────

    @Test
    fun `settling the same day twice changes nothing the second time`() {
        val quests = BANK.take(3).map { it.instance(everything) }
        val once = settle(stateOn("2026-08-08", quests), everything, "2026-08-09")
        val twice = settle(once, everything, "2026-08-09")
        assertEquals(once, twice)
    }

    @Test
    fun `a day already in history is not settled again`() {
        val quests = BANK.take(3).map { it.instance(everything) }
        val once = settle(stateOn("2026-08-08", quests), everything, "2026-08-09")
        // Rewind to the same open day and settle again: history must not gain a duplicate row.
        val replayed = settle(once.copy(today = Today(date = "2026-08-08", quests = quests)), everything, "2026-08-09")
        assertEquals(1, replayed.history.count { it.date == "2026-08-08" })
    }

    @Test
    fun `settling before the day is over does nothing`() {
        val s = stateOn("2026-08-08", BANK.take(2).map { it.instance(everything) })
        assertEquals(s, settle(s, everything, "2026-08-08"))
    }

    // ── the miss path ────────────────────────────────────────────────────────

    @Test
    fun `a day with nothing cleared falls below the threshold and breaks the streak`() {
        val nothing = Readings(usage = UsageReading(0L, emptyMap(), 0L, emptySet(), available = true))
        val quests = listOf(BANK.first { it.id == "screen-off-45" }.instance(nothing))
        val before = stateOn("2026-08-08", quests).copy(hunter = HunterSnapshot(rankOrdinal = 0, streak = 9))
        val after = settle(before, nothing, "2026-08-09")

        assertEquals(0, after.hunter.streak)
        assertEquals("BELOW_THRESHOLD", after.history.first().outcome)
    }

    @Test
    fun `a shield absorbs the miss and carries no penalty`() {
        val nothing = Readings(usage = UsageReading(0L, emptyMap(), 0L, emptySet(), available = true))
        val quests = listOf(BANK.first { it.id == "screen-off-45" }.instance(nothing))
        val before = stateOn("2026-08-08", quests).copy(hunter = HunterSnapshot(rankOrdinal = 0, streak = 7, shields = 1))
        val after = settle(before, nothing, "2026-08-09")

        assertEquals(0, after.hunter.shields)
        assertEquals(null, after.history.first().penalty)
    }

    // ── level ────────────────────────────────────────────────────────────────

    @Test
    fun `level only rises`() {
        var s = stateOn("2026-08-08", BANK.map { it.instance(everything) })
        val start = s.level
        repeat(5) { day ->
            s = settle(s, everything, "2026-08-${9 + day}")
            assertTrue("level fell on day $day", s.level >= start)
            s = s.copy(today = Today(date = "2026-08-${9 + day}", quests = BANK.map { it.instance(everything) }))
        }
        assertTrue("level should have risen", s.level > start)
    }

    @Test
    fun `level is derived from lifetime aura so the two cannot drift`() {
        val after = settle(stateOn("2026-08-08", BANK.map { it.instance(everything) }), everything, "2026-08-09")
        assertEquals(1 + (after.auraLifetime / AURA_PER_LEVEL).toInt(), after.level)
    }

    // ── drawing ──────────────────────────────────────────────────────────────

    @Test
    fun `the same date draws the same quests`() {
        assertEquals(draw("2026-08-08", everything), draw("2026-08-08", everything))
        assertNotEquals(draw("2026-08-08", everything), draw("2026-08-09", everything))
    }

    @Test
    fun `a verifier whose reading is unavailable is never drawn`() {
        // No Health Connect grant means the objective is not offered, which is not the same as failing it.
        val noHealth = everything.copy(health = HealthReading.Unavailable)
        val drawn = draw("2026-08-08", noHealth)
        assertTrue(
            "a health quest was drawn without a grant",
            drawn.none { it.verifier is Verifier.Steps || it.verifier is Verifier.Distance || it.verifier is Verifier.Sleep },
        )
    }

    @Test
    fun `no sensor grant at all still leaves a day worth playing`() {
        // §8: a user with no key and no grants still gets quests. If this ever returns nothing, the build is wrong.
        val bare = Readings()
        assertTrue("declared quests must survive with no sensors", draw("2026-08-08", bare).isNotEmpty())
    }

    @Test
    fun `at most one wide quest a day`() {
        (1..28).forEach { d ->
            val drawn = draw("2026-08-%02d".format(d), everything)
            assertTrue("day $d drew ${drawn.count { it.wide }} wide", drawn.count { it.wide } <= 1)
        }
    }

    @Test
    fun `the debit the engine takes is the debit the balance names`() {
        assertEquals(200, Balance.AURA_DEBIT)
    }
}

/** The balance rule the first real device run exposed: the shuffle dealt four declared quests out of five. */
class DrawBalanceTest {

    private val sensorsOn = Readings(
        usage = UsageReading(0L, emptyMap(), 0L, emptySet(), available = true),
        health = HealthReading(0, 0.0, 0, emptySet(), available = true),
    )

    @Test
    fun `no more than two declared quests on any day of a month`() {
        (1..31).forEach { d ->
            val drawn = draw("2026-08-%02d".format(d), sensorsOn)
            val declared = drawn.count { it.verifier is Verifier.Declared }
            assertTrue("day $d drew $declared declared", declared <= MAX_DECLARED_PER_DAY)
        }
    }

    @Test
    fun `most of a day is provable when the sensors are granted`() {
        (1..31).forEach { d ->
            val drawn = draw("2026-08-%02d".format(d), sensorsOn)
            val provable = drawn.count { it.verifier !is Verifier.Declared }
            assertTrue("day $d had only $provable provable", provable >= drawn.size - MAX_DECLARED_PER_DAY)
        }
    }

    @Test
    fun `the cap relaxes rather than leaving the day empty`() {
        // With nothing granted, declared is all there is. A day with nothing in it is worse than a soft cap.
        val drawn = draw("2026-08-08", Readings())
        assertTrue("a bare device still gets a day", drawn.isNotEmpty())
    }

    @Test
    fun `declared quests actually appear when the sensors are granted`() {
        // The cap is a share, not a preference order. Filling with provable first would silently drop the
        // quests §Economy calls the ones that matter most.
        (1..31).forEach { d ->
            val drawn = draw("2026-08-%02d".format(d), sensorsOn)
            assertTrue("day $d drew no declared quest", drawn.any { it.verifier is Verifier.Declared })
        }
    }

    @Test
    fun `one quest per kind of verifier, so one action is never paid twice`() {
        // Clearing a 90-minute screen-off block clears a 45-minute one as a side effect. Drawing both pays 850
        // aura for a single unbroken block.
        //
        // Declared is keyed by id, matching the draw: it is one object shared by six unrelated activities, and
        // a meal with people is not the same quest as thirty minutes with your parents.
        (1..31).forEach { d ->
            val drawn = draw("2026-08-%02d".format(d), sensorsOn)
            val kinds = drawn.map { if (it.verifier is Verifier.Declared) it.id else it.verifier::class.simpleName }
            assertEquals("day $d drew a duplicate verifier kind", kinds.size, kinds.distinct().size)
        }

        // And the thing the rule exists for, stated directly.
        (1..31).forEach { d ->
            val drawn = draw("2026-08-%02d".format(d), sensorsOn)
            assertTrue("day $d drew two screen-off blocks", drawn.count { it.verifier is Verifier.ScreenOffBlock } <= 1)
            assertTrue("day $d drew two screen budgets", drawn.count { it.verifier is Verifier.TotalScreenTime } <= 1)
        }
    }

    @Test
    fun `a day is still five quests`() {
        assertEquals(QUESTS_PER_DAY, draw("2026-08-08", sensorsOn).size)
    }
}

/** The two gaps the loop had: one answer clearing two quests, and a bonus that never spawned. */
class DeclaredAndBonusTest {

    private val sensorsOn = Readings(
        usage = UsageReading(0L, emptyMap(), 0L, emptySet(), available = true),
        health = HealthReading(0, 0.0, 0, emptySet(), available = true),
    )

    @Test
    fun `answering one declared quest does not clear another`() {
        // A day can draw two. "A meal with people" must not settle "thirty minutes with your parents".
        val meal = BANK.first { it.id == "meal" }
        val parents = BANK.first { it.id == "parents" }
        val r = sensorsOn.copy(declared = mapOf("meal" to true))

        assertTrue(meal.verifier.evaluate(r).cleared)
        assertFalse(parents.verifier.evaluate(r).cleared)
    }

    @Test
    fun `each declared quest answers for itself`() {
        BANK.filter { it.verifier is Verifier.Declared }.forEach { t ->
            val only = sensorsOn.copy(declared = mapOf(t.id to true))
            assertTrue("${t.id} did not clear on its own answer", t.verifier.evaluate(only).cleared)
            BANK.filter { it.verifier is Verifier.Declared && it.id != t.id }.forEach { other ->
                assertFalse("${t.id} cleared ${other.id}", other.verifier.evaluate(only).cleared)
            }
        }
    }

    @Test
    fun `a no is not a yes`() {
        val r = sensorsOn.copy(declared = mapOf("meal" to false))
        assertFalse(BANK.first { it.id == "meal" }.verifier.evaluate(r).cleared)
    }

    @Test
    fun `the same date always draws the same bonus`() {
        assertEquals(drawBonus("2026-08-08", 0L), drawBonus("2026-08-08", 0L))
    }

    @Test
    fun `a bonus stays inside the band the economy names`() {
        // §Economy: raises today's ceiling by 120 to 450, and nothing outside that is a bonus.
        (1..200).forEach { d ->
            drawBonus("2026-%02d-%02d".format((d % 12) + 1, (d % 28) + 1), 0L)?.let {
                assertTrue("aura ${it.aura} is outside 120..450", it.aura in 120..450)
            }
        }
    }

    @Test
    fun `bonuses are occasional rather than daily or never`() {
        val days = (1..90).map { "2026-08-%02d".format((it % 28) + 1) }.distinct()
        val spawned = days.count { drawBonus(it, 0L) != null }
        assertTrue("$spawned of ${days.size} days had one", spawned in 1 until days.size)
    }

    @Test
    fun `a bonus expires with its day`() {
        val endOfDay = 1_786_000_000_000L
        val b = (1..40).firstNotNullOfOrNull { drawBonus("2026-08-%02d".format((it % 28) + 1), endOfDay) }
        assertEquals(endOfDay, b?.expiresAtEpochMs)
    }
}
