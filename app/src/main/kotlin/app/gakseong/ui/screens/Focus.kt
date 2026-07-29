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
import app.gakseong.ui.Plate
import app.gakseong.ui.RingTimer
import app.gakseong.ui.Screen
import app.gakseong.ui.ShadeRadial
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType

/** `data-s="focus"`. A session bounded by its own length, with the speed bump shown in place. */
@Composable
fun FocusScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current

    Screen {
        Bg()
        Bg2()
        Aura(widthFraction = 0.90f, heightFraction = 0.30f, top = 0.24f, left = 0.05f, alpha = 0.42f)
        Art(R.drawable.sc_flame, top = -0.04f, left = -0.04f, width = 1.06f, alpha = 0.9f)
        ShadeRadial(0.5f, 0.34f, 0.62f, 0f to 0.42f, 0.62f to 0.82f, 1f to 0.95f)
        Grain()
        TopFade()

        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("Focus Active")
                Filler()
                RingTimer(progress = 0.689f, time = "31:12", of = "of 45:00")
                Gap(20.8)
                Tag("Forbidden for the next 28 minutes")
                Gap(9.6)
                Row(horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
                    Pill("Instagram")
                    Pill("YouTube")
                    Pill("Chrome")
                }

                Gap(12.8)
                // Flex children stretch to the tallest sibling; Compose needs to be told.
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(m.d(5.4)),
                ) {
                    Card(Modifier.weight(1f).fillMaxHeight()) {
                        Tag("◐ Do Not Disturb", t.tag.copy(color = p.hot))
                        Gap(2.2)
                        Tag("calls still ring", t.tag.copy(color = p.faint))
                    }
                    Card(Modifier.weight(1f).fillMaxHeight()) {
                        Tag("⏸ Screen-off credit", t.tag.copy(color = p.hot))
                        Gap(2.2)
                        Tag("paused · 45 min", t.tag.copy(color = p.faint))
                    }
                }

                Gap(8)
                Plate(Modifier.width(m.d(250))) {
                    Text(
                        "The System is watching.",
                        style = t.body.copy(fontSize = m.s(12.8), textAlign = TextAlign.Center, color = p.ink),
                    )
                    Text(
                        "Opening any of these ends the session.",
                        style = t.body.copy(fontSize = m.s(12.8), textAlign = TextAlign.Center, color = p.ink),
                    )
                    Text(
                        "Screen off is fine — the timer keeps running. Phone-down just stops collecting, so the " +
                            "same minutes are never paid twice.",
                        style = t.body.copy(fontSize = m.s(12.8), textAlign = TextAlign.Center),
                    )
                }

                Filler()
                Card(Modifier.fillMaxWidth(), big = true, lit = true) {
                    Tag("⚠ Speed bump", t.tag.copy(color = p.hot))
                    Gap(6.4)
                    Text("The System does not permit this", style = t.md)
                    Gap(3.2)
                    Text("31 minutes remain. Proceeding ends your session.", style = t.body.copy(fontSize = m.s(12.2)))
                    Gap(13.6)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(m.d(6.4))) {
                        Cta("Return", modifier = Modifier.weight(1f))
                        Cta("Proceed", ghost = true, modifier = Modifier.weight(1f))
                    }
                }
                Gap(17.6)
            }
        }
    }
}
