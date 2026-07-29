package app.gakseong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import app.gakseong.R
import app.gakseong.ui.Art
import app.gakseong.ui.Aura
import app.gakseong.ui.Bg
import app.gakseong.ui.Bg2
import app.gakseong.ui.Body
import app.gakseong.ui.Card
import app.gakseong.ui.Cta
import app.gakseong.ui.Eye
import app.gakseong.ui.Filler
import app.gakseong.ui.Gap
import app.gakseong.ui.Grain
import app.gakseong.ui.Pill
import app.gakseong.ui.Screen
import app.gakseong.ui.Shade
import app.gakseong.ui.SystemWindow
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.XlNumber
import app.gakseong.ui.theme.LocalHunterClass
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Ok

/**
 * `data-s="runraid"` — DECISIONS.md §26. The window is committed; the place never is. The screen has to say that
 * out loud, because every other raid in the app implies being in the same session at the same moment.
 */
@Composable
fun RunRaidScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val hunter = LocalHunterClass.current

    Screen {
        Bg()
        Bg2()
        Aura(widthFraction = 0.80f, heightFraction = 0.20f, top = 0.14f, left = 0.10f, alpha = 0.40f)
        Art(R.drawable.sc_hero, top = -0.13f, left = -0.04f, width = 1.06f, alpha = 0.5f)
        Shade(0f to 0.48f, 0.10f to 0f, 0.28f to 0.65f, 0.42f to 1f)
        Grain()
        TopFade()

        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Raid · Running")
                Filler()
                Tag("objective 12 of 18")
            }
            Gap(16)
            Text("Same hour.\nDifferent roads.", style = t.display(24))
            Gap(9.6)
            Tag("Neither of you ever learns where the other ran")

            Gap(16)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(m.d(6.4))) {
                Card(Modifier.weight(1f), big = true) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Tag("Window")
                        Gap(3.8)
                        Text("18:00 — 19:00", style = t.md.copy(fontSize = m.s(16), color = p.soft))
                        Gap(3.2)
                        Tag("committed", t.key)
                    }
                }
                Card(Modifier.weight(1f), big = true) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Tag("Target")
                        Gap(3.8)
                        Text("5.0 km", style = t.md.copy(fontSize = m.s(16), color = p.soft))
                        Gap(3.2)
                        Tag("scales with rank", t.key)
                    }
                }
            }

            Gap(8)
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                PartnerCard(hunter.portrait, "You", "◷ not started", p.soft, Modifier.weight(1f).fillMaxHeight())
                Text("⚔", style = t.tag.copy(color = p.hot, fontSize = m.s(14.4)))
                Card(Modifier.weight(1f).fillMaxHeight(), big = true) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("◇", style = t.md.copy(fontSize = m.s(17.6), color = p.soft))
                        Gap(8)
                        Text("pacer", style = t.listItem.copy(textAlign = TextAlign.Center))
                        Gap(3.2)
                        Tag("6:00/km · never breaks", t.key)
                    }
                }
            }

            Gap(8)
            SystemWindow {
                Text(
                    "Start it where you always start it. The System reads the result, not the run.",
                    style = t.body.copy(fontSize = m.s(12.8)),
                )
            }

            Gap(8)
            Card(Modifier.fillMaxWidth(), big = true) {
                Tag("Not a focus session", t.tag.copy(color = p.hot))
                Gap(4.8)
                Text(
                    "Leave the app. No speed bump, no grace timer, no penalty. Time in Strava during this window " +
                        "is not screen time, or a 10 km evening would eat your daily budget.",
                    style = t.body.copy(fontSize = m.s(12.5)),
                )
            }

            Filler()
            Cta("Start in Strava")
            Gap(8.8)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                Pill("Nike")
                Pill("Samsung Health")
                Pill("Garmin")
            }
            Gap(8)
            Tag("Launch intent only · Health Connect delivers the distance")
            Gap(17.6)
        }
    }
}

/**
 * `data-s="runsettle"` — the retroactive verdict. Health Connect receives provider writes on sync, so the screen
 * waits rather than declare a failure it would have to take back.
 */
@Composable
fun RunSettleScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val hunter = LocalHunterClass.current

    Screen {
        Bg()
        Bg2()
        Aura(widthFraction = 0.88f, heightFraction = 0.26f, top = 0.20f, left = 0.06f, alpha = 0.44f)
        Art(R.drawable.sc_ruins, top = -0.11f, left = -0.04f, width = 1.06f, alpha = 0.58f)
        Shade(0f to 0.57f, 0.12f to 0f, 0.30f to 0f, 0.46f to 0.88f, 0.58f to 1f)
        Grain()
        TopFade()

        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("Raid · Settling")
                Filler()
                XlNumber("5.02 km", designPx = 43.2)
                Gap(7.2)
                Tag("of 5.0 km · via Strava")

                Gap(19.2)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    PartnerCard(hunter.portrait, "You", "✓ cleared", Ok, Modifier.weight(1f))
                    Text("⚔", style = t.tag.copy(color = p.hot, fontSize = m.s(14.4)))
                    PartnerCard(R.drawable.p_ranger, "Rahul", "◷ settling", p.soft, Modifier.weight(1f), alpha = 0.85f)
                }

                Gap(14.4)
                SystemWindow {
                    Text(
                        "His phone has not told mine yet. I would rather wait than call it wrong.",
                        style = t.body.copy(fontSize = m.s(12.8)),
                    )
                }

                Gap(8)
                Card(Modifier.fillMaxWidth(), big = true) {
                    Tag("Why this takes a moment", t.tag.copy(color = p.hot))
                    Gap(4.8)
                    Text(
                        "Strava, Nike and Samsung Health write to Health Connect on sync, minutes after a run " +
                            "ends. Window closed 19:00 · re-checked every 15 minutes for 30. A verdict is never " +
                            "reversed once given.",
                        style = t.body.copy(fontSize = m.s(12.5)),
                    )
                }

                Gap(8)
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Tag("Raid bonus")
                            Gap(2.6)
                            Tag("sensor-proven · base was never at risk", t.tag.copy(color = p.faint))
                        }
                        Text("+450", style = t.md.copy(fontSize = m.s(15.2), color = p.soft))
                    }
                }
                Gap(17.6)
            }
        }
    }
}
