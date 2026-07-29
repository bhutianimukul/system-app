package app.gakseong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import app.gakseong.R
import app.gakseong.ui.*
import app.gakseong.ui.theme.LocalHunterClass
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType

/**
 * A specimen, not a product screen. `--ui: system-ui` and `--mono: ui-monospace` resolve to Roboto and Droid Sans
 * Mono on Android, which are not the same faces the design was drawn against, so this puts the candidates next to
 * each other at real size on a real device instead of arguing about them.
 *
 * Route: `--es screen type`.
 */
@Composable
fun TypeSpecimenScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val hunter = LocalHunterClass.current

    // Roboto Condensed ships with Android, so this costs no asset, no licence and no APK size.
    val condensed = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight(900)))

    Screen {
        Bg(); Bg2()
        Art(R.drawable.sc_ruins, top = -0.11f, left = -0.04f, width = 1.06f, alpha = 0.5f)
        Shade(0f to 0.52f, 0.08f to 0f, 0.20f to 0.8f, 0.30f to 1f)
        Grain(); TopFade()

        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Typography"); Filler(); Tag("specimen")
            }
            Gap(16)

            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(9.6)),
            ) {
                Specimen("A · display, as shipped", "Roboto Black · no squeeze · what every headline uses now") {
                    Text("SET LOW.\nRAISE IT YOURSELF.", style = t.display(27.2))
                }

                Specimen("B · display, squeezed", "Roboto Black · scaleX(.9), which is what the CSS actually does") {
                    Text(
                        "SET LOW.\nRAISE IT YOURSELF.",
                        style = t.display(27.2),
                        modifier = Modifier.graphicsLayer(
                            scaleX = 0.9f,
                            transformOrigin = TransformOrigin(0f, 0.5f),
                        ),
                    )
                }

                Specimen("C · display, condensed face", "sans-serif-condensed at 900 · drawn narrow, not squashed") {
                    Text(
                        "SET LOW.\nRAISE IT YOURSELF.",
                        style = t.display(27.2).copy(fontFamily = condensed),
                    )
                }

                Specimen("Labels · mono, as shipped", "Droid Sans Mono · wide, and it carries most of the app's text") {
                    Tag("threshold cleared · shadow in 3 days")
                    Gap(4)
                    Tag("RANK D · TIER III", t.tag.copy(color = p.soft))
                }

                Specimen("Labels · sans, letterspaced", "Roboto at the same size and tracking, no monospace") {
                    Text(
                        "THRESHOLD CLEARED · SHADOW IN 3 DAYS",
                        style = t.tag.copy(fontFamily = FontFamily.SansSerif, letterSpacing = 0.22.em),
                    )
                    Gap(4)
                    Text(
                        "RANK D · TIER III",
                        style = t.tag.copy(
                            fontFamily = FontFamily.SansSerif,
                            color = p.soft,
                            letterSpacing = 0.22.em,
                        ),
                    )
                }

                Specimen("Numerals", "The one big number per screen, in the class gradient") {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(m.d(12))) {
                        XlNumber("640")
                        CeremonyHero("D", designPx = 62.4)
                    }
                }

                Specimen("Body over art", "ink, dim and faint on the same background, to check the shades") {
                    Text("Ink: the System notes no error in this.", style = t.body.copy(color = p.ink))
                    Gap(3.2)
                    Text("Dim: base aura is never at risk.", style = t.body.copy(color = p.dim))
                    Gap(3.2)
                    Text("Faint: picked because your block was 38 minutes.", style = t.body.copy(color = p.faint))
                    Gap(3.2)
                    Text("Soft: ${hunter.label} · rank D · tier III", style = t.body.copy(color = p.soft))
                    Gap(3.2)
                    Text("Ghost: type anything…", style = t.body.copy(color = p.ghost))
                }

                Specimen("The same shades on a card", "A card is lighter than art, so faint reads differently") {
                    Card(Modifier.fillMaxWidth(), padding = 12.8) {
                        Text("Ink on card", style = t.body.copy(color = p.ink))
                        Text("Dim on card", style = t.body.copy(color = p.dim))
                        Text("Faint on card", style = t.body.copy(color = p.faint))
                        Text("Ghost on card", style = t.body.copy(color = p.ghost))
                    }
                }
                Gap(11.2)
            }
        }
    }
}

@Composable
private fun Specimen(label: String, note: String, content: @Composable ColumnScope.() -> Unit) {
    val t = LocalType.current
    Column(Modifier.fillMaxWidth()) {
        Tag(label, t.tag.copy(color = LocalPalette.current.hot))
        Gap(2.6)
        Tag(note, t.key)
        Gap(8)
        content()
    }
}
