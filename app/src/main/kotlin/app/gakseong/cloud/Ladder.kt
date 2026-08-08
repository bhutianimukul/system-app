package app.gakseong.cloud

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

// The weekly league. §Social: guilds before global leaderboards, weekly leagues of about thirty, and a division
// below thirty real hunters is padded with System-run shadows that are stated on the screen to not be people.

private const val TAG = "ladder"

/** §Social: about thirty. A division below this is padded with labelled pacers, never with fake people. */
const val LEAGUE_SIZE = 30

/** One row on the ladder. [pacer] rows are the System's, and every screen that draws one must say so. */
data class Standing(
    val handle: String,
    val rank: String,
    val aura: Int,
    val you: Boolean = false,
    val pacer: Boolean = false,
)

/**
 * A hunter's public handle.
 *
 * Derived from their own UID, opaque, and stable. §Social forbids generating plausible human usernames, and
 * this is the reason the design already writes them as `Hunter D-91`: a handle nobody could mistake for a name
 * cannot make a padded ladder look populated.
 */
fun handleFor(uid: String, rankLetter: String): String =
    "Hunter $rankLetter-${(uid.hashCode().toLong() and 0xFFFF) % 100}".let {
        if (it.length > 14) it.take(14) else it
    }

/**
 * A league is one rank letter.
 *
 * §Economy: thresholds scale with rank, so hunters in the same letter are being asked for the same thing. A
 * ladder that mixes an E with an S is not a comparison anybody learns from.
 */
fun leagueFor(rankLetter: String) = rankLetter

/**
 * How long the cloud gets before the app carries on without it.
 *
 * Firestore retries a denied write indefinitely and its `get()` does not give up on its own, so without this a
 * screen waits forever on a project whose database was never provisioned. Verified: the league sat on "reading
 * the division" until the process was killed. §8's rule is that everything works offline, and a spinner that
 * never resolves is not working offline.
 */
private const val CLOUD_TIMEOUT_MS = 4_000L

/** Write your own row and nobody else's. The security rules enforce the same thing from the other side. */
suspend fun pushStanding(context: Context, uid: String, rankLetter: String, aura: Int) {
    val store = db(context) ?: return
    if (uid.isBlank()) return
    withTimeoutOrNull(CLOUD_TIMEOUT_MS) {
    runCatching {
        store.collection("gakseong").document("leagues")
            .collection(leagueFor(rankLetter)).document("standings")
            .collection("rows").document(uid)
            .set(mapOf("aura" to aura, "rank" to rankLetter, "updatedAt" to FieldValue.serverTimestamp()))
            .await()
    }.onFailure { Log.w(TAG, "standing not pushed, staying local") }
    }
}

/**
 * Read the division, and pad it with pacers if it is thin.
 *
 * §Social: shadow pacers hold a fixed pace, carry `◇` and the label `pacer`, are stated on the screen to not be
 * people, and are never counted as members. Each real hunter who joins replaces one.
 */
suspend fun readLadder(context: Context, uid: String, rankLetter: String, yourAura: Int): List<Standing> {
    val store = db(context)
    val real = if (store == null) emptyList() else withTimeoutOrNull(CLOUD_TIMEOUT_MS) {
        runCatching {
        store.collection("gakseong").document("leagues")
            .collection(leagueFor(rankLetter)).document("standings")
            .collection("rows").get().await()
            .documents.mapNotNull { doc ->
                val aura = (doc.getLong("aura") ?: return@mapNotNull null).toInt()
                Standing(
                    handle = handleFor(doc.id, rankLetter),
                    rank = rankLetter,
                    aura = aura,
                    you = doc.id == uid,
                )
                }
        }.getOrElse {
            Log.w(TAG, "ladder unreadable, showing yours alone")
            emptyList()
        }
    } ?: emptyList<Standing>().also { Log.w(TAG, "ladder timed out, showing yours alone") }

    val withYou =
        if (real.any { it.you }) real
        else real + Standing(handleFor(uid.ifBlank { "local" }, rankLetter), rankLetter, yourAura, you = true)

    return (withYou + pacers(rankLetter, LEAGUE_SIZE - withYou.size)).sortedByDescending { it.aura }
}

/**
 * The System's own rows, for a division that has not filled yet.
 *
 * Their pace is fixed and their handles say what they are. §Social: never generate plausible human usernames to
 * pad a ladder, because the ladder is the one thing in this app that has to be trustworthy and a fabricated
 * member who never posts in the guild feed gets noticed.
 */
fun pacers(rankLetter: String, count: Int): List<Standing> {
    if (count <= 0) return emptyList()
    // A fixed pace per position rather than a random one: a pacer that drifts is pretending to have a bad day.
    return (1..count).map { i ->
        Standing(
            // The row draws the ◇ itself. §Social wants both the diamond and the label; carrying the
            // diamond here too printed "◇ ◇ pacer 1".
            handle = "pacer $i",
            rank = rankLetter,
            aura = 400 + (count - i) * 60,
            pacer = true,
        )
    }
}

/** §Social: pacers are never counted as members. The screen shows this number, not the row count. */
fun memberCount(ladder: List<Standing>) = ladder.count { !it.pacer }
