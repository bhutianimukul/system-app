package app.gakseong.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.gakseong.ui.theme.LocalHunterClass
import app.gakseong.ui.theme.LocalMetrics
import app.gakseong.ui.theme.LocalPalette
import app.gakseong.ui.theme.LocalType
import app.gakseong.ui.theme.Radius
import app.gakseong.ui.theme.mix
import app.gakseong.ui.theme.pct
import kotlin.random.Random

// The component kit, ported from the design page. Every screen is the same layer stack, and the order is the
// stylesheet's z-index rather than its markup order: bg 0, bg2 0, aura 1, art 2, shade 3, grain 4, topfade 5,
// body 8, nav 9. Compose draws in declaration order, so screens call them in that sequence. Reading the HTML
// top to bottom gives the wrong answer, which cost one round of washed-out headers to find.

/** `clip-path: polygon(...)` from the stylesheet: a corner sliced off rather than rounded. */
class CutCorner(private val topLeft: Dp = 0.dp, private val bottomRight: Dp = 0.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val tl = with(density) { topLeft.toPx() }
        val br = with(density) { bottomRight.toPx() }
        val path = Path().apply {
            moveTo(tl, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - br)
            lineTo(size.width - br, size.height)
            lineTo(0f, size.height)
            lineTo(0f, tl)
            close()
        }
        return Outline.Generic(path)
    }
}

/** The screen root: the base colour every layer sits on. */
@Composable
fun Screen(content: @Composable BoxScope.() -> Unit) {
    val p = LocalPalette.current
    Box(Modifier.fillMaxSize().background(p.base), content = content)
}

/**
 * `.bg` — the mesh ground. Two large blurred blobs in the class's own mesh colours, not a flat radial.
 * Below API 31 `Modifier.blur` does nothing, which matches the design's own "degrade to static art" rule.
 */
@Composable
fun BoxScope.Bg() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    // The blobs are wider than the screen, and `fillMaxWidth(1.3f)` would be clamped back to it, so the size
    // comes from the measured box and goes through requiredSize to escape the incoming constraints.
    BoxWithConstraints(Modifier.matchParentSize()) {
        val w = maxWidth
        val h = maxHeight
        Box(
            Modifier
                .requiredSize(w * 1.30f, h * 0.56f)
                .offset(x = w * -0.22f, y = h * -0.14f)
                .blur(m.d(58), BlurredEdgeTreatment.Unbounded)
                .background(p.m1, CircleShape)
        )
        Box(
            Modifier
                .requiredSize(w * 1.20f, h * 0.48f)
                .offset(x = w * 0.10f, y = h * 0.24f)
                .blur(m.d(58), BlurredEdgeTreatment.Unbounded)
                .background(p.m2.copy(alpha = 0.95f), CircleShape)
        )
    }
}

/** `.bg2` — two soft radial washes rising from the bottom edge. */
@Composable
fun BoxScope.Bg2() {
    val p = LocalPalette.current
    val alpha = if (p.light) 0.22f else 1f
    Box(
        Modifier
            .matchParentSize()
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        0f to p.hot.pct(0.20f * alpha), 0.62f to Color.Transparent,
                        center = Offset(size.width * 0.5f, size.height * 1.04f),
                        radius = size.width * 1.2f,
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        0f to p.m2.copy(alpha = alpha), 0.64f to Color.Transparent,
                        center = Offset(size.width * 0.12f, size.height * 0.78f),
                        radius = size.width * 0.9f,
                    )
                )
            }
    )
}

/** `.topfade` — 26% of the screen, so the status bar always has something to sit on. */
@Composable
fun BoxScope.TopFade() {
    val p = LocalPalette.current
    Box(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.26f)
            .align(Alignment.TopStart)
            .background(
                Brush.verticalGradient(
                    0f to p.base.copy(alpha = 0.95f),
                    0.34f to p.base.copy(alpha = 0.77f),
                    0.66f to p.base.copy(alpha = 0.42f),
                    1f to Color.Transparent,
                )
            )
    )
}

