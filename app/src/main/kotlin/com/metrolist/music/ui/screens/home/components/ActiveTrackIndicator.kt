/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.sin

/**
 * Three animated equaliser bars indicating the active/playing track state.
 *
 * Performance notes:
 * - Entirely draw-phase: only Canvas pixels are redrawn — no layout or composition
 *   invalidation occurs during animation.
 * - Accepts [isPlaying] as a lambda to avoid capturing the outer composition scope
 *   and prevent unnecessary recompositions in the parent.
 * - [accentColor] should be derived from `MaterialTheme.colorScheme.primary` at the
 *   call site so this composable itself remains stateless and colour-system-agnostic.
 *
 * Usage:
 * ```
 * ActiveTrackIndicator(
 *     isPlaying = { isPlaying },
 *     accentColor = MaterialTheme.colorScheme.primary,
 *     modifier = Modifier.size(24.dp),
 * )
 * ```
 */
@Composable
fun ActiveTrackIndicator(
    isPlaying: () -> Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "equaliser")

    // Three bars with offset phase so they animate independently
    val bar1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 700, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "bar1",
    )
    val bar2 by transition.animateFloat(
        initialValue = (Math.PI / 2).toFloat(),
        targetValue = (2 * Math.PI + Math.PI / 2).toFloat(),
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "bar2",
    )
    val bar3 by transition.animateFloat(
        initialValue = (Math.PI).toFloat(),
        targetValue = (3 * Math.PI).toFloat(),
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "bar3",
    )

    Canvas(modifier = modifier) {
        val playing = isPlaying()
        val barCount = 3
        val barWidth = size.width / (barCount * 2 - 1)
        val cornerRadius = CornerRadius(barWidth / 2f)

        val heights =
            if (playing) {
                listOf(
                    (0.3f + 0.7f * ((sin(bar1.toDouble()) + 1) / 2f)).toFloat(),
                    (0.3f + 0.7f * ((sin(bar2.toDouble()) + 1) / 2f)).toFloat(),
                    (0.3f + 0.7f * ((sin(bar3.toDouble()) + 1) / 2f)).toFloat(),
                )
            } else {
                // Static mid-height when paused
                listOf(0.4f, 0.6f, 0.4f)
            }

        heights.forEachIndexed { index, heightFraction ->
            val barHeight = size.height * heightFraction
            val x = index * (barWidth * 2)
            val y = size.height - barHeight
            drawRoundRect(
                color = accentColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius,
            )
        }
    }
}

