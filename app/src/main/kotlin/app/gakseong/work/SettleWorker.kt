package app.gakseong.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.gakseong.data.Repo
import app.gakseong.quest.Readings
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
        return Result.success()
    }

    companion object {
        private const val NAME = "settle"

        /**
         * Fifteen minutes is WorkManager's floor, not a choice. Asking for less silently becomes this anyway,
         * so it is written plainly rather than discovered later.
         */
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