/** `.aura` — one blurred accent ellipse. The only place the class colour is spent on atmosphere. */
@Composable
fun BoxScope.Aura(widthFraction: Float, heightFraction: Float, top: Float, left: Float, alpha: Float) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    BoxWithConstraints(Modifier.matchParentSize()) {
        Box(
            Modifier
                .requiredSize(maxWidth * widthFraction, maxHeight * heightFraction)
                .offset(x = maxWidth * left, y = maxHeight * top)
                .blur(m.d(46), BlurredEdgeTreatment.Unbounded)
                .background(p.hot.pct(0.92f * alpha), CircleShape)
        )
    }
}

/**
 * `.art` — class portraits and scene plates, composited with `mix-blend-mode: lighten` exactly as the design
 * does, which is why every portrait is generated on pure black.
 *
 * [feather] is `.art.feather`: the mask has to be baked into the bitmap rather than applied as a layer, because
 * Compose cannot composite an offscreen layer with a blend mode, and this art needs both.
 */
@Composable
fun BoxScope.Art(
    res: Int,
    top: Float,
    left: Float,
    width: Float,
    alpha: Float = 1f,
    blend: BlendMode = BlendMode.Lighten,
    feather: Boolean = false,
) {
    val loaded = ImageBitmap.imageResource(res)
    val image = if (feather) remember(loaded) { loaded.feathered() } else loaded
    Box(
        Modifier
            .matchParentSize()
            .drawBehind {
                val w = size.width * width
                val h = w * image.height / image.width
                drawImage(
                    image = image,
                    dstOffset = IntOffset((size.width * left).toInt(), (size.height * top).toInt()),
                    dstSize = IntSize(w.toInt(), h.toInt()),
                    alpha = alpha,
                    blendMode = blend,
                )
            }
    )
}

/**
 * `.shade` — the vertical gradient that hands the screen from art to text. Callers pass the dark stops from
 * their own markup as (position, alpha) pairs; light mode uses the single override the stylesheet defines for
 * every screen, so the art keeps its dark plate and the blend modes keep working on white.
 */
@Composable
fun BoxScope.Shade(vararg stops: Pair<Float, Float>) {
    val p = LocalPalette.current
    val brush = if (p.light) {
        Brush.verticalGradient(
            0f to Color.Transparent,
            0.26f to Color.Transparent,
            0.38f to p.base.copy(alpha = 0.30f),
            0.50f to p.base.copy(alpha = 0.90f),
            0.60f to p.base,
        )
    } else {
        Brush.verticalGradient(*stops.map { (at, a) -> at to p.base.copy(alpha = a) }.toTypedArray())
    }
    Box(Modifier.matchParentSize().background(brush))
}

/**
 * `.grain` — fractal noise at 26% over the whole screen.
 * ponytail: random grey rather than feTurbulence's fractal noise. At this opacity, under an overlay blend, the
 * difference is not visible. Swap in a real noise texture if a designer ever disagrees.
 */
@Composable
fun BoxScope.Grain() {
    val p = LocalPalette.current
    val noise = remember { noiseBitmap() }
    val brush = remember(noise) { ShaderBrush(ImageShader(noise, TileMode.Repeated, TileMode.Repeated)) }
    Box(
        Modifier
            .matchParentSize()
            .drawBehind {
                drawRect(
                    brush = brush,
                    alpha = if (p.light) 0.05f else 0.26f,
                    blendMode = if (p.light) BlendMode.Multiply else BlendMode.Overlay,
                )
            }
    )
}

/**
 * `mask-image: radial-gradient(ellipse 74% 64% at 50% 44%, #000 42%, transparent 88%)`, baked in.
 * ponytail: a circular gradient standing in for the ellipse. The two differ by a few percent of falloff on an
 * image that is already fading out; scale the canvas if that ever matters.
 */
private fun ImageBitmap.feathered(): ImageBitmap {
    val src = asAndroidBitmap()
    val out = android.graphics.Bitmap.createBitmap(src.width, src.height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(out)
    canvas.drawBitmap(src, 0f, 0f, null)
    val paint = android.graphics.Paint().apply {
        shader = android.graphics.RadialGradient(
            src.width * 0.5f,
            src.height * 0.44f,
            src.width * 0.74f,
            intArrayOf(android.graphics.Color.BLACK, android.graphics.Color.BLACK, android.graphics.Color.TRANSPARENT),
            floatArrayOf(0f, 0.42f, 0.88f),
            android.graphics.Shader.TileMode.CLAMP,
        )
        xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN)
    }
    canvas.drawRect(0f, 0f, src.width.toFloat(), src.height.toFloat(), paint)
    return out.asImageBitmap()
}

