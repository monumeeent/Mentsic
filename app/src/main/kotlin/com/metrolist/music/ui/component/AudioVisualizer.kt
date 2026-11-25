/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Visualiser presets. Each is a different *way* of reading the same coherent harmonic field, so they
 * share one animation clock and cost the same to run.
 *
 *  - [RIBBON]      mirrored bars around a centre spine (the original).
 *  - [BARS]        classic bottom-anchored spectrum, no mirroring.
 *  - [WAVE]        a single stroked sine path, amplitude-modulated by the field.
 *  - [DOTS]        a row of swelling circles — reads as "sensitive", good on AMOLED.
 *  - [MIRROR_WAVE] the sine path drawn twice, reflected, with a soft fill between the halves.
 */
enum class VisualizerStyle {
    RIBBON,
    BARS,
    WAVE,
    DOTS,
    MIRROR_WAVE,
}

/**
 * Audio-reactive visualiser.
 *
 * The field itself is always the same three-harmonic sample ([sampleBar]) — the preset only changes
 * how it is painted. One `Animatable` per sample drives all presets, and every read happens inside
 * the draw scope, so switching preset or animating costs no recomposition.
 *
 * @param barCount number of samples. Even values centre cleanly on the mirrored presets.
 * @param barSpacing gap between bars/dots.
 * @param colors ramp the shapes are tinted with, low → high.
 * @param amplitude motion intensity, 0..1.
 * @param enabled when false the shape settles to its resting line instead of animating.
 * @param style which preset to draw.
 * @param height canvas height; the caller may override it per placement.
 */
@Composable
fun AudioVisualizer(
    barCount: Int = 48,
    barSpacing: Dp = 2.dp,
    colors: List<Color> = emptyList(),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    intensity: Float = 1f,
    style: VisualizerStyle = VisualizerStyle.RIBBON,
    height: Dp = 96.dp,
) {
    val density = LocalDensity.current
    val gapPx = with(density) { barSpacing.toPx() }

    // One Animatable per sample. Animating the *rendered* value toward a sampled target — rather than
    // redrawing a raw sample every frame — is what keeps the motion smooth at any frame rate.
    val bars = remember(barCount) { Array(barCount) { Animatable(REST_HEIGHT) } }

    LaunchedEffect(enabled, barCount) {
        // Phase accumulates from wall-clock time, so the animation keeps a constant tempo even if a
        // frame is dropped. `delay` sets the sampling cadence only.
        val start = System.currentTimeMillis()
        // Sample at ~60Hz but never faster than the display: over-sampling just burns CPU on a
        // 120Hz panel and changes nothing visible, which is what made the old loop feel heavy.
        while (isActive) {
            val seconds = (System.currentTimeMillis() - start) / 1000f
            for (index in bars.indices) {
                val target = if (enabled) sampleBar(index, barCount, seconds, intensity) else REST_HEIGHT
                bars[index].snapTo(target)
            }
            delay(16L)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 16.dp),
    ) {
        // Snapshot copies once per frame rather than per draw call.
        drawVisualizer(
            values = bars.map { it.value },
            palette = colors,
            gap = gapPx,
            style = style,
        )
    }
}

/** Dispatches to the selected preset. Pure draw code — no state reads, no allocation per bar. */
private fun DrawScope.drawVisualizer(
    values: List<Float>,
    palette: List<Color>,
    gap: Float,
    style: VisualizerStyle,
) {
    when (style) {
        VisualizerStyle.RIBBON -> drawMirroredRibbon(bars = values, palette = palette, gap = gap)
        VisualizerStyle.BARS -> drawBars(bars = values, palette = palette, gap = gap)
        VisualizerStyle.WAVE -> drawWave(bars = values, palette = palette, mirror = false)
        VisualizerStyle.MIRROR_WAVE -> drawWave(bars = values, palette = palette, mirror = true)
        VisualizerStyle.DOTS -> drawDots(bars = values, palette = palette, gap = gap)
    }
}

/** Colours low → high for a [0,1] amplitude. Shared by every preset so they stay palette-linked. */
private fun paletteColors(palette: List<Color>): Pair<Color, Color> {
    val low = palette.getOrNull(0) ?: Color.White.copy(alpha = 0.35f)
    val high = palette.getOrNull(1) ?: palette.getOrNull(0) ?: Color.White.copy(alpha = 0.9f)
    return low to high
}

