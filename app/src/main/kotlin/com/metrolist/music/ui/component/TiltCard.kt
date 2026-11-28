/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * 3D tilt for a card — the Compose port of the "Tilt" wrapper used on the Personal Space cards.
 *
 * The original is a pointer-tracking transform: as the cursor/finger moves across the card, the card
 * rotates slightly toward it, which reads as a physical tile you can push. On touch, there is no hover,
 * so the gesture that matters is the drag — [TiltCard] follows the finger while it is down and springs
 * back flat on release.
 *
 * Design notes:
 *  - Rotation is applied in `graphicsLayer`, so the tilt costs a render pass, not a recomposition of
 *    the card content (which holds charts, images and text).
 *  - `cameraDistance` is set explicitly: without it Compose's default produces a very wide perspective
 *    and the card looks warped rather than tilted.
 *  - The effect is purely decorative; when reduced motion is on, or the preference is off, the wrapper
 *    is a pass-through and the content is laid out exactly as before.
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity

/**
 * Wraps [content] in a card that tilts toward the touch point.
 *
 * @param enabled false renders the content untouched (preference off or reduced motion).
 * @param rotationFactor peak rotation, in degrees, at the card's corners. The upstream default is 8.
 * @param reverse mirrors the direction of the tilt (the `isRevese` prop of the original).
 */
@Composable
fun TiltCard(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    rotationFactor: Float = 8f,
    reverse: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    if (!enabled) {
        Box(modifier = modifier, content = content)
        return
    }

    // Rotation targets, in degrees, around the X (vertical drag) and Y (horizontal drag) axes.
    val rotateX = remember { Animatable(0f) }
    val rotateY = remember { Animatable(0f) }

    // A close camera: the perspective has to be strong enough to read as depth, but not so strong
    // that the far edge of the card is half its size.
    val cameraDistance = with(LocalDensity.current) { 12 * density }

    val springSpec =
        remember {
            spring<Float>(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )
        }

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    rotationX = rotateX.value
                    rotationY = rotateY.value
                    this.cameraDistance = cameraDistance
                }
                .pointerInput(rotationFactor, reverse) {
                    // `awaitEachGesture` rather than `detectDragGestures`: the card must also flatten
                    // when the finger lifts without moving, and a plain drag detector would leave the
                    // last rotation in place.
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        // Do not consume here — the card is also clickable, and the click has to
                        // survive the tilt.
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null && size.width > 0 && size.height > 0) {
                                // -1..1 from the centre on each axis.
                                val nx = (change.position.x / size.width - 0.5f) * 2f
                                val ny = (change.position.y / size.height - 0.5f) * 2f
                                val sign = if (reverse) -1f else 1f
                                rotateY.snapTo(rotationFactor * nx * sign)
                                rotateX.snapTo(-rotationFactor * ny * sign)
                            }
                        } while (event.changes.any { it.pressed })
                        // Released: spring back to flat.
                        rotateX.animateTo(0f, springSpec)
                        rotateY.animateTo(0f, springSpec)
                    }
                },
    ) {
        content()
    }
}
