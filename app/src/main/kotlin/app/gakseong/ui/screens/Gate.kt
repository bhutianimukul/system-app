package app.gakseong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
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
import app.gakseong.ui.Screen
import app.gakseong.ui.Shade
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType

/** `data-s="gate"` — a location gate. Entry is optional, which is the line that keeps it a game. */
@Composable
fun GateScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current

    Screen {
        Bg()
        Bg2()
        Aura(widthFraction = 0.70f, heightFraction = 0.32f, top = 0.14f, left = 0.15f, alpha = 0.56f)
        Art(
            R.drawable.gt_3_brass, top = 0.01f, left = 0.16f, width = 0.68f, alpha = 0.92f,
            blend = BlendMode.Screen, feather = true,
        )
        Shade(0f to 0.67f, 0.18f to 0f, 0.44f to 0.53f, 0.60f to 0.95f, 0.72f to 1f)
        Grain()
        TopFade()

        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("A Gate Has Opened")
                Filler()
                Tag("C-Rank · 2.1 km", t.tag.copy(color = p.soft))
                Gap(6.4)
                Text("HOLLOW WARD", style = t.display(32).copy(letterSpacing = 0.02.em, textAlign = TextAlign.Center))

                Gap(19.2)
                Card(Modifier.fillMaxWidth(), big = true) {
                    Tag("Objectives · 48h")
                    Gap(9.6)
                    Column(verticalArrangement = Arrangement.spacedBy(m.d(8))) {
                        Objective("Reach the location")
                        Objective("Arrive on foot")
                        Objective("No forbidden app en route")
                    }
                }

                Gap(8)
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(m.d(6.4)),
                ) {
                    StatCard("Reward", "600", Modifier.weight(1f).fillMaxHeight())
                    StatCard("Bonus", "1 shadow", Modifier.weight(1f).fillMaxHeight())
                }

                Gap(16)
                Text(
                    "Entry is optional. The Gate will close regardless.",
                    style = t.body.copy(fontSize = m.s(12.5), textAlign = TextAlign.Center),
                    modifier = Modifier.width(m.d(225)),
                )

                Filler()
                Cta("Enter")
                Gap(17.6)
            }
        }
    }
}

@Composable
private fun Objective(text: String) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(9.6))) {
        Text("◈", style = t.tag.copy(color = p.hot, fontSize = m.s(9.6)))
        Text(text, style = t.questTitle)
    }
}