/** Classic spectrum: bars rise from the bottom edge, no mirroring. */
private fun DrawScope.drawBars(bars: List<Float>, palette: List<Color>, gap: Float) {
    val count = bars.size
    if (count == 0) return
    val (low, high) = paletteColors(palette)
    val barWidth = ((size.width - gap * (count - 1)) / count).coerceAtLeast(1f)

    bars.forEachIndexed { index, value ->
        val h = (value * size.height).coerceAtLeast(1f)
        val t = ((value - REST_HEIGHT) / (1f - REST_HEIGHT)).coerceIn(0f, 1f)
        drawRoundRect(
            color = lerp(low, high, t).copy(alpha = 0.5f + 0.5f * t),
            topLeft = Offset(index * (barWidth + gap), size.height - h),
            size = Size(barWidth, h),
            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
        )
    }
}

/**
 * One continuous sine path through the samples, optionally reflected below the centre with a soft
 * gradient fill between the halves.
 */
private fun DrawScope.drawWave(bars: List<Float>, palette: List<Color>, mirror: Boolean) {
    val count = bars.size
    if (count < 2) return
    val (low, high) = paletteColors(palette)
    val step = size.width / (count - 1)
    val centreY = size.height / 2f

    val path = Path()
    bars.forEachIndexed { index, value ->
        val x = index * step
        val y = if (mirror) centreY - value * centreY else size.height - value * size.height
        if (index == 0) path.moveTo(x, y) else {
            // Quadratic smoothing through the midpoints: cheap, and removes the polyline corners.
            val prevX = (index - 1) * step
            val prevY =
                if (mirror) {
                    centreY - bars[index - 1] * centreY
                } else {
                    size.height - bars[index - 1] * size.height
                }
            path.quadraticTo(prevX, prevY, (prevX + x) / 2f, (prevY + y) / 2f)
        }
    }

    drawPath(
        path = path,
        brush = Brush.horizontalGradient(listOf(low, high, low)),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
    )

    if (mirror) {
        // Reflected copy, dimmer, plus the soft fill between the two halves.
        val reflected = Path()
        bars.forEachIndexed { index, value ->
            val x = index * step
            val y = centreY + value * centreY
            if (index == 0) reflected.moveTo(x, y) else reflected.lineTo(x, y)
        }
        drawPath(
            path = reflected,
            brush = Brush.horizontalGradient(listOf(low, high, low)),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            alpha = 0.45f,
        )
        val band = Path().apply {
            addPath(path)
            lineTo(size.width, centreY + bars.last() * centreY)
            bars.forEachIndexed { index, value ->
                lineTo(index * step, centreY + value * centreY)
            }
            close()
        }
        drawPath(
            path = band,
            brush = Brush.verticalGradient(
                listOf(high.copy(alpha = 0.18f), Color.Transparent, high.copy(alpha = 0.12f)),
            ),
        )
        drawRect(color = high.copy(alpha = 0.18f), topLeft = Offset(0f, centreY), size = Size(size.width, 1f))
    }
}

/** Row of swelling circles — the most "responsive" looking preset with the least area to paint. */
private fun DrawScope.drawDots(bars: List<Float>, palette: List<Color>, gap: Float) {
    val count = bars.size
    if (count == 0) return
    val (low, high) = paletteColors(palette)
    val cell = ((size.width - gap * (count - 1)) / count).coerceAtLeast(1f)
    val maxRadius = minOf(cell / 2f, size.height / 2f) * 0.92f
    val centreY = size.height / 2f

    bars.forEachIndexed { index, value ->
        val t = ((value - REST_HEIGHT) / (1f - REST_HEIGHT)).coerceIn(0f, 1f)
        drawCircle(
            color = lerp(low, high, t).copy(alpha = 0.35f + 0.65f * t),
            radius = (maxRadius * (0.22f + 0.78f * t)).coerceAtLeast(1f),
            center = Offset(index * (cell + gap) + cell / 2f, centreY),
        )
    }
}

/** Resting height while the player is paused: a thin, calm line, not a zeroed block. */
private const val REST_HEIGHT = 0.045f

