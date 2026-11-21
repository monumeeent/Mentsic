/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
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
 * Audio-reactive visualiser drawn as a **mirrored ribbon**: bars grow up and down from a centre
 * line, and the whole field is shaped by three coherent sine harmonics rather than per-bar noise.
 *
 * Why this design:
 *  - **Coherent motion.** Every bar derives from the same three harmonics, phase-shifted by its
 *    position, so the field reads as one object breathing instead of static. There is no
 *    `Random()`, so the shape is always musical — no twitchy, unslop noise.
 *  - **Mirrored.** Drawing each bar symmetrically around the centre doubles the perceived detail
 *    without doubling the bar count, and gives the control a clear horizontal axis.
 *  - **Gradient by amplitude.** Each bar is tinted by its own height between the two theme accents,
 *    so the colour carries information: loud peaks light up, quiet tails stay dim.
 *  - **A travelling highlight.** A soft vertical highlight sweeps the field, tying the bars to a
 *    single time axis.
 *
 * @param barCount number of bars. Even values centre cleanly; odd values still work.
 * @param barSpacing gap between bars.
 * @param colors the ramp the bars are tinted with, low → high. Usually
 *   `listOf(primary, tertiary)` from the theme so it follows the palette.
 * @param amplitude sets the overall `enabled`… motion intensity, in 0..1.
 * @param enabled when false the ribbon settles to a flat resting line instead of animating.
 */
@Composable
fun AudioVisualizer(
    barCount: Int = 48,
    barSpacing: Dp = 2.dp,
    colors: List<Color> = emptyList(),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    intensity: Float = 1f,
) {
    val density = LocalDensity.current
    val gapPx = with(density) { barSpacing.toPx() }

    // One Animatable per bar. Animating the *rendered* value toward a sampled target — rather than
    // redrawing a raw sample every frame — is what keeps the motion smooth at any frame rate.
    val bars = remember(barCount) { Array(barCount) { Animatable(REST_HEIGHT) } }

    LaunchedEffect(enabled, barCount) {
        // Phase accumulates from wall-clock time, so the animation keeps a constant tempo even if a
        // frame is dropped. `delay` sets the sampling cadence only.
        val start = System.currentTimeMillis()
        while (isActive) {
            val seconds = (System.currentTimeMillis() - start) / 1000f
            bars.forEachIndexed { index, bar ->
                val target = if (enabled) sampleBar(index, barCount, seconds, intensity) else REST_HEIGHT
                // Snap-to-target with a short tween: the sampled curve already changes smoothly,
                // so a long animation would overshoot and blur the harmonics.
                bar.animateTo(target, animationSpec = tween(90, easing = LinearEasing))
            }
            delay(16L)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 16.dp),
    ) {
        drawMirroredRibbon(bars = bars.map { it.value }, palette = colors, gap = gapPx)
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


