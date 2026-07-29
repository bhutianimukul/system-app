package app.gakseong.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.gakseong.ui.theme.Bad
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Ok
import app.gakseong.ui.theme.Warn
import app.gakseong.ui.theme.pct

// The three charts the Report, Reality and Baseline screens share. Colour carries the meaning and it is the
// economy's own: below the threshold is red, inside the band is green, above the cap is a grey outline because
// overflow is real work that buys no standing.

/** Where one day's aura landed relative to that rank's band. */
enum class Band { LOW, IN, OVER }

/** `.chart` — 96px of bars with a dashed threshold reference line. */
@Composable
fun AuraByDay(values: List<Int>, threshold: Int, cap: Int, labels: List<String>) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val ceiling = maxOf(values.maxOrNull() ?: cap, cap)

    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(m.d(96))) {
            Row(
                Modifier.fillMaxWidth().fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                values.forEach { v ->
                    val band = when {
                        v < threshold -> Band.LOW
                        v > cap -> Band.OVER
                        else -> Band.IN
                    }
                    val shape = RoundedCornerShape(topStart = m.d(4), topEnd = m.d(4), bottomStart = 1.dp, bottomEnd = 1.dp)
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(v.toFloat() / ceiling)
                            .then(
                                when (band) {
                                    Band.LOW -> Modifier.background(Bad, shape)
                                    Band.IN -> Modifier.background(Ok, shape)
                                    // `.bar.over` is an outline, not a fill: it is work that bought no rank.
                                    Band.OVER -> Modifier
                                        .background(Color(0x336B7490), shape)
                                        .border(1.dp, Color(0xFF6B7490), shape)
                                }
                            )
                    )
                }
            }
            // `.ref` — the threshold, dashed, with its own label.
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .drawBehind {
                        val y = size.height * (1f - threshold.toFloat() / ceiling)
                        drawLine(
                            color = p.line2,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 6f)),
                        )
                    }
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = m.d(5.4)), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            labels.forEach {
                Text(
                    it.uppercase(),
                    style = t.key.copy(fontSize = m.s(6.7), textAlign = TextAlign.Center),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** `.hrs` — one cell an hour, intensity by how much of the day's scrolling landed there. */
@Composable
fun HoursHeatmap(intensity: List<Float>) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    Row(Modifier.fillMaxWidth().padding(top = m.d(8)), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        intensity.forEach { v ->
            Box(
                Modifier
                    .weight(1f)
                    .height(m.d(26))
                    .background(
                        if (v <= 0f) p.meterTrack else p.hot.pct(0.18f + 0.82f * v),
                        RoundedCornerShape(2.dp),
                    )
            )
        }
    }
}

/** How a day in the last 28 resolved. `SHIELDED` exists because a shield absorbs a miss rather than hiding it. */
enum class DayResult { HELD, SHIELDED, MISSED, NONE }

/** `.cal` — 14 columns, so 28 days is exactly two rows. */
@Composable
fun DayCalendar(days: List<DayResult>) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    Column(Modifier.fillMaxWidth().padding(top = m.d(8.8)), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        days.chunked(14).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                week.forEach { d ->
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                when (d) {
                                    DayResult.HELD -> Ok
                                    DayResult.SHIELDED -> Warn
                                    DayResult.MISSED -> Bad
                                    DayResult.NONE -> p.meterTrack
                                },
                                RoundedCornerShape(2.dp),
                            )
                    )
                }
            }
        }
    }
}

/** `.lgnd` — 8px swatch plus a label. */
@Composable
fun Legend(items: List<Pair<Color, String>>) {
    val m = LocalMetrics.current
    val t = LocalType.current
    Row(
        Modifier.fillMaxWidth().padding(top = m.d(8.8)),
        horizontalArrangement = Arrangement.spacedBy(m.d(11.2)),
    ) {
        items.forEach { (color, label) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(4.5))) {
                Box(Modifier.size(m.d(8)).background(color, RoundedCornerShape(2.dp)))
                Text(label.uppercase(), style = t.key.copy(fontSize = m.s(7)))
            }
        }
    }
}
