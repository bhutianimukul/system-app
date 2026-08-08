package app.gakseong.quest

import kotlin.random.Random

// The static quest bank. §8: this gets built regardless of AI, because the daily quest never locks and this is
// what it falls back to. A user with no key gets quests, thresholds, streaks, gates, raids, leagues, shadows,
// the private track and the whole ladder.
//
// A Kotlin list rather than JSON in assets: this data ships inside the APK and cannot change without a rebuild,
// so a parser would be a parser for a file that is already a Kotlin list.

/**
 * One quest the System can issue.
 *
 * [baseAura] is the ceiling before provability is applied. The verifier owns the tier, so a template cannot
 * accidentally overpay a declared quest.
 */
data class QuestTemplate(
    val id: String,
    val icon: String,
    val title: String,
    val baseAura: Int,
    val verifier: Verifier,
    val wide: Boolean = false,
)

/**
 * §Economy: sensor-proven pays full and reaches S-rank, app-initiated pays medium, declared pays low. The
 * non-confirmable ones sit near 120–180 against 400–450 by design, because equal pay would make self-reporting
 * the rational way to farm.
 *
 * These matter most and the phone can see none of them: meditation, music, sitting with no screen, thirty
 * minutes with your parents, talking to a friend, a meal with people.
 */
val BANK: List<QuestTemplate> = listOf(
    // ── sensor-proven ────────────────────────────────────────────────────────
    QuestTemplate("screen-off-45", "🌙", "Screen off\n45 min", 400, Verifier.ScreenOffBlock(45)),
    QuestTemplate("screen-off-90", "🌙", "Screen off\n90 min", 450, Verifier.ScreenOffBlock(90)),
    QuestTemplate("scroll-cap", "◉", "Scroll under\n90 min", 420, Verifier.TotalScreenTime(90)),
    QuestTemplate("screen-budget", "◉", "Screen under\n3 hours", 400, Verifier.TotalScreenTime(180)),
    QuestTemplate("steps-6000", "⚡", "6,000\nsteps", 420, Verifier.Steps(6000)),
    QuestTemplate("steps-2000", "⚡", "2,000\nsteps", 300, Verifier.Steps(2000)),
    QuestTemplate("walk-1500", "◈", "Walk or run\n1.5 km", 400, Verifier.Distance(1500)),
    QuestTemplate("sleep-390", "☾", "Sleep 6h 30m", 450, Verifier.Sleep(390), wide = true),
    QuestTemplate(
        "night-gate", "☾", "Night gate · 00:30 to 06:00", 450,
        Verifier.ScreenOffBlock(330), wide = true,
    ),

    // ── app-initiated ────────────────────────────────────────────────────────
    QuestTemplate("focus-20", "◈", "Focus session\n20 min", 300, Verifier.FocusSession(20)),
    QuestTemplate("focus-45", "◈", "Focus session\n45 min", 400, Verifier.FocusSession(45)),
    QuestTemplate("read-20", "✦", "Read\n20 minutes", 300, Verifier.ReadSession(20)),

    // ── declared: the half the sensors cannot see, and the half that matters most ─
    QuestTemplate("meditate", "○", "Ten minutes\nsitting still", 450, Verifier.Declared),
    QuestTemplate("parents", "♡", "Thirty minutes\nwith your parents", 450, Verifier.Declared, wide = true),
    QuestTemplate("friend", "♡", "Talk to\na friend", 450, Verifier.Declared),
    QuestTemplate("meal", "○", "A meal\nwith people", 450, Verifier.Declared),
    QuestTemplate("music", "✦", "A music session\nwith no screen", 450, Verifier.Declared),
    QuestTemplate("call-10", "☎", "A call\nover 10 minutes", 400, Verifier.CallDuration(10)),
)

/** How many quests a day carries. Five is what the Home screen lays out. */
const val QUESTS_PER_DAY = 5

/**
 * Draw today's quests.
 *
 * Seeded by [date] so the same day produces the same quests across a widget refresh, an app open and a worker
 * run. Determinism here is what stops a user rerolling by pulling to refresh.
 *
 * A verifier whose reading is unavailable is never drawn. §Verifiers: no Health Connect grant means the
 * objective is simply not offered, which is not the same as failing it.
 */
fun draw(date: String, readings: Readings, count: Int = QUESTS_PER_DAY): List<QuestTemplate> {
    val eligible = BANK.filter { it.verifier.evaluate(readings).available }
    if (eligible.isEmpty()) return emptyList()

    // One wide quest at most: two full-width cards on Home reads as a list rather than a board.
    val random = Random(date.hashCode())
    val shuffled = eligible.shuffled(random)
    val wide = shuffled.firstOrNull { it.wide }
    val narrow = shuffled.filterNot { it.wide }.take(count - if (wide != null) 1 else 0)
    return (narrow + listOfNotNull(wide)).take(count)
}
