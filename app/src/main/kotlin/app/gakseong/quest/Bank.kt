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
 * At most two self-reported quests a day.
 *
 * §Economy: an honest player using only declared quests still climbs, but nobody reaches S-rank without doing
 * things that can be checked. Without this the shuffle happily deals four declared out of five, and a day that
 * can be cleared entirely by saying so is not a day the ladder can trust.
 */
const val MAX_DECLARED_PER_DAY = 2

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

    val random = Random(date.hashCode())
    // One quest per kind of verifier a day. Two ScreenOffBlocks in the same day pay twice for one unbroken
    // block: clearing 90 minutes clears 45 as a side effect, and the day quietly awards 850 for one behaviour.
    //
    // Declared is keyed by id instead, because it is a single object shared by six unrelated activities. A meal
    // with people and thirty minutes with your parents are not the same quest, and deduping them by class
    // silently collapses the whole declared bank into one entry.
    val shuffled = eligible.shuffled(random)
        .distinctBy { if (it.verifier is Verifier.Declared) it.id else it.verifier::class.simpleName }

    // A fixed share each, not a preference order. Filling with provable first would mean declared quests never
    // appear at all once enough sensors are granted, and those are the ones that matter most: meditation, a
    // meal with people, thirty minutes with your parents. The phone can see none of them.
    val provable = shuffled.filter { it.verifier !is Verifier.Declared }
    val declared = shuffled.filter { it.verifier is Verifier.Declared }
    val wantDeclared = minOf(MAX_DECLARED_PER_DAY, declared.size)
    val picked = provable.take(count - wantDeclared) + declared.take(wantDeclared)

    // Whichever pool ran short, the other one fills the day rather than leaving it under-length.
    val filled = (picked + provable + declared).distinct().take(count)

    // One wide quest at most: two full-width cards on Home reads as a list rather than a board.
    val wide = filled.filter { it.wide }
    return (filled.filterNot { it.wide } + wide.take(1)).take(count)
}
