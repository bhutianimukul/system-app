package app.gakseong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import app.gakseong.ui.SystemWindow
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.XlNumber
import app.gakseong.ui.theme.LocalHunterClass
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Ok

/** `data-s="raid"` — a live co-op session. Base aura is never at risk; only the bonus is. */
@Composable
fun RaidScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val hunter = LocalHunterClass.current

    Screen {
        Bg()
        Bg2()
        Aura(widthFraction = 0.88f, heightFraction = 0.28f, top = 0.22f, left = 0.06f, alpha = 0.46f)
        Art(R.drawable.sc_hero, top = -0.14f, left = -0.04f, width = 1.06f, alpha = 0.8f)
        Shade(0f to 0.57f, 0.14f to 0f, 0.30f to 0f, 0.46f to 0.88f, 0.58f to 1f)
        Grain()
        TopFade()

        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("Raid · Co-op")
                Filler()
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    PartnerCard(hunter.portrait, "You", "● Holding", Ok, Modifier.weight(1f))
                    Text("⚔", style = t.tag.copy(color = p.hot, fontSize = m.s(14.4)))
                    PartnerCard(R.drawable.p_ranger, "Rahul", "● Holding", Ok, Modifier.weight(1f), alpha = 0.85f)
                }
                Gap(24)
                XlNumber("18:44", designPx = 48)
                Gap(7.2)
                Tag("of 45:00 · both must hold")

                Gap(17.6)
                SystemWindow {
                    Text("This raid · drawn from 12 at random", style = t.body.copy(fontSize = m.s(12.5)))
                }

                Gap(8)
                Card(Modifier.fillMaxWidth(), big = true) {
                    Tag("Settlement", t.tag.copy(color = p.hot))
                    Gap(4.8)
                    Text(
                        "Base aura is never at risk. If either breaks, only the raid bonus is lost, and the one " +
                            "who broke it loses base as well.",
                        style = t.body.copy(fontSize = m.s(12.5)),
                    )
                }

                Gap(8)
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Tag("Raid bonus")
                            Gap(2.6)
                            Tag("+450 proven · +180 on your word", t.tag.copy(color = p.faint))
                        }
                        Text("+450", style = t.md.copy(fontSize = m.s(15.2), color = p.soft))
                    }
                }

                Filler()
                Cta("Invite via WhatsApp", ghost = true)
                Gap(17.6)
            }
        }
    }
}

/** The two-up partner block used by every raid screen. */
@Composable
internal fun PartnerCard(
    portrait: Int,
    name: String,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    dashed: Boolean = false,
) {
    val t = LocalType.current
    Card(modifier, big = true, lit = dashed) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            PortraitChip(portrait, alpha = alpha)
            Gap(6.4)
            Text(name, style = t.questTitle.copy(fontSize = t.listItem.fontSize, textAlign = TextAlign.Center))
            Gap(3.2)
            Tag(status, t.tag.copy(color = statusColor))
        }
    }
}

/** A 42×50 portrait thumbnail, top-cropped and keyed onto the card the same way the full art is. */
@Composable
internal fun PortraitChip(res: Int, width: Number = 42, height: Number = 50, alpha: Float = 1f) {
    val m = LocalMetrics.current
    val image = ImageBitmap.imageResource(res)
    Box(
        Modifier
            .size(m.d(width), m.d(height))
            .clip(RoundedCornerShape(m.d(8)))
            .drawBehind {
                val w = size.width
                val h = w * image.height / image.width
                drawImage(
                    image = image,
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(w.toInt(), h.toInt()),
                    alpha = alpha,
                    blendMode = BlendMode.Lighten,
                )
            }
    )
}
