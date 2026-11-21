/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Listen Together "stage" building blocks — an animated alternative to the stacked screen.
 *
 * Design language, deliberately unlike the rest of the app:
 *
 *  - **Pulse core.** The connection state is a breathing orb whose halo expands with the current
 *    state's energy (connecting is fast and nervous, connected is slow and calm).
 *  - **Listener constellation.** In-room listeners orbit the core on a ring, each tethered to it by
 *    a thin "signal" line, so the room reads as a live constellation rather than a list.
 *  - **Broadcast code.** The room code is set in wide-tracked monospace with a scanline sweeping
 *    across it — it reads as a signal, not a form field.
 *
 * All colour comes from `MaterialTheme.colorScheme`; all motion uses Compose animation APIs. No
 * WebView or JS: three.js is not applicable to a native Compose surface, and embedding it for a
 * decorative effect would add ~600 KB to the APK.
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.music.ui.theme.Spacing
import kotlin.math.cos
import kotlin.math.sin

/**
 * Breathing halo behind the stage core.
 *
 * Driven by one infinite transition whose *duration* changes with [energy], so the pulse can feel
 * calm when connected and agitated while connecting without restarting an animation.
 *
 * @param energy 0..1 — how agitated the pulse is. Connected ≈ 0.2, connecting ≈ 0.9.
 */
@Composable
fun StagePulse(
    modifier: Modifier = Modifier,
    energy: Float = 0.25f,
    color: Color = MaterialTheme.colorScheme.primary,
    rings: Int = 3,
) {
    // Faster (shorter) period at high energy. Clamped so it never strobes.
    val periodMillis = (2600 - 1600 * energy).toInt().coerceIn(900, 2600)
    val transition = rememberInfiniteTransition(label = "stage-pulse")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing)),
        label = "stage-pulse-phase",
    )

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val centre = Offset(size.width / 2f, size.height / 2f)
        val coreRadius = size.minDimension * 0.16f
        val maxRadius = size.minDimension * 0.48f

        // Expanding rings, each offset by a third of the cycle.
        for (ring in rings downTo 1) {
            val ringPhase = (t + ring / (rings + 1f)) % 1f
            val radius = coreRadius + (maxRadius - coreRadius) * ringPhase
            val alpha = (1f - ringPhase) * (0.18f + 0.22f * energy)
            drawCircle(
                color = color.copy(alpha = alpha.coerceIn(0f, 0.5f)),
                radius = radius,
                center = centre,
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }

        // Solid core with a soft radial falloff.
        drawCircle(
            brush =
                Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0.12f)),
                    center = centre,
                    radius = coreRadius * 1.7f,
                ),
            radius = coreRadius * 1.7f,
            center = centre,
        )
    }
}

/**
 * Listeners arranged on a ring around the stage core.
 *
 * @param listeners `username to connected` pairs. Disconnected listeners render as hollow nodes.
 * @param isHostIndex index of the host, tinted with the tertiary accent.
 */
@Composable
fun ListenerConstellation(
    listeners: List<Pair<String, Boolean>>,
    modifier: Modifier = Modifier,
    isHostIndex: Int = 0,
) {
    val scheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "constellation")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(60_000, easing = LinearEasing)),
        label = "constellation-spin",
    )

    Canvas(modifier = modifier.fillMaxWidth().height(160.dp)) {
        val centre = Offset(size.width / 2f, size.height / 2f)
        val ringRadius = size.minDimension * 0.34f
        val nodeRadius = 9.dp.toPx()

        drawCircle(
            color = scheme.outlineVariant.copy(alpha = 0.4f),
            radius = ringRadius,
            center = centre,
            style = Stroke(width = 1.dp.toPx()),
        )

        val count = listeners.size.coerceAtLeast(1)
        listeners.forEachIndexed { index, listener ->
            val connected = listener.second
            // A full revolution per minute, plus a per-index offset so nodes do not align.
            val angle =
                (index.toFloat() / count) * 2f * Math.PI.toFloat() +
                    spin * 2f * Math.PI.toFloat() +
                    (index % 3) * 0.12f

            val node =
                Offset(
                    x = centre.x + ringRadius * cos(angle),
                    y = centre.y + ringRadius * sin(angle),
                )
            val hue = if (index == isHostIndex) scheme.tertiary else scheme.primary

            drawLine(
                color = hue.copy(alpha = if (connected) 0.22f else 0.08f),
                start = centre,
                end = node,
                strokeWidth = 1.dp.toPx(),
            )

            if (connected) {
                drawCircle(color = hue.copy(alpha = 0.22f), radius = nodeRadius * 2.1f, center = node)
                drawCircle(color = hue, radius = nodeRadius, center = node)
            } else {
                drawCircle(
                    color = hue.copy(alpha = 0.45f),
                    radius = nodeRadius,
                    center = node,
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
        }
    }
}

/** Room code rendered as a broadcast signal: oversized, wide-tracked, with a sweeping scanline. */
@Composable
fun BroadcastRoomCode(
    code: String,
    modifier: Modifier = Modifier,
    emphasis: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "room-code-scan")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "room-code-sweep",
    )

    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape)
                .background(scheme.surfaceContainerHighest.copy(alpha = 0.6f))
                .border(1.dp, scheme.primary.copy(alpha = if (emphasis) 0.45f else 0.18f), shape)
                .padding(vertical = Spacing.medium),
        contentAlignment = Alignment.Center,
    ) {
        // Scanline painted behind the glyphs.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val x = sweep * size.width
            drawRect(
                brush =
                    Brush.horizontalGradient(
                        colorStops =
                            arrayOf(
                                0f to Color.Transparent,
                                ((x - size.width * 0.12f).coerceAtLeast(0f) / size.width) to Color.Transparent,
                                (x / size.width).coerceIn(0.001f, 0.999f) to scheme.primary.copy(alpha = 0.22f),
                                ((x + size.width * 0.12f).coerceAtMost(size.width) / size.width) to Color.Transparent,
                                1f to Color.Transparent,
                            ),
                    ),
                size = size,
            )
        }

        Text(
            text = code.ifBlank { "· · · ·" },
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 10.sp,
            color = scheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}
