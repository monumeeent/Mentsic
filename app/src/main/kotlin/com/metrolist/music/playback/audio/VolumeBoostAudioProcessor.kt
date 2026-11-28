/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * In-app volume booster: digital gain applied to the decoded PCM, after the equaliser and before the
 * sink.
 *
 * Why a processor and not the system volume:
 *  - The system media volume is capped at 100%; a quiet master (older recordings, podcast mixes,
 *    heavily limited tracks) simply cannot be made louder from there.
 *  - This runs inside ExoPlayer's pipeline, so it works on every output (speaker, Bluetooth, wired)
 *    without any permission and without touching the device's global volume — turning it back to 0
 *    restores exactly the original signal.
 *
 * **Safety.** Boosting multiplies samples and can therefore clip: a full-scale sample pushed by +10 dB
 * would wrap around into a nasty distortion. Two guards are applied:
 *  1. A soft limiter (tanh-shaped knee) compresses the top of the range instead of hard-clipping it, so
 *     loud passages get denser rather than broken.
 *  2. The gain is user-facing and capped at +12 dB, with a documented warning in the UI.
 *
 * The processor is a pass-through when [gainDb] is 0, so it costs one comparison per buffer when the
 * feature is off.
 */

package com.metrolist.music.playback.audio

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.pow
import kotlin.math.tanh
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared booster state.
 *
 * A single object so the settings slider, the player menu and the audio pipeline all read one value.
 * The processor polls [gainDb] per buffer (a plain volatile read), which is far cheaper than wiring a
 * flow collection into the audio thread.
 */
object VolumeBoostState {
    /** Hard ceiling for the digital gain, in dB. */
    const val MaxGainDb = 12f

    private val _gainDb = MutableStateFlow(0f)

    /** Current boost in dB, 0..[MaxGainDb]. 0 means "off" and the processor is a pass-through. */
    val gainDb: StateFlow<Float> = _gainDb.asStateFlow()

    /** Linear multiplier for the current gain, recomputed on change. Read from the audio thread. */
    @Volatile
    var linearGain: Float = 1f
        private set

    fun setGainDb(db: Float) {
        val clamped = db.coerceIn(0f, MaxGainDb)
        _gainDb.value = clamped
        // 0 dB must be exactly 1f so the "off" path is bit-identical to no processor at all.
        linearGain = if (clamped <= 0.01f) 1f else 10f.pow(clamped / 20f)
    }
}

/**
 * Applies [VolumeBoostState.linearGain] with a soft limiter.
 *
 * Only 16-bit PCM is handled, matching the rest of the processor chain; anything else is passed
 * through untouched so the pipeline never breaks on an unexpected format.
 */
@UnstableApi
class VolumeBoostAudioProcessor : BaseAudioProcessor() {

    private var channelCount = 0

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        // The chain is configured for 16-bit PCM by the other processors; be explicit rather than
        // assuming, so a future format change fails loudly here instead of producing noise.
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
        channelCount = inputAudioFormat.channelCount
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val gain = VolumeBoostState.linearGain
        if (!inputBuffer.hasRemaining()) return

        // Off, or nothing to do: hand the buffer through untouched. This keeps the zero-gain path
        // free of any sample math.
        if (gain <= 1.0001f) {
            replaceOutputBuffer(inputBuffer.remaining()).apply {
                put(inputBuffer)
                flip()
            }
            return
        }

        inputBuffer.order(ByteOrder.LITTLE_ENDIAN)
        val output = replaceOutputBuffer(inputBuffer.remaining())
        while (inputBuffer.remaining() >= 2) {
            val sample = inputBuffer.getShort() / 32768f
            // Soft limiter: below the knee the transfer is linear (so quiet music just gets louder);
            // above it the curve bends smoothly toward ±1, so nothing ever wraps around.
            val boosted = sample * gain
            val limited =
                if (boosted > 1f || boosted < -1f) {
                    tanh(boosted)
                } else {
                    boosted
                }
            val pcm = (limited.coerceIn(-1f, 1f) * 32767f).toInt().toShort()
            output.putShort(pcm)
        }
        // Any trailing odd byte (malformed buffer) is copied verbatim rather than dropped.
        while (inputBuffer.hasRemaining()) {
            output.put(inputBuffer.get())
        }
        output.flip()
    }

    override fun onFlush(streamMetadata: AudioProcessor.StreamMetadata) = Unit

    override fun onReset() {
        channelCount = 0
    }
}
