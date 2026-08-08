package app.gakseong.session

import kotlinx.serialization.Serializable

// The pure half of a focus session: the clock, the grace window and the break rule. No Android, so the timing
// edges are testable without holding a phone for forty-five minutes.
//
// `session/FocusService.kt` is the other half: a foreground service that ticks this and posts the notification.

/**
 * ~10 seconds away before a session breaks.
 *
 * A mis-tap must not cost the session. Someone who swipes up by accident and comes straight back has not broken
 * anything, and a session that punishes that teaches people to be afraid of their own phone rather than
 * deliberate with it.
 */
const val RETURN_GRACE_MS = 10_000L

/**
 * Packages that may be opened mid-session without breaking it.
 *
 * The dialer is here for the same reason DND lets calls through: the audience includes fifteen-year-olds, and
 * an app that stands between somebody and a phone call for forty-five minutes is indefensible.
 */
val ALWAYS_ALLOWED = setOf(
    "com.android.dialer",
    "com.google.android.dialer",
    "com.samsung.android.dialer",
    "com.android.server.telecom",
    "com.android.incallui",
)

@Serializable
data class FocusState(
    val startedAtMs: Long,
    val lengthMinutes: Int,
    /** When the user left for a package that is not allowed. Null while they are here. */
    val awaySinceMs: Long? = null,
    val broken: Boolean = false,
    /** Milliseconds spent away inside the grace window, which do not count as held time. */
    val awayMs: Long = 0L,
)

sealed interface Status {
    data class Running(val heldMs: Long, val remainingMs: Long) : Status
    /** Away, but still inside the grace window. The screen shows the countdown rather than a failure. */
    data class Grace(val graceLeftMs: Long, val remainingMs: Long) : Status
    data class Complete(val heldMs: Long) : Status
    data object Broken : Status
}

/**
 * Advance a session to [nowMs], given which package is in front.
 *
 * [foreground] null means this app is in front. A package in [ALWAYS_ALLOWED] is treated the same way, so a
 * call neither breaks the session nor pauses the clock.
 */
fun step(state: FocusState, nowMs: Long, foreground: String?): FocusState {
    if (state.broken) return state

    val away = foreground != null && foreground !in ALWAYS_ALLOWED
    return when {
        away && state.awaySinceMs == null -> state.copy(awaySinceMs = nowMs)

        away -> {
            val gone = nowMs - state.awaySinceMs!!
            if (gone > RETURN_GRACE_MS) state.copy(broken = true) else state
        }

        // Back inside the grace window. The time away is banked as not-held rather than forgiven, so leaving
        // repeatedly for nine seconds cannot buy a shorter session.
        state.awaySinceMs != null ->
            state.copy(awaySinceMs = null, awayMs = state.awayMs + (nowMs - state.awaySinceMs))

        else -> state
    }
}

fun status(state: FocusState, nowMs: Long): Status {
    if (state.broken) return Status.Broken

    val target = state.lengthMinutes * 60_000L
    val pendingAway = state.awaySinceMs?.let { nowMs - it } ?: 0L
    val held = (nowMs - state.startedAtMs - state.awayMs - pendingAway).coerceAtLeast(0L)

    return when {
        held >= target -> Status.Complete(held)
        state.awaySinceMs != null -> Status.Grace(
            graceLeftMs = (RETURN_GRACE_MS - pendingAway).coerceAtLeast(0L),
            remainingMs = target - held,
        )
        else -> Status.Running(heldMs = held, remainingMs = target - held)
    }
}

/** Whole minutes held, which is what the `FocusSession` verifier reads. */
fun heldMinutes(state: FocusState, nowMs: Long): Int = when (val s = status(state, nowMs)) {
    is Status.Complete -> (s.heldMs / 60_000).toInt()
    is Status.Running -> (s.heldMs / 60_000).toInt()
    is Status.Grace -> ((state.lengthMinutes * 60_000L - s.remainingMs) / 60_000).toInt()
    Status.Broken -> 0
}
