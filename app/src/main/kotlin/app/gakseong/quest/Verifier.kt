package app.gakseong.quest

import app.gakseong.sense.HealthReading
import app.gakseong.sense.UsageReading
import gakseong.engine.Provability

// The closed verifier set. Build these once and the quest bank writes itself: the same code that issues
// "don't touch your phone for 90 minutes" issues a 72-hour Gate.
//
// Pure Kotlin. `Readings` is handed in already gathered, so the whole quest-to-aura path is testable without a
// device, and the number a user reads on a card is the number that clears the quest rather than a second one.

/** Everything the verifiers can see, gathered once per settle. */
data class Readings(
    val usage: UsageReading = UsageReading.Unavailable,
    val health: HealthReading = HealthReading.Unavailable,
    /** Minutes with the in-app reader foregrounded and scroll advancing. §24. */
    val readerMinutes: Int = 0,
    /** Minutes held in a focus session. Owned by the phase-06 service, never by the template. */
    val focusMinutes: Int = 0,
    /**
     * The longest unbroken screen-off block **inside the night gate window**, and whether that window has
     * closed yet.
     *
     * Separate from [usage] because the gate is measured over its own window, which crosses midnight and so is
     * not the day the rest of the readings cover. Null means the window is still open: §Verifiers checks the
     * gate once after it closes, because a query at 02:00 can only ever say "so far".
     */
    val nightGateMinutes: Int? = null,
    /**
     * The yes/no answers, keyed by quest id. A missing key means never answered, which is not a yes.
     *
     * Keyed rather than a single flag because a day can draw two declared quests, and one answer clearing both
     * would let "a meal with people" quietly settle "thirty minutes with your parents".
     */
    val declared: Map<String, Boolean> = emptyMap(),
    /** Seconds of call time. Duration only, never who. */
    val callSeconds: Int = 0,
    val locationCheckedIn: Boolean = false,
)

/**
 * How a quest stands right now.
 *
 * [available] false means the verifier could not be read at all, which is why the quest is never drawn rather
 * than being failed. A refused Health Connect grant is not a missed step target.
 */
data class Progress(
    val cleared: Boolean,
    val current: Long,
    val target: Long,
    val available: Boolean,
    val label: String,
)

/**
 * The ten. Nothing outside this set can be a quest, which is what keeps every quest tierable and the
 * leaderboard meaningful. §7: free-text quests cannot be tiered, and an untierable quest kills the ladder.
 *
 * [provability] lives on the verifier and never on the template, so a declared quest cannot be handed a sensor
 * rate by a bank entry. The rule is in the type rather than in a review.
 */
sealed class Verifier {
    abstract val provability: Provability
    abstract fun evaluate(r: Readings): Progress

    /** Longest unbroken phone-free block. The night gate is this across a configurable window. */
    data class ScreenOffBlock(val minMinutes: Int) : Verifier() {
        override val provability = Provability.SENSOR
        override fun evaluate(r: Readings): Progress {
            val minutes = r.usage.longestScreenOffMs / 60_000
            return Progress(
                cleared = r.usage.available && minutes >= minMinutes,
                current = minutes, target = minMinutes.toLong(), available = r.usage.available,
                label = if (!r.usage.available) "Usage access needed" else "$minutes of $minMinutes min",
            )
        }
    }

    /** Named packages unopened for a window. Cleared while untouched, judged when the window closes. */
    data class AppAbsent(val packages: Set<String>, val windowMinutes: Int) : Verifier() {
        override val provability = Provability.SENSOR
        override fun evaluate(r: Readings): Progress {
            val touched = r.usage.packagesOpened.count { it in packages }
            return Progress(
                cleared = r.usage.available && touched == 0,
                current = touched.toLong(), target = 0L, available = r.usage.available,
                label = when {
                    !r.usage.available -> "Usage access needed"
                    touched == 0 -> "Untouched"
                    else -> "Opened $touched"
                },
            )
        }
    }

    /** Per-day or per-window screen budget. A ceiling, so it is cleared until it is broken. */
    data class TotalScreenTime(val maxMinutes: Int) : Verifier() {
        override val provability = Provability.SENSOR
        override fun evaluate(r: Readings): Progress {
            val minutes = r.usage.totalForegroundMs / 60_000
            return Progress(
                cleared = r.usage.available && minutes <= maxMinutes,
                current = minutes, target = maxMinutes.toLong(), available = r.usage.available,
                label = if (!r.usage.available) "Usage access needed" else "$minutes min used",
            )
        }
    }

    /** Per-package cap. Same shape as the total, scoped to one app. */
    data class AppBudget(val packageName: String, val maxMinutes: Int) : Verifier() {
        override val provability = Provability.SENSOR
        override fun evaluate(r: Readings): Progress {
            val minutes = (r.usage.perPackageMs[packageName] ?: 0L) / 60_000
            return Progress(
                cleared = r.usage.available && minutes <= maxMinutes,
                current = minutes, target = maxMinutes.toLong(), available = r.usage.available,
                label = if (!r.usage.available) "Usage access needed" else "$minutes of $maxMinutes min",
            )
        }
    }

