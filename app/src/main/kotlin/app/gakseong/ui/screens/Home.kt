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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.font.FontWeight
import app.gakseong.R
import app.gakseong.ui.Art
import app.gakseong.ui.Aura
import app.gakseong.ui.Bg
import app.gakseong.ui.Bg2
import app.gakseong.ui.Body
import app.gakseong.ui.BottomNav
import app.gakseong.ui.Eye
import app.gakseong.ui.Filler
import app.gakseong.ui.Gap
import app.gakseong.ui.Grain
import app.gakseong.ui.Meter
import app.gakseong.ui.QuestCard
import app.gakseong.ui.QuestState
import app.gakseong.ui.Screen
import app.gakseong.ui.Shade
import app.gakseong.ui.Sig
import app.gakseong.ui.SystemWindow
import app.gakseong.ui.Tag
import app.gakseong.ui.TopFade
import app.gakseong.ui.Wordmark
import app.gakseong.ui.XlNumber
import app.gakseong.ui.theme.LocalHunterClass
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType

/**
 * Screen 1, Home. The layer order and every number here come straight from the design page's `data-s="home"`
 * section, so the two can be diffed side by side.
 */
@Composable
fun HomeScreen() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val hunter = LocalHunterClass.current

    Screen {
        // Painted in z-index order, not markup order: aura 1, art 2, shade 3, grain 4, topfade 5, body 8, nav 9.
        Bg()
        Bg2()
        Aura(widthFraction = 0.78f, heightFraction = 0.22f, top = 0.06f, left = 0.11f, alpha = 0.5f)
        Art(
            R.drawable.sc_portal, top = -0.06f, left = 0.24f, width = 0.52f, alpha = 0.95f,
            blend = BlendMode.Screen, feather = true,
        )
        Art(hunter.portrait, top = -0.03f, left = 0.02f, width = 0.96f)
        Shade(0f to 0.57f, 0.10f to 0f, 0.34f to 0f, 0.44f to 0.82f, 0.52f to 1f)
        Grain()
        TopFade()

        Body(navSpace = true) {
            Row(verticalAlignment = Alignment.Bottom) {
                Wordmark()
                Filler()
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(m.d(2.6))) {
                    Tag("D · III", t.tag.copy(color = p.hot, fontWeight = FontWeight(700)))
                    Tag("LV 34 · 14d", t.tag.copy(color = p.soft))
                }
            }

            Gap(10.4)
            Eye("Daily Quest")

            Gap(118.4)
            Tag("Aura today")
            Gap(4)
            Row(verticalAlignment = Alignment.Bottom) {
                XlNumber("640")
                Filler()
                Column {
                    Tag("560 to D · II", t.tag.copy(color = p.soft))
                    Gap(8)
                }
            }

            Gap(17.6)
            Meter(fill = 0.53f, marker = 0.33f)
            Gap(6.7)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Tag("Threshold cleared", t.key)
                Tag("Shadow in 3 days", t.key)
            }

            Gap(17.6)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(m.d(8)),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
                    QuestCard(
                        icon = "🌙", title = "Screen off\n45 min", sub = "Verified", value = "+180",
                        state = QuestState.DONE, modifier = Modifier.weight(1f),
                    )
                    QuestCard(
                        icon = "◉", title = "Scroll under\n90 min", sub = "41 min used", value = "+220",
                        state = QuestState.DONE, modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(m.d(8))) {
                    QuestCard(
                        icon = "⚡", title = "6,000\nsteps", sub = "Health Connect", value = "+240",
                        state = QuestState.DONE, modifier = Modifier.weight(1f),
                    )
                    QuestCard(
                        icon = "◈", title = "Focus session\n45 min", sub = "Not started", value = "+300",
                        state = QuestState.PENDING, modifier = Modifier.weight(1f),
                    )
                }
                QuestCard(
                    icon = "☾", title = "Night gate · 00:30 to 06:00", sub = "Pending · tonight", value = "+260",
                    state = QuestState.PENDING, wide = true, modifier = Modifier.fillMaxWidth(),
                )

                Gap(3.2)
                SystemWindow {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Tag("⚡ Bonus spawned", t.tag.copy(color = p.hot))
                        Filler()
                        Tag("expires 41:08", t.tag.copy(color = p.soft))
                    }
                    Gap(7.2)
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(m.d(9.6)),
                    ) {
                        Sig()
                        Column(Modifier.weight(1f)) {
                            Text("Phone down · 2 hours", style = t.listItem)
                            Gap(2.2)
                            Tag("Start within the hour or it is gone", t.tag.copy(letterSpacing = t.key.letterSpacing))
                        }
                        Text("+400", style = t.monoSmall)
                    }
                }
                Gap(11.2)
            }
        }

        BottomNav(active = 0)
    }
}
