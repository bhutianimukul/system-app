package app.gakseong.quest

import app.gakseong.data.DaySettlement
import app.gakseong.data.HunterSnapshot
import app.gakseong.data.QuestInstance
import app.gakseong.data.SystemState
import app.gakseong.data.Today
import gakseong.engine.Provability
import gakseong.engine.award
import gakseong.engine.settleDay

// The layer the phase-01 engine was written to sit under: readings become per-quest aura, per-quest aura becomes
// one integer, and the engine turns that integer into a day.
//
// Pure. No Android, no clock, no I/O. `work/SettleWorker.kt` gathers the readings and persists the result.

/** How much accumulated aura one Level costs. Level is private, only rises, and gates nothing. */
const val AURA_PER_LEVEL = 500

/** A template becomes an instance for today. The stored aura is the base; the tier is applied when it is shown. */
fun QuestTemplate.instance(readings: Readings): QuestInstance {
    val progress = verifier.evaluate(readings)
    return QuestInstance(
        id = id,
        icon = icon,
        title = title,
        sub = progress.label,
        baseAura = baseAura,
        provability = verifier.provability.name,
        state = if (progress.cleared) "DONE" else "PENDING",
        wide = wide,
    )
}

/** Issue today's quests. Idempotent for a given date: [draw] is seeded by it. */
fun issue(date: String, readings: Readings): List<QuestInstance> =
    draw(date, readings).map { it.instance(readings) }

/**
 * Re-evaluate the day's quests against the current readings and total what has been earned.
 *
 * Aura per quest is `award(baseAura, provability)`, never the base, so the provability tier is applied exactly
 * once and in one place.
 */
fun auraSoFar(quests: List<QuestInstance>, readings: Readings, bank: List<QuestTemplate> = BANK): Int =
    refresh(quests, readings, bank)
        .filter { it.state == "DONE" }
        .sumOf { award(it.baseAura, Provability.valueOf(it.provability)) }

/** Today's quests with their state and sub-line brought up to date. Unknown ids are left untouched. */
fun refresh(
    quests: List<QuestInstance>,
    readings: Readings,
    bank: List<QuestTemplate> = BANK,
): List<QuestInstance> = quests.map { q ->
    val template = bank.firstOrNull { it.id == q.id } ?: return@map q
    val progress = template.verifier.evaluate(readings)
    q.copy(state = if (progress.cleared) "DONE" else "PENDING", sub = progress.label)
}

/**
 * Settle the day that [state] is holding and roll onto [nextDate].
 *
 * The daily cap is enforced inside `settleDay` and is a safety property, not balance: it puts a hard ceiling on
 * how much the app can reward in one day, and there are fifteen-year-olds in a fitness-adjacent app. Nothing
 * here may raise it.
 *
 * Idempotent. A day already present in history is not settled twice, which is what makes the fifteen-minute
 * worker safe to run as often as it likes.
 */
fun settle(state: SystemState, readings: Readings, nextDate: String): SystemState {
    val closing = state.today.date
    if (closing.isEmpty() || closing == nextDate) return state
    if (state.history.any { it.date == closing }) return state.copy(today = Today(date = nextDate))

    val aura = auraSoFar(state.today.quests, readings)
    val bonusCeiling = state.today.bonus?.aura ?: 0
    val result = settleDay(state.hunter.toEngine(), aura, bonusCeiling)

    return state.copy(
        hunter = HunterSnapshot.of(result.state),
        // Rank credit and overflow both build Level. Overflow never buys standing, which settleDay already
        // guarantees by keeping it out of rankCredit.
        auraLifetime = state.auraLifetime + result.rankCredit + result.overflow,
        level = 1 + ((state.auraLifetime + result.rankCredit + result.overflow) / AURA_PER_LEVEL).toInt(),
        today = Today(date = nextDate),
        history = state.history + DaySettlement(
            date = closing,
            aura = aura,
            rankCredit = result.rankCredit,
            overflow = result.overflow,
            outcome = result.outcome.name,
            penalty = result.penalty?.name,
        ),
    )
}
