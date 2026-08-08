package app.gakseong.data

// What a fresh install starts from, and what every screen renders against until onboarding writes over it.
//
// ponytail: the five quests here are hand-written placeholders standing in for the phase-05 quest bank. They
// exist so the screens have shaped data to read during phase 03. CRITIC.md tracks them as `exists, not wired`
// until quest/Bank.kt replaces them.

/**
 * The Home screen's own design-page day, so the first launch matches the reference art.
 *
 * [Today.date] is deliberately empty. `SystemSerializer.defaultValue` stamps the current date onto it, because
 * an empty date never matches today and `rolledTo` would otherwise clear these quests the moment they loaded.
 */
val SEED = SystemState(
    hunter = HunterSnapshot(rankOrdinal = 4, daysHeldAtTier = 3, streak = 14, shields = 1, consecutiveMisses = 0),
    level = 34,
    // 34 levels at AURA_PER_LEVEL, so the seed's Level and its lifetime aura agree.
    auraLifetime = 33 * 500L,
    today = Today(
        date = "",
        auraEarned = 640,
        quests = listOf(
            QuestInstance(
                id = "screen-off", icon = "🌙", title = "Screen off\n45 min", sub = "Verified",
                baseAura = 180, provability = "SENSOR", state = "DONE",
            ),
            QuestInstance(
                id = "scroll-cap", icon = "◉", title = "Scroll under\n90 min", sub = "41 min used",
                baseAura = 220, provability = "SENSOR", state = "DONE",
            ),
            QuestInstance(
                id = "steps", icon = "⚡", title = "6,000\nsteps", sub = "Health Connect",
                baseAura = 240, provability = "SENSOR", state = "DONE",
            ),
            QuestInstance(
                id = "focus", icon = "◈", title = "Focus session\n45 min", sub = "Not started",
                baseAura = 300, provability = "APP_INITIATED", state = "PENDING",
            ),
            QuestInstance(
                id = "night-gate", icon = "☾", title = "Night gate · 00:30 to 06:00", sub = "Pending · tonight",
                baseAura = 260, provability = "SENSOR", state = "PENDING", wide = true,
            ),
        ),
        bonus = Bonus(
            title = "Phone down · 2 hours",
            detail = "Start within the hour or it is gone",
            aura = 400,
            expiresAtEpochMs = 0L,
        ),
    ),
    profile = Profile(hunterClass = "ASSASSIN"),
    onboarded = false,
)
