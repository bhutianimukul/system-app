package app.gakseong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import app.gakseong.ui.theme.pct
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import app.gakseong.R
import app.gakseong.ui.*
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType

/**
 * `data-s="private"` — the private track. §7 and non-negotiable 9 govern every line of this screen: biometric
 * gate on every open, nothing auto-detected except app-mediated vices, never on a leaderboard, and it emits no
 * analytics event of any kind. There is deliberately no share affordance anywhere on it.
 *
 * The seeded item names are NOT in this file. §7 keeps the shipped list out of the repo because the repo is
 * public and the list reads as a confession when it is not one. These three are the categories the decision
 * record already names in public, standing in for the real seeds.
 */
@Composable
fun PrivateScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val haptics = rememberHaptics()

    // The shipped seed labels are not in this repo, per §7. These are the categories the decision record already
    // names in public, and every one of them starts inactive.
    val tracked = remember {
        mutableStateListOf(
            Tracked("Substance", "Something you take that you would rather not", shadowAt = 90),
            Tracked("Compulsion", "Something you do that you did not decide to", shadowAt = 90),
            Tracked("Delivery apps", "The phone can see this one", autoDetected = true, shadowAt = 30),
        )
    }

    Screen {
        Bg()
        Bg2()
        Art(R.drawable.sc_tank, top = -0.06f, left = 0.08f, width = 0.84f, alpha = 0.6f)
        Shade(0f to 0.57f, 0.10f to 0f, 0.28f to 0.67f, 0.40f to 1f)
        Grain()
        TopFade()

        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Deliberately not the accent eyebrow: this track never announces itself in the class colour.
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(7.2))) {
                    Text("·", style = t.tag.copy(color = p.dim))
                    Tag("Private Track", t.tag.copy(color = p.dim))
                }
                Filler()
                Pill("☉ Unlocked", on = true)
            }
            Gap(19.2)
            Text("The things you\nfight alone", style = t.md.copy(fontSize = m.s(20.8)))
            Gap(8)
            Tag("Fingerprint or face required each time · never on a leaderboard")

            Gap(14.4)
            SystemWindow {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Tag("Check-in pending", t.tag.copy(color = p.hot)); Filler()
                    Tag("reminder 17:00 daily", t.tag.copy(color = p.soft))
                }
                Gap(4.2)
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = p.ink, fontWeight = FontWeight(650))) {
                            append("None of this is auto-detected.")
                        }
                        append(" The System cannot see any of it. You tell it, once a day, or the streak does not advance.")
                    },
                    style = t.body.copy(fontSize = m.s(12.2)),
                )
                Gap(11.2)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                    Pill("Confirm all", on = true)
                    Pill("Submit")
                }
            }

            Gap(17.6)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(7.2)),
            ) {
                // Everything starts off. §7 makes this track opt-in and skippable, and a screen that arrives with
                // items already running would have decided for the user what they are fighting.
                tracked.forEachIndexed { i, item ->
                    Streak(
                        label = item.label,
                        detail = item.detail,
                        active = item.active,
                        days = item.days,
                        shadowAt = item.shadowAt,
                        autoDetected = item.autoDetected,
                        onToggle = {
                            haptics.tick()
                            tracked[i] = item.copy(active = !item.active)
                        },
                    )
                }

                Card(
                    Modifier.fillMaxWidth().clickable { haptics.tick() },
                    dashed = true,
                    padding = 15.2,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(11.2))) {
                        Box(
                            Modifier
                                .size(m.d(32))
                                .clip(RoundedCornerShape(m.d(10)))
                                .background(p.hot.pct(0.16f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("＋", style = t.md.copy(fontSize = m.s(15.2), color = p.hot))
                        }
                        Column {
                            Text("Add something else", style = t.questTitle.copy(fontSize = m.s(13.4)))
                            Gap(2.9)
                            Tag("You name it · the System never asks what it means", t.key)
                        }
                    }
                }

                Gap(4)
                Card(Modifier.fillMaxWidth(), dashed = true, padding = 12.8) {
                    Text(
                        "Nothing here leaves the phone. No guild notification, no leaderboard entry, no flag that " +
                            "you went dark, and no analytics event of any kind.",
                        style = t.body.copy(fontSize = m.s(11.8)),
                    )
                }
                Gap(11.2)
            }
        }
    }
}

/** One thing being tracked. Off until the user turns it on, which is the whole posture of §7. */
private data class Tracked(
    val label: String,
    val detail: String,
    val active: Boolean = false,
    val days: Int = 0,
    val shadowAt: Int = 30,
    val autoDetected: Boolean = false,
)

@Composable
private fun Streak(
    label: String,
    detail: String,
    active: Boolean,
    days: Int,
    shadowAt: Int,
    autoDetected: Boolean,
    onToggle: () -> Unit,
) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onToggle),
        big = true,
        lit = active,
        dashed = !active,
        padding = 15.2,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        label,
                        style = t.questTitle.copy(
                            fontSize = m.s(14.1),
                            color = if (active) p.ink else p.dim,
                        ),
                    )
                    if (autoDetected) {
                        GapW(6)
                        Tag("app-mediated", t.key.copy(color = p.faint))
                    }
                }
                Gap(2.9)
                Tag(detail, t.key)
            }
            if (active) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$days", style = t.lg(22.4).copy(color = p.soft))
                    Tag(" d", t.tag)
                }
            } else {
                Tag("tap to begin", t.key.copy(color = p.hot))
            }
        }
        if (active) {
            Gap(6)
            Meter(fill = days.toFloat() / shadowAt, height = 7)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Tag("Shadow at $shadowAt", t.key)
                Tag("+40 / day", t.key)
            }
        }
    }
}
