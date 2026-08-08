package app.gakseong.sense

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings

// The Android half of usage reading. Everything decidable without a device lives in `sense/Fold.kt`; this file
// only asks the system for events and hands them over.

/**
 * The app's own package, read from the context rather than written down.
 *
 * `UsageStatsManager` reports the applicationId, which is not the Kotlin namespace: this app's code lives in
 * `app.gakseong` and ships as `app.gakeseong`. A hard-coded constant would silently carve out nothing.
 *
 * §24 carves **reader session** time out of screen time, not all time spent in the app. Until reader sessions
 * are recorded there is nothing finer to carve, so callers that measure a threshold pass this explicitly and
 * everything else passes nothing. Defaulting to it made the Reality screen read 0 hours while its own list
 * showed 231.
 */
fun readerCarveOut(context: Context): String = context.packageName

/**
 * Usage access is **special access**, not a runtime permission. There is no `requestPermissions` for it and no
 * dialog to show: the only route is [usageAccessIntent] into system Settings.
 */
fun hasUsageAccess(context: Context): Boolean {
    val ops = context.getSystemService(AppOpsManager::class.java) ?: return false
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ops.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName,
        )
    } else {
        @Suppress("DEPRECATION")
        ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

/** Ask at the first quest that needs it, never at onboarding. §3. */
fun usageAccessIntent(): Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

/**
 * Read `[windowStart, windowEnd)` off the device.
 *
 * Retroactive by design: this is called on app open, on widget refresh and from the 15-minute WorkManager job,
 * never from a persistent watcher. OEM battery managers kill long-lived services, and a design that depends on
 * one is dead on most Indian devices.
 *
 * Returns [UsageReading.Unavailable] rather than an empty reading when access is absent, so a missing grant can
 * never be mistaken for a clean day.
 */
fun readUsage(
    context: Context,
    windowStart: Long,
    windowEnd: Long,
    carveOut: Set<String> = emptySet(),
): UsageReading {
    if (!hasUsageAccess(context)) return UsageReading.Unavailable
    val manager = context.getSystemService(UsageStatsManager::class.java) ?: return UsageReading.Unavailable

    // Events are queried from before the window so a session already running at windowStart is seen starting,
    // not appearing from nowhere. The fold clips it to the window.
    val lookBehind = windowStart - LOOK_BEHIND_MS
    val stream = manager.queryEvents(lookBehind, windowEnd)
    val out = ArrayList<UsageEvent>(256)
    val event = UsageEvents.Event()
    while (stream.hasNextEvent()) {
        stream.getNextEvent(event)
        out += UsageEvent(event.packageName ?: continue, event.eventType, event.timeStamp)
    }
    return fold(out, windowStart, windowEnd, carveOut)
}

/**
 * Android keeps roughly thirty days of events, which is the whole rhetorical weight of the Reality screen: the
 * System did not compute the number, the phone already had it.
 */
const val RETAINED_DAYS = 30

/** Twelve hours is longer than any plausible single session, so a straddling one is always caught. */
private const val LOOK_BEHIND_MS = 12 * 60 * 60 * 1000L
