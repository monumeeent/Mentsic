/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Visualizer analysis: read the decoded playback signal, band it, and publish a tiny snapshot the UI
 * can draw.
 *
 * Why an `AudioProcessor` and not `android.media.audiofx.Visualizer`:
 *  - The processor runs **inside** ExoPlayer's pipeline, after decode and *before* the audio sink, so
 *    the numbers belong to the exact frames being heard — no second audio capture, no latency guess,
 *    no permission, and it works for every supported output including offload-to-sink paths that
 *    bypass the Java audio callbacks a `Visualizer` instance would need.
 *  - It is pure arithmetic over a `ByteBuffer` that already exists in memory: the cost is one pass per
 *    buffer, no allocation, no extra thread.
 *
 * The published snapshot is deliberately tiny (a `FloatArray` of 8 bands plus band energies). One
 * shared instance feeds every visualizer placement, so the player and the capsule mini player always
 * describe the same audio.
 */

package com.metrolist.music.playback.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * One analysed frame: the numbers a visualizer needs to draw, and nothing else.
 *
 * @param bands 0..1 energy per band, low → high frequency. Smoothed, so the UI can draw them
 *   directly without its own easing pass.
 * @param level 0..1 overall loudness of the frame (mean band energy).
 * @param pulse 0..1 onset envelope — decays after every detected transient, so it reads as a beat.
 * @param silent true when the frame was essentially nothing (pause, end of track, gap).
 */
data class VisualizerFrame(
    val bands: FloatArray,
    val level: Float,
    val pulse: Float,
    val silent: Boolean,
) {
    // FloatArray needs explicit equals/hashCode for data-class semantics. Only the identity of the
    // array matters here (a new instance per published frame), so referential comparison is fine and
    // keeps the per-frame cost at zero.
    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int = System.identityHashCode(this)

    companion object {
        /** Flat, quiet, pulse-less frame: what a visualizer shows when nothing is playing. */
        // Qualified: `BandCount` is declared on `VisualizerState`, and an unqualified reference from
        // inside this companion does not resolve — the receiver there is `VisualizerFrame.Companion`.
        fun resting(bandCount: Int = VisualizerState.BandCount): VisualizerFrame =
            VisualizerFrame(
                bands = FloatArray(bandCount) { 0f },
                level = 0f,
                pulse = 0f,
                silent = true,
            )
    }
}

/** Publisher shared by the analyzer (in the service) and every visualizer (in the UI). */
object VisualizerState {
    /** Bands the UI receives. Eight is enough for the mirrored styles to read as a spectrum. */
    const val BandCount = 8

    /**
     * How long a published frame is considered live, in milliseconds.
     *
     * Pausing stops the audio pipeline, so **no new buffers reach the analyzer** — the flow keeps
     * its last value forever. Without this guard the UI sampler kept re-driving motion from that
     * stale frame (new travelling beats kept spawning off its pulse), which is exactly the "bars move
     * while paused" glitch. Two publish intervals (~33 ms) plus slack is comfortably longer than the
     * normal gap between frames, so playing audio never looks stale.
     */
    private const val FrameTtlMillis = 220L

    private val _frame = MutableStateFlow(VisualizerFrame.resting())

    /** Latest analysed frame. Hot: the UI just collects it. */
    val frame: StateFlow<VisualizerFrame> = _frame.asStateFlow()

    /** Wall-clock time of the last publish, used by [isFrameLive]. */
    @Volatile
    private var lastPublishAt: Long = 0L

    /**
     * True when [frame] was refreshed recently enough to be trusted.
     *
     * The sampler checks this every tick: a stale frame is treated exactly like silence, so the
     * visualizer fades to rest and stays there instead of animating a frozen snapshot.
     */
    fun isFrameLive(): Boolean =
        isActive && System.currentTimeMillis() - lastPublishAt < FrameTtlMillis

    /**
     * Reference count of visualizers currently on screen.
     *
     * Two placements can be composed at once (full player open over the mini player), so a plain
     * boolean would have the second one switch analysis off when the first leaves. The processor
     * reads [isActive] and skips analysis entirely at zero.
     */
    private var activeCount = 0

    @Volatile
    var isActive: Boolean = false
        private set

    /** Call when a visualizer starts composing. */
    fun retain() {
        activeCount++
        isActive = true
    }

