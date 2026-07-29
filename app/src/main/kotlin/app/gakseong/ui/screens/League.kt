package app.gakseong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.em
import app.gakseong.R
import app.gakseong.ui.Art
import app.gakseong.ui.Aura
import app.gakseong.ui.Bg
import app.gakseong.ui.Bg2
import app.gakseong.ui.Body
import app.gakseong.ui.BottomNav
import app.gakseong.ui.Card
import app.gakseong.ui.Eye
import app.gakseong.ui.Filler
import app.gakseong.ui.Gap
import app.gakseong.ui.Grain
import app.gakseong.ui.Mask
import app.gakseong.ui.MaskedImage
import app.gakseong.ui.Screen
import app.gakseong.ui.Shade
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.theme.Bad
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Ok

/**
 * `data-s="league"` — a weekly division of about thirty.
 *
 * The rows come from §9's rules rather than from the design page's row markup, which is generated in JS I could
 * not locate: thin divisions are padded with System-run shadows carrying `◇` and the label `pacer`, they are
 * stated on screen not to be people, and they are never counted as members. No fabricated human usernames.
 */
@Composable
fun LeagueScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current

    Screen {
        Bg()
        Bg2()
        Aura(widthFraction = 0.80f, heightFraction = 0.20f, top = 0.02f, left = 0.10f, alpha = 0.42f)
        Art(R.drawable.sc_ruins, top = -0.11f, left = -0.04f, width = 1.06f, alpha = 0.58f)
        Shade(0f to 0.52f, 0.08f to 0f, 0.22f to 0.77f, 0.32f to 1f)
        Grain()
        TopFade()

        Body(navSpace = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Weekly League")
                Filler()
                Tag("2d 04h")
            }

            Gap(19.2)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(11.2))) {
                MaskedImage(R.drawable.em_d, width = 52, mask = Mask.CIRCLE)
                Column {
                    Text("D Division", style = t.md.copy(fontSize = m.s(20.8)))
                    Gap(3.2)
                    Tag("30 hunters · same thresholds")
                }
            }

            Gap(16)
            Row(
                Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(m.d(5.4)),
            ) {
                ZoneCard("Promote", "Top 5", Ok, Modifier.weight(1f).fillMaxHeight())
                ZoneCard("Hold", "6–25", p.dim, Modifier.weight(1f).fillMaxHeight())
                ZoneCard("Demote", "26–30", Bad, Modifier.weight(1f).fillMaxHeight())
            }

            Gap(14.4)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(5.4)),
            ) {
                LeagueRow(4, "You", "7,120", you = true)
                LeagueRow(5, "Hunter D-91", "7,004")
                LeagueRow(6, "pacer", "6,880", pacer = true)
                LeagueRow(7, "Hunter D-44", "6,510")
                LeagueRow(8, "pacer", "6,220", pacer = true)
                LeagueRow(9, "Hunter D-12", "5,940")
                Gap(4)
                Card(Modifier.fillMaxWidth(), dashed = true) {
                    Tag("◇ Pacers are not people", t.tag.copy(color = p.soft))
                    Gap(3.8)
                    Text(
                        "The System runs them at a fixed pace to fill a thin division. They hold no place in the " +
                            "guild and are never counted as members. Each hunter who joins replaces one.",
                        style = t.body.copy(fontSize = m.s(12.2)),
                    )
                }
                Gap(11.2)
            }
        }

        BottomNav(active = 3)
    }
}

@Composable
private fun ZoneCard(label: String, range: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    val t = LocalType.current
    Card(modifier, padding = 9.6) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Tag(label)
            Gap(3.8)
            Tag(range, t.tag.copy(color = color, letterSpacing = 0.16.em))
        }
    }
}

@Composable
private fun LeagueRow(place: Int, name: String, aura: String, you: Boolean = false, pacer: Boolean = false) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), lit = you, dashed = pacer, padding = 10.4) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("$place", style = t.monoSmall.copy(color = if (you) p.hot else p.faint), modifier = Modifier.width(m.d(22)))
            if (pacer) Text("◇  ", style = t.tag.copy(color = p.soft))
            Text(name, style = t.questTitle.copy(fontSize = m.s(12.8)))
            if (pacer) {
                GapW(6)
                Tag("pacer", t.key.copy(color = p.soft))
            }
            Filler()
            Text(aura, style = t.monoSmall.copy(color = if (you) p.soft else p.dim))
        }
    }
}
