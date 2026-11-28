/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
 * tracks look like a hard beat. 0.94 leaves a sliver of ceiling for genuine peaks while letting the
 * field use most of the space it is given — the older 0.85 read as undersized on a tall canvas.
 */
private const val FIELD_GAIN = 0.94f

/**
 * Onset lift added on top of the bands, before [FIELD_GAIN].
 *
 * It was 0.30 — a flat +0.30 on every bar for a whole beat, which is why a quiet song jumped as
 * high as a loud one. This lift is multiplied by the bar's own band energy, so it stays a relative
 * bump: silence adds nothing, a quiet band peeks up slightly, a loud one punches.
 */
private const val PULSE_LIFT = 0.22f

/**
 * How long a single beat's travelling highlight takes to cross the field, in seconds.
 *
 * This is the "rhythm starting from a random bar" effect: every onset stamps an impulse on one bar
 * (see [OnsetSweep]), and that impulse then walks outward bar by bar. ~1.1 s is slow enough to read as
 * a deliberate ripple and fast enough that a 120 bpm track does not pile sweeps on top of each other.
 */
private const val SWEEP_SECONDS = 1.1f

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
 * **Per-bar rhythm.** Each bar carries its own displayed value and its own responsiveness: a fixed
 * per-index attack/release personality, a slow oscillator that continuously detunes that bar, and a
 * short damping window after a sudden jump (see [nextDisplay]). The field therefore breathes — every
 * bar keeps its own time — instead of the whole row moving as one tilted block.
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

    // How long each bar has been holding its current target, in seconds. Bars that got a fresh target
    // while their neighbours are still catching up are what makes the field breathe instead of leaning:
    // see [nextDisplay]. Plain array, allocated once per bar count, because nothing draws it.
    val barAge = remember(barCount) { FloatArray(barCount) }

    // Active travelling beats (see [OnsetSweep]). A small fixed-size mutable list: beats are added a
    // few times a second and retired as soon as they leave the field, so nothing grows unbounded.
    val sweeps = remember(barCount) { ArrayList<OnsetSweep>(8) }

    // Position of the most recent beat's head, normalised 0..1 across the field, and its strength.
    // Read only in the draw pass (see the gradient below), so it repaints the colour wash without
    // recomposing anything.
    val sweepHead = remember { mutableFloatStateOf(0f) }
    val sweepStrength = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(barCount, enabled, style) {
        // Fresh bars start from the resting line, not from the tail of the previous layout.
        barAge.fill(3f)
        sweeps.clear()
        var elapsed = 0f
        var sweepSeed = barCount * 7919
        while (isActive) {
            val live = VisualizerState.frame.value
            val silent = !enabled || live.silent

            // Every time the onset envelope rises sharply, start one travelling beat from a bar
            // picked at random. This is what makes the rhythm begin somewhere new on each beat and
            // then run bar by bar toward one side, instead of every bar jumping together.
            val onset = live.pulse
            if (!silent && onset > 0.55f && barCount > 0) {
                sweepSeed = sweepSeed * 1103515245 + 12345
                val origin = (sweepSeed ushr 16).mod(barCount)
                // Alternate the travel direction so consecutive beats mirror each other.
                val direction = if ((sweepSeed ushr 8) and 1 == 0) 1 else -1
                sweeps.add(
                    OnsetSweep(
                        origin = origin,
                        direction = direction,
                        bornAt = elapsed,
                        strength = onset.coerceIn(0f, 1f),
                    ),
                )
                // Keep the list bounded if the audio is dense; the oldest beat is the least visible.
                if (sweeps.size > 8) sweeps.removeAt(0)
            }

            // Retire beats that have crossed the whole field.
            sweeps.removeAll { elapsed - it.bornAt > SWEEP_SECONDS }

            val sweepBoost =
                if (silent) {
                    FloatArray(0)
                } else {
                    sweepContribution(sweeps, barCount, elapsed)
                }

            // Publish the brightest traveling beat for the colour wash. We take the newest sweep that
            // is still alive: it is the one the eye is following, so the gradient rides the beat the
            // user just heard rather than a pile of older ones.
            val lead = sweeps.lastOrNull()
            if (lead != null && !silent) {
                val progress = ((elapsed - lead.bornAt) / SWEEP_SECONDS).coerceIn(0f, 1f)
                val headBar = lead.origin + progress * barCount * lead.direction
                sweepHead.value = (headBar / barCount.coerceAtLeast(1)).coerceIn(0f, 1f)
                sweepStrength.value = (lead.strength * (1f - progress)).coerceIn(0f, 1f)
            } else {
                // Fade the wash out rather than snapping it off when the last beat retires.
                val faded = sweepStrength.value * 0.86f
                sweepStrength.value = if (faded < 0.01f) 0f else faded
            }

            val targets =
                if (!silent) {
                    expandBands(
                        bands = live.bands,
                        count = barCount,
                        pulse = live.pulse,
                        intensity = intensity,
                        sweep = sweepBoost,
                    )
                } else {
                    List(barCount) { REST_HEIGHT }
                }

            display =
                if (reduceMotion) {
                    // Accessibility: no motion at all, just the analysed shape.
                    targets
                } else {
                    nextDisplay(
                        previous = display,
                        targets = targets,
                        ages = barAge,
                        level = if (silent) 0f else live.level.coerceIn(0f, 1f),
                        silent = silent,
                        elapsed = elapsed,
                    )
                }

            elapsed += FRAME_MS / 1000f
            delay(FRAME_MS)
        }
    }

    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height)
                // No horizontal inset here: callers position the field, and an inset inside the
                // canvas only stole width from the bars. Height is the caller's budget too.
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

        // The travelling beat's colour wash. Its head position and strength are read here (draw
        // phase) so the wash follows the rhythm without recomposing the visualizer.
        val washHead = sweepHead.value
        val washStrength = if (enabled) sweepStrength.value else 0f

        drawVisualizer(
            values = display,
            palette = colors,
            gap = gapPx,
            style = style,
            pulse = pulse,
            bloom = bloom,
            washHead = washHead,
            washStrength = washStrength,
        )
    }
}

