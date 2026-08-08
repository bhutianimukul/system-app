package app.gakseong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import app.gakseong.R
import app.gakseong.ui.LocalSystem
import gakseong.engine.Rank
import app.gakseong.ui.Art
import app.gakseong.ui.Aura
import app.gakseong.ui.Bg
import app.gakseong.ui.Bg2
import app.gakseong.ui.Body
import app.gakseong.ui.Card
import app.gakseong.ui.Filler
import app.gakseong.ui.Eye
import app.gakseong.ui.Gap
import app.gakseong.ui.Grain
import app.gakseong.ui.Mask
import app.gakseong.ui.MaskedImage
import app.gakseong.ui.Screen
import app.gakseong.ui.Shade
import app.gakseong.ui.StatsRow
import app.gakseong.ui.SystemWindow
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.theme.LocalHunterClass
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType

/**
 * `data-s="rank"` — the ascension ceremony. The struck-through old tier next to the new one is the whole point:
 * Rank falls as well as rises, so a promotion has to show what it replaced.
 */
@Composable
fun CeremonyScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val hunter = LocalHunterClass.current
    val sys = LocalSystem.current
    val rank = sys.hunter.toEngine().rank
    // An ascension is always from the tier below. History supplies the real prior rank from phase 05.
    val before = Rank(maxOf(0, rank.ordinal - 1))

    Screen {
        Bg()
        Bg2()
        Aura(widthFraction = 0.96f, heightFraction = 0.34f, top = 0.14f, left = 0.02f, alpha = 0.62f)
        Art(R.drawable.gt_1_iron, top = 0f, left = 0f, width = 1f, alpha = 0.42f, feather = true)
        Art(hunter.portrait, top = -0.01f, left = 0.01f, width = 0.98f)
        Shade(0f to 0.57f, 0.12f to 0f, 0.42f to 0f, 0.56f to 0.85f, 0.66f to 1f)
        Grain()
        TopFade()

        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("System Notification")
                Filler()
                Tag("Threshold held · ${sys.hunter.streak} days")
                Gap(8)
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(before.label) }
                        append("   ▶   ")
                        withStyle(SpanStyle(color = p.soft)) { append(rank.label) }
                    },
                    style = t.tag.copy(letterSpacing = 0.20.em),
                )
                Gap(10.4)
                MaskedImage(R.drawable.em_d, width = 88, mask = Mask.CIRCLE)
                Gap(8)
                Text(rank.title.uppercase(), style = t.display(32.8).copy(letterSpacing = 0.03.em, textAlign = TextAlign.Center))
                Gap(4.5)
                Tag("Rank ${rank.letter} · Tier ${rank.tier}")

                Gap(19.2)
                SystemWindow {
                    StatsRow(values = listOf(15, 27, 13, 21), gains = listOf(2, 6, 0, 3))
                }

                Gap(8)
                Card(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(9.6))) {
                        MaskedImage(R.drawable.sh_01_knight, width = 36, height = 44, mask = Mask.SQUARISH, keyed = true)
                        Column {
                            Tag("Shadow extracted", t.tag.copy(color = p.soft))
                            Gap(2.2)
                            Text("Silence of the Third Watch", style = t.listItem.copy(fontSize = m.s(13.44)))
                        }
                    }
                }

                Gap(16)
                Column(Modifier.width(m.d(215)), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Instagram unopened for fourteen days.",
                        style = t.body.copy(fontSize = m.s(12.8), textAlign = TextAlign.Center),
                    )
                    Text(
                        "The System notes no error in this.",
                        style = t.body.copy(fontSize = m.s(12.8), textAlign = TextAlign.Center, color = p.ink,
                            fontWeight = FontWeight(650)),
                    )
                }

                Filler()
                Tag("Tap to continue", t.tag.copy(letterSpacing = 0.30.em))
                Gap(17.6)
            }
        }
    }
}