    data class Steps(val target: Int) : Verifier() {
        override val provability = Provability.SENSOR
        override fun evaluate(r: Readings) = Progress(
            cleared = r.health.available && r.health.steps >= target,
            current = r.health.steps, target = target.toLong(), available = r.health.available,
            label = if (!r.health.available) "Health Connect needed" else "${r.health.steps} of $target",
        )
    }

    data class Distance(val targetMetres: Int) : Verifier() {
        override val provability = Provability.SENSOR
        override fun evaluate(r: Readings): Progress {
            val metres = r.health.distanceMetres.toLong()
            return Progress(
                cleared = r.health.available && metres >= targetMetres,
                current = metres, target = targetMetres.toLong(), available = r.health.available,
                label = if (!r.health.available) "Health Connect needed"
                else "%.2f of %.1f km".format(metres / 1000.0, targetMetres / 1000.0),
            )
        }
    }

    /** One query at 06:00, zero battery cost. */
    data class Sleep(val minMinutes: Int) : Verifier() {
        override val provability = Provability.SENSOR
        override fun evaluate(r: Readings) = Progress(
            cleared = r.health.available && r.health.sleepMinutes >= minMinutes,
            current = r.health.sleepMinutes, target = minMinutes.toLong(), available = r.health.available,
            label = if (!r.health.available) "Health Connect needed"
            else "${r.health.sleepMinutes / 60}h ${r.health.sleepMinutes % 60}m",
        )
    }

    /** Foreground location, one tap on arrival. Passive geofencing needs a restricted permission. */
    data object LocationCheckin : Verifier() {
        override val provability = Provability.SENSOR
        override fun evaluate(r: Readings) = Progress(
            cleared = r.locationCheckedIn, current = if (r.locationCheckedIn) 1L else 0L, target = 1L,
            available = true, label = if (r.locationCheckedIn) "Checked in" else "Tap on arrival",
        )
    }

    /** Duration only, never who. READ_PHONE_STATE rather than READ_CALL_LOG. */
    data class CallDuration(val minMinutes: Int) : Verifier() {
        override val provability = Provability.SENSOR
        override fun evaluate(r: Readings): Progress {
            val minutes = (r.callSeconds / 60).toLong()
            return Progress(
                cleared = minutes >= minMinutes, current = minutes, target = minMinutes.toLong(),
                available = true, label = "$minutes of $minMinutes min",
            )
        }
    }

    /**
     * User confirmation, low ceiling, one yes/no at expiry.
     *
     * A quest never answered expires unclaimed. Defaulting an unanswered question to cleared would make
     * silence the cheapest way to farm, which is exactly what the low ceiling exists to prevent.
     */
    data class Declared(val questId: String) : Verifier() {
        override val provability = Provability.DECLARED
        override fun evaluate(r: Readings): Progress {
            val answer = r.declared[questId]
            return Progress(
                cleared = answer == true, current = if (answer == true) 1L else 0L, target = 1L,
                available = true,
                label = when (answer) {
                    true -> "Confirmed"
                    false -> "Not done"
                    null -> "Answer when it expires"
                },
            )
        }
    }

    /**
     * The night gate. A [ScreenOffBlock] over its own window rather than over the day.
     *
     * Pending while the window is open, which is deliberate. A gate that reported halfway through would teach
     * the user that leaving it early is free, and the whole point is that it is checked once, after.
     */
    data class NightGate(val minMinutes: Int) : Verifier() {
        override val provability = Provability.SENSOR
        override fun evaluate(r: Readings): Progress {
            val held = r.nightGateMinutes
            return Progress(
                cleared = held != null && held >= minMinutes,
                current = (held ?: 0).toLong(),
                target = minMinutes.toLong(),
                available = r.usage.available,
                label = when {
                    !r.usage.available -> "Usage access needed"
                    held == null -> "Pending · tonight"
                    held >= minMinutes -> "Held"
                    else -> "Broken after ${held / 60}h ${held % 60}m"
                },
            )
        }
    }

    /**
     * The in-app reader foregrounded with scroll advancing. App-initiated, never sensor-proven, because the app
     * is marking its own homework.
     */
    data class ReadSession(val minMinutes: Int) : Verifier() {
        override val provability = Provability.APP_INITIATED
        override fun evaluate(r: Readings) = Progress(
            cleared = r.readerMinutes >= minMinutes, current = r.readerMinutes.toLong(),
            target = minMinutes.toLong(), available = true,
            label = "${r.readerMinutes} of $minMinutes min",
        )
    }

    /**
     * A focus session is app-initiated for the same reason the reader is: the app times it.
     *
     * The minutes held live in [Readings], not on the verifier. A bank template is a description of the quest
     * and cannot also carry today's progress, or every focus quest is permanently unstarted.
     */
    data class FocusSession(val minMinutes: Int) : Verifier() {
        override val provability = Provability.APP_INITIATED
        override fun evaluate(r: Readings) = Progress(
            cleared = r.focusMinutes >= minMinutes, current = r.focusMinutes.toLong(),
            target = minMinutes.toLong(), available = true,
            label = if (r.focusMinutes == 0) "Not started" else "${r.focusMinutes} of $minMinutes min",
        )
    }
}
