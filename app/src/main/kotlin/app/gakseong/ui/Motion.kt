package app.gakseong.ui

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

// Motion and haptics. The design page's own transition is
// `@keyframes pgIn{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:none}}` at
// `.4s cubic-bezier(.16,1,.3,1)`, so those exact numbers are what a screen change uses here.

// Internal rather than private: `ui/Nav.kt` drives the NavHost with these same numbers, and two copies of a
// curve is two chances for the app to animate differently from the design page.
internal val PgIn = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
internal const val PG_IN_MS = 400

/**
 * True when the reader has turned animations off. Checked rather than assumed, because a splash or transition
 * that ignores this is an accessibility problem, not a style choice.
 */
fun animationsEnabled(context: Context): Boolean =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f

/**
 * Wraps whatever screen [key] selects and animates the change. A screen switch also ticks: the System should be
 * felt as well as seen, and `VibrationEffect` composition primitives fire even when the phone is on silent.
 */
@Composable
fun ScreenTransition(key: Any, content: @Composable (Any) -> Unit) {
    val context = LocalContext.current
    val animate = remember { animationsEnabled(context) }
    val haptics = rememberHaptics()

    LaunchedEffect(key) { haptics.tick() }

    if (!animate) {
        content(key)
        return
    }
    val offset = with(LocalDensity.current) { 8.dp.roundToPx() }
    AnimatedContent(
        targetState = key,
        transitionSpec = {
            (
                fadeIn(tween(PG_IN_MS, easing = PgIn)) +
                    slideInVertically(tween(PG_IN_MS, easing = PgIn)) { offset }
                ) togetherWith fadeOut(tween(PG_IN_MS / 2))
        },
        label = "screen",
    ) { current ->
        content(current)
    }
}

/** Composition primitives where the device has them, a one-shot where it does not. */
class Haptics(private val vibrator: Vibrator?) {

    /** A screen change, a selection, a tap that landed. */
    fun tick() = primitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.6f, fallbackMs = 12)

    /** A quest cleared, a rank taken. Heavier, and used sparingly so it keeps meaning something. */
    fun thud() = primitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1f, fallbackMs = 28)

    private fun primitive(primitive: Int, scale: Float, fallbackMs: Long) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && v.areAllPrimitivesSupported(primitive)) {
            v.vibrate(VibrationEffect.startComposition().addPrimitive(primitive, scale).compose())
        } else {
            v.vibrate(VibrationEffect.createOneShot(fallbackMs, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val context = LocalContext.current
    return remember {
        val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        Haptics(v)
    }
}
