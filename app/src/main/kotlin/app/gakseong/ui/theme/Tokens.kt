package app.gakseong.ui.theme

import androidx.compose.ui.graphics.Color
import app.gakseong.R

// Ported verbatim from the design page's CSS custom properties. CSS writes #rrggbbaa, Compose takes 0xAARRGGBB,
// so #FFFFFF1F becomes 0x1FFFFFFF. Every value here exists in that stylesheet; none was invented.

/** Blend [pct] of [this] into [onto], the way CSS `color-mix(in srgb, X n%, Y)` does. */
fun Color.mix(onto: Color, pct: Float): Color = Color(
    red = red * pct + onto.red * (1 - pct),
    green = green * pct + onto.green * (1 - pct),
    blue = blue * pct + onto.blue * (1 - pct),
    alpha = alpha * pct + onto.alpha * (1 - pct),
)

/** `color-mix(in srgb, X n%, transparent)`, which is just X at n% alpha. */
fun Color.pct(pct: Float): Color = copy(alpha = alpha * pct)

val Ok = Color(0xFF4AE3A0)
val Warn = Color(0xFFFFB84D)
val Bad = Color(0xFFFF4D6D)

/** The dark surround the art sits on. Also the base of every screen in dark mode. */
val ArtPlate = Color(0xFF0B0D1A)

object DarkTokens {
    val ink = Color(0xFFFFFFFF)
    val dim = Color(0xFFC6CCDD)
    val faint = Color(0xFF8B93A8)
    val ghost = Color(0xFF525A70)
    val line = Color(0x1FFFFFFF)
    val line2 = Color(0x2EFFFFFF)
    val card = Color(0x12FFFFFF)
    val card2 = Color(0x1FFFFFFF)
    val base = Color(0xFF0B0D1A)
    val sysBase = Color(0xFF10131F)
    val sysFade = Color(0xE60E1120)
    val pill = Color(0xD912162B)
    val plate = Color(0x9E0B0D1A)
    val navBar = Color(0xD90B0D1A)
    val meterTrack = Color(0x0FFFFFFF)
}

object LightTokens {
    val ink = Color(0xFF12141F)
    val dim = Color(0xFF4B5266)
    val faint = Color(0xFF7C8398)
    val ghost = Color(0xFFB4BAC9)
    val line = Color(0x140B0D16)
    val line2 = Color(0x260B0D16)
    val card = Color(0xCCFFFFFF)
    val card2 = Color(0xFFFFFFFF)
    val base = Color(0xFFFBFBFE)
    val sysBase = Color(0xFFFFFFFF)
    val sysFade = Color(0xFFFFFFFF)
    val pill = Color(0xFFFFFFFF)
    val plate = Color(0xE6FFFFFF)
    val navBar = Color(0xE6FBFBFE)
    val meterTrack = Color(0x140B0D16)
}

/**
 * One accent variable per class, spent on a soft aura and one filled button. Hairlines stay neutral.
 * [m1] and [m2] are the two blurred mesh blobs behind every screen.
 */
enum class HunterClass(
    val label: String,
    val hot: Color,
    val soft: Color,
    val deep: Color,
    val m1: Color,
    val m2: Color,
    val portrait: Int,
    val sigil: Int,
) {
    FIGHTER("Fighter", Color(0xFFFF6A2C), Color(0xFFFFB894), Color(0xFF7A2408), Color(0xFF5A2410), Color(0xFF2E1440), R.drawable.p_fighter, R.drawable.sg_fighter_fist),
    RANGER("Ranger", Color(0xFF2EE8A0), Color(0xFFA6F7D6), Color(0xFF0A6B4A), Color(0xFF0C4A3A), Color(0xFF123048), R.drawable.p_ranger, R.drawable.sg_ranger_arrowhead),
    SAGE("Sage", Color(0xFFA96CFF), Color(0xFFD9BEFF), Color(0xFF4A1690), Color(0xFF3E1878), Color(0xFF1E1852), R.drawable.p_sage, R.drawable.sg_sage_rings),
    HEALER("Healer", Color(0xFFFFD98A), Color(0xFFFFF1D4), Color(0xFF8A6320), Color(0xFF4A3616), Color(0xFF241E44), R.drawable.p_healer, R.drawable.sg_healer_halo),
    ASSASSIN("Assassin", Color(0xFFFF48D0), Color(0xFFFFAEEC), Color(0xFF7A0C62), Color(0xFF5A0E48), Color(0xFF241650), R.drawable.p_assassin, R.drawable.sg_assassin_dagger),
    TANKER("Tanker", Color(0xFF4FD4FF), Color(0xFFB8ECFF), Color(0xFF0E5C82), Color(0xFF0E4468), Color(0xFF122A50), R.drawable.p_tanker, R.drawable.sg_tanker_shield),
    ENVOY("Envoy", Color(0xFFFFAE45), Color(0xFFFFDCA8), Color(0xFF8A4E08), Color(0xFF5A3208), Color(0xFF281E42), R.drawable.p_envoy, R.drawable.sg_envoy_palm),
}
