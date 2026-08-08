package app.gakseong.cloud

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

// Guilds and the guild feed. §Social: guilds before global leaderboards, invite only, twenty at most, and the
// feed ends at twenty posts a day because global UGC would need moderation, reporting, blocking and a published
// policy, all four being Play requirements.

private const val TAG = "guild"
private const val CLOUD_TIMEOUT_MS = 4_000L

/** §Social: twenty at most. Small enough that an absent member is noticed, which is the point of a guild. */
const val GUILD_MAX = 20

/** §Social: the feed ends at twenty a day. */
const val FEED_PER_DAY = 20

data class Guild(val id: String, val name: String, val members: List<String>, val foundedOn: String)

data class Post(val authorHandle: String, val text: String, val aura: Int?, val at: String, val you: Boolean)

/**
 * Join a guild by its invite code, which is its document id.
 *
 * §Stack says no server code, so joining has to be something a security rule can police on its own. The rule
 * allows exactly one shape of update: adding your own uid and changing nothing else. That is enough to make a
 * roster unforgeable without a Cloud Function, because the only member you can ever add is yourself.
 */
suspend fun joinGuild(context: Context, guildId: String, uid: String): Boolean {
    val store = db(context) ?: return false
    if (uid.isBlank() || guildId.isBlank()) return false
    return withTimeoutOrNull(CLOUD_TIMEOUT_MS) {
        runCatching {
            store.collection("gakseong").document("guilds")
                .collection("rows").document(guildId)
                .update("members", FieldValue.arrayUnion(uid))
                .await()
            true
        }.onFailure { Log.w(TAG, "join refused", it) }.getOrDefault(false)
    } ?: false
}

/**
 * Found one, with yourself as its only member.
 *
 * The id doubles as the invite code, so there is no separate pairing step and no code to type beyond this one.
 * §Social: the invite is the install loop, and a friend list is one more thing that can go wrong.
 */
suspend fun createGuild(context: Context, name: String, uid: String, today: String): String? {
    val store = db(context) ?: return null
    if (uid.isBlank()) return null
    return withTimeoutOrNull(CLOUD_TIMEOUT_MS) {
        runCatching {
            val doc = store.collection("gakseong").document("guilds").collection("rows").document()
            doc.set(mapOf("name" to name.take(24), "members" to listOf(uid), "foundedOn" to today)).await()
            doc.id
        }.onFailure { Log.w(TAG, "guild not created", it) }.getOrNull()
    }
}

/** The guild you are in, or null. A hunter belongs to one at a time, which is what makes thirty feel small. */
suspend fun readGuild(context: Context, uid: String): Guild? {
    val store = db(context) ?: return null
    if (uid.isBlank()) return null
    return withTimeoutOrNull(CLOUD_TIMEOUT_MS) {
        runCatching {
            store.collection("gakseong").document("guilds").collection("rows")
                .whereArrayContains("members", uid).limit(1).get().await()
                .documents.firstOrNull()?.let { doc ->
                    Guild(
                        id = doc.id,
                        name = doc.getString("name").orEmpty(),
                        members = (doc.get("members") as? List<*>)?.filterIsInstance<String>().orEmpty(),
                        foundedOn = doc.getString("foundedOn").orEmpty(),
                    )
                }
        }.onFailure { Log.w(TAG, "guild unreadable", it) }.getOrNull()
    }
}

/**
 * The day's posts, guild-scoped and capped.
 *
 * Authors are shown by handle, never by anything they typed about themselves. §Social forbids plausible human
 * usernames, and a guild feed is exactly where a fabricated member would be noticed.
 */
suspend fun readFeed(context: Context, guildId: String, uid: String, rankLetter: String): List<Post> {
    val store = db(context) ?: return emptyList()
    return withTimeoutOrNull(CLOUD_TIMEOUT_MS) {
        runCatching {
            store.collection("gakseong").document("guilds").collection("rows").document(guildId)
                .collection("feed")
                .orderBy("at", Query.Direction.DESCENDING)
                .limit(FEED_PER_DAY.toLong())
                .get().await()
                .documents.mapNotNull { doc ->
                    val author = doc.getString("authorUid") ?: return@mapNotNull null
                    Post(
                        authorHandle = if (author == uid) "You" else handleFor(author, rankLetter),
                        text = doc.getString("text").orEmpty(),
                        aura = doc.getLong("aura")?.toInt(),
                        at = doc.getString("at").orEmpty(),
                        you = author == uid,
                    )
                }
        }.onFailure { Log.w(TAG, "feed unreadable", it) }.getOrDefault(emptyList())
    } ?: emptyList()
}

/**
 * The set of things a post may say.
 *
 * §10 again, in the place it is easiest to forget: a feed post goes to other people. It may name a milestone
 * and an aura figure, and it may never carry a package name, a duration or a screen-time number. The enum is
 * the allowlist, so a screen cannot compose a sentence out of the user's own usage.
 */
enum class PostKind(val text: String) {
    THRESHOLD_CLEARED("cleared the threshold"),
    NIGHT_GATE_HELD("cleared the night gate"),
    FOCUS_HELD("held a focus session"),
    RAID_CLEARED("cleared a raid"),
    RANK_TAKEN("took a new rank"),
    STREAK_HELD("held the streak another week"),
}

/** Post as yourself, and only as yourself. The rule enforces the same thing from the other side. */
suspend fun post(context: Context, guildId: String, uid: String, kind: PostKind, aura: Int?, at: String): Boolean {
    val store = db(context) ?: return false
    if (uid.isBlank() || guildId.isBlank()) return false
    return withTimeoutOrNull(CLOUD_TIMEOUT_MS) {
        runCatching {
            store.collection("gakseong").document("guilds").collection("rows").document(guildId)
                .collection("feed").document()
                .set(
                    // Every field is either an id, an allowlisted phrase, or an aura figure. Nothing here is
                    // free text, which is also why the feed needs no moderation queue.
                    mapOf("authorUid" to uid, "text" to kind.text, "aura" to aura, "at" to at),
                ).await()
            true
        }.onFailure { Log.w(TAG, "post refused", it) }.getOrDefault(false)
    } ?: false
}