/**
 * Sampling cadence for the target refresh. The easing does the smoothing; this just feeds it.
 *
 * 33 ms ≈ 30 fps. The visualiser only needs to *feel* live, and half the sampling rate halves the
 * arithmetic and keeps the bars evenly spaced in time, which matters for the rhythm below.
 */
private const val FRAME_MS = 33L

/**
 * Per-bar motion: every bar reacts on its own clock, so the field breathes instead of leaning.
 *
 * The old version gave every bar the same rise/fall *shape*, only offset by distance from the centre.
 * A bar 40 px from the left and the bar 40 px from the right therefore moved in exact lockstep, which
 * is why the whole row looked like one tilted curtain sliding sideways rather than a spectrum.
 *
 * Three independent sources of variation replace that, and all three are smooth in time — no bar
 * ever holds a frozen value while it waits for a "slot", which would read as stutter:
 *  1. **Per-bar rate.** Each bar owns its attack/release coefficients, derived from its index. Equal
 *     inputs still rise at visibly different speeds, so neighbouring bars separate from each other.
 *  2. **Per-bar phase.** A slow oscillator per bar continuously modulates that bar's responsiveness.
 *     Two bars reading the *same* band therefore trade height against each other instead of moving
 *     as twins — the eye-soothing ``breathing'' of the row.
 *  3. **Age damping.** A bar that has just jumped to a much higher target is damped for a few frames
 *     while it settles, so a kick rolls through the field rather than snapping all of it at once.
 *
 * Rise is still faster than fall (crisp attacks, smooth tails), the modulation never inverts the
 * response, and everything is clamped to [REST_HEIGHT]..1, so no bar can overshoot or sink through
 * the resting line. Pure arithmetic: one array of ages, one list out.
 */
