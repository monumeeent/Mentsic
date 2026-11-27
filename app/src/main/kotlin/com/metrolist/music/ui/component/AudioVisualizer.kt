/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metrolist.music.playback.audio.VisualizerState
import android.provider.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
 * Overall height budget for the analysed bands.
 *
 * The analyser publishes 0..1, but a bar at full canvas height for ordinary music is what made soft
 * tracks look like a hard beat. 0.85 leaves a visible ceiling: only a genuine peak reaches the top of
 * the field, and the default intensity stays readable without every preset looking saturated.
 */
private const val FIELD_GAIN = 0.85f

/**
 * Onset lift added on top of the bands, before [FIELD_GAIN].
 *
 * It was 0.30 — a flat +0.30 on every bar for a whole beat, which is why a quiet song jumped as
 * high as a loud one. This lift is multiplied by the bar's own band energy, so it stays a relative
 * bump: silence adds nothing, a quiet band peeks up slightly, a loud one punches.
 */
private const val PULSE_LIFT = 0.16f

/**
 * True when the OS asks for reduced motion ("Remove animations" / Animator duration scale = 0).
 *
 * Compose has no public reduced-motion signal, so this reads the platform setting once per
 * composition. With it on, the visualizer snaps to the analysed shape instead of easing, which is
 * the accessible behaviour — the data stays truthful, only the motion goes away.
 */
@Composable
private fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

/**
 * Audio-reactive visualiser.
 *
 * Reads [VisualizerState.frame] and expands the analysed bands onto [barCount] bars. Every preset
 * draws from that one array, so the full player and the capsule mini player can never disagree about
 * what the music is doing — they only differ in density.
 *
 * **Per-bar easing.** Each bar carries its own displayed value, eased toward that bar's band energy
 * with a centre-outward lag ([easeOutward]). That is what gives the wavy feel: a kick lifts the
 * middle bars first and the effect rolls toward the edges over the next frames, instead of the whole
 * field jumping as one block.
 *
 * @param barCount bars to draw. The analysed bands are stretched across this width.
 * @param barSpacing gap between bars/dots.
 * @param colors ramp the shapes are tinted with, low → high.
 * @param enabled draw the audio at all; when false the shape rests flat.
 * @param style which preset to draw.
 * @param height canvas height; callers override it per placement.
 * @param intensity scales the amplitude, for dense placements where full height is too loud.
 * @param onCycleStyle invoked on a double tap; the host advances to the next preset.
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
    onCycleStyle: (() -> Unit)? = null,
) {
    val density = LocalDensity.current
    val gapPx = with(density) { barSpacing.toPx() }
    val reduceMotion = rememberReduceMotion()

    // Collected once per placement; the processor already smooths the *bands*, and the per-bar
    // easing below smooths the *display*, so this canvas is never recomposed by playback.
    val frame by VisualizerState.frame.collectAsStateWithLifecycle()

    // Ask the analyser for data only while a visualizer is actually composed and enabled. The
    // processor sits in the hot audio path, so leaving it on with nothing to draw would burn CPU
    // for every song the user never opens the player over.
    DisposableEffect(enabled) {
        if (enabled) VisualizerState.retain() else VisualizerState.release()
        onDispose { if (enabled) VisualizerState.release() }
    }

    // One value per bar, updated in a single loop. A `List<Float>` in snapshot state is read only
    // inside the Canvas draw lambda, so each update repaints and never recomposes.
    var display by remember(barCount) { mutableStateOf(List(barCount) { REST_HEIGHT }) }

    LaunchedEffect(barCount, enabled, style) {
        while (isActive) {
            val live = VisualizerState.frame.value
            val targets =
                if (enabled && !live.silent) {
                    expandBands(
                        bands = live.bands,
                        count = barCount,
                        pulse = live.pulse,
                        intensity = intensity,
                    )
                } else {
                    List(barCount) { REST_HEIGHT }
                }

            display =
                if (reduceMotion) {
                    // Accessibility: no motion at all, just the analysed shape.
                    targets
                } else {
                    easeOutward(previous = display, targets = targets)
                }

            delay(FRAME_MS)
        }
    }

    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = 16.dp)
                // Double tap anywhere on the beating bars cycles the preset — the most direct place
                // to change it, since that is where the user is already looking.
                .then(
                    if (onCycleStyle != null) {
                        Modifier.pointerInput(onCycleStyle) {
                            detectTapGestures(
                                onDoubleTap = { onCycleStyle() },
                            )
                        }
                    } else {
                        Modifier
                    },
                ),
    ) {
        // Read in the draw scope only, so per-frame motion repaints without recomposing.
        val live = frame
        val pulse = if (enabled && !live.silent) live.pulse else 0f
        // Peak brightness follows the frame's loudness, so a quiet song blooms less than a loud one.
        val bloom = if (enabled && !live.silent) 0.35f + 0.65f * live.level.coerceIn(0f, 1f) else 0f

        drawVisualizer(
            values = display,
            palette = colors,
            gap = gapPx,
            style = style,
            pulse = pulse,
            bloom = bloom,
        )
    }
}

/** Sampling cadence for the target refresh. The easing does the smoothing; this just feeds it. */
private const val FRAME_MS = 24L

/**
 * Per-bar easing with a **centre-outward lag** — the core of the "wave" feel.
 *
 * Bars near the middle respond fast; bars toward the edges respond progressively slower. That is
 * what makes a kick blossom in the centre and roll toward the sides instead of the whole field
 * snapping at once. Rise is faster than fall, so transients are crisp but the tail is smooth.
 *
 * Pure arithmetic on two lists: no coroutines, no per-bar animatables, no allocation beyond the one
 * result list per frame.
 */
