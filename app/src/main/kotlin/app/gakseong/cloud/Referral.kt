package app.gakseong.cloud

// The referral release condition, pure. §Referral tracking.
//
// `cloud/Firebase.kt` does the Play Install Referrer read and the Firestore write; everything decidable without
// a network lives here so it can be tested.

/** §Social: one rewarded summon per week, +600 aura, paid when the invitee clears day 3. */
const val REFERRAL_AURA = 600

/** §Referral: three cleared days inside a fourteen-day window. */
const val DAYS_REQUIRED = 3
const val WINDOW_DAYS = 14

/** §Social: one per week, which is what caps the upside for anyone farming it. */
const val REWARDED_PER_WEEK = 1

/**
 * Whether an invitee has earned their inviter the reward.
 *
 * [clearedDates] are ISO dates on which the invitee cleared their daily threshold, and [installedOn] is the day
 * the pairing was created.
 *
 * **Not necessarily consecutive.** Consecutive would punish the inviter for the invitee's bad Tuesday, and the
 * point of the gate is that the fake account has to defeat the same UsageStats and Health Connect checks as
 * real play three separate times.
 */
fun released(clearedDates: Set<String>, installedOn: String): Boolean =
    clearedDates.count { it >= installedOn && it < installedOn.plusDaysIso(WINDOW_DAYS) } >= DAYS_REQUIRED

/**
 * ISO date arithmetic without `java.time`, so this file stays testable as pure Kotlin and free of any Android
 * or JVM date parsing that a caller might have configured differently.
 *
 * ponytail: string arithmetic on `yyyy-MM-dd` only. It is used for one fourteen-day window and nothing else. If
 * this ever needs months or timezones, it needs `java.time` instead and this comment is the signal.
 */
internal fun String.plusDaysIso(days: Int): String {
    val (y, m, d) = split("-").map { it.toInt() }
    var year = y
    var month = m
    var day = d + days
    while (day > daysIn(month, year)) {
        day -= daysIn(month, year)
        month++
        if (month > 12) { month = 1; year++ }
    }
    return "%04d-%02d-%02d".format(year, month, day)
}

private fun daysIn(month: Int, year: Int) = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    else -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
}

/**
 * Pull the opaque code out of a Play referrer string or an App Link.
 *
 * The link is `gakseong.app/s/<code>` and the code is derived from the inviter's Anonymous Auth UID. Not a
 * name, not an address: §Referral adds no email and no phone, because it buys almost no fraud resistance and
 * costs the entire no-account posture.
 */
fun codeFrom(referrer: String?): String? {
    if (referrer.isNullOrBlank()) return null
    val fromQuery = Regex("""(?:^|[&?])(?:gs|utm_content)=([A-Za-z0-9]{4,16})(?:&|$)""").find(referrer)
    val fromPath = Regex("""/s/([A-Za-z0-9]{4,16})""").find(referrer)
    return (fromQuery ?: fromPath)?.groupValues?.get(1)
}

/**
 * Where this is forgeable, stated plainly rather than hidden.
 *
 * Anonymous Auth UIDs rotate on reinstall, so a determined person with a spare device can farm this. The
 * three-day gate is the actual defence rather than a retention nicety: the fake account has to clear three real
 * thresholds, which costs more work than 600 capped aura is worth, and the weekly limit caps the upside at
 * roughly one per week regardless.
 */
const val KNOWN_LIMITATION =
    "Anonymous UIDs rotate on reinstall. The three-day gate is the defence, not the pairing."