private fun noiseBitmap(size: Int = 170): ImageBitmap {
    val rng = Random(0x9A5)
    val pixels = IntArray(size * size) {
        val v = rng.nextInt(256)
        (0xFF shl 24) or (v shl 16) or (v shl 8) or v
    }
    return android.graphics.Bitmap
        .createBitmap(pixels, size, size, android.graphics.Bitmap.Config.ARGB_8888)
        .asImageBitmap()
}

/**
 * `.body` — 2.9rem of top padding for the status bar, 1.05rem at the sides, and it owns the vertical flow.
 * [navSpace] reserves the bottom bar's height, because in the markup `.nav` is a sibling of `.body` rather than
 * an overlay, so nothing should ever sit underneath it.
 */
@Composable
fun BoxScope.Body(navSpace: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val m = LocalMetrics.current
    Column(
        Modifier
            .matchParentSize()
            .padding(start = m.d(16.8), end = m.d(16.8), top = m.d(46.4))
            .then(if (navSpace) Modifier.padding(bottom = m.d(70)) else Modifier)
            .then(if (navSpace) Modifier.navigationBarsPadding() else Modifier),
        content = content,
    )
}

enum class QuestState { DONE, PENDING }

/**
 * `.qc` — a quest tile. The sliced bottom-right corner and the tick are the whole visual language of "the phone
 * checked this", so a done tile is never just a colour change.
 */
@Composable
fun QuestCard(
    icon: String,
    title: String,
    sub: String,
    value: String,
    state: QuestState,
    wide: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val done = state == QuestState.DONE
    val shape = CutCorner(bottomRight = m.d(11))
    val background: Brush =
        if (done) Brush.linearGradient(listOf(p.hot.pct(0.17f), Color.White.pct(0.027f)))
        else Brush.linearGradient(listOf(p.card, p.card))

    Box(
        modifier
            .clip(shape)
            .background(background)
            .border(1.dp, if (done) p.hot.pct(0.46f) else p.line, shape)
            .padding(m.d(12.8))
    ) {
        if (wide) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(11.2))) {
                Text(icon, style = t.md.copy(fontSize = m.s(18.4), color = p.ink))
                Column(Modifier.weight(1f)) {
                    Text(title, style = t.questTitle.copy(color = if (done) p.ink else p.dim))
                    Gap(4.8)
                    Text(sub.uppercase(), style = t.questSub)
                }
                Text(value, style = t.questValue.copy(color = if (done) p.soft else p.faint))
                Tick(done)
            }
        } else {
            Column {
                Text(icon, style = t.md.copy(fontSize = m.s(18.4), color = p.ink))
                Gap(8)
                Text(title, style = t.questTitle.copy(color = if (done) p.ink else p.dim))
                Gap(4.8)
                Text(sub.uppercase(), style = t.questSub)
                Gap(8)
                Text(value, style = t.questValue.copy(color = if (done) p.soft else p.faint))
            }
            Box(Modifier.align(Alignment.TopEnd)) { Tick(done) }
        }
    }
}

@Composable
private fun Tick(done: Boolean) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val shape = CutCorner(topLeft = m.d(5), bottomRight = m.d(5))
    Box(
        Modifier
            .size(m.d(20))
            .clip(shape)
            .background(if (done) p.hot else Color.Transparent)
            .border(1.dp, if (done) p.hot else p.line2, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Text("✓", style = LocalType.current.pill.copy(fontSize = m.s(9), color = Color(0xFF07080C)))
        }
    }
}

@Composable
fun ColumnScope.Filler() = Spacer(Modifier.weight(1f))

@Composable
fun RowScope.Filler() = Spacer(Modifier.weight(1f))

@Composable
fun Gap(designPx: Number) = Spacer(Modifier.height(LocalMetrics.current.d(designPx)))

/** `.wm` — hangul first, latin as a spaced-out sub-label. The mark, never the voice. */
@Composable
fun Wordmark() {
    val t = LocalType.current
    val m = LocalMetrics.current
    Column(verticalArrangement = Arrangement.spacedBy(m.d(1.6))) {
        Text("각성", style = t.wordmarkHangul)
        Text("GAKSEONG", style = t.wordmarkLatin)
    }
}

