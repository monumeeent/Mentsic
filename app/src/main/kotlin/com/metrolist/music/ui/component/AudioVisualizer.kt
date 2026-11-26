/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metrolist.music.playback.audio.VisualizerState

/**
 * Visualiser presets. Each is a different *way* of painting the same analysed audio, so they all
 * share one data source and cost the same to run.
 *
 *  - [RIBBON]      mirrored bars around a centre spine.
 *  - [BARS]        classic bottom-anchored spectrum, no mirroring.
 *  - [WAVE]        a single flowing line through the band energies.
 *  - [DOTS]        a row of swelling circles — lightest to render.
 *  - [MIRROR_WAVE] the line drawn twice, reflected, with a soft fill between the halves.
 */
enum class VisualizerStyle {
    RIBBON,
    BARS,
    WAVE,
    DOTS,
    MIRROR_WAVE,
}

/**
 * Resting height while nothing is playing: a thin, calm line, not a zeroed block.
 *
 * Nothing here is generated. Every value comes from [VisualizerState], which is fed by the audio
 * processor sitting in ExoPlayer's pipeline — so the movement is the music, not a timer.
 */
private const val REST_HEIGHT = 0.045f

/**
 * Audio-reactive visualiser.
 *
 * Reads [VisualizerState.frame] and expands the analysed bands onto [barCount] bars. Every preset
 * draws from that one array, so the full player and the capsule mini player can never disagree about
 * what the music is doing — they only differ in density.
 *
 * @param barCount bars to draw. The analysed bands are stretched across this width.
 * @param barSpacing gap between bars/dots.
 * @param colors ramp the shapes are tinted with, low → high.
 * @param enabled draw the audio at all; when false the shape rests flat.
 * @param style which preset to draw.
 * @param height canvas height; callers override it per placement.
 * @param intensity scales the amplitude, for dense placements where full height is too loud.
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

    // Collected once per placement; the processor already smooths, so there is no easing pass here.
    // Reading it in the draw lambda keeps playback from recomposing the canvas.
    val frame by VisualizerState.frame.collectAsStateWithLifecycle()

    // Ask the analyser for data only while a visualizer is actually composed and enabled. The
    // processor sits in the hot audio path, so leaving it on with nothing to draw would burn CPU
    // for every song the user never opens the player over.
    DisposableEffect(enabled) {
        if (enabled) VisualizerState.retain() else VisualizerState.release()
        onDispose { if (enabled) VisualizerState.release() }
    }

    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = 16.dp),
    ) {
        // The compose snapshot is read here, in the draw phase: a new frame invalidates the draw
        // only, never the composition.
        val live = frame
        val values =
            if (enabled && !live.silent) {
                expandBands(live.bands, barCount, live.pulse, intensity)
            } else {
                List(barCount) { REST_HEIGHT }
            }

        drawVisualizer(
            values = values,
            palette = colors,
            gap = gapPx,
            style = style,
            pulse = if (enabled && !live.silent) live.pulse else 0f,
        )
    }
}

/**
 * Stretches [bands] (typically 8) onto [count] bars by linear interpolation, so a narrow capsule and
 * a wide player show the same spectrum at different densities.
 *
 * The onset pulse is mixed in as a uniform lift, which makes the whole shape breathe on a beat
 * instead of only the bars near the loudest band.
 */
private fun expandBands(
    bands: FloatArray,
    count: Int,
    pulse: Float,
    intensity: Float,
): List<Float> {
    if (bands.isEmpty() || count <= 0) return emptyList()
    if (bands.size == 1) return List(count) { (bands[0] * intensity).coerceIn(0f, 1f) }

    val result = ArrayList<Float>(count)
    for (i in 0 until count) {
        // Map bar index onto the band axis, mirroring the ends so the edges are not clipped flat.
        val pos = (i.toFloat() / (count - 1).coerceAtLeast(1)) * (bands.size - 1)
        val lo = pos.toInt().coerceIn(0, bands.size - 1)
        val hi = (lo + 1).coerceAtMost(bands.size - 1)
        val t = pos - lo
        val value = bands[lo] + (bands[hi] - bands[lo]) * t
        val withPulse = value + pulse * 0.22f
        result.add((withPulse * intensity).coerceIn(0f, 1f))
    }
    return result
}

/** Dispatches to the selected preset. Pure draw code — no state reads, no allocation per bar. */
private fun DrawScope.drawVisualizer(
    values: List<Float>,
    palette: List<Color>,
    gap: Float,
    style: VisualizerStyle,
    pulse: Float = 0f,
) {
    when (style) {
        VisualizerStyle.RIBBON -> drawMirroredRibbon(bars = values, palette = palette, gap = gap, pulse = pulse)
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

/**
 * Paints the ribbon: mirrored bars around the vertical centre, tinted by height.
 *
 * @param pulse onset envelope from the analyser. It drives a soft bloom over the whole field, which
 *   replaces the old wall-clock highlight sweep — the glow now marks a real transient.
 */
private fun DrawScope.drawMirroredRibbon(
    bars: List<Float>,
    palette: List<Color>,
    gap: Float,
    pulse: Float = 0f,
) {
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

    // Beat bloom: instead of a highlight sweeping on a 4.2s timer, the field brightens on a real
    // transient. Uniform across the width, so it reads as the whole shape reacting at once.
    if (pulse > 0.01f) {
        drawRect(
            brush =
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        high.copy(alpha = 0.20f * pulse),
                        Color.Transparent,
                    ),
                ),
            size = Size(size.width, size.height),
        )
    }

    // Centre spine: a hairline that anchors the mirror axis without competing with the bars.
    drawRect(
        color = high.copy(alpha = 0.18f),
        topLeft = Offset(0f, centre),
        size = Size(size.width, 1f),
    )
}