private fun nextDisplay(
    previous: List<Float>,
    targets: List<Float>,
    ages: FloatArray,
    level: Float,
    silent: Boolean,
    elapsed: Float,
): List<Float> {
    val count = targets.size
    if (count == 0) return targets

    // Silence: no new targets, only a gentle return to the resting line, so pausing fades out
    // instead of freezing or dropping like a stone.
    if (silent) {
        ages.fill(0f)
        return List(count) { i ->
            val from = previous.getOrElse(i) { REST_HEIGHT }
            (from + (REST_HEIGHT - from) * 0.14f).coerceIn(0f, 1f)
        }
    }

    // Loudness trims how much of the per-bar variation applies: quiet passages keep their variation
    // (that is where it reads best), loud ones converge slightly so peaks still look coherent.
    val variation = 1f - 0.25f * level.coerceIn(0f, 1f)

    val result = ArrayList<Float>(count)
    for (i in 0 until count) {
        val from = previous.getOrElse(i) { REST_HEIGHT }
        val to = targets[i]

        // 1. Personality: a fixed per-bar coefficient in 0.45..1. An incommensurable step (1.7 rad)
        //    keeps adjacent bars from sharing a personality, so no two neighbours move in unison.
        val personality = 0.45f + 0.55f * kotlin.math.abs(kotlin.math.sin(i * 1.7f))

        // 2. Slow per-bar oscillator (~0.4..1.4 rad/s): modulates responsiveness, not the target. It
        //    never goes negative, so a bar can be lazy but never stops or reverses.
        val phaseOffset = (i * 0.618034f) % 1f
        val oscillator = 0.55f + 0.45f * kotlin.math.sin(elapsed * 0.9f + phaseOffset * 6.2832f)

        // 3. Age damping: a bar that gained a lot of height in one frame is held back briefly, which
        //    is what staggers a kick across the field instead of moving every bar at once.
        val jump = (to - from).coerceAtLeast(0f)
        val damping = if (jump > 0.12f) 0.35f else 1f
        ages[i] = if (jump > 0.12f) 0f else (ages[i] + FRAME_MS / 1000f).coerceAtMost(1f)
        val settling = (ages[i] / 0.12f).coerceIn(0.55f, 1f)

        val rise = (0.26f + 0.30f * personality) * variation * oscillator * damping * settling
        val fall = (0.09f + 0.13f * personality) * variation * (0.7f + 0.3f * oscillator)
        val rate = if (to > from) rise else fall

        // Targets are already inside [0,1]; the clamp guards against float drift and keeps every bar
        // at or above the resting line.
        result.add((from + (to - from) * rate).coerceIn(REST_HEIGHT, 1f))
    }
    return result
}

/**
 * Stretches [bands] (typically 8) onto [count] bars.
 *
 * Only band interpolation and a *symmetric* silhouette shaping live here. The old centre-out flare and
 * the edge taper were both asymmetric or centre-biased, which reinforced the "tilted curtain" look;
 * they are gone, and all timing/character now comes from [nextDisplay].
 *
 * Output is scaled by [FIELD_GAIN] so ordinary music never occupies the full height, and the beat lift
 * is scaled by each band's own energy so soft passages stay shallow.
 */
private fun expandBands(
    bands: FloatArray,
    count: Int,
    pulse: Float,
    intensity: Float,
    sweep: FloatArray = FloatArray(0),
): List<Float> {
    if (bands.isEmpty() || count <= 0) return emptyList()

    val result = ArrayList<Float>(count)

    for (i in 0 until count) {
        // Position of this bar in the field: 0 at the far left, 1 at the far right.
        val position = if (count == 1) 0.5f else i.toFloat() / (count - 1)

        // Map onto the band axis.
        val pos = position * (bands.size - 1)
        val lo = pos.toInt().coerceIn(0, bands.size - 1)
        val hi = (lo + 1).coerceAtMost(bands.size - 1)
        val t = pos - lo
        val bandValue = bands[lo] + (bands[hi] - bands[lo]) * t

        // Onset lift, scaled by the bar's own band so silence adds nothing. Constant across the row —
        // the *timing* of the lift is what varies between bars, not its size.
        val flare = pulse * PULSE_LIFT * bandValue

        // Travelling beat contribution for this specific bar (0 when no sweep is crossing it).
        val beat = if (i < sweep.size) sweep[i] else 0f

        val value = (bandValue + flare) * intensity * FIELD_GAIN + beat
        result.add(value.coerceIn(0f, 1f))
    }
    return result
}

/**
 * One travelling beat: a highlight born on [origin] that walks outward bar by bar.
 *
 * @param origin index of the bar the beat started on (chosen pseudo-randomly per onset).
 * @param direction +1 walks toward the right edge, -1 toward the left.
 * @param bornAt sampler time (seconds) the beat appeared, used to place and retire it.
 * @param strength onset envelope at birth, 0..1 — a hard hit sweeps brighter than a soft one.
 */
