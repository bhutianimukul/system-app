package app.gakseong.session

import android.app.AutomaticZenRule
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.service.notification.Condition
import android.service.notification.ZenPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

// Do Not Disturb during sessions. §22 and the DND rules in CLAUDE.md.
//
// The whole file exists to avoid one bug: setInterruptionFilter can strand a user in permanent silence after a
// crash, and they have no way to find out what did it. An AutomaticZenRule appears in system Settings under
// this app's own name, so it can be found, edited and killed by the person it is happening to.

private const val RULE_NAME = "The System · focus session"
private const val CONDITION_HOST = "gakseong.app"

/** Special access, granted through a Settings screen rather than a runtime dialog. Asked at the first session. */
fun hasDndAccess(context: Context): Boolean =
    context.getSystemService(NotificationManager::class.java)?.isNotificationPolicyAccessGranted == true

fun dndAccessIntent(): Intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

/**
 * Turn DND on for a session ending at [endAtMs].
 *
 * Returns false when access is refused or the platform already has DND on, and the caller carries on with a
 * normal session. §22: a refused grant degrades rather than blocking, and the app does not fight Bedtime mode
 * for control of the same setting.
 */
fun enableDnd(context: Context, endAtMs: Long): Boolean {
    if (!hasDndAccess(context)) return false
    val nm = context.getSystemService(NotificationManager::class.java) ?: return false

    // Someone who already had DND on for their own reasons keeps it, and gets it back untouched afterwards.
    val prior = nm.currentInterruptionFilter
    if (prior != NotificationManager.INTERRUPTION_FILTER_ALL) return false

    // §22: calls and repeat callers always ring. The audience includes fifteen-year-olds, and an app that
    // silences a parent's call for forty-five minutes is indefensible.
    val policy = ZenPolicy.Builder()
        .allowCalls(ZenPolicy.PEOPLE_TYPE_ANYONE)
        .allowRepeatCallers(true)
        .allowAlarms(true)
        .allowMedia(false)
        .allowMessages(ZenPolicy.PEOPLE_TYPE_NONE)
        .build()

    val ruleId = existingRuleId(nm) ?: runCatching {
        nm.addAutomaticZenRule(
            AutomaticZenRule(
                RULE_NAME,
                // No owner: this rule has no ConditionProviderService. A configuration activity makes it
                // app-owned, which is what setAutomaticZenRuleState requires and what puts the rule in system
                // Settings under this app's name.
                null,
                ComponentName(context, "app.gakseong.MainActivity"),
                conditionId(endAtMs),
                policy,
                NotificationManager.INTERRUPTION_FILTER_PRIORITY,
                true,
            ),
        )
    }.getOrNull() ?: return false

    runCatching {
        nm.setAutomaticZenRuleState(ruleId, Condition(conditionId(endAtMs), "Focus session", Condition.STATE_TRUE))
    }.onFailure { return false }

    // The backstop that makes "expires on its own" true rather than aspirational. WorkManager survives process
    // death, so a crash mid-session still ends in a phone that rings.
    WorkManager.getInstance(context).enqueue(
        OneTimeWorkRequestBuilder<DndOffWorker>()
            .setInitialDelay((endAtMs - System.currentTimeMillis()).coerceAtLeast(0L), TimeUnit.MILLISECONDS)
            .setInputData(Data.Builder().putString(DndOffWorker.RULE_ID, ruleId).build())
            .build(),
    )
    return true
}

/**
 * Hand the phone back. Safe to call when DND was never turned on, and safe to call twice.
 *
 * The rule is removed rather than set to STATE_FALSE. A state change is only honoured for a Condition whose id
 * matches the rule's own conditionId, and this app's conditionId carries the session's end time, so a generic
 * "off" condition is silently ignored and the phone stays silent. Verified on device: setting STATE_FALSE with
 * a non-matching id left zen_mode at 1.
 *
 * Removing it is also the honest end state. The rule describes one session; when the session is over there is
 * nothing for it to describe, and leaving it in the user's Settings list is clutter they did not ask for.
 */
fun disableDnd(context: Context) {
    val nm = context.getSystemService(NotificationManager::class.java) ?: return
    if (!nm.isNotificationPolicyAccessGranted) return
    existingRuleId(nm)?.let { id -> runCatching { nm.removeAutomaticZenRule(id) } }
}

private fun existingRuleId(nm: NotificationManager): String? = runCatching {
    nm.automaticZenRules.entries.firstOrNull { it.value.name == RULE_NAME }?.key
}.getOrNull()

private fun conditionId(endAtMs: Long): Uri =
    Uri.Builder().scheme("condition").authority(CONDITION_HOST).appendPath("focus")
        .appendQueryParameter("end", endAtMs.toString()).build()

/**
 * Clears the rule at the session's end even if the app is gone.
 *
 * This is the difference between a rule that expires on its own and one that merely claims to. Nothing here
 * reads state: it turns DND off, which is safe to do twice and safe to do when it was never on.
 */
class DndOffWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        disableDnd(applicationContext)
        return Result.success()
    }

    companion object {
        const val RULE_ID = "ruleId"
    }
}
