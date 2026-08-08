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
fun tick(state: SystemState, readings: Readings, today: String): SystemState {
    val settled = settle(state, readings, today)
    val dated = if (settled.today.date == today) settled else settled.copy(today = settled.today.copy(date = today))
    val issued =
        if (dated.today.quests.isEmpty()) dated.copy(today = dated.today.copy(quests = issue(today, readings)))
        else dated

    val quests = refresh(issued.today.quests, readings)
    return issued.copy(
        today = issued.today.copy(quests = quests, auraEarned = auraSoFar(quests, readings)),
    )
}