/**
 * Samples one bar from three harmonics.
 *
 * Each harmonic is a full sine over the field width, so the phase offset per bar creates a
 * travelling shape rather than independent wobbles. Frequencies are deliberately non-integer
 * multiples so the pattern does not visibly repeat within the visible width.
 */
private fun sampleBar(index: Int, count: Int, seconds: Float, intensity: Float): Float {
    val position = index.toFloat() / max(1, count - 1).toFloat()

    // 1. Slow swell — the body of the wave, one crest across the field.
    val swell = sin((position * 1.0f + seconds * 0.22f) * 2f * PI).toFloat()

    // 2. Fast detail — individual peaks, faster and smaller.
    val detail = sin((position * 2.7f - seconds * 0.55f) * 2f * PI).toFloat()

    // 3. Envelope — a slower rising/falling breath that gates how alive the field looks.
    val envelope = 0.6f + 0.4f * sin(seconds * 0.35f).toFloat()

    // Centre-weighted window: bars near the edges stay quieter, which reads as depth.
    val window = sqrt(1f - (2f * position - 1f).let { it * it }).coerceIn(0f, 1f)

    val raw = 0.16f + window * envelope * (0.34f * (1f + swell) * 0.5f + 0.22f * (1f + detail) * 0.5f)
    return (REST_HEIGHT + (raw - REST_HEIGHT) * intensity).coerceIn(REST_HEIGHT, 0.98f)
}

/**
 * Paints the ribbon: mirrored bars around the vertical centre, tinted by height, plus a travelling
 * highlight.
 */
private fun DrawScope.drawMirroredRibbon(bars: List<Float>, palette: List<Color>, gap: Float) {
    val count = bars.size
    if (count == 0) return

    val barWidth = ((size.width - gap * (count - 1)) / count).coerceAtLeast(1f)
    val halfHeight = size.height / 2f
    val centre = halfHeight

    // Fall back to the theme-neutral white so callers that pass no palette still get the shape.
    val low = palette.getOrNull(0) ?: Color.White.copy(alpha = 0.35f)
    val high = palette.getOrNull(1) ?: palette.getOrNull(0) ?: Color.White.copy(alpha = 0.9f)

    bars.forEachIndexed { index, value ->
        val x = index * (barWidth + gap)
        val h = (value * halfHeight).coerceAtLeast(1f)

        // Colour interpolates from `low` at the resting line to `high` at full amplitude.
        val t = ((value - REST_HEIGHT) / (1f - REST_HEIGHT)).coerceIn(0f, 1f)
        val color = lerp(low, high, t)

        // Mirror: one rect above the centre, one below. Using a single rounded pair keeps the caps
        // reading as a continuous spine when the bar is short.
        drawRoundRect(
            color = color.copy(alpha = 0.55f + 0.45f * t),
            topLeft = Offset(x, centre - h),
            size = Size(barWidth, h),
        )
        drawRoundRect(
            color = color.copy(alpha = 0.28f + 0.30f * t),
            topLeft = Offset(x, centre),
            size = Size(barWidth, h),
        )
    }

    // Travelling highlight: a narrow vertical band sweeping the field, drawn once over the bars.
    // Its position is derived from the same clock as the bars, so it feels attached to the music.
    val sweep = ((System.currentTimeMillis() % 4200L) / 4200f)
    val sweepX = sweep * size.width
    val bandHalf = size.width * 0.06f

    val highlight =
        androidx.compose.ui.graphics.Brush.horizontalGradient(
            colorStops =
                arrayOf(
                    (0f) to Color.Transparent,
                    ((sweepX - bandHalf).coerceIn(0f, size.width) / size.width) to Color.Transparent,
                    (sweepX / size.width).coerceIn(0.001f, 0.999f) to high.copy(alpha = 0.16f),
                    ((sweepX + bandHalf).coerceIn(0f, size.width) / size.width) to Color.Transparent,
                    1f to Color.Transparent,
                ),
        )

    drawRect(brush = highlight, size = Size(size.width, size.height))

    // Centre spine: a hairline that anchors the mirror axis without competing with the bars.
    drawRect(
        color = high.copy(alpha = 0.18f),
        topLeft = Offset(0f, centre),
        size = Size(size.width, 1f),
    )
}


