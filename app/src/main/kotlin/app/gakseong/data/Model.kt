package app.gakseong.data

import gakseong.engine.HunterState
import gakseong.engine.Rank
import kotlinx.serialization.Serializable

// Everything the app persists, in one file. The engine stays pure, so its HunterState is mirrored here rather
// than annotated: @Serializable needs the compiler plugin on the declaring module, and CLAUDE.md requires
// `kotlinc engine/*.kt` to keep working.

/**
 * The whole of what the app knows, persisted as one JSON document.
 *
 * ponytail: one document rewritten per settle rather than a relational store. A year of play is ~365 small
 * history rows, so this is microseconds. Move to Room if the report screen or the private journal measures slow.
 */
@Serializable
data class SystemState(
    val hunter: HunterSnapshot = HunterSnapshot(),
    /** §Economy: private, only rises. Absent from the engine because it gates nothing. */
    val level: Int = 1,
    val today: Today = Today(),
    val profile: Profile = Profile(),
    val settings: Settings = Settings(),
    val history: List<DaySettlement> = emptyList(),
    val onboarded: Boolean = false,
)

/** The engine's [HunterState] as five integers. [Rank] serializes as its ordinal, so its `require` still guards. */
@Serializable
data class HunterSnapshot(
    val rankOrdinal: Int = 0,
    val daysHeldAtTier: Int = 0,
    val streak: Int = 0,
    val shields: Int = 0,
    val consecutiveMisses: Int = 0,
) {
    /** Clamped rather than trusted: a hand-edited or half-written file must not crash the app on read. */
    fun toEngine(): HunterState = HunterState(
        rank = Rank(rankOrdinal.coerceIn(0, Rank.MAX)),
        daysHeldAtTier = daysHeldAtTier,
        streak = streak,
        shields = shields,
        consecutiveMisses = consecutiveMisses,
    )

    companion object {
        fun of(state: HunterState) = HunterSnapshot(
            rankOrdinal = state.rank.ordinal,
            daysHeldAtTier = state.daysHeldAtTier,
            streak = state.streak,
            shields = state.shields,
            consecutiveMisses = state.consecutiveMisses,
        )
    }
}

/** [date] is an ISO local date. A mismatch against the current date is how a day boundary is detected. */
@Serializable
data class Today(
    val date: String = "",
    val quests: List<QuestInstance> = emptyList(),
    val auraEarned: Int = 0,
    val bonus: Bonus? = null,
)

/**
 * One issued quest. [provability] and [state] are stored as enum names rather than as the enums themselves,
 * because `Provability` lives in the unannotated engine.
 */
@Serializable
data class QuestInstance(
    val id: String,
    val icon: String,
    val title: String,
    val sub: String,
    val baseAura: Int,
    val provability: String,
    val state: String,
    val wide: Boolean = false,
)

/** §Economy: one per day, spawned at random, raises today's ceiling by 120 to 450. */
@Serializable
data class Bonus(
    val title: String,
    val detail: String,
    val aura: Int,
    val expiresAtEpochMs: Long,
)

@Serializable
data class Profile(
    val hunterClass: String = "ASSASSIN",
    val intent: List<String> = emptyList(),
    /** Never leaves the device. §10. */
    val watchedPackages: List<String> = emptyList(),
)

@Serializable
data class Settings(
    val analytics: Boolean = true,
    val dnd: Boolean = false,
    val speedBump: Boolean = false,
    val privateTrack: Boolean = false,
    val nightGateStart: String = "00:30",
    val nightGateEnd: String = "06:00",
    val whisper: Boolean = false,
)

@Serializable
data class DaySettlement(
    val date: String,
    val aura: Int,
    val rankCredit: Int = 0,
    val overflow: Int = 0,
    val outcome: String = "IN_BAND",
    val penalty: String? = null,
)

/**
 * Adopt [date] as today. Settling the day that just ended is phase 05's job; this only detects the boundary and
 * clears the slate, so a rollover can never silently award or penalise.
 */
fun SystemState.rolledTo(date: String): SystemState =
    if (today.date == date) this else copy(today = Today(date = date))
