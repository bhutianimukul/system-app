package app.gakseong.session

// The night gate, as arithmetic. §Verifiers: SCREEN_OFF_BLOCK across a configurable 00:30–06:00 window, checked
// once after it closes.
//
// Pure, because the only hard part is a window that crosses midnight and the only way to be sure about that is
// to enumerate the cases.

/** Minutes from midnight, from an `HH:mm` string. Returns null rather than guessing at anything malformed. */
fun minutesOfDay(hhmm: String): Int? {
    val parts = hhmm.split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}

/**
 * How long the gate is, in minutes.
 *
 * The window crosses midnight by default, which is the whole reason this is a function. 00:30 to 06:00 is 330
 * minutes; naively subtracting gives 330 only because both are after midnight, and 23:00 to 06:00 would give
 * a negative number that a threshold check would read as instantly satisfied.
 */
fun gateMinutes(start: String, end: String): Int? {
    val s = minutesOfDay(start) ?: return null
    val e = minutesOfDay(end) ?: return null
    // Equal times are not a zero-length window under the crossing branch, they are a twenty-four hour one:
    // (1440 - 360) + 360 is 1440. A day-long night gate is not a night gate, so it is refused here rather than
    // issued as a quest nobody can clear.
    if (s == e) return null
    return if (e > s) e - s else (24 * 60 - s) + e
}

/**
 * Whether [nowMinutes] falls inside the window.
 *
 * Used to decide whether the gate is open rather than whether it was held: §Verifiers checks the gate once
 * **after** it closes, because a query at 02:00 can only ever say "so far", and a night gate that reports
 * halfway through teaches the user that leaving it early is free.
 */
fun insideGate(nowMinutes: Int, start: String, end: String): Boolean {
    val s = minutesOfDay(start) ?: return false
    val e = minutesOfDay(end) ?: return false
    return if (e > s) nowMinutes in s until e else nowMinutes >= s || nowMinutes < e
}

/**
 * Whether the gate has closed for the night that [nowMinutes] belongs to.
 *
 * True from the moment the window ends until it opens again. The settle that reads the night's screen-off block
 * runs in this span and nowhere else.
 */
fun gateClosed(nowMinutes: Int, start: String, end: String): Boolean = !insideGate(nowMinutes, start, end)

/**
 * The absolute window to query, given the day boundary the app is settling.
 *
 * Returned as an offset pair from the start of [dayStartMs], so the caller does no date arithmetic. A window
 * that crosses midnight starts on the previous day, which is exactly the case a naive `startOfDay + 00:30`
 * would get wrong by twenty-three and a half hours.
 */
fun gateWindow(dayStartMs: Long, start: String, end: String): LongRange? {
    val s = minutesOfDay(start) ?: return null
    val e = minutesOfDay(end) ?: return null
    val minute = 60_000L
    return if (e > s) {
        // Wholly inside one day: 00:30 to 06:00.
        (dayStartMs + s * minute)..(dayStartMs + e * minute)
    } else {
        // Crosses midnight: 23:00 today to 06:00 tomorrow, which for a settle means yesterday into today.
        (dayStartMs - (24 * 60 - s) * minute)..(dayStartMs + e * minute)
    }
}
