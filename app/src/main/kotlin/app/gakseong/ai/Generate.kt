package app.gakseong.ai

import app.gakseong.quest.QuestTemplate
import app.gakseong.quest.Verifier

// Turning a model's reply into a quest. §7 is the whole design here.
//
// **AI never touches the aura number.** The model picks a verifier from the closed set and some numbers, and
// this file decides what that is worth. Clamps are enforced in code after generation, never in the prompt: a
// prompt is a request, and a request is not a guarantee.

/** What the model is allowed to say. Anything else in the reply is discarded before it reaches a quest. */
data class Generated(
    val verifier: String,
    val minutes: Int = 0,
    val steps: Int = 0,
    val metres: Int = 0,
    val title: String = "",
    val icon: String = "◈",
)

/**
 * Every bound, in one place, applied after the model has spoken.
 *
 * These are not suggestions to the model. A generated quest asking for a nine-hour screen-off block or forty
 * thousand steps is not a hard quest, it is a broken one, and §3 already says no penalty may demand physical
 * effort. The ceilings here are what stop a bad generation becoming a bad day.
 */
object Clamp {
    const val MIN_MINUTES = 5
    const val MAX_MINUTES = 360
    const val MIN_STEPS = 500
    const val MAX_STEPS = 15_000
    const val MIN_METRES = 200
    const val MAX_METRES = 15_000
    const val MAX_TITLE = 40
}

/**
 * The aura a generated quest is worth, by verifier kind.
 *
 * A table in code, keyed by what the quest is rather than by what the model asked for. The model has no field
 * for aura, cannot suggest one, and would be ignored if it did: this map is consulted by kind alone.
 *
 * `APP_BUDGET` and `APP_ABSENT` are deliberately absent, and this map is the only list the model is given, so
 * it is never offered them. Both need package names, and a generated quest must not name an app: the packages
 * on this phone are the user's own list, §10 keeps that list on the device, and a model choosing which app to
 * forbid would be the app deciding something the user already decided at onboarding. The static bank carries
 * package-scoped quests instead, built from the five the user confirmed.
 *
 * The first draft listed both here without a branch to build them, so every generation naming one was silently
 * dropped. A test asking that every allowed verifier builds is what surfaced it.
 */
private val BASE_AURA_BY_KIND = mapOf(
    "SCREEN_OFF_BLOCK" to 400,
    "TOTAL_SCREEN_TIME" to 400,
    "STEPS" to 400,
    "DISTANCE" to 400,
    "SLEEP" to 450,
    "READ_SESSION" to 300,
    "FOCUS_SESSION" to 400,
    "DECLARED" to 450,
)

/**
 * Build a quest from a generation, or null when the model named something outside the closed set.
 *
 * Returning null is the point. §7: free-text quests cannot be tiered, and an untierable quest kills the
 * leaderboard, so a verifier this app does not have is not a quest it will issue.
 */
fun toTemplate(g: Generated, id: String): QuestTemplate? {
    val minutes = g.minutes.coerceIn(Clamp.MIN_MINUTES, Clamp.MAX_MINUTES)
    val steps = g.steps.coerceIn(Clamp.MIN_STEPS, Clamp.MAX_STEPS)
    val metres = g.metres.coerceIn(Clamp.MIN_METRES, Clamp.MAX_METRES)

    val verifier: Verifier = when (g.verifier.uppercase()) {
        "SCREEN_OFF_BLOCK" -> Verifier.ScreenOffBlock(minutes)
        "TOTAL_SCREEN_TIME" -> Verifier.TotalScreenTime(minutes)
        "STEPS" -> Verifier.Steps(steps)
        "DISTANCE" -> Verifier.Distance(metres)
        "SLEEP" -> Verifier.Sleep(minutes)
        "READ_SESSION" -> Verifier.ReadSession(minutes)
        "FOCUS_SESSION" -> Verifier.FocusSession(minutes)
        "DECLARED" -> Verifier.Declared(id)
        else -> return null
    }

    val title = g.title.trim().take(Clamp.MAX_TITLE).ifBlank { return null }

    return QuestTemplate(
        id = id,
        icon = g.icon.take(2).ifBlank { "◈" },
        title = title,
        // Never g.anything. The model has no say in this and no field to say it with.
        baseAura = BASE_AURA_BY_KIND[g.verifier.uppercase()] ?: return null,
        verifier = verifier,
    )
}

/**
 * The closed set, as the model is told it.
 *
 * Sent in the prompt as a convenience to the model, not as the enforcement. [toTemplate] rejects anything
 * outside it regardless of what the prompt said, because a prompt cannot be relied on to constrain a model.
 */
val ALLOWED_VERIFIERS: List<String> = BASE_AURA_BY_KIND.keys.toList()
