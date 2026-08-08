package app.gakseong.quest

import app.gakseong.data.SystemState

/**
 * One settle pass, as a pure function.
 *
 * Called on app open, on widget refresh and from the fifteen-minute worker. Retroactive, never a live watcher:
 * OEM battery managers kill long-lived services, and a design that depends on one is dead on most Indian
 * devices.
 *
 * Order matters. Settle the day that ended before issuing today's, or the new day's quests are settled against
 * yesterday's readings.
 */
fun tick(state: SystemState, readings: Readings, today: String, endOfDayMs: Long = 0L): SystemState {
    // The answers live on the day, so they are folded into the readings here rather than gathered from a
    // sensor. Everything a verifier sees arrives through Readings, including the things the user typed.
    val withAnswers = readings.copy(declared = state.today.declared)
    val settled = settle(state, withAnswers, today)
    val dated = if (settled.today.date == today) settled else settled.copy(today = settled.today.copy(date = today))
    val issued =
        if (dated.today.quests.isEmpty()) {
            dated.copy(
                today = dated.today.copy(
                    quests = issue(today, withAnswers),
                    // Drawn with the quests so every surface agrees about whether today has one. Expiry is the
                    // end of the day: a bonus that outlives its day would raise tomorrow's ceiling.
                    bonus = drawBonus(today, endOfDayMs),
                ),
            )
        } else {
            dated
        }

    val quests = refresh(issued.today.quests, withAnswers)
    return issued.copy(
        today = issued.today.copy(quests = quests, auraEarned = auraSoFar(quests, withAnswers)),
    )
}