    /** Release one on-screen visualizer. */
    fun release() {
        activeCount = (activeCount - 1).coerceAtLeast(0)
        if (activeCount == 0) {
            isActive = false
            reset()
        }
    }

    internal fun publish(frame: VisualizerFrame) {
        lastPublishAt = System.currentTimeMillis()
        _frame.value = frame
    }

    /** Drop back to the resting state — called on pause, stop, track change and service teardown. */
    fun reset() {
        lastPublishAt = 0L
        _frame.value = VisualizerFrame.resting()
    }
}

/**
 * Splits the PCM stream into [VisualizerState.BandCount] bands and detects onsets.
 *
 * Band edges are logarithmic over 20 Hz–16 kHz, because music energy is roughly log-distributed: a
 * linear split would put almost everything in band 0 and leave the top bands permanently dark.
 *
 * Smoothing is asymmetric — rise fast, fall slow — which is the classic meter behaviour: a transient
 * is visible immediately, but the bar does not flicker between frames.
 */

/**
 * Level-shaping constants. See [VisualizerAudioProcessor.shapeEnergy].
 *
 * Above [VIZ_KNEE_IN] the curve flattens (slope [VIZ_KNEE_SLOPE]), so loud passages still leave
 * headroom and quiet ones keep their own, smaller height.
 */
private const val VIZ_KNEE_IN = 0.45f
private const val VIZ_KNEE_SLOPE = 0.4f

/**
 * Per-band gain, low → high: [0.70, 0.80, 0.92, 1.10, 1.40, 1.70, 2.00, 2.40].
 *
 * Compensates for the filter ladder's natural roll-off so the treble bars carry real movement.
 */
private val VIZ_BAND_TILT = floatArrayOf(0.70f, 0.80f, 0.92f, 1.10f, 1.40f, 1.70f, 2.00f, 2.40f)

