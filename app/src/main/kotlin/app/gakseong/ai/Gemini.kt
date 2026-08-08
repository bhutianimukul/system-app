package app.gakseong.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.net.HttpURLConnection
import java.net.URL

// One plain HTTPS POST. §Stack: bring your own key, and no SDK for a single endpoint.
//
// ponytail: HttpURLConnection rather than a client library. This is one request shape to one host; adding
// Retrofit and OkHttp to make it would be several megabytes of dependency for a function that fits on a screen.
// Swap it the day a second endpoint appears.

private const val TAG = "ai"
private const val MODEL = "gemini-2.0-flash"
private const val HOST = "https://generativelanguage.googleapis.com/v1beta/models"

private val json = Json { ignoreUnknownKeys = true }

/** What went wrong, in words a screen can show without pretending it knows more than it does. */
sealed interface AiResult {
    data class Ok(val generated: Generated) : AiResult
    data class Failed(val reason: String) : AiResult
    /** No key, which is not a failure. §AI gate: the feature is locked, the app is not. */
    data object Locked : AiResult
}

/**
 * Ask for one quest.
 *
 * [record] is a short, already-anonymous description of how the week has gone. It carries no package name, no
 * duration and nothing from the private track: §9 puts the private track off-limits to AI on either tier, and
 * §10 governs everything else that leaves the device. What goes in the prompt is built by the caller from the
 * same allowlist thinking as the share card.
 *
 * The schema is sent so the model returns JSON rather than prose, and [toTemplate] rejects anything outside the
 * closed set regardless. Structured output is a convenience; the clamp is the enforcement.
 */
suspend fun generateQuest(key: String, record: String): AiResult = withContext(Dispatchers.IO) {
    if (key.isBlank()) return@withContext AiResult.Locked

    val body = buildJsonObject {
        putJsonArray("contents") {
            add(
                buildJsonObject {
                    putJsonArray("parts") {
                        add(buildJsonObject { put("text", prompt(record)) })
                    }
                },
            )
        }
        putJsonObject("generationConfig") {
            put("responseMimeType", "application/json")
            putJsonObject("responseSchema") {
                put("type", "OBJECT")
                putJsonObject("properties") {
                    putJsonObject("verifier") {
                        put("type", "STRING")
                        putJsonArray("enum") { ALLOWED_VERIFIERS.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } }
                    }
                    putJsonObject("minutes") { put("type", "INTEGER") }
                    putJsonObject("steps") { put("type", "INTEGER") }
                    putJsonObject("metres") { put("type", "INTEGER") }
                    putJsonObject("title") { put("type", "STRING") }
                }
                putJsonArray("required") {
                    add(kotlinx.serialization.json.JsonPrimitive("verifier"))
                    add(kotlinx.serialization.json.JsonPrimitive("title"))
                }
            }
        }
    }

    runCatching {
        val connection = (URL("$HOST/$MODEL:generateContent").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            // The key goes in a header, not the query string. A URL turns up in logs and crash traces; a
            // header does not, and §AI gate says the key is never logged.
            setRequestProperty("x-goog-api-key", key)
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 15_000
            readTimeout = 30_000
            doOutput = true
        }
        connection.outputStream.use { it.write(body.toString().encodeToByteArray()) }

        val code = connection.responseCode
        if (code != 200) {
            // The body of an error can carry the key back in some APIs, so only the code is kept.
            val reason = when (code) {
                400 -> "The System could not read that reply"
                401, 403 -> "That key was refused. Check it in Settings"
                429 -> "The free tier is rate limited. Try later"
                in 500..599 -> "Google is having a moment. Try later"
                else -> "Unreachable"
            }
            return@runCatching AiResult.Failed(reason)
        }

        val text = json.parseToJsonElement(connection.inputStream.bufferedReader().readText())
            .jsonObject["candidates"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("content")?.jsonObject?.get("parts")?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: return@runCatching AiResult.Failed("Empty reply")

        AiResult.Ok(json.decodeFromString<GeneratedWire>(text).toGenerated())
    }.getOrElse {
        // Never the exception message: it can contain the URL, and the URL used to contain the key.
        Log.w(TAG, "generation failed: ${it::class.simpleName}")
        AiResult.Failed("The System could not reach its voice")
    }
}

/** The reply, exactly as the schema describes it. Nothing here becomes a quest without passing [toTemplate]. */
@kotlinx.serialization.Serializable
private data class GeneratedWire(
    val verifier: String = "",
    val minutes: Int = 0,
    val steps: Int = 0,
    val metres: Int = 0,
    val title: String = "",
) {
    fun toGenerated() = Generated(verifier, minutes, steps, metres, title)
}

/**
 * The prompt.
 *
 * It states the closed set and the bounds so the model has a chance of getting it right first time, and none of
 * that is relied on: §7 says clamps are enforced in code after generation, never in the prompt.
 */
private fun prompt(record: String) = """
    You write one daily quest for a discipline app. The user's record this week: $record

    Pick exactly one verifier from: ${ALLOWED_VERIFIERS.joinToString(", ")}
    Give the numeric parameter that verifier needs, and a title of at most ${Clamp.MAX_TITLE} characters.

    Minutes must be between ${Clamp.MIN_MINUTES} and ${Clamp.MAX_MINUTES}.
    Steps must be between ${Clamp.MIN_STEPS} and ${Clamp.MAX_STEPS}.
    Metres must be between ${Clamp.MIN_METRES} and ${Clamp.MAX_METRES}.

    Never mention an app by name. Never suggest exercise as a punishment. Never assign a point or aura value:
    that is not yours to decide. Write the title as the System would speak it: plain, short, no encouragement.
""".trimIndent()
