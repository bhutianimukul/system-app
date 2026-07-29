package app.gakseong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.em
import app.gakseong.R
import app.gakseong.ui.Art
import app.gakseong.ui.Bg
import app.gakseong.ui.Bg2
import app.gakseong.ui.Body
import app.gakseong.ui.BottomNav
import app.gakseong.ui.Card
import app.gakseong.ui.Eye
import app.gakseong.ui.Filler
import app.gakseong.ui.Gap
import app.gakseong.ui.Grain
import app.gakseong.ui.Screen
import app.gakseong.ui.Shade
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.theme.Bad
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Ok

/** `data-s="raidhub"` — Raid takes the centre nav tab, so this is the screen that has to sell co-op. */
@Composable
fun RaidHubScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current

    Screen {
        Bg()
        Bg2()
        Art(R.drawable.sc_hero, top = -0.12f, left = -0.04f, width = 1.06f, alpha = 0.44f)
        Shade(0f to 0.42f, 0.08f to 0f, 0.26f to 0.65f, 0.40f to 1f)
        Grain()
        TopFade()

        Body(navSpace = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Raid")
                Filler()
                Tag("2 this week")
            }
            Gap(16)
            Text("Hold the line\nwith someone.", style = t.display(24))
            Gap(9.6)
            Tag("One objective, drawn at random, both of you get the same")

            Gap(16)
            Row(
                Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(m.d(6.4)),
            ) {
                Card(Modifier.weight(1f).fillMaxHeight(), big = true, padding = 11.2) {
                    Text("⚔", style = t.md.copy(fontSize = m.s(17.6)))
                    Gap(6.4)
                    Text("Summon a person", style = t.questTitle.copy(fontSize = m.s(13.8)))
                    Gap(3.2)
                    Tag("Share a link · they join", t.key)
                    Gap(8)
                    Text("+450 each", style = t.questSub.copy(color = p.soft, fontSize = m.s(9.9)))
                }
                Card(Modifier.weight(1f).fillMaxHeight(), dashed = true, padding = 11.2) {
                    Text("◇", style = t.md.copy(fontSize = m.s(17.6), color = p.soft))
                    Gap(6.4)
                    Text("Raid a shadow", style = t.questTitle.copy(fontSize = m.s(13.8)))
                    Gap(3.2)
                    Tag("Available now · never breaks", t.key)
                    Gap(8)
                    Text("+180", style = t.questSub.copy(color = p.soft, fontSize = m.s(9.9)))
                }
            }

            Gap(8)
            Card(Modifier.fillMaxWidth(), padding = 12.8) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Tag("Party raid · your guild", t.tag.copy(color = p.hot))
                        Gap(1.9)
                        Tag("4 of 6 holding · ends 21:00")
                    }
                    Text("→", style = t.tag.copy(color = p.soft))
                }
            }

            Gap(16)
            Tag("Last three", t.tag.copy(letterSpacing = 0.20.em))
            Gap(8)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(5.4)),
            ) {
                HistoryRow("✓ held", Ok, "Zero phone use · with Rahul", "+450", p.dim)
                HistoryRow("✓ held", Ok, "Meditate together · shadow", "+180", p.dim)
                HistoryRow("✕ broke", Bad, "No browser · you opened Chrome", "−0 base", Bad)
                Gap(11.2)
            }
        }

        BottomNav(active = 2)
    }
}

@Composable
private fun HistoryRow(state: String, stateColor: Color, label: String, value: String, valueColor: Color) {
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), padding = 10.4) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
            Tag(state, t.tag.copy(color = stateColor))
            Text(label, style = t.questTitle.copy(fontSize = m.s(12.5)))
            Filler()
            Text(value, style = t.questSub.copy(color = valueColor, fontSize = m.s(9.6)))
        }
    }
}