/** `.eye` — the accent-coloured section label, with its glowing 4px dot. */
@Composable
fun Eye(text: String) {
    val t = LocalType.current
    val m = LocalMetrics.current
    val p = LocalPalette.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(m.d(6))) {
        Box(Modifier.size(m.d(4)).background(p.hot, CircleShape))
        Text(text.uppercase(), style = t.eye)
    }
}

@Composable
fun Tag(text: String, style: TextStyle = LocalType.current.tag) = Text(text.uppercase(), style = style)

/** `.card`, `.card.big`, `.card.lit`. */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    big: Boolean = false,
    lit: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val shape = RoundedCornerShape(m.d(if (big) Radius.BIG else Radius.CARD))
    val background: Brush =
        if (lit) Brush.linearGradient(listOf(p.hot.pct(0.13f), Color.White.pct(0.02f)))
        else Brush.linearGradient(listOf(p.card, p.card))
    Column(
        modifier
            .clip(shape)
            .background(background)
            .border(1.dp, if (lit) p.hot.pct(0.38f) else p.line, shape)
            .padding(m.d(13)),
        content = content,
    )
}

/** `.cta` — one filled accent button per screen, and never two. */
@Composable
fun Cta(text: String, ghost: Boolean = false, bad: Boolean = false, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val fill = when {
        ghost -> Color.Transparent
        bad -> app.gakseong.ui.theme.Bad
        else -> p.hot
    }
    Box(
        modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(fill)
            .then(if (ghost) Modifier.border(1.dp, p.line2, CircleShape) else Modifier)
            .padding(vertical = m.d(13.6)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text.uppercase(),
            style = t.cta.copy(
                color = if (ghost) p.faint else Color(0xFF07080C),
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
fun Pill(text: String, on: Boolean = false) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Box(
        Modifier
            .clip(CircleShape)
            .background(if (on) p.hot.mix(Color(0xFF12162B), 0.30f) else p.pill)
            .border(1.dp, if (on) p.hot.pct(0.62f) else p.line2, CircleShape)
            .padding(horizontal = m.d(10.9), vertical = m.d(5.4)),
    ) {
        Text(text.uppercase(), style = t.pill)
    }
}

/** `.plate` — a translucent slab for one line of System reasoning. */
@Composable
fun Plate(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val shape = RoundedCornerShape(m.d(14))
    Column(
        modifier
            .clip(shape)
            .background(p.plate)
            .border(1.dp, if (p.light) p.line else Color.White.pct(0.08f), shape)
            .padding(horizontal = m.d(12.8), vertical = m.d(9.6)),
        content = content,
    )
}

/**
 * `.sys` — the System's own window. Clipped corners, a glowing accent bevel, two corner marks and a scanline
 * wash. This is the one component that must never be mistaken for a card: cards hold facts, this one speaks.
 */
@Composable
fun SystemWindow(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val shape = CutCorner(topLeft = m.d(11), bottomRight = m.d(11))
    Box(modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(p.hot.mix(p.sysBase, 0.12f), p.sysFade),
                    )
                )
                .border(1.dp, p.hot.pct(0.40f), shape)
                .padding(horizontal = m.d(14.4), vertical = m.d(13.6)),
            content = content,
        )
        // ::before and ::after — a 22px glowing hairline at the top-left and the bottom-right.
        Box(Modifier.align(Alignment.TopStart).width(m.d(22)).height(1.dp).background(p.hot))
        Box(Modifier.align(Alignment.BottomEnd).width(m.d(22)).height(1.dp).background(p.hot))
        Marks()
    }
}

/** `.marks` — 7px corner brackets, top-right and bottom-left. */
@Composable
private fun BoxScope.Marks() {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val c = p.hot.pct(0.80f)
    val s = m.d(7)
    Box(
        Modifier
            .align(Alignment.TopEnd)
            .padding(m.d(4))
            .size(s)
            .drawBehind {
                drawLine(c, Offset(0f, 0f), Offset(size.width, 0f))
                drawLine(c, Offset(size.width, 0f), Offset(size.width, size.height))
            }
    )
    Box(
        Modifier
            .align(Alignment.BottomStart)
            .padding(m.d(4))
            .size(s)
            .drawBehind {
                drawLine(c, Offset(0f, size.height), Offset(size.width, size.height))
                drawLine(c, Offset(0f, 0f), Offset(0f, size.height))
            }
    )
}

