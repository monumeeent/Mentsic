/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.max
import kotlin.math.min

/**
 * Audio reactive waveform visualizer that draws animated bars.
 * Uses organic oscillation to simulate audio reactivity when real-time
 * audio analysis is not available.
 *
 * @param barCount Number of vertical bars to render
 * @param barSpacing Spacing between bars in density-independent pixels
 * @param barRadius Corner radius for the bars
 * @param colors List of colors to cycle through for the bars
 * @param modifier Modifier for the canvas
 * @param enabled Whether the visualizer is active
 */
@Composable
fun AudioVisualizer(
    barCount: Int = 32,
    barSpacing: Dp = 1f.dp,
    barRadius: Dp = 2f.dp,
    colors: List<Color> = emptyList(),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val density = LocalDensity.current
    val spacingPx = with(density) { barSpacing.toPx() }
    val radiusPx = with(density) { barRadius.toPx() }

    var amplitudes by remember { mutableStateOf(FloatArray(barCount) { 0.1f }) }
    val barAnimatables = remember(barCount) {
        Array(barCount) { i ->
            Animatable(0.1f)
        }
    }

    // Simulate audio reactivity with organic-looking oscillation
    LaunchedEffect(enabled) {
        while (isActive) {
            delay(50L)
            if (enabled) {
                val currentTime = System.currentTimeMillis().toFloat()
                val newAmplitudes = FloatArray(barCount) { i ->
                    val t = i.toFloat() / max(1f, (barCount - 1).toFloat())
                    // Create a bell-curve-like base with traveling wave
                    val base = 0.2f + 0.5f * (1f - (t - 0.5f) * (t - 0.5f) * 4f)
                    // Traveling sine wave for animation
                    val wave = 0.2f * (kotlin.math.sin(t * 10f + currentTime * 0.004f) + 1f) * 0.5f
                    // Random noise for organic feel
                    val noise = ((Math.random() - 0.5) * 0.08).toFloat()
                    min(1.0f, max(0.05f, base + wave + noise))
                }
                amplitudes = newAmplitudes
            } else {
                // Decay to minimal when disabled
                amplitudes.fill(0.05f)
            }
        }
    }

    // Smoothly animate bars toward target amplitudes
    LaunchedEffect(amplitudes) {
        amplitudes.forEachIndexed { i, amp ->
            if (i < barAnimatables.size) {
                barAnimatables[i].animateTo(
    amp,
    animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
)
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp),
    ) {
        val barWidth = (size.width - (barCount - 1) * spacingPx) / barCount
        val maxBarHeight = size.height * 0.85f

        for (i in barAnimatables.indices) {
            val height = barAnimatables[i].value * maxBarHeight
            val x = i * (barWidth + spacingPx)
            val y = size.height - height

            val color = if (colors.isNotEmpty()) colors[i % colors.size] else Color.White.copy(alpha = 0.7f)

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, max(height, 2f)),
                cornerRadius = CornerRadius(radiusPx, radiusPx),
            )
        }
    }
}
