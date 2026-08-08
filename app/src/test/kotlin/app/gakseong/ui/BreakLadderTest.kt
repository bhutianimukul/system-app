package app.gakseong.ui

import app.gakseong.ui.screens.LADDER
import gakseong.engine.Balance
import gakseong.engine.Penalty
import app.gakseong.session.containmentEarned
import gakseong.engine.penaltyFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The Break screen draws the escalation from its own list. If that list ever drifts from `penaltyFor`, the
 * screen tells the user a sequence the engine will not actually run.
 */
class BreakLadderTest {

    @Test
    fun `every day the ladder shows is a day the engine penalises`() {
        LADDER.forEach { (day, _) ->
            assertEquals("day $day should fire", true, penaltyFor(day) != null)
        }
    }

    @Test
    fun `the ladder is in the engine's order`() {
        assertEquals(listOf(1, 3, 5, 7), LADDER.map { it.first })
        assertEquals(Penalty.STREAK_BROKEN, penaltyFor(1))
        assertEquals(Penalty.AURA_DEBIT, penaltyFor(3))
        assertEquals(Penalty.DEMOTION_WARNING, penaltyFor(5))
        assertEquals(Penalty.DEMOTION, penaltyFor(7))
    }

    @Test
    fun `the quiet days are quiet, and the screen shows them anyway`() {
        // §4: the gaps between penalties are deliberate. An avalanche numbs where a countdown frightens.
        listOf(2, 4, 6).forEach { assertNull("day $it must not fire", penaltyFor(it)) }
    }

    @Test
    fun `the debit shown is the debit the engine takes`() {
        assertEquals("Aura −${Balance.AURA_DEBIT}", LADDER.first { it.first == 3 }.second)
    }

    @Test
    fun `only one penalty is ever live`() {
        (0..9).forEach { misses ->
            assertEquals("at $misses misses", if (penaltyFor(misses) == null) 0 else 1, LADDER.count { it.first == misses })
        }
    }
}

/**
 * Containment is the last thing the sequence does, and only the last thing.
 *
 * §Punishment: streak, then aura, then warning, then demotion and containment, spread across a week. Real
 * blocking arriving before that would make it the app's first move rather than its last.
 */
class ContainmentEarnedTest {

    @Test
    fun `containment is not earned before the end of the sequence`() {
        (0..6).forEach {
            assertEquals("day $it must not authorise containment", false, containmentEarned(it))
        }
    }

    @Test
    fun `containment is earned on the day the demotion lands`() {
        assertEquals(true, containmentEarned(7))
        assertEquals(Penalty.DEMOTION, penaltyFor(7))
    }

    @Test
    fun `the sequence restarts, so containment is authorised once per run and not daily`() {
        // settleDay resets consecutiveMisses on demotion, so day 8 of a run cannot exist.
        (8..14).forEach {
            assertEquals("misses cannot legitimately reach $it", null, penaltyFor(it))
            assertEquals(false, containmentEarned(it))
        }
    }
}
