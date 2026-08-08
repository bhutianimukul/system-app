package app.gakseong.data

// What a fresh install starts from, before the first settle has run.
//
// It carries no quests. The phase-05 bank issues them on the first tick, and a seed that shipped its own would
// never let the bank run: `tick` only draws when the day's list is empty. Five hand-written placeholders lived
// here during phase 03 so the screens had shaped data to read, and they have done their job.

/** A hunter at the start of the ladder, with the day not yet drawn. */
val SEED = SystemState(
    hunter = HunterSnapshot(rankOrdinal = 0, daysHeldAtTier = 0, streak = 0, shields = 0, consecutiveMisses = 0),
    level = 1,
    auraLifetime = 0L,
    // [Today.date] is deliberately empty. `SystemSerializer.defaultValue` stamps the current date onto it,
    // because an empty date never matches today and `rolledTo` would clear the day the moment it loaded.
    today = Today(date = ""),
    profile = Profile(hunterClass = "ASSASSIN"),
    onboarded = false,
)
