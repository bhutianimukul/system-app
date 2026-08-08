package app.gakseong.cloud

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// The thin Firebase half. Everything decidable without a network lives in `cloud/Wire.kt` and
// `cloud/Referral.kt`, which is where the rules that matter are tested.
//
// Every call here degrades. A missing google-services.json, a refused sign-in and an offline device all leave
// the app fully playable: §8 says a user with no key gets the whole ladder, and the same is true of no network.

private const val TAG = "cloud"

/**
 * Whether Firebase is configured at all.
 *
 * `FirebaseApp.initializeApp` returns null when there is no google-services.json, which is the state every
 * build was in before phase 07 and the state a fork without its own project will be in.
 */
fun cloudAvailable(context: Context): Boolean =
    runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)

/**
 * Sign in anonymously. No account, no email, no phone.
 *
 * §Referral: the whole chain runs on identifiers the platform already gives you, and adding email or phone
 * collection buys almost no fraud resistance while costing the entire no-account posture.
 */
suspend fun signIn(context: Context): String? {
    if (!cloudAvailable(context)) return null
    return runCatching {
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.uid ?: auth.signInAnonymously().await().user?.uid
    }.onFailure { Log.w(TAG, "anonymous sign-in failed, staying local", it) }.getOrNull()
}

/**
 * §Referral: only genuine app instances may write.
 *
 * Cheap hardening with no PII. Paired with a rate limit on `referrals` creates per inviter in the security
 * rules, which is the other half and lives in `firestore.rules`.
 */
fun installAppCheck(context: Context) {
    if (!cloudAvailable(context)) return
    runCatching {
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
    }.onFailure { Log.w(TAG, "App Check unavailable", it) }
}

/**
 * The only path to analytics in this app.
 *
 * Two gates, and the first one is not a toggle. §9: the private track emits no event of any kind,
 * unconditionally, so there is no [Event] that names it and no caller inside it. §10: the value of every
 * dimension is checked against [refusedReason] before it goes, because a `RANK_LETTER` holding a package name
 * is not something the enum can catch.
 *
 * A refused value drops the whole event rather than the one field. Sending an event with a hole in it would be
 * choosing to guess which half the user consented to.
 */
fun log(context: Context, event: Event, dimensions: Map<Dimension, String> = emptyMap()) {
    if (!cloudAvailable(context)) return

    dimensions.forEach { (dimension, value) ->
        refusedReason(value)?.let { why ->
            // Logged locally, never sent. Somebody added a field they should not have, and silence here would
            // hide it until it was in production.
            Log.e(TAG, "refused ${dimension.wireName}: $why")
            return
        }
    }

    runCatching {
        FirebaseAnalytics.getInstance(context).logEvent(
            event.wireName,
            android.os.Bundle().apply {
                dimensions.forEach { (dimension, value) -> putString(dimension.wireName, value) }
            },
        )
    }
}

/** Firestore, or null when the app is running local-only. */
fun db(context: Context): FirebaseFirestore? =
    if (cloudAvailable(context)) runCatching { FirebaseFirestore.getInstance() }.getOrNull() else null

/**
 * Record the pairing. §Referral: `referrals/{inviteeUid} = {inviterUid, installedAt, state:pending}`, with a
 * security rule allowing create only and never update, so one credit per invitee UID, ever.
 *
 * The client cannot mark itself released. A client-reported "I cleared three days" is written by the very
 * account being verified, so [released] runs against days the server can see, and until there is a Cloud
 * Function that is a deliberate gap rather than an oversight.
 */
suspend fun pair(context: Context, inviterCode: String, inviteeUid: String, installedOn: String): Boolean {
    val store = db(context) ?: return false
    return runCatching {
        // Namespaced like every other collection: a Firestore ruleset is project-wide, and this
        // project also serves com.gakeseong.
        store.collection("gakseong").document("referrals")
            .collection("rows").document(inviteeUid).set(
            mapOf(
                "inviterCode" to inviterCode,
                "installedOn" to installedOn,
                "state" to "pending",
            ),
        ).await()
        true
    }.onFailure { Log.w(TAG, "pairing refused, which is what create-only rules look like", it) }
        .getOrDefault(false)
}
