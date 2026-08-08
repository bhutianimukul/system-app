package app.gakseong.sense

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

// The Android half of Health Connect. Provenance lives in `sense/Provenance.kt`, which is pure and tested.

/** Three-valued on purpose. "Not installed" and "needs an update" are different problems with different fixes. */
enum class HealthAvailability { AVAILABLE, UPDATE_REQUIRED, UNSUPPORTED }

/** Steps, distance and sleep. Nothing else is read, because nothing else is verified. */
val HEALTH_PERMISSIONS: Set<String> = setOf(
    HealthPermission.getReadPermission(StepsRecord::class),
    HealthPermission.getReadPermission(DistanceRecord::class),
    HealthPermission.getReadPermission(SleepSessionRecord::class),
)

fun healthAvailability(context: Context): HealthAvailability =
    when (HealthConnectClient.getSdkStatus(context)) {
        HealthConnectClient.SDK_AVAILABLE -> HealthAvailability.AVAILABLE
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthAvailability.UPDATE_REQUIRED
        else -> HealthAvailability.UNSUPPORTED
    }

/**
 * Read `[from, to)` and apply provenance.
 *
 * Returns [HealthReading.Unavailable] when Health Connect is missing, stale or ungranted. §Verifiers: no grant
 * means the objective is never drawn, which is not the same as failing it.
 *
 * Sleep is read here rather than polled. §Verifiers puts one query at 06:00, at zero battery cost, and this is
 * the function that query calls.
 */
suspend fun readHealth(context: Context, from: Instant, to: Instant): HealthReading {
    if (healthAvailability(context) != HealthAvailability.AVAILABLE) return HealthReading.Unavailable
    val client = runCatching { HealthConnectClient.getOrCreate(context) }.getOrNull()
        ?: return HealthReading.Unavailable

    val granted = runCatching { client.permissionController.getGrantedPermissions() }.getOrNull().orEmpty()
    if (!granted.containsAll(HEALTH_PERMISSIONS)) return HealthReading.Unavailable

    val range = TimeRangeFilter.between(from, to)
    val self = context.packageName

    return runCatching {
        val steps = client.readRecords(ReadRecordsRequest(StepsRecord::class, range)).records
            .map { OriginedValue(it.metadata.dataOrigin.packageName, it.count.toDouble()) }
        val distance = client.readRecords(ReadRecordsRequest(DistanceRecord::class, range)).records
            .map { OriginedValue(it.metadata.dataOrigin.packageName, it.distance.inMeters) }
        val sleep = client.readRecords(ReadRecordsRequest(SleepSessionRecord::class, range)).records
            .map {
                OriginedValue(
                    it.metadata.dataOrigin.packageName,
                    (it.endTime.toEpochMilli() - it.startTime.toEpochMilli()) / 60_000.0,
                )
            }

        val (stepCount, stepOrigins) = trusted(steps, self)
        val (metres, distanceOrigins) = trusted(distance, self)
        val (minutes, sleepOrigins) = trusted(sleep, self)

        HealthReading(
            steps = stepCount.toLong(),
            distanceMetres = metres,
            sleepMinutes = minutes.toLong(),
            origins = stepOrigins + distanceOrigins + sleepOrigins,
        )
    }.getOrElse {
        // A read that throws is not a zero day. Treating a failure as "you walked nothing" would penalise
        // somebody for a Health Connect outage.
        HealthReading.Unavailable
    }
}

/** Whether all three read permissions are held. Suspending, because Health Connect's grant set is an IPC. */
suspend fun hasHealthGrants(context: Context): Boolean {
    if (healthAvailability(context) != HealthAvailability.AVAILABLE) return false
    val client = runCatching { HealthConnectClient.getOrCreate(context) }.getOrNull() ?: return false
    return runCatching {
        client.permissionController.getGrantedPermissions().containsAll(HEALTH_PERMISSIONS)
    }.getOrDefault(false)
}
