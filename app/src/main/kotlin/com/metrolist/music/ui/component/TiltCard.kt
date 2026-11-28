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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

    // Target rotation, in degrees, around the X (vertical drag) and Y (horizontal drag) axes. These
    // are plain snapshot state written from the pointer handler: `AwaitPointerEventScope` is a
    // *restricted* suspend scope, so it may not call `Animatable.snapTo`/`animateTo` (that is the
    // ILLEGAL_RESTRICTED_SUSPENDING_FUNCTION_CALL this file used to raise). The gesture only sets a
    // target; an animation effect below glides the card toward it and back to flat on release.
    var targetRotateX by remember { mutableFloatStateOf(0f) }
    var targetRotateY by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }

    val animatedRotateX by animateFloatAsState(
        targetValue = targetRotateX,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                // Snappy while the finger is down, relaxed on the spring back to flat.
                stiffness = if (dragging) Spring.StiffnessHigh else Spring.StiffnessLow,
            ),
        label = "tilt-rotate-x",
    )
    val animatedRotateY by animateFloatAsState(
        targetValue = targetRotateY,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = if (dragging) Spring.StiffnessHigh else Spring.StiffnessLow,
            ),
        label = "tilt-rotate-y",
    )

    // A close camera: the perspective has to be strong enough to read as depth, but not so strong
    // that the far edge of the card is half its size.
    val cameraDistance = with(LocalDensity.current) { 12 * density }

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    rotationX = animatedRotateX
                    rotationY = animatedRotateY
                    this.cameraDistance = cameraDistance
                }
                .pointerInput(rotationFactor, reverse) {
                    // `awaitEachGesture` rather than `detectDragGestures`: the card must also flatten
                    // when the finger lifts without moving, and a plain drag detector would leave the
                    // last rotation in place.
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        dragging = true
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
                                // Assign the targets only; the animation effect does the moving.
                                targetRotateY = rotationFactor * nx * sign
                                targetRotateX = -rotationFactor * ny * sign
                            }
                        } while (event.changes.any { it.pressed })
                        // Released: let the animation spec spring back to flat.
                        dragging = false
                        targetRotateX = 0f
                        targetRotateY = 0f
                    }
                },
    ) {
        content()
    }
}
