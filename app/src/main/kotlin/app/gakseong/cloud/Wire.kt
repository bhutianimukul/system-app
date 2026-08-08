package app.gakseong.cloud

// What may leave the device, as a closed set.
//
// §10 names three things and nothing else: guild identity when you join one, anonymous feature counts, and
// crash stack traces. Never a package name from the user's app list, never a duration, never anything from the
// private track.
//
// This is an allowlist rather than a blocklist, for the same reason the share card is: adding a new event means
// adding a case here, and a screen cannot invent one. A `logEvent(String, Bundle)` call anywhere in the app
// would make every one of these rules a matter of review discipline instead.

/**
 * Every analytics event the app can emit.
 *
 * There is no free-text constructor on purpose. §10's "anonymous feature counts" means a count of which
 * features get opened, and nothing about what the user did inside them.
 */
enum class Event(val wireName: String) {
    APP_OPENED("app_opened"),
    ONBOARDING_COMPLETED("onboarding_completed"),
    QUEST_CLEARED("quest_cleared"),
    SESSION_STARTED("session_started"),
    SESSION_HELD("session_held"),
    SESSION_BROKEN("session_broken"),
    GUILD_JOINED("guild_joined"),
    RAID_STARTED("raid_started"),
    SHARE_OPENED("share_opened"),
    AI_KEY_ADDED("ai_key_added"),
    ;

    companion object {
        /**
         * The private track emits none of these, and there is no event it may emit.
         *
         * §9: the exemption is unconditional and is not tied to the analytics toggle. Somebody who opens the
         * private track at 1am must leave no trace that they did, including the fact that it happened.
         */
        val PRIVATE_TRACK_EXEMPT = true
    }
}

/**
 * Parameter values that may accompany an event.
 *
 * A bare enum, so a caller cannot attach a package name or a duration by writing a string. §10 forbids both,
 * and the most likely way either would arrive is somebody adding "just one" contextual field.
 */
enum class Dimension(val wireName: String) {
    RANK_LETTER("rank_letter"),
    HAS_GUILD("has_guild"),
    QUEST_PROVABILITY("quest_provability"),
}

// Shapes that must never reach the wire, whatever anyone believes they are sending.
//
// One dot, not two. The first draft required two or more, which let com.whatsapp, in.mohalla and app.gakeseong
// through: two-segment package names are ordinary, and every one of them is a package name from the user's app
// list. Nothing this app legitimately sends contains a dot at all.
private val PACKAGE_SHAPED = Regex("""^[a-z][a-z0-9_]*(\.[a-z0-9_]+)+$""")

// Plurals included. `hour\b` cannot match "hours", because s is a word character.
private val DURATION_SHAPED = Regex(
    """\d+\s*(ms|s|m|h|mins?|hours?|hrs?|secs?|minutes?|seconds?)\b|\d+:\d{2}""",
    RegexOption.IGNORE_CASE,
)

/**
 * The last line before the wire.
 *
 * Belt and braces over the enums above: a `RANK_LETTER` of "com.instagram.android" is not something the type
 * system can catch, and the cost of being wrong here is a package name in Google's logs.
 */
fun refusedReason(value: String): String? = when {
    PACKAGE_SHAPED.matches(value) -> "looks like a package name"
    DURATION_SHAPED.containsMatchIn(value) -> "looks like a duration"
    value.length > 36 -> "too long to be a dimension, and long strings carry detail"
    else -> null
}

/** True when [value] may be sent for [dimension]. */
fun permitted(dimension: Dimension, value: String): Boolean = refusedReason(value) == null