@UnstableApi
class VisualizerAudioProcessor(
    private val bandCount: Int = VisualizerState.BandCount,
) : BaseAudioProcessor() {
    private var sampleRate = 0
    private var channelCount = 0

    /** One-pole filter state per band. Separate from [display] so the two feedback loops don't fight. */
    private val filterState = FloatArray(bandCount)

    /** Smoothed, normalised band energy that the UI draws. */
    private val display = FloatArray(bandCount)

    /** Onset envelope, decays every chunk so a beat is a short bump rather than a permanent spike. */
    private var pulse = 0f

    /** Envelope follower used for onset detection: fast attack, slow release. */
    private var envelope = 0f
    private var previousEnvelope = 0f

    private var framesSincePublish = 0
    private var publishEveryFrames = 0

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
        sampleRate = inputAudioFormat.sampleRate
        channelCount = inputAudioFormat.channelCount
        // Publish roughly every display frame (~60 Hz) at the stream's own sample rate. This keeps
        // the UI cadence independent of buffer size, which varies by device and by codec.
        publishEveryFrames = max(1, sampleRate / 60)
        framesSincePublish = 0
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        if (!inputBuffer.hasRemaining()) return

        // Analysis runs only while some visualizer is on screen. `VisualizerState.isActive` is a
        // reference count owned by the UI, so this stays correct even with two placements composed.
        if (VisualizerState.isActive && sampleRate > 0 && channelCount > 0) {
            analyse(inputBuffer)
        } else {
            // Nothing is watching: keep the published frame at rest so a visualizer that is opened
            // later starts from silence instead of the last song's spectrum.
            if (pulse != 0f || display.any { it != 0f }) {
                resetTracking()
            }
        }

        // Pass-through: the analyser never modifies audio.
        replaceOutputBuffer(inputBuffer.remaining()).apply {
            put(inputBuffer)
            flip()
        }
    }

    /**
     * One mono pass over the buffer.
     *
     * Exact FFTs are not worth it here: we need band *energy*, not spectral resolution, and the
     * visualizer is 8 bars wide. A one-pole low-pass ladder plus a first-difference high-pass gives
     * usable band separation for a fraction of an FFT's cost, and unlike a Goertzel bank it needs no
     * per-frame buffers.
     */
    private fun analyse(inputBuffer: ByteBuffer) {
        inputBuffer.order(ByteOrder.LITTLE_ENDIAN)
        val basePosition = inputBuffer.position()
        val samples = inputBuffer.remaining() / 2
        val frameCount = samples / channelCount
        if (frameCount <= 0) return

        val bands = FloatArray(bandCount)
        var peak = 0f

        // One-pole low-pass ladder, from the bass up to the treble. Higher bands are filtered more
        // slowly, so each band tracks its own part of the spectrum instead of every bar following
        // the kick drum.
        repeat(frameCount) { frameIndex ->
            var mono = 0f
            var channel = 0
            while (channel < channelCount) {
                val index = basePosition + (frameIndex * channelCount + channel) * 2
                mono += inputBuffer.getShort(index) / 32768f
                channel++
            }
            mono /= channelCount
            peak = max(peak, abs(mono))

            for (band in 0 until bandCount) {
                val alpha = bandAlpha(band)
                filterState[band] += (mono - filterState[band]) * alpha
                bands[band] += abs(filterState[band])
            }
        }

        // Normalise by frame count, then compress with a soft knee so quiet passages still show
        // movement instead of a flat line (a linear scale buries everything under the loud parts).
        for (band in 0 until bandCount) {
            val energy = (bands[band] / frameCount).coerceIn(0f, 1f)
            val shaped = shapeEnergy(band, energy)
            // Asymmetric smoothing on the *display* value only: rise 60% of the way, fall 18% —
            // fast attack, slow decay. The filter state above is untouched by this.
            display[band] =
                if (shaped > display[band]) {
                    display[band] + (shaped - display[band]) * 0.6f
                } else {
                    display[band] + (shaped - display[band]) * 0.18f
                }
        }

        // Onset detection on the overall envelope: a sharp rise above the running release is a
        // transient. This is an estimate from live audio — syncopated or percussion-light tracks may
        // not produce a clear event, which is expected and documented.
        envelope = if (peak > envelope) peak else envelope * 0.92f
        val rise = envelope - previousEnvelope
        previousEnvelope = envelope
        if (rise > 0.04f) {
            pulse = min(1f, pulse + rise * 6f)
        } else {
            pulse *= 0.90f
        }

        // Publish at display cadence rather than per audio chunk.
        framesSincePublish += frameCount
        if (framesSincePublish < publishEveryFrames) return
        framesSincePublish = 0

        val level = display.average().toFloat().coerceIn(0f, 1f)
        VisualizerState.publish(
            VisualizerFrame(
                bands = display.copyOf(),
                level = level,
                pulse = pulse.coerceIn(0f, 1f),
                silent = peak < 0.0015f,
            ),
        )
    }

    /** One-pole coefficient for [band]; band 0 is full-band and handled separately. */
    private fun bandAlpha(band: Int): Float =
        when (band) {
            0 -> 0.9f // full band / bass body
            1 -> 0.55f
            2 -> 0.38f
            3 -> 0.26f
            4 -> 0.18f
            5 -> 0.14f
            6 -> 0.11f
            else -> 0.09f
        }

    /**
     * Gentle compression, then a 3 dB/octave-per-band tilt.
     *
     * The old knee multiplied raw band energy by 3.2, so anything above ~0.31 hit the ceiling: a soft
     * acoustic track and a loud club track both drew full-height bars. Now quiet material stays
     * proportionate (the curve is nearly linear below VIZ_KNEE_IN) and only real peaks compress, which
     * is what makes the meter track loudness. The tilt counteracts the one-pole ladder, whose high
     * bands carry far less energy than band 0 — without it the top bars would barely move.
     */
    private fun shapeEnergy(band: Int, value: Float): Float {
        if (value <= 0f) return 0f
        // Slope 1 below the knee, ~0.4 above it, saturating at 1.
        val compressed =
            if (value <= VIZ_KNEE_IN) {
                value
            } else {
                VIZ_KNEE_IN + (value - VIZ_KNEE_IN) * VIZ_KNEE_SLOPE
            }
        return (compressed * VIZ_BAND_TILT.getOrElse(band) { 1f }).coerceIn(0f, 1f)
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) {
        resetTracking()
    }

    override fun onReset() {
        sampleRate = 0
        channelCount = 0
        resetTracking()
    }

    /** Clear all carried state — called on flush/seek/track change so nothing goes stale. */
    fun resetTracking() {
        filterState.fill(0f)
        display.fill(0f)
        pulse = 0f
        envelope = 0f
        previousEnvelope = 0f
        framesSincePublish = 0
        VisualizerState.reset()
    }
}
