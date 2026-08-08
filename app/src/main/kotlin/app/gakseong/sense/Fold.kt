package app.gakseong.sense

// The pure half of usage reading. No Android import, so it is exhaustively testable without an emulator, which
// matters because an emulator's own usage history is thin and nothing like a real phone's.
//
// `sense/Usage.kt` is the other half: it queries UsageStatsManager and maps the result onto [UsageEvent].

/** One event from the system's stream, reduced to the three fields any verifier needs. */
data class UsageEvent(val packageName: String, val type: Int, val timestampMs: Long)

/**
 * The event type constants, by value rather than by name.
 *
 * `ACTIVITY_RESUMED` and `ACTIVITY_PAUSED` arrived in API 29 as renames of `MOVE_TO_FOREGROUND` and
 * `MOVE_TO_BACKGROUND`, with the same integer values, so minSdk 26 costs nothing. Declaring them here rather
 * than importing `UsageEvents.Event` is what keeps this file free of Android.
 */
object EventType {
    const val FOREGROUND = 1
    const val BACKGROUND = 2
    const val SCREEN_INTERACTIVE = 15
    const val SCREEN_NON_INTERACTIVE = 16
    /** API 29+. A second chance to close a session whose BACKGROUND the system dropped. */
    const val ACTIVITY_STOPPED = 23
}

/**
 * The longest a single unbroken foreground session may contribute.
 *
 * ponytail: a ceiling, not a model. Real event streams drop BACKGROUND pairs, and a device whose screen never
 * sleeps never emits SCREEN_NON_INTERACTIVE either, so an unclosed session otherwise runs across every idle gap
 * until the next foreground event. That produced 231 hours in a day on an emulator. Six hours is far longer
 * than any session these thresholds care about; if genuine marathon sessions ever need counting in full, the
 * upgrade is to close on ACTIVITY_STOPPED and KEYGUARD_SHOWN and drop the cap.
 */
const val MAX_SESSION_MS = 6 * 60 * 60 * 1000L

/**
 * Everything the closed verifier set needs, from one pass over the stream.
 *
 * Four separate queries over the same events would be four times the work for identical output, so this is a
 * single fold. [Unavailable] is what an ungranted or unsupported read returns; quest generation treats it as
 * "do not draw this verifier" rather than as a failure.
 */
data class UsageReading(
    val totalForegroundMs: Long,
    val perPackageMs: Map<String, Long>,
    val longestScreenOffMs: Long,
    val packagesOpened: Set<String>,
    val available: Boolean = true,
) {
    companion object {
        val Unavailable = UsageReading(0L, emptyMap(), 0L, emptySet(), available = false)
    }
}

/**
 * Fold [events] into one reading for the window `[windowStart, windowEnd)`.
 *
 * [carveOut] is subtracted from [UsageReading.totalForegroundMs] but stays in [UsageReading.perPackageMs]. It
 * covers the in-app reader and, during a committed running-raid window, the recording app: DECISIONS.md §24
 * says reader time is not screen time, and without the second carve-out a 10 km evening eats the day's budget
 * and the app penalises somebody for running.
 *
 * Events are sorted defensively. `queryEvents` returns them in order, but a caller merging two windows may not.
 */
fun fold(
    events: List<UsageEvent>,
    windowStart: Long,
    windowEnd: Long,
    carveOut: Set<String> = emptySet(),
): UsageReading {
    require(windowEnd >= windowStart) { "window ends before it starts" }

    val perPackage = mutableMapOf<String, Long>()
    val opened = mutableSetOf<String>()
    var longestOff = 0L

    var foregroundPackage: String? = null
    var foregroundSince = 0L
    // Screen state is unknown until the first screen event. Assuming "on" would invent a screen-off block for
    // every window that opens mid-sleep, which is exactly when the night gate is being checked.
    var offSince: Long? = null

    fun closeForeground(at: Long) {
        val pkg = foregroundPackage ?: return
        val from = maxOf(foregroundSince, windowStart)
        // A session that ran past the cap is credited the cap and no more. See MAX_SESSION_MS.
        val to = minOf(at, windowEnd, foregroundSince + MAX_SESSION_MS)
        if (to > from) perPackage[pkg] = (perPackage[pkg] ?: 0L) + (to - from)
        foregroundPackage = null
    }

    for (e in events.sortedBy { it.timestampMs }) {
        if (e.timestampMs > windowEnd) break
        when (e.type) {
            EventType.FOREGROUND -> {
                closeForeground(e.timestampMs)
                foregroundPackage = e.packageName
                foregroundSince = e.timestampMs
                if (e.timestampMs in windowStart..windowEnd) opened += e.packageName
            }

            EventType.BACKGROUND, EventType.ACTIVITY_STOPPED -> {
                // A BACKGROUND for a package that is not the tracked one is a stale pair, not this session.
                if (foregroundPackage == e.packageName) closeForeground(e.timestampMs)
            }

            EventType.SCREEN_NON_INTERACTIVE -> {
                // The screen going off also ends whatever was in front of it.
                closeForeground(e.timestampMs)
                if (offSince == null) offSince = e.timestampMs
            }

            EventType.SCREEN_INTERACTIVE -> {
                offSince?.let { start ->
                    val from = maxOf(start, windowStart)
                    val to = minOf(e.timestampMs, windowEnd)
                    if (to > from) longestOff = maxOf(longestOff, to - from)
                }
                offSince = null
            }
        }
    }

    // Whatever was still running when the window closed counts up to the window's edge and no further.
    closeForeground(windowEnd)
    offSince?.let { start ->
        val from = maxOf(start, windowStart)
        if (windowEnd > from) longestOff = maxOf(longestOff, windowEnd - from)
    }

    val total = perPackage.entries.filterNot { it.key in carveOut }.sumOf { it.value }
    return UsageReading(
        totalForegroundMs = total,
        perPackageMs = perPackage,
        longestScreenOffMs = longestOff,
        packagesOpened = opened,
    )
}
