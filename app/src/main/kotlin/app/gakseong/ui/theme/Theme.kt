package app.gakseong.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * The design page draws the phone at `width:min(330px,92vw)` with a 9:19.5 screen, and every size in that
 * stylesheet is relative to a 16px root. So the whole design is proportional to its own width, and a real
 * screen reproduces it exactly by scaling every dimension by `screenWidth / 330`.
 *
 * [d] takes design pixels and returns device-independent pixels. [s] does the same for text, in sp rather than
 * dp so the reader's font-size setting still applies. Exactness is not worth breaking text scaling for.
 */
@Immutable
class Metrics(val scale: Float) {
    fun d(designPx: Number): Dp = (designPx.toFloat() * scale).dp
    fun s(designPx: Number): TextUnit = (designPx.toFloat() * scale).sp

    companion object {
        const val DESIGN_WIDTH = 330f
    }
}

@Immutable
class Palette(
    val ink: Color,
    val dim: Color,
    val faint: Color,
    val ghost: Color,
    val line: Color,
    val line2: Color,
    val card: Color,
    val card2: Color,
    val base: Color,
    val sysBase: Color,
    val sysFade: Color,
    val pill: Color,
    val plate: Color,
    val navBar: Color,
    val meterTrack: Color,
    val hot: Color,
    val soft: Color,
    val deep: Color,
    val m1: Color,
    val m2: Color,
    val light: Boolean,
)

/** Text shadows exist in the stylesheet because most of this type sits over art. They are not decoration. */
@Immutable
class GakType(private val m: Metrics, private val p: Palette) {
    private val mono = FontFamily.Monospace
    private val overArt = if (p.light) null else Shadow(Color(0xCC000000), Offset(0f, 1f), 8f)

    val wordmarkHangul = TextStyle(
        fontSize = m.s(18.4), fontWeight = FontWeight(700), letterSpacing = 0.02.em,
        color = if (p.light) p.deep else p.ink,
    )
    val wordmarkLatin = TextStyle(
        fontFamily = mono, fontSize = m.s(8), fontWeight = FontWeight(700), letterSpacing = 0.34.em,
        color = p.hot.mix(p.ink, 0.88f),
    )
    val tag = TextStyle(
        fontFamily = mono, fontSize = m.s(8), letterSpacing = 0.28.em, color = p.faint, shadow = overArt,
    )
    val key = TextStyle(
        fontFamily = mono, fontSize = m.s(7.36), letterSpacing = 0.16.em, color = p.faint,
    )
    val eye = TextStyle(
        fontFamily = mono, fontSize = m.s(8), letterSpacing = 0.28.em, color = p.hot,
    )
    val body = TextStyle(
        fontSize = m.s(13), lineHeight = 1.6.em, color = p.dim, shadow = overArt,
    )
    val md = TextStyle(
        fontSize = m.s(19.2), fontWeight = FontWeight(820), letterSpacing = (-0.035).em, color = p.ink,
    )
    val xl = TextStyle(
        fontSize = m.s(62.4), fontWeight = FontWeight(900), letterSpacing = (-0.07).em, lineHeight = 0.86.em,
    )
    val cta = TextStyle(
        fontFamily = mono, fontSize = m.s(9.28), letterSpacing = 0.22.em, fontWeight = FontWeight(800),
    )
    val pill = TextStyle(
        fontFamily = mono, fontSize = m.s(8), letterSpacing = 0.16.em, fontWeight = FontWeight(600), color = p.ink,
    )
    val nav = TextStyle(
        fontFamily = mono, fontSize = m.s(6.72), letterSpacing = 0.14.em, color = p.ghost,
    )
    val questTitle = TextStyle(
        fontSize = m.s(13.12), fontWeight = FontWeight(660), letterSpacing = (-0.02).em, lineHeight = 1.22.em,
        color = p.ink,
    )
    val questSub = TextStyle(
        fontFamily = mono, fontSize = m.s(7.04), letterSpacing = 0.14.em, color = p.faint,
    )
    val questValue = TextStyle(
        fontFamily = mono, fontSize = m.s(9.92), fontWeight = FontWeight(700), color = p.soft,
    )
    val monoSmall = TextStyle(
        fontFamily = mono, fontSize = m.s(9.92), fontWeight = FontWeight(700), color = p.soft,
    )
    val listItem = TextStyle(fontSize = m.s(13.28), fontWeight = FontWeight(650), color = p.ink)

    /** `.disp` is 900 weight, uppercase and horizontally squeezed. Size varies per screen, so it is a function. */
    fun display(designPx: Number) = TextStyle(
        fontSize = m.s(designPx), fontWeight = FontWeight(900), letterSpacing = (-0.055).em, lineHeight = 0.92.em,
        color = p.ink,
    )
}

val LocalMetrics = staticCompositionLocalOf<Metrics> { error("no Metrics") }
val LocalPalette = staticCompositionLocalOf<Palette> { error("no Palette") }
val LocalType = staticCompositionLocalOf<GakType> { error("no GakType") }
val LocalHunterClass = staticCompositionLocalOf { HunterClass.ASSASSIN }

/** Radii from the stylesheet: `--r`, `--r2`, `--r3`. */
object Radius {
    const val BIG = 22
    const val CARD = 16
    const val SMALL = 11
}

@Composable
fun GakseongTheme(
    hunterClass: HunterClass = HunterClass.ASSASSIN,
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val widthDp = LocalConfiguration.current.screenWidthDp.toFloat()
    val metrics = Metrics(widthDp / Metrics.DESIGN_WIDTH)

    val palette = if (dark) {
        Palette(
            ink = DarkTokens.ink, dim = DarkTokens.dim, faint = DarkTokens.faint, ghost = DarkTokens.ghost,
            line = DarkTokens.line, line2 = DarkTokens.line2, card = DarkTokens.card, card2 = DarkTokens.card2,
            base = DarkTokens.base, sysBase = DarkTokens.sysBase, sysFade = DarkTokens.sysFade,
            pill = DarkTokens.pill, plate = DarkTokens.plate, navBar = DarkTokens.navBar,
            meterTrack = DarkTokens.meterTrack,
            hot = hunterClass.hot, soft = hunterClass.soft, deep = hunterClass.deep,
            m1 = hunterClass.m1, m2 = hunterClass.m2, light = false,
        )
    } else {
        Palette(
            ink = LightTokens.ink, dim = LightTokens.dim, faint = LightTokens.faint, ghost = LightTokens.ghost,
            line = LightTokens.line, line2 = LightTokens.line2, card = LightTokens.card, card2 = LightTokens.card2,
            base = LightTokens.base, sysBase = LightTokens.sysBase, sysFade = LightTokens.sysFade,
            pill = LightTokens.pill, plate = LightTokens.plate, navBar = LightTokens.navBar,
            meterTrack = LightTokens.meterTrack,
            hot = hunterClass.hot, soft = hunterClass.soft, deep = hunterClass.deep,
            m1 = hunterClass.m1, m2 = hunterClass.m2, light = true,
        )
    }
    CompositionLocalProvider(
        LocalMetrics provides metrics,
        LocalPalette provides palette,
        LocalType provides GakType(metrics, palette),
        LocalHunterClass provides hunterClass,
        content = content,
    )
}
