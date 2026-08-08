package app.gakseong.sense

// The pure half of Health Connect reading: what makes a record trustworthy enough to pay the sensor rate.
// `sense/Health.kt` is the Android half.

/** One record reduced to the two fields provenance cares about. */
data class OriginedValue(val originPackage: String, val value: Double)

/**
 * What a Health Connect read produced, once provenance has been applied.
 *
 * [origins] is surfaced so a screen can name where the number came from. [available] is false when Health
 * Connect is absent, out of date, or ungranted; quest generation treats that as "never draw this verifier",
 * which is not the same as a zero.
 */
data class HealthReading(
    val steps: Long = 0L,
    val distanceMetres: Double = 0.0,
    val sleepMinutes: Long = 0L,
    val origins: Set<String> = emptySet(),
    val available: Boolean = true,
) {
    companion object {
        val Unavailable = HealthReading(available = false)
    }
}

/**
 * Sum [values], dropping anything this app wrote itself.
 *
 * §Verifiers: `dataOrigin` checked against real providers is what lets the sensor tier reach S-rank. Gakseong
 * never writes to Health Connect, so a record claiming to come from Gakseong is either a bug or someone
 * spoofing, and either way it cannot be allowed to prove a quest.
 *
 * ponytail: an exclusion, not an allowlist of blessed provider packages. Health Connect already carries
 * Strava, Nike, Garmin, Fitbit and Samsung Health, and a fixed allowlist would silently refuse to count a
 * legitimate app that is simply newer than this list. If pace or step gaming actually appears, the upgrade is a
 * curated allowlist plus ExerciseSessionRecord type checking, which §26 already names.
 */
fun trusted(values: List<OriginedValue>, selfPackage: String): Pair<Double, Set<String>> {
    val kept = values.filterNot { it.originPackage == selfPackage }
    return kept.sumOf { it.value } to kept.map { it.originPackage }.toSet()
}
