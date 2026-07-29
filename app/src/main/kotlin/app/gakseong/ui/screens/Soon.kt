package app.gakseong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.em
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.gakseong.R
import app.gakseong.ui.*
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType

/** `data-s="soon"` — sealed gates. Naming what is not built yet is cheaper than pretending it is. */
@Composable
fun SoonScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current

    Screen {
        Bg()
        Bg2()
        Art(R.drawable.sc_ruins, top = -0.12f, left = -0.04f, width = 1.06f, alpha = 0.4f)
        Shade(0f to 0.42f, 0.08f to 0f, 0.24f to 0.67f, 0.38f to 1f)
        Grain()
        TopFade()

        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Not Yet Open"); Filler(); Tag("4 gates sealed")
            }
            Gap(16)
            Text("Gates that have\nnot opened yet.", style = t.display(24))

            Gap(14.4)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(5.4)),
            ) {
                Sealed("◆", "Aura Exchange", "Q1",
                    "Spend banked aura on art, sigils, themes and shadow skins. Cosmetics only, and nothing that " +
                        "touches a threshold.")
                Sealed("◈", "Cloud Sync", "Q4",
                    "Your record survives a lost phone. Encrypted before it leaves, so the System still cannot " +
                        "read your private track.")
                Sealed("◍", "iOS", "when Apple moves",
                    "Waiting on Apple. Screen Time hands back opaque tokens, so the leaderboard cannot exist " +
                        "there yet.")
                Sealed("❖", "Guild Wars", "Q1",
                    "Two guilds, one week, aggregate thresholds. Your bad day costs thirty people.")
                Sealed("◎", "Watch Face", "exploring",
                    "Rank and today's band on the wrist. Nothing to open.")

                Gap(4)
                SystemWindow {
                    Tag("Tell me when a gate opens", t.tag.copy(color = p.hot))
                    Gap(4.8)
                    Text("One mail per launch. Nothing else, ever.", style = t.body.copy(fontSize = m.s(12.2)))
                    Gap(9.6)
                    Card(Modifier.fillMaxWidth(), padding = 11.2) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Tag("your@email", t.tag.copy(color = p.ghost, letterSpacing = 0.10.em))
                            Filler()
                            Pill("Notify me", on = true)
                        }
                    }
                }
                Gap(11.2)
            }
        }
    }
}

@Composable
private fun Sealed(glyph: String, title: String, when_: String, why: String) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(Modifier.fillMaxWidth(), dashed = true, padding = 13.6) {
        Row(horizontalArrangement = Arrangement.spacedBy(m.d(9.6))) {
            Box(
                Modifier
                    .size(m.d(32))
                    .clip(RoundedCornerShape(m.d(10)))
                    .background(Color(0xFF12162B)),
                contentAlignment = Alignment.Center,
            ) {
                Text(glyph, style = t.md.copy(fontSize = m.s(14.4), color = p.soft))
            }
            Column {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = t.questTitle.copy(fontSize = m.s(14.1)))
                    Filler()
                    Tag(when_, t.key.copy(color = p.faint))
                }
                Gap(3.5)
                Text(why, style = t.body.copy(fontSize = m.s(12)))
            }
        }
    }
}
