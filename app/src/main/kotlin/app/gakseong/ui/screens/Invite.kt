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
import app.gakseong.ui.Screen
import app.gakseong.ui.Shade
import app.gakseong.ui.Sig
import app.gakseong.ui.SystemWindow
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType

/**
 * `data-s="invite"` — the raid link. No friend list, no pairing step, no code to type: whoever taps the link
 * joins, and if they do not have the app the link installs it and drops them straight in.
 */
@Composable
fun InviteScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current

    Screen {
        Bg()
        Bg2()
        Aura(widthFraction = 0.74f, heightFraction = 0.18f, top = 0.12f, left = 0.13f, alpha = 0.44f)
        Art(R.drawable.sc_hero, top = -0.02f, left = -0.02f, width = 1.04f, alpha = 0.6f)
        Shade(0f to 0.57f, 0.10f to 0f, 0.26f to 0.67f, 0.38f to 1f)
        Grain()
        TopFade()

        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("Summon a Partner")
                Filler()
                Sig(size = 40)
                Gap(14.4)
                Text("45 MINUTES.\nBOTH OF YOU HOLD.", style = t.display(25.6).copy(textAlign = TextAlign.Center))

                Gap(16)
                Plate(Modifier.width(m.d(250))) {
                    Text(
                        buildAnnotatedString {
                            append("Whoever taps the link joins this raid. ")
                            withStyle(SpanStyle(color = p.ink, fontWeight = FontWeight(650))) {
                                append("No app? The link installs it and drops them straight in.")
                            }
                        },
                        style = t.body.copy(fontSize = m.s(12.8), textAlign = TextAlign.Center),
                    )
                }

                Gap(17.6)
                Card(Modifier.fillMaxWidth(), big = true, padding = 14.4) {
                    Tag("Raid link", t.tag.copy(color = p.hot))
                    Gap(5.6)
                    Text("gakseong.app/r/7K2W-QD9", style = t.questSub.copy(color = p.ink, fontSize = m.s(10.9)))
                }

                Gap(8)
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(m.d(6.4)),
                ) {
                    StatCard("Expires", "60s", Modifier.weight(1f).fillMaxHeight())
                    StatCard("Bonus", "+450", Modifier.weight(1f).fillMaxHeight())
                }

                Gap(8)
                SystemWindow {
                    Text(
                        "One rewarded summon a week. It pays when they clear day three, not when they install.",
                        style = t.body.copy(fontSize = m.s(12.8)),
                    )
                }

                Filler()
                Cta("Share raid link")
                Gap(8.8)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                    Pill("WhatsApp")
                    Pill("Instagram")
                    Pill("Copy")
                }
                Gap(8)
                Tag("Android share sheet · whatever they actually use")
                Gap(19.2)
            }
        }
    }
}
