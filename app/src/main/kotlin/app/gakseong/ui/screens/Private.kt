package app.gakseong.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
                Streak("Substance", 41, shadowAt = 90, fill = 0.46f)
                Streak("Compulsion", 12, shadowAt = 30, fill = 0.40f)
                Streak("Delivery apps", 6, shadowAt = 30, fill = 0.20f, autoDetected = true)
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

@Composable
private fun Streak(label: String, days: Int, shadowAt: Int, fill: Float, autoDetected: Boolean = false) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), big = true, padding = 15.2) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = t.questTitle.copy(fontSize = m.s(14.1)))
            if (autoDetected) {
                GapW(6)
                Tag("app-mediated", t.key.copy(color = p.faint))
            }
            Filler()
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$days", style = t.lg(22.4).copy(color = p.soft))
                Tag(" d", t.tag)
            }
        }
        Gap(6)
        Meter(fill = fill, height = 7)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Tag("Shadow at $shadowAt", t.key)
            Tag("+40 / day", t.key)
        }
    }
}
