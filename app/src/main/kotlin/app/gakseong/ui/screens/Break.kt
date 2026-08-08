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
import androidx.compose.ui.text.withStyle
import app.gakseong.R
import app.gakseong.ui.LocalSystem
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
import app.gakseong.ui.theme.Bad
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Ok
import gakseong.engine.Balance

/**
 * `data-s="break"` — a dungeon break. §6 is the whole screen: the four penalties are shown as a sequence with
 * exactly one marked `now`, and the road back is on the same screen as the punishment, never a screen later.
 */
@Composable
fun BreakScreen() {
    val sys = LocalSystem.current
    val misses = sys.hunter.consecutiveMisses
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current

    Screen {
        Bg()
        Bg2()
        Aura(widthFraction = 1f, heightFraction = 0.30f, top = 0.18f, left = 0f, alpha = 0.4f, color = Bad)
        Art(R.drawable.sc_boss, top = 0.02f, left = -0.04f, width = 1.06f, alpha = 0.76f)
        Shade(0f to 0.67f, 0.14f to 0.19f, 0.32f to 0f, 0.50f to 0.67f, 0.64f to 0.95f, 0.74f to 1f)
        Grain()
        TopFade()

        Body {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Eye("Dungeon Break", color = Bad)
                Filler()
                Text(
                    "YOU ARE BEING\nOVERRUN",
                    style = t.display(32).copy(color = Bad, textAlign = TextAlign.Center),
                )
                Gap(11.2)
                Text(
                    "6h 41m of scrolling today. The threshold was 90 minutes.",
                    style = t.body.copy(fontSize = m.s(12.8), textAlign = TextAlign.Center),
                    modifier = Modifier.width(m.d(225)),
                )

                Gap(17.6)
                Card(Modifier.fillMaxWidth(), big = true, accent = Bad) {
                    Tag("Escalation · sequenced", t.tag.copy(color = Bad))
                    Gap(9.6)
                    Column(verticalArrangement = Arrangement.spacedBy(m.d(7.4))) {
                        // §4: penalties sequence, never stack. penaltyFor() decides which day is live, and
                        // exactly one ever is. Five at once are illegible and the player learns nothing.
                        LADDER.forEach { (day, label) ->
                            Step(
                                day = "Day $day",
                                label = label,
                                trailing = when {
                                    misses > day -> "done"
                                    misses == day -> "now"
                                    else -> null
                                },
                                live = misses == day,
                                dimmed = misses < day,
                            )
                        }
                    }
                }

                Gap(8)
                Card(Modifier.fillMaxWidth(), accent = Ok) {
                    Tag("The road back", t.tag.copy(color = Ok))
                    Gap(3.8)
                    Text(
                        buildAnnotatedString {
                            append("Clear today's threshold to begin recovery. ")
                            withStyle(SpanStyle(color = Ok, fontWeight = FontWeight(650))) {
                                append("3 consecutive days restores D-rank.")
                            }
                        },
                        style = t.body.copy(fontSize = m.s(12.8)),
                    )
                }

                Filler()
                Cta("Enter the penalty gate", bad = true)
                Gap(17.6)
            }
        }
    }
}

/** One rung of the escalation. Exactly one is ever live, which is the point of §6. */
@Composable
private fun Step(day: String, label: String, trailing: String? = null, live: Boolean = false, dimmed: Boolean = false) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val alpha = if (dimmed) 0.5f else 1f
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(day.uppercase(), style = t.tag.copy(color = p.faint.copy(alpha = alpha)), modifier = Modifier.width(m.d(46.4)))
        Text(
            label,
            style = t.body.copy(
                fontSize = m.s(12.5),
                color = if (live) Bad else p.dim.copy(alpha = alpha),
                fontWeight = if (live) FontWeight(650) else FontWeight.Normal,
            ),
        )
        if (trailing != null) {
            Filler()
            Text(trailing.uppercase(), style = t.tag.copy(color = if (live) Bad else p.faint))
        }
    }
}

/**
 * The escalation, day by day, matching `penaltyFor` exactly. Kept beside it rather than derived from it because
 * the screen needs the quiet days too: day 2, 4 and 6 carry no penalty on purpose, and a list built only from
 * the days that fire would hide that the gaps are deliberate.
 */
internal val LADDER = listOf(
    1 to "Streak broken",
    3 to "Aura −${Balance.AURA_DEBIT}",
    5 to "Demotion warning",
    7 to "Demotion and containment",
)

