package gakseong.engine

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// Phase 01: the aura and rank engine, pure functions only. No Android, no persistence, no clock, no I/O.
// Everything else in the app depends on this being right, and it is the only part cheap to test exhaustively.
//
// DECISIONS.md §4 is the band, §5 is progression and shields, §6 is the penalty sequence. The numbers here are
// calibration, the rules are not: change a constant freely, change a rule only by changing the decision first.

/** Every tunable number in one place. The curve needs tuning against real play; the logic does not. */
object Balance {
    // ponytail: linear curve, two constants each. Anchored on the Home screen's D-III day (500 threshold,
    // 1200 cap). Replace with a table if real play shows the top ranks are unreachable or the bottom trivial.
    const val THRESHOLD_BASE = 200
    const val THRESHOLD_STEP = 100
    const val CAP_BASE = 600
    const val CAP_STEP = 200

    /** Held days needed to promote, by rank letter E→S. Longer at the top, so S is a season and not a week. */
    val DAYS_TO_PROMOTE_BY_LETTER = intArrayOf(7, 10, 14, 18, 22, 26)

    /** §4: sensor-proven full, app-initiated medium, declared low. Declared lands near a third, by design. */
    const val SENSOR_SHARE = 1.0
    const val APP_INITIATED_SHARE = 0.6
    const val DECLARED_SHARE = 0.35

    const val SHIELD_EVERY_DAYS = 7
    const val MAX_SHIELDS = 3

    /** §6, day three. A flat debit, never scaled by streak length: a long streak buys insurance, not exposure. */
    const val AURA_DEBIT = 200
}

enum class Provability { SENSOR, APP_INITIATED, DECLARED }

enum class DayOutcome { BELOW_THRESHOLD, IN_BAND, ABOVE_CAP }

enum class Penalty { STREAK_BROKEN, AURA_DEBIT, DEMOTION_WARNING, DEMOTION }

/** §4: a threshold below and a cap above. Land inside it. */
data class Band(val threshold: Int, val cap: Int)

/**
 * Public standing. [ordinal] 0 is E-III and 17 is S-I, so tiers run III → II → I inside each letter and the
 * whole ladder is one integer. Eighteen promotion moments instead of six.
 */
data class Rank(val ordinal: Int) {
    init {
        require(ordinal in 0..MAX) { "rank ordinal $ordinal is outside E-III..S-I" }
    }

    private val letterIndex get() = ordinal / TIERS_PER_LETTER

    val letter: String get() = LETTERS[letterIndex]
    val title: String get() = TITLES[letterIndex]
    val tier: String get() = TIERS[ordinal % TIERS_PER_LETTER]
    val label: String get() = "$letter · $tier"

    companion object {
        const val MAX = 17
        private const val TIERS_PER_LETTER = 3
        private val LETTERS = listOf("E", "D", "C", "B", "A", "S")
        private val TIERS = listOf("III", "II", "I")

        /** §5. "Monarch" is deliberately absent; the top rank is freedom from compulsion. */
        private val TITLES = listOf("Awakened", "Sentinel", "Vanguard", "Ascendant", "Sovereign", "Unbound")

        val ALL: List<Rank> = (0..MAX).map(::Rank)
    }
}

data class HunterState(
    val rank: Rank,
    val daysHeldAtTier: Int,
    val streak: Int,
    val shields: Int,
    val consecutiveMisses: Int,
)

/** What one settled day did. The caller persists [state] and shows the rest. */
data class Settlement(
    val state: HunterState,
    val outcome: DayOutcome,
    /** Aura that counted toward Rank. Zero below the threshold, clamped to today's ceiling above it. */
    val rankCredit: Int,
    /** Aura above the ceiling. Builds Level and never buys standing. */
    val overflow: Int,
    val penalty: Penalty?,
    val auraDebit: Int,
    val shieldSpent: Boolean,
    val promoted: Boolean,
    val demoted: Boolean,
)

fun bandFor(rank: Rank): Band = Band(
    threshold = Balance.THRESHOLD_BASE + Balance.THRESHOLD_STEP * rank.ordinal,
    cap = Balance.CAP_BASE + Balance.CAP_STEP * rank.ordinal,
)

