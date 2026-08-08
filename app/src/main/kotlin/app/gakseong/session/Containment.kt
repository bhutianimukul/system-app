package app.gakseong.session

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import app.gakseong.data.Repo
import gakseong.engine.Penalty
import gakseong.engine.penaltyFor

// Containment: real blocking, and the most dangerous thing in this app.
//
// §Verifiers: it needs an AccessibilityService, so it stays opt-in, is **earned by repeated failure**, is
// disclosed at onboarding, and the permission is requested at the trigger. An accessibility dialog warning that
// the app "can view your screen" will kill installs if it is shown on day one.
//
// Everything here is written so that the answer to "can the app block me right now" is a rule the user already
// agreed to, and never a surprise.

/**
 * Whether containment may even be offered.
 *
 * §Punishment: the sequence is streak, then aura, then warning, then demotion and containment, spread across a
 * week. Containment is authorised once per miss run, at the end of it, and never earlier. Offering it sooner
 * would make it the app's first move rather than its last.
 */
fun containmentEarned(consecutiveMisses: Int): Boolean = penaltyFor(consecutiveMisses) == Penalty.DEMOTION

/** Whether the user has actually turned it on. Off is the only default this could have. */
fun containmentEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false
    val self = ComponentName(context, ContainmentService::class.java).flattenToString()
    return flat.split(':').any { it.equals(self, ignoreCase = true) }
}

/**
 * The grant screen.
 *
 * Opened at the trigger and nowhere else. §Onboarding discloses that this exists; it does not ask for it, and
 * the difference is the whole reason the app is installable.
 */
fun containmentIntent(): Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

/**
 * The service that does the blocking.
 *
 * It reads one thing: which package came to the front. It never reads text, never reads fields, and has no
 * reason to. The manifest declares the narrowest configuration that can answer that question, because an
 * accessibility service is the broadest permission Android has and this one is here for a single yes/no.
 */
class ContainmentService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val package_ = event.packageName?.toString() ?: return

        val state = Repo.state.value
        // Containment applies to the user's own confirmed list and to nothing else. It is not a general
        // blocker, and the app never decides on its own what somebody should be kept out of.
        if (package_ !in state.profile.watchedPackages) return
        if (!containmentEarned(state.hunter.consecutiveMisses)) return

        // Back, not a kill. Sending somebody home is enough to interrupt; force-stopping an app they opened is
        // a level of authority this feature was never granted, whatever the permission technically allows.
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    override fun onInterrupt() = Unit
}
