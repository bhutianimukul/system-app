package app.gakseong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
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
import app.gakseong.ui.Screen
import app.gakseong.ui.Shade
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.theme.LocalHunterClass
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Radius

/**
 * `data-s="share"` — DECISIONS.md §28. The preview exists because capture needs the card laid out anyway, and
 * because nobody should post something they have not seen. Everything on the card comes from an allowlist.
 */
@Composable
fun ShareScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val hunter = LocalHunterClass.current

    Screen {
        Bg()
        Bg2()
        Grain()
        TopFade()

        Body {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Eye("Ascension · Share")
                Filler()
                Tag("1080 × 1920")
            }

            Gap(14.4)
            // The card at the ratio it will be written out in. At full width a 9:16 card is taller than the
            // screen once the note and the share row are accounted for, so the preview is inset and centred.
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.72f)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(m.d(Radius.BIG)))
                    .background(p.base)
                    .border(1.dp, p.line, RoundedCornerShape(m.d(Radius.BIG))),
            ) {
                Art(R.drawable.sc_arise, top = 0f, left = -0.02f, width = 1.04f, alpha = 0.95f)
                Aura(widthFraction = 0.76f, heightFraction = 0.22f, top = 0.26f, left = 0.12f, alpha = 0.5f)
                // The card carries text over its own art, so it darkens earlier than a screen would: everything
                // below the portrait has to stay legible in someone else's Instagram feed.
                Shade(0f to 0.35f, 0.22f to 0f, 0.45f to 0.55f, 0.60f to 0.92f, 0.72f to 1f)
                Column(
                    Modifier
                        .matchParentSize()
                        .padding(horizontal = m.d(14.4), vertical = m.d(16)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    PortraitChip(hunter.portrait, width = 46, height = 56)
                    Gap(9.6)
                    Tag("${hunter.label} · 41 days held")
                    Gap(4.8)
                    Text(
                        buildAnnotatedString {
                            append("D · III → ")
                            withStyle(SpanStyle(color = p.soft, fontWeight = FontWeight(900))) { append("C · I") }
                        },
                        style = t.md.copy(fontSize = m.s(21.6)),
                    )
                    Gap(4.8)
                    Tag("Level 27")
                    Gap(11.2)
                    Text(
                        "The gate did not open for you. You were simply strong enough to walk through it.",
                        style = t.body.copy(fontSize = m.s(11.8), textAlign = TextAlign.Center),
                        modifier = Modifier.width(m.d(190)),
                    )
                    Gap(14.4)
                    Text("각성 GAKSEONG", style = t.wordmarkLatin.copy(fontSize = m.s(9.3), color = p.soft))
                    Gap(4)
                    Text("gakseong.app/s/7F2K9Q", style = t.questSub.copy(fontSize = m.s(9)))
                }
            }

            Gap(8)
            Card(Modifier.fillMaxWidth(), big = true) {
                Tag("What the card can never carry", t.tag.copy(color = p.hot))
                Gap(4.8)
                Text(
                    "No app name, no duration, no screen-time number, no guild member, nothing from the private " +
                        "track. Built from an allowlist, not a blocklist.",
                    style = t.body.copy(fontSize = m.s(12.5)),
                )
            }

            Filler()
            Cta("Share ascension")
            Gap(8.8)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(m.d(5.4))) {
                Pill("WhatsApp")
                Pill("Instagram")
                Pill("Copy")
            }
            Gap(8)
            Tag("The link is in the image and in the text · Stories drops the text")
            Gap(17.6)
        }
    }
}