fun daysToPromote(rank: Rank): Int = Balance.DAYS_TO_PROMOTE_BY_LETTER[rank.ordinal / 3]

/** §4: the ceiling depends on how provable the task is. The System assigns this; the player never does. */
fun award(baseAura: Int, provability: Provability): Int {
    val share = when (provability) {
        Provability.SENSOR -> Balance.SENSOR_SHARE
        Provability.APP_INITIATED -> Balance.APP_INITIATED_SHARE
        Provability.DECLARED -> Balance.DECLARED_SHARE
    }
    return (baseAura * share).roundToInt()
}

/**
 * §6: penalties sequence, never stack. Exactly one lands on any given day of a miss run, and the quiet days
 * between are deliberate — an avalanche numbs where a countdown frightens.
 */
fun penaltyFor(consecutiveMisses: Int): Penalty? = when (consecutiveMisses) {
    1 -> Penalty.STREAK_BROKEN
    3 -> Penalty.AURA_DEBIT
    5 -> Penalty.DEMOTION_WARNING
    7 -> Penalty.DEMOTION
    else -> null
}

/**
 * Settle one day. [auraToday] is already awarded aura, so provability has been applied by [award] before this.
 * [bonusCeiling] is the day's random bonus (§4), which raises the ceiling and nothing else.
 */
fun settleDay(state: HunterState, auraToday: Int, bonusCeiling: Int = 0): Settlement {
    val band = bandFor(state.rank)
    val ceiling = band.cap + bonusCeiling
    val cleared = auraToday >= band.threshold

    val outcome = when {
        !cleared -> DayOutcome.BELOW_THRESHOLD
        auraToday > ceiling -> DayOutcome.ABOVE_CAP
        else -> DayOutcome.IN_BAND
    }
    val rankCredit = if (cleared) min(auraToday, ceiling) else 0
    val overflow = max(0, auraToday - ceiling)

    fun settled(
        state: HunterState,
        penalty: Penalty? = null,
        shieldSpent: Boolean = false,
        promoted: Boolean = false,
        demoted: Boolean = false,
    ) = Settlement(
        state = state,
        outcome = outcome,
        rankCredit = rankCredit,
        overflow = overflow,
        penalty = penalty,
        auraDebit = if (penalty == Penalty.AURA_DEBIT) Balance.AURA_DEBIT else 0,
        shieldSpent = shieldSpent,
        promoted = promoted,
        demoted = demoted,
    )

    if (cleared) {
        val streak = state.streak + 1
        val daysHeld = state.daysHeldAtTier + 1
        val shields =
            if (streak % Balance.SHIELD_EVERY_DAYS == 0) min(state.shields + 1, Balance.MAX_SHIELDS)
            else state.shields
        val promotes = daysHeld >= daysToPromote(state.rank) && state.rank.ordinal < Rank.MAX
        return settled(
            state = state.copy(
                rank = if (promotes) Rank(state.rank.ordinal + 1) else state.rank,
                daysHeldAtTier = if (promotes) 0 else daysHeld,
                streak = streak,
                shields = shields,
                consecutiveMisses = 0,
            ),
            promoted = promotes,
        )
    }

    // §5: a missed day consumes a shield instead of the streak. The day still did not count toward the tier.
    if (state.shields > 0) {
        return settled(state = state.copy(shields = state.shields - 1), shieldSpent = true)
    }

    val misses = state.consecutiveMisses + 1
    val penalty = penaltyFor(misses)
    val demotes = penalty == Penalty.DEMOTION
    return settled(
        state = state.copy(
            rank = if (demotes) Rank(max(0, state.rank.ordinal - 1)) else state.rank,
            daysHeldAtTier = if (demotes) 0 else state.daysHeldAtTier,
            streak = 0,
            // The sequence restarts after a demotion, so containment is authorised once per run rather than daily.
            consecutiveMisses = if (demotes) 0 else misses,
        ),
        penalty = penalty,
        demoted = demotes,
    )
}
