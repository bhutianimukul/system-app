package app.gakseong.ai

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// The key, and nothing else. §AI gate: five features need one, and the app is fully complete with zero AI
// configured. Whatever is wrong in here, the daily quest still arrives.

private const val FILE = "ai"
private const val KEY = "gemini"

/**
 * Bring your own key, stored in the Android keystore.
 *
 * Never logged, never in analytics, never sent anywhere but Google's endpoint. `cloud/Wire.kt` cannot carry it
 * even by accident: a key is 39 characters and every dimension is refused above 36.
 */
private fun prefs(context: Context): SharedPreferences? = runCatching {
    EncryptedSharedPreferences.create(
        context,
        FILE,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
}.getOrNull()

fun hasKey(context: Context): Boolean = !readKey(context).isNullOrBlank()

fun readKey(context: Context): String? = prefs(context)?.getString(KEY, null)

fun writeKey(context: Context, key: String) {
    prefs(context)?.edit()?.putString(KEY, key.trim())?.apply()
}

/** §AI gate: `Not now` must be a real answer, and so must changing your mind later. */
fun clearKey(context: Context) {
    prefs(context)?.edit()?.remove(KEY)?.apply()
}

/**
 * What a key looks like, so a paste of the wrong thing says so immediately.
 *
 * Google's AI Studio keys are `AIza` followed by 35 characters. Checking the shape is not checking the key: a
 * wrong-but-well-formed key fails at the first request, and that is where the real answer comes from.
 */
fun looksLikeKey(candidate: String): Boolean =
    Regex("""^AIza[A-Za-z0-9_\-]{35}$""").matches(candidate.trim())

/** The five features §AI gate names. Each shows a locked state in place rather than a modal over the core loop. */
enum class AiFeature(val label: String) {
    GENERATED_QUESTS("Generated quests"),
    POST_MORTEM("The weekly post-mortem"),
    CHAT("Speaking to the System"),
    READER_PASSAGE("Reader passage selection"),
    TITLES("Title generation"),
}

/**
 * The daily quest is deliberately absent from [AiFeature].
 *
 * §AI gate: it never locks. It falls back to the static bank, which is why §8 says the bank gets built
 * regardless. If a user with no key ever loses quests, thresholds, streaks, gates, raids, leagues, shadows or
 * the private track, the build has gone wrong.
 */
const val DAILY_QUEST_NEVER_LOCKS = true
