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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import app.gakseong.R
import app.gakseong.ui.Art
import app.gakseong.ui.Bg
import app.gakseong.ui.Bg2
import app.gakseong.ui.Body
import app.gakseong.ui.Card
import app.gakseong.ui.Cta
import app.gakseong.ui.Eye
import app.gakseong.ui.Filler
import app.gakseong.ui.Gap
import app.gakseong.ui.Grain
import app.gakseong.ui.Mask
import app.gakseong.ui.MaskedImage
import app.gakseong.ui.Plate
import app.gakseong.ui.Screen
import app.gakseong.ui.Shade
import app.gakseong.ui.SystemWindow
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType

/**
 * `data-s="arise"` — extraction. The one line that matters is on the plate: the count is public, and what you
 * beat to earn it is not. That is §7 rendered as copy.
 */
@Composable
fun AriseScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current

    Screen {
        Bg()
        Bg2()
        Art(R.drawable.sc_arise, top = -0.01f, left = -0.02f, width = 1.04f)
        Shade(0f to 0.45f, 0.08f to 0f, 0.46f to 0f, 0.60f to 0.77f, 0.72f to 1f)
        Grain()
        TopFade()

        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("Extraction")
                Filler()
                Tag("90 days held", t.tag.copy(letterSpacing = 0.34.em))
                Gap(6.4)
                Text("ARISE.", style = t.display(38.4).copy(textAlign = TextAlign.Center))

                Gap(17.6)
                SystemWindow {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(9.6))) {
                        MaskedImage(R.drawable.sh_01_knight, width = 40, height = 48, mask = Mask.SQUARISH, keyed = true)
                        Column {
                            Tag("Shadow extracted", t.tag.copy(color = p.hot))
                            Gap(2.6)
                            Text("Silence of the Third Watch", style = t.md.copy(fontSize = m.s(16.3)))
                            Gap(3.5)
                            Tag("Rank C · passive +3% aura", t.key)
                        }
                    }
                }

                Gap(8)
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(m.d(6.4)),
                ) {
                    StatCard("Army", "7 of 10", Modifier.weight(1f).fillMaxHeight())
                    StatCard("From", "private", Modifier.weight(1f).fillMaxHeight())
                }

                Gap(8.8)
                Plate(Modifier.width(m.d(250))) {
                    Text(
                        buildAnnotatedString {
                            append("The count is public. ")
                            withStyle(SpanStyle(color = p.ink, fontWeight = FontWeight(650))) {
                                append("What you beat to earn it is not.")
                            }
                        },
                        style = t.body.copy(fontSize = m.s(12.5), textAlign = TextAlign.Center),
                    )
                }

                Filler()
                Cta("Add to the army")
                Gap(17.6)
            }
        }
    }
}

/** The two-up label-over-value block used on Arise, Gate and half the app. */
@Composable
internal fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Card(modifier, padding = 12) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Tag(label)
            Gap(3.8)
            Text(value, style = t.md.copy(fontSize = m.s(16), color = p.soft))
        }
    }
}