/** `.sig` — a class sigil, keyed onto the background and tinted with the accent. */
@Composable
fun Sig(size: Number = 22) {
    val hunter = LocalHunterClass.current
    val m = LocalMetrics.current
    val image = ImageBitmap.imageResource(hunter.sigil)
    Box(
        Modifier.size(m.d(size)).drawBehind {
            drawImage(
                image = image,
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(this.size.width.toInt(), this.size.height.toInt()),
                colorFilter = ColorFilter.tint(hunter.hot, BlendMode.Modulate),
                blendMode = BlendMode.Lighten,
            )
        }
    )
}

/** `.xl` — the one big number per screen, filled with a white-to-accent gradient. */
@Composable
fun XlNumber(text: String, designPx: Number? = null) {
    val p = LocalPalette.current
    val t = LocalType.current
    val m = LocalMetrics.current
    Text(
        text,
        style = (if (designPx == null) t.xl else t.xl.copy(fontSize = m.s(designPx))).copy(
            brush = Brush.linearGradient(
                0f to Color.White, 0.52f to p.soft, 1f to p.hot,
            )
        ),
    )
}

/** `.meter` with its `i` fill and `u` threshold marker. */
@Composable
fun Meter(fill: Float, marker: Float) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    Box(Modifier.fillMaxWidth().height(m.d(18)), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(m.d(10))
                .clip(CircleShape)
                .background(p.meterTrack)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fill)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(p.deep, p.hot)))
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(m.d(18))
                .drawBehind {
                    val x = size.width * marker
                    drawRoundRect(
                        color = p.ink.pct(0.6f),
                        topLeft = Offset(x, 0f),
                        size = Size(with(this) { m.d(2).toPx() }, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
                    )
                }
        )
    }
}

/** `.nav` — five destinations, the middle one raised. Raid takes the centre. */
@Composable
fun BoxScope.BottomNav(active: Int) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    val items = listOf("◈" to "Quest", "◎" to "Gates", "⚔" to "Raid", "❖" to "Guild", "◭" to "Self")
    Column(Modifier.align(Alignment.BottomCenter)) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(p.navBar)
                .drawBehind { drawLine(p.line, Offset(0f, 0f), Offset(size.width, 0f)) }
                .padding(start = m.d(6.4), end = m.d(6.4), top = m.d(9.6), bottom = m.d(13.6)),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom,
        ) {
            items.forEachIndexed { i, (glyph, label) ->
                val on = i == active
                val mid = i == 2
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(m.d(3.2)),
                ) {
                    if (mid) {
                        Box(
                            Modifier
                                .offset(y = m.d(-15))
                                .size(m.d(36))
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(p.hot, p.deep))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(glyph, style = t.md.copy(fontSize = m.s(14.4), color = p.base))
                        }
                    } else {
                        Text(glyph, style = t.nav.copy(fontSize = m.s(13), color = if (on) p.hot else p.ghost))
                    }
                    Text(
                        label.uppercase(),
                        style = t.nav.copy(color = if (mid) p.soft else if (on) p.hot else p.ghost),
                    )
                }
            }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

/** A `.shade` whose CSS is a radial rather than a linear gradient. */
@Composable
fun BoxScope.ShadeRadial(
    centerX: Float,
    centerY: Float,
    radiusFraction: Float,
    vararg stops: Pair<Float, Float>,
) {
    val p = LocalPalette.current
    Box(
        Modifier
            .matchParentSize()
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colorStops = stops.map { (at, a) -> at to p.base.copy(alpha = a) }.toTypedArray(),
                        center = Offset(size.width * centerX, size.height * centerY),
                        radius = size.width * radiusFraction,
                    )
                )
            }
    )
}

/**
 * The focus session's progress ring: the SVG in the markup, as an arc. Stroke 3 of a 100 viewBox on a 212px
 * box, and `stroke-dasharray:283 stroke-dashoffset:88` is 69% elapsed.
 */