private data class OnsetSweep(
    val origin: Int,
    val direction: Int,
    val bornAt: Float,
    val strength: Float,
)

/**
 * Converts every active [OnsetSweep] into a per-bar additive lift.
 *
 * Each beat has a *head* (the brightest bar) and a *tail* of trailing bars behind it, so the walk reads
 * as a comet crossing the field rather than a single blinking cell. As the head moves, the bars it
 * passed fade out over a short trail — which is what produces the left- or right-running rhythm.
 *
 * Work is O(active beats × trail length), independent of bar count.
 */
private fun sweepContribution(
    sweeps: List<OnsetSweep>,
    barCount: Int,
    elapsed: Float,
): FloatArray {
    if (sweeps.isEmpty() || barCount <= 0) return FloatArray(0)
    val lift = FloatArray(barCount)
    // Trail length in bars: a fifth of the field, capped so tiny placements still show a tail.
    val trail = maxOf(3, barCount / 5)

    for (sweep in sweeps) {
        val age = elapsed - sweep.bornAt
        if (age < 0f) continue
        val progress = (age / SWEEP_SECONDS).coerceIn(0f, 1f)
        // The head advances one bar at a time; `travelled` is in bars, not a fraction.
        val travelled = progress * barCount
        val head = sweep.origin + (travelled * sweep.direction).toInt()

        // Envelope: the beat fades over its whole life, so the last bars of the field get a dimmer
        // visit than the first ones — energy is spent as it travels.
        val life = 1f - progress
        val amplitude = sweep.strength * life * 0.42f

        for (k in 0..trail) {
            val index = head - k * sweep.direction
            if (index < 0 || index >= barCount) continue
            // Head is full, the tail falls off quadratically.
            val weight = 1f - k.toFloat() / (trail + 1)
            lift[index] = maxOf(lift[index], amplitude * weight * weight)
        }
    }
    return lift
}

/** Dispatches to the selected preset. Pure draw code — no state reads, no allocation per bar. */
private fun DrawScope.drawVisualizer(
    values: List<Float>,
    palette: List<Color>,
    gap: Float,
    style: VisualizerStyle,
    pulse: Float = 0f,
    bloom: Float = 0f,
    washHead: Float = 0f,
    washStrength: Float = 0f,
) {
    // The travelling-beat colour wash is painted *under* the bars, so it reads as light spilling
    // across the field behind the shapes rather than tinting them. Every preset gets it, which is what
    // makes the gradient feel like part of the visualizer instead of a decoration on one style.
    if (washStrength > 0.01f) {
        drawBeatWash(palette = palette, head = washHead, strength = washStrength)
    }

    when (style) {
        VisualizerStyle.RIBBON -> drawMirroredRibbon(bars = values, palette = palette, gap = gap, pulse = pulse, bloom = bloom)
        VisualizerStyle.BARS -> drawBars(bars = values, palette = palette, gap = gap)
        VisualizerStyle.WAVE -> drawWave(bars = values, palette = palette, mirror = false)
        VisualizerStyle.MIRROR_WAVE -> drawWave(bars = values, palette = palette, mirror = true)
        VisualizerStyle.DOTS -> drawDots(bars = values, palette = palette, gap = gap)
    }
}

/**
 * Gradient wash that rides the travelling beat.
 *
 * A radial bloom is centred on the sweep head ([head], 0..1 across the field) and tinted with the high
 * end of the palette. As the beat walks, the glow walks with it — the "gradient vibe as the beat goes
 * to one side". Opacity is [strength], which already decays over the beat's life, so the wash fades
 * out instead of blinking off.
 */
private fun DrawScope.drawBeatWash(
    palette: List<Color>,
    head: Float,
    strength: Float,
) {
    val high = palette.getOrNull(1) ?: palette.getOrNull(0) ?: Color.White.copy(alpha = 0.9f)
    val centre = Offset(size.width * head, size.height / 2f)
    // Radius scales with the field so the glow covers a similar area at any size, but is wide enough
    // to read as a gradient rather than a spot.
    val radius = maxOf(size.width * 0.45f, size.height * 1.1f)

    drawRect(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        high.copy(alpha = 0.26f * strength),
                        high.copy(alpha = 0.08f * strength),
                        Color.Transparent,
                    ),
                center = centre,
                radius = radius,
            ),
    )
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