private fun easeOutward(previous: List<Float>, targets: List<Float>): List<Float> {
    val count = targets.size
    if (count == 0) return targets
    val centre = (count - 1) / 2f

    return List(count) { i ->
        val from = previous.getOrElse(i) { REST_HEIGHT }
        val to = targets[i]
        // 0 at the middle, 1 at the edges.
        val fromCentre = (kotlin.math.abs(i - centre) / centre.coerceAtLeast(1f)).coerceIn(0f, 1f)

        // Fast in the centre, slower at the edges: 0.62 → 0.26.
        val rise = 0.62f - 0.36f * fromCentre
        // Decay is always slower than the rise, and also lags toward the edges.
        val fall = 0.22f - 0.12f * fromCentre

        val rate = if (to > from) rise else fall
        (from + (to - from) * rate).coerceIn(0f, 1f)
    }
}

/**
 * Stretches [bands] (typically 8) onto [count] bars, with a **centre-out flare**.
 *
 * Three things make the motion read as a wave instead of a block:
 *  1. **Centre-out emphasis.** The pulse lift is strongest at the middle bars and decays toward the
 *     edges, so a beat blossoms out of the centre and rolls toward the sides.
 *  2. **Edge taper.** Bars near the ends are scaled down slightly, so the field has a soft silhouette
 *     instead of a flat-topped rectangle.
 *  3. **Per-bar phase offset.** Each bar's target also carries a tiny oscillation of its own, which
 *     gives the neighbouring bars slightly different timing — the ripple that makes it feel fluid.
 *
 * Output is scaled by [FIELD_GAIN] so ordinary music never occupies the full height, and the beat
 * flare is scaled by each band's own energy so soft passages stay shallow.
 */
private fun expandBands(
    bands: FloatArray,
    count: Int,
    pulse: Float,
    intensity: Float,
): List<Float> {
    if (bands.isEmpty() || count <= 0) return emptyList()

    val result = ArrayList<Float>(count)
    val centre = (count - 1) / 2f

    for (i in 0 until count) {
        // Position of this bar in the field: 0 at the far left, 1 at the far right.
        val position = if (count == 1) 0.5f else i.toFloat() / (count - 1)
        // Distance from the centre, 0 in the middle, 1 at the edges.
        val fromCentre = (kotlin.math.abs(i - centre) / centre.coerceAtLeast(1f)).coerceIn(0f, 1f)

        // Map onto the band axis.
        val pos = position * (bands.size - 1)
        val lo = pos.toInt().coerceIn(0, bands.size - 1)
        val hi = (lo + 1).coerceAtMost(bands.size - 1)
        val t = pos - lo
        val bandValue = bands[lo] + (bands[hi] - bands[lo]) * t

        // Centre-out beat flare: strongest in the middle, fading to nothing at the edges. It is
        // scaled by the band's own energy, so a transient on a quiet passage lifts a *short* bar —
        // only loud music can push the field to the top.
        val flare = pulse * PULSE_LIFT * bandValue * (1f - fromCentre * fromCentre)

        // Edge taper: the silhouette falls away toward the sides.
        val taper = 1f - 0.18f * fromCentre * fromCentre

        val value = (bandValue * taper + flare) * intensity * FIELD_GAIN
        result.add(value.coerceIn(0f, 1f))
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
    bloom: Float = 0f,
) {
    when (style) {
        VisualizerStyle.RIBBON -> drawMirroredRibbon(bars = values, palette = palette, gap = gap, pulse = pulse, bloom = bloom)
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
 * One continuous curve through the samples, optionally reflected below the centre.
 *
 * Uses midpoint-quadratic smoothing (each segment is a quadratic whose control point is the data
 * point itself), which removes polyline corners without overshoot. The final point is drawn
 * explicitly — without it the curve visibly stopped one sample short of the right edge.
 */
private fun DrawScope.drawWave(bars: List<Float>, palette: List<Color>, mirror: Boolean) {
    val count = bars.size
    if (count < 2) return
    val (low, high) = paletteColors(palette)
    val step = size.width / (count - 1)
    val centreY = size.height / 2f

    // Where a sample sits vertically, for the top (or mirrored-top) half.
    fun yAt(index: Int): Float {
        val value = bars[index]
        return if (mirror) centreY - value * centreY else size.height - value * size.height
    }

    val path = Path()
    path.moveTo(0f, yAt(0))
    for (index in 1 until count) {
        val prevX = (index - 1) * step
        val x = index * step
        // Control point = the previous data point; endpoint = the midpoint between the two.
        path.quadraticTo(prevX, yAt(index - 1), (prevX + x) / 2f, (yAt(index - 1) + yAt(index)) / 2f)
    }
    // Land exactly on the last sample so the stroke reaches the right edge.
    path.lineTo((count - 1) * step, yAt(count - 1))

    drawPath(
        path = path,
        brush = Brush.horizontalGradient(listOf(low, high, low)),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
    )

    if (mirror) {
        // Reflected copy, dimmer, plus the soft fill between the two halves.
        val reflected = Path()
        reflected.moveTo(0f, centreY + bars[0] * centreY)
        for (index in 1 until count) {
            val prevX = (index - 1) * step
            val x = index * step
            val prevY = centreY + bars[index - 1] * centreY
            val y = centreY + bars[index] * centreY
            reflected.quadraticTo(prevX, prevY, (prevX + x) / 2f, (prevY + y) / 2f)
        }
        reflected.lineTo((count - 1) * step, centreY + bars.last() * centreY)

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
    bloom: Float = 0f,
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
    // transient. Scaled by the frame's level so a soft track does not flash like a club track.
    if (pulse > 0.01f) {
        drawRect(
            brush =
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        high.copy(alpha = 0.20f * pulse * bloom),
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


