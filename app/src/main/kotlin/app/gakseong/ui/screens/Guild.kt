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
import app.gakseong.ui.theme.Ok

/** `data-s="guild"` — invite only, twenty at most. Guilds exist before any global ladder does. */
@Composable
fun GuildScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current

    Screen {
        Bg()
        Bg2()
        Aura(0.82f, 0.20f, top = 0.03f, left = 0.09f, alpha = 0.42f)
        Art(R.drawable.sc_ruins, top = -0.12f, left = -0.04f, width = 1.06f, alpha = 0.66f)
        Shade(0f to 0.52f, 0.08f to 0f, 0.24f to 0.77f, 0.34f to 1f)
        Grain()
        TopFade()

        Body(navSpace = true) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Guild"); Filler(); Tag("6 of 20")
            }
            Gap(19.2)
            Text("Shadow Wardens", style = t.md.copy(fontSize = m.s(21.6)))
            Gap(8)
            Tag("Founded 41 days ago · invite only")

            Gap(17.6)
            SystemWindow {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Tag("Party raid active", t.tag.copy(color = p.hot)); Filler()
                    Tag("4 / 6 holding", t.tag.copy(color = p.soft))
                }
                Gap(4.8)
                Text(
                    buildAnnotatedString {
                        append("Gate clears only if every member hits their part. ")
                        withStyle(SpanStyle(color = p.ink, fontWeight = FontWeight(650))) { append("2 have not started.") }
                    },
                    style = t.body.copy(fontSize = m.s(12.5)),
                )
                Gap(6)
                Meter(fill = 0.66f, height = 7)
            }

            Gap(8.8)
            Card(Modifier.fillMaxWidth(), padding = 12) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
                    MaskedImage(R.drawable.em_d, width = 26, mask = Mask.CIRCLE)
                    Column {
                        Tag("D Division", t.tag.copy(color = p.hot))
                        Gap(1.9)
                        Tag("6th of 30 · 2d 04h")
                    }
                    Filler()
                    Text("→", style = t.tag.copy(color = p.soft))
                }
            }

            Gap(8.8)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(5.4)),
            ) {
                MemberRow("You", "D · III", "7,120", you = true)
                MemberRow("Rahul", "D · II", "7,004")
                MemberRow("Aditi", "C · III", "6,880")
                MemberRow("Kabir", "D · III", "6,510")
                MemberRow("Neha", "E · I", "5,940")
                MemberRow("Ishan", "D · III", "5,610")
                Gap(11.2)
            }
        }
        BottomNav(active = 3)
    }
}

@Composable
private fun MemberRow(name: String, rank: String, aura: String, you: Boolean = false) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), lit = you, padding = 10.4) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
            Text(name, style = t.questTitle.copy(fontSize = m.s(12.8)))
            Tag(rank, t.key.copy(color = if (you) p.hot else p.faint))
            Filler()
            Text(aura, style = t.monoSmall.copy(color = if (you) p.soft else p.dim))
        }
    }
}
