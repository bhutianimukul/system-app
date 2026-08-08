package app.gakseong.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.gakseong.data.Repo
import app.gakseong.ai.AiResult
import app.gakseong.ai.generateQuest
import app.gakseong.ai.readKey
import app.gakseong.ai.toTemplate
import app.gakseong.quest.Readings
import app.gakseong.quest.instance
import app.gakseong.quest.tick
import app.gakseong.sense.readerCarveOut
import app.gakseong.sense.readHealth
import app.gakseong.sense.readUsage
import app.gakseong.session.FocusService
import app.gakseong.session.heldMinutes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * The retroactive settle. §2: query history on app open, on widget refresh and on a fifteen-minute job. Never a
 * persistent foreground service for tracking.
 *
 * `tick` is idempotent by date, so running this more often than necessary costs a query and changes nothing.
 */
class SettleWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val readings = gather(applicationContext)
        Repo.remember(readings)
        Repo.update { tick(it, readings, Repo.today(), Repo.endOfDayMs()) }
        generateOne(applicationContext, readings)
        return Result.success()
    }

    companion object {
        private const val NAME = "settle"

        /**
         * Fifteen minutes is WorkManager's floor, not a choice. Asking for less silently becomes this anyway,
         * so it is written plainly rather than discovered later.
         */
        /** A single pass now. Used on app open so a pasted key does not wait for the periodic job. */
        fun runNow(context: Context) {
            WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<SettleWorker>().build())
        }

        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<SettleWorker>(15, TimeUnit.MINUTES).build(),
            )
        }
    }
}

/**
 * Gather everything the verifiers can see, for the day that is currently open.
 *
 * The reader carve-out is passed here and only here: this is a threshold measurement, and §24 says reader time
 * is not screen time in every threshold that measures screen time.
 */
suspend fun gather(context: Context): Readings {
    val zone = ZoneId.systemDefault()
    val startOfDay = LocalDate.now(zone).atStartOfDay(zone).toInstant()
    val now = Instant.now()

    return Readings(
        usage = readUsage(context, startOfDay.toEpochMilli(), now.toEpochMilli(), setOf(readerCarveOut(context))),
        health = readHealth(context, startOfDay, now),
        focusMinutes = FocusService.state.value?.let { heldMinutes(it, now.toEpochMilli()) } ?: 0,
    )
}

/**
 * The one quest the System writes itself, when there is a key.
 *
 * §AI gate: the daily quest never locks, so this is an addition to the static day rather than a replacement for
 * it. With no key, a refused key, or no network, the day is exactly the day a keyless user gets.
 *
 * Issued at most once a day. The id carries the date so the check is a lookup rather than a flag, and a second
 * worker run in the same day finds it already there.
 */
private suspend fun generateOne(context: Context, readings: Readings) {
    val key = readKey(context) ?: return
    val state = Repo.state.value
    val id = "gen-${Repo.today()}"
    if (state.today.quests.any { it.id == id }) return

    // What the model is told about the user. Built from an allowlist the same way the share card is: a rank
    // letter and a streak count, and nothing about which apps or how long. §10 governs this even though it is
    // going to Google rather than to another hunter.
    val record = "rank ${state.hunter.toEngine().rank.letter}, ${state.hunter.streak} day streak, " +
        "${state.history.takeLast(7).count { it.outcome != "BELOW_THRESHOLD" }} of the last 7 days held"

    when (val result = generateQuest(key, record)) {
        is AiResult.Ok -> {
            val template = toTemplate(result.generated, id) ?: return
            Repo.update { s ->
                if (s.today.quests.any { it.id == id }) s
                else s.copy(today = s.today.copy(quests = s.today.quests + template.instance(readings)))
            }
        }
        // A failure is silent here. §AI gate: never a nag, and the user did not ask for this right now.
        is AiResult.Failed, AiResult.Locked -> Unit
    }
}
