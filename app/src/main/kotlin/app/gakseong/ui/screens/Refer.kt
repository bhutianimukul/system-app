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
import androidx.compose.ui.unit.em
import app.gakseong.R
import app.gakseong.ui.*
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Ok

/**
 * `data-s="refer"` — one rewarded summon a week, paid when the invitee clears day three. Paying on install
 * rewards installing the app twice, which is the whole reason the gate is where it is.
 */
@Composable
fun ReferScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current

    Screen {
        Bg()
        Bg2()
        Art(R.drawable.sc_portal, top = -0.03f, left = 0.16f, width = 0.68f, alpha = 0.42f, feather = true)
        Shade(0f to 0.42f, 0.10f to 0f, 0.26f to 0.59f, 0.40f to 1f)
        Grain()
        TopFade()

        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Summon"); Filler(); Pill("1 of 1 this week", on = true)
            }
            Gap(16)
            Text("Bring someone\ninto the dungeon.", style = t.display(24.8))
            Gap(9.6)
            Tag("One rewarded summon a week · resets Monday")

            Gap(16)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(8)),
            ) {
                SystemWindow {
                    Tag("What you get", t.tag.copy(color = p.hot))
                    Gap(8.8)
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(m.d(6.4))) {
                        XlNumber("+600", designPx = 28.8)
                        Column {
                            Tag("aura", t.tag.copy(color = p.hot, letterSpacing = 0.16.em))
                            Gap(1.6)
                            Tag("counts like any other aura")
                        }
                    }
                    Gap(8.8)
                    Text(
                        buildAnnotatedString {
                            append("Paid into today's aura, under the same daily cap as everything else. ")
                            withStyle(SpanStyle(color = p.ink, fontWeight = FontWeight(650))) {
                                append("Hit the cap already and it rolls into Level")
                            }
                            append(", so a good week of summons cannot outrun a good week of showing up.")
                        },
                        style = t.body.copy(fontSize = m.s(11.8)),
                    )
                }

                Card(Modifier.fillMaxWidth(), padding = 13.6) {
                    Tag("Paid when they last three days", t.tag.copy(color = p.hot))
                    Gap(8.8)
                    Column(verticalArrangement = Arrangement.spacedBy(m.d(8))) {
                        Milestone("They install", "nothing yet", false)
                        Milestone("They clear day 1", "nothing yet", false)
                        Milestone("They clear day 3", "+600 aura", true)
                    }
                    Gap(8.8)
                    Text(
                        "Paying on install rewards installing the app twice. Paying on day 3 rewards bringing " +
                            "someone who stays.",
                        style = t.body.copy(fontSize = m.s(11.7)),
                    )
                }

                Card(Modifier.fillMaxWidth(), big = true, padding = 14.4) {
                    Tag("Your link", t.tag.copy(color = p.hot))
                    Gap(5.6)
                    Text("gakseong.app/s/M9K-4TQ2", style = t.questSub.copy(color = p.ink, fontSize = m.s(10.9)))
                    Gap(9.6)
                    Row(horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                        Pill("Share", on = true); Pill("Copy"); Pill("QR")
                    }
                }

                Card(Modifier.fillMaxWidth(), dashed = true, padding = 12.8) {
                    Tag("How the System knows", t.tag.copy(color = p.hot))
                    Gap(4.2)
                    Text(
                        buildAnnotatedString {
                            append("Play tells the app which link installed it, once. That pairs the two of you, ")
                            append("write-once, and cannot be edited afterwards. ")
                            withStyle(SpanStyle(color = p.ink, fontWeight = FontWeight(650))) {
                                append("Three days of cleared thresholds inside two weeks releases the aura")
                            }
                            append(", and they do not have to be consecutive.")
                        },
                        style = t.body.copy(fontSize = m.s(11.7)),
                    )
                }

                Tag("Same device or a reinstall pays nothing · one credit per person, ever")
                Gap(17.6)
            }
        }
    }
}

@Composable
private fun Milestone(label: String, state: String, paid: Boolean) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = t.body.copy(fontSize = m.s(12.6), color = if (paid) p.ink else p.dim))
        Filler()
        Tag(state, t.tag.copy(color = if (paid) Ok else p.faint))
    }
}
