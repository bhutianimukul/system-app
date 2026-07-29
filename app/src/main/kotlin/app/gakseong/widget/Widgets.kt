package app.gakseong.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import app.gakseong.MainActivity

// The System without opening the app, which is where retention actually lives. Four widgets, per §27 of the
// design page's widget screen.
//
// Two constraints shape all of this and neither is negotiable:
//
// 1. A widget renders through RemoteViews, so none of ui/Kit.kt is available here. No blur, no blend modes, no
//    gradients-on-text, no Canvas. Glance has boxes, text, images and colours. The look is therefore the design's
//    palette and type scale applied to flat surfaces rather than a port of the screens.
// 2. Refresh follows §2's retroactive settle: app open, widget refresh, and the 15-minute WorkManager job. A
//    widget must never hold a service open or poll on its own.

// The palette, inlined. Widgets run in the launcher's process and cannot read a CompositionLocal from the app.
private val Base = Color(0xFF0B0D1A)
private val Card = Color(0xFF141728)
private val Ink = Color(0xFFFFFFFF)
private val Dim = Color(0xFFC6CCDD)
private val Faint = Color(0xFF8B93A8)
private val Hot = Color(0xFFFF48D0)
private val Soft = Color(0xFFFFAEEC)
private val Ok = Color(0xFF4AE3A0)

/** Placeholder state. Wiring this to Room and the phase-01 engine is the next phase, not this one. */
private data class WidgetState(
    val rank: String = "D · III",
    val auraToday: Int = 640,
    val toNextTier: Int = 560,
    val questsDone: Int = 3,
    val questsTotal: Int = 5,
    val thresholdCleared: Boolean = true,
    val streakDays: Int = 14,
    val shields: Int = 2,
    val nightGate: String = "00:30 — 06:00",
    val nightGatePending: Boolean = true,
)

private val label = TextStyle(color = androidx.glance.unit.ColorProvider(Faint), fontSize = 9.sp)
private val value = TextStyle(color = androidx.glance.unit.ColorProvider(Ink), fontSize = 15.sp, fontWeight = FontWeight.Bold)

@androidx.compose.runtime.Composable
private fun Shell(content: @androidx.compose.runtime.Composable () -> Unit) {
    Box(
        GlanceModifier
            .fillMaxSize()
            .background(Base)
            .cornerRadius(22.dp)
            .padding(13.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) { content() }
}

@androidx.compose.runtime.Composable
private fun Eyebrow(text: String, trailing: String? = null) {
    Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text.uppercase(),
            style = TextStyle(color = androidx.glance.unit.ColorProvider(Hot), fontSize = 9.sp, fontWeight = FontWeight.Medium),
        )
        Spacer(GlanceModifier.defaultWeight())
        if (trailing != null) Text(trailing, style = label)
    }
}

/** 4×2 — the day's quests as filled pips, the aura, and whether the threshold is behind you. */
class DailyQuestWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) = provideContent {
        val s = WidgetState()
        GlanceTheme { Shell {
            // fillMaxSize on the column left dead space under the content in a 4x2 cell. The widget states its
            // height from what it contains and lets the launcher's cell be whatever it is.
            Column(GlanceModifier.fillMaxWidth()) {
                Eyebrow("Daily Quest", s.rank)
                Spacer(GlanceModifier.height(9.dp))
                Row(GlanceModifier.fillMaxWidth()) {
                    repeat(s.questsTotal) { i ->
                        Box(
                            GlanceModifier
                                .defaultWeight()
                                .height(20.dp)
                                .padding(end = 4.dp),
                        ) {
                            Box(
                                GlanceModifier
                                    .fillMaxSize()
                                    .cornerRadius(5.dp)
                                    .background(if (i < s.questsDone) Hot else Card),
                            ) {}
                        }
                    }
                }
                Spacer(GlanceModifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${s.auraToday}", style = value.copy(fontSize = 26.sp))
                    Spacer(GlanceModifier.width(7.dp))
                    Text(
                        if (s.thresholdCleared) "threshold cleared" else "below threshold",
                        style = label.copy(
                            color = androidx.glance.unit.ColorProvider(if (s.thresholdCleared) Ok else Hot),
                        ),
                    )
                }
                Spacer(GlanceModifier.height(3.dp))
                Text("${s.toNextTier} to the next tier", style = label)
            }
        } }
    }
}

/** 2×2 — one number. The band is the whole game, so the widget shows where today sits in it. */
class AuraWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) = provideContent {
        val s = WidgetState()
        GlanceTheme { Shell {
            Column(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Text("AURA TODAY", style = label)
                Spacer(GlanceModifier.height(4.dp))
                Text("${s.auraToday}", style = value.copy(fontSize = 34.sp, color = androidx.glance.unit.ColorProvider(Soft)))
                Spacer(GlanceModifier.height(5.dp))
                Text("${s.toNextTier} to ${s.rank}", style = label)
            }
        } }
    }
}

/** 4×1 — tonight's gate. It is checked once after the window closes, so the widget only ever states the window. */
class NightGateWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) = provideContent {
        val s = WidgetState()
        GlanceTheme { Shell {
            Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Text("☾", style = value.copy(fontSize = 17.sp, color = androidx.glance.unit.ColorProvider(Soft)))
                Spacer(GlanceModifier.width(10.dp))
                Column {
                    Text("Night gate", style = value.copy(fontSize = 13.sp))
                    Text(s.nightGate, style = label)
                }
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    if (s.nightGatePending) "PENDING" else "HELD",
                    style = label.copy(
                        color = androidx.glance.unit.ColorProvider(if (s.nightGatePending) Soft else Ok),
                    ),
                )
            }
        } }
    }
}

/** 2×1 — the streak and the shields it bought. §5: a long streak buys insurance, so both numbers belong together. */
class StreakWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) = provideContent {
        val s = WidgetState()
        GlanceTheme { Shell {
            Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Text("${s.streakDays}d", style = value.copy(fontSize = 22.sp))
                Spacer(GlanceModifier.defaultWeight())
                Column(horizontalAlignment = Alignment.End) {
                    Text("SHIELDS", style = label)
                    Text("${s.shields} of 3", style = value.copy(fontSize = 12.sp, color = androidx.glance.unit.ColorProvider(Dim)))
                }
            }
        } }
    }
}

class DailyQuestReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyQuestWidget()
}

class AuraReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AuraWidget()
}

class NightGateReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NightGateWidget()
}

class StreakReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakWidget()
}