@Composable
fun RingTimer(progress: Float, time: String, of: String) {
    val p = LocalPalette.current
    val m = LocalMetrics.current
    val t = LocalType.current
    Box(Modifier.size(m.d(212)), contentAlignment = Alignment.Center) {
        Box(
            Modifier.matchParentSize().drawBehind {
                val stroke = size.width * 0.03f
                val inset = stroke / 2 + size.width * 0.05f
                val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                drawArc(
                    color = Color.White.pct(0.07f),
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize,
                    style = Stroke(stroke),
                )
                drawArc(
                    color = p.hot,
                    startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            XlNumber(time, designPx = 46.4)
            Gap(8)
            Text(of.uppercase(), style = t.tag.copy(color = p.ink, letterSpacing = 0.32.em))
        }
    }
}

/** `.cut` and `.cutsq` — circular and near-square radial masks, baked in for the same reason `.art.feather` is. */
enum class Mask { CIRCLE, SQUARISH }

@Composable
fun MaskedImage(res: Int, width: Number, height: Number = width, mask: Mask = Mask.CIRCLE, keyed: Boolean = false) {
    val m = LocalMetrics.current
    val loaded = ImageBitmap.imageResource(res)
    val image = remember(loaded, mask) {
        when (mask) {
            // `radial-gradient(circle at 50% 48%, ...)` with no size keyword resolves to farthest-corner, so on a
            // square image the gradient radius is hypot(w/2, h/2) and the visible disc is well inside the frame.
            // Treating the radius as half the width leaves the corners opaque, which reads as a black box.
            Mask.CIRCLE -> loaded.masked(cy = 0.48f, radius = FARTHEST_CORNER, inner = 0.52f, outer = 0.74f)
            Mask.SQUARISH -> loaded.masked(cy = 0.50f, radius = 0.62f, inner = 0.58f, outer = 0.82f)
        }
    }
    Box(
        Modifier.size(m.d(width), m.d(height)).drawBehind {
            val w = size.width
            val h = w * image.height / image.width
            drawImage(
                image = image,
                dstOffset = IntOffset(0, ((size.height - h) / 2).toInt()),
                dstSize = IntSize(w.toInt(), h.toInt()),
                blendMode = if (keyed) BlendMode.Lighten else BlendMode.SrcOver,
            )
        }
    )
}

/** `[data-stats]` — STR, AGI, VIT, INT with their gains, divided by hairlines, inside a System window. */
@Composable
fun StatsRow(values: List<Int>, gains: List<Int>) {
    val p = LocalPalette.current
    val t = LocalType.current
    val names = listOf("STR", "AGI", "VIT", "INT")
    Row(Modifier.fillMaxWidth()) {
        names.forEachIndexed { i, name ->
            Column(
                Modifier
                    .weight(1f)
                    .then(
                        if (i > 0) Modifier.drawBehind {
                            drawLine(p.line, Offset(0f, 0f), Offset(0f, size.height))
                        } else Modifier
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(name, style = t.tag)
                Gap(4.5)
                Text("${values[i]}", style = t.lg(21.6))
                Gap(2.6)
                Text("+${gains[i]}", style = t.tag.copy(color = p.soft))
            }
        }
    }
}

/** Sentinel for "size this gradient to the farthest corner", which is the CSS default. */
private const val FARTHEST_CORNER = -1f

private fun ImageBitmap.masked(cy: Float, radius: Float, inner: Float, outer: Float): ImageBitmap {
    val src = asAndroidBitmap()
    val out = android.graphics.Bitmap.createBitmap(src.width, src.height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(out)
    canvas.drawBitmap(src, 0f, 0f, null)
    val r = if (radius == FARTHEST_CORNER) {
        kotlin.math.hypot(src.width * 0.5, src.height * 0.5).toFloat()
    } else {
        src.width * radius
    }
    val paint = android.graphics.Paint().apply {
        shader = android.graphics.RadialGradient(
            src.width * 0.5f,
            src.height * cy,
            r,
            intArrayOf(android.graphics.Color.BLACK, android.graphics.Color.BLACK, android.graphics.Color.TRANSPARENT),
            floatArrayOf(0f, inner, outer),
            android.graphics.Shader.TileMode.CLAMP,
        )
        xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN)
    }
    canvas.drawRect(0f, 0f, src.width.toFloat(), src.height.toFloat(), paint)
    return out.asImageBitmap()
}

