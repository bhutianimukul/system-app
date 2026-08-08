package app.gakseong.cloud

import android.content.Context
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Attribution. §Referral: Play hands the app the referrer string once, on first launch, for installs that went
// through the Store. This is why the design specifies App Links **plus** Install Referrer: a plain deep link
// cannot survive the Play round-trip, and Firebase Dynamic Links is shut down.

private const val TAG = "install"

/**
 * Read the referrer Play kept for this install, once.
 *
 * Returns null on an organic install, a sideload, an emulator with no Play Store, and any failure. All four are
 * the same thing as far as the app is concerned: nobody to credit, and nothing to say about it.
 *
 * The client disconnects immediately. `InstallReferrerClient` holds a service binding, and leaving one open for
 * a value that is read once in the app's lifetime is a binding that outlives its reason.
 */
suspend fun readReferrer(context: Context): String? = suspendCoroutine { cont ->
    val client = runCatching { InstallReferrerClient.newBuilder(context).build() }.getOrNull()
    if (client == null) {
        cont.resume(null)
        return@suspendCoroutine
    }

    var resumed = false
    fun finish(value: String?) {
        if (resumed) return
        resumed = true
        runCatching { client.endConnection() }
        cont.resume(value)
    }

    runCatching {
        client.startConnection(
            object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    if (responseCode != InstallReferrerClient.InstallReferrerResponse.OK) {
                        // FEATURE_NOT_SUPPORTED on a device with no Play Store, SERVICE_UNAVAILABLE otherwise.
                        // Neither is worth telling the user about: they did not ask to be attributed.
                        finish(null)
                        return
                    }
                    finish(runCatching { client.installReferrer.installReferrer }.getOrNull())
                }

                // The service went away before answering. Same outcome as never having answered.
                override fun onInstallReferrerServiceDisconnected() = finish(null)
            },
        )
    }.onFailure { finish(null) }
}

/**
 * Pair this install with whoever invited it, at most once ever.
 *
 * §Referral: `referrals/{inviteeUid}` with a create-only rule, so one credit per invitee UID for all time. The
 * rule is the enforcement; [alreadyPaired] only saves a round trip.
 *
 * Nothing here is retried. A referral that fails to land costs the inviter 600 aura they never knew about,
 * where a retry loop against a create-only rule would be a loop that can only ever fail.
 */
suspend fun attribute(context: Context, uid: String, today: String, alreadyPaired: Boolean): String? {
    if (alreadyPaired || uid.isBlank()) return null
    val code = codeFrom(readReferrer(context)) ?: return null

    // §Referral: the invitee's own UID is their identity, and the inviter is an opaque code derived from
    // theirs. No email, no phone, no account, on either side.
    val ok = pair(context, code, uid, today)
    Log.i(TAG, if (ok) "paired with an inviter" else "pairing refused, which create-only rules look like")
    return if (ok) code else null
}
