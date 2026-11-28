/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Two-row marquee carousel — the "New Arrivals" style block used above Keep Listening.
 *
 * The reference implementation is a web component: two infinite marquees running in opposite
 * directions, each item a tilted 3D card with a blurred-in entrance, and a drag that can push the
 * strip around. This is the Compose equivalent:
 *
 *  - **Infinite loop by modular offset.** The strip is laid out as a fixed run of tiles and the whole
 *    run slides by `drift` pixels; the slot → item mapping folds the whole-tile part of that offset
 *    back into the sequence, so the loop is seamless and there is no "reached the end" state.
 *  - **Opposite directions.** Row one drifts left, row two drifts right, each with its own offset.
 *  - **Drag.** A horizontal drag adds to the row's offset one-for-one and pauses the auto-drift.
 *  - **Tilt.** Each card carries a fixed rotation that grows toward the edges of the run, drawn in
 *    `graphicsLayer` — the perspective "fan" of the original.
 *
 * Every tile is the user's actual song artwork and tapping it plays that song, so the block is a
 * usable surface rather than a decorative hero banner.
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.metrolist.music.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Inter-run gap between tiles, and the tile count of one full loop pass. */
private const val Overscan = 2

/**
 * Two-row marquee of tilted artwork cards.
 *
 * @param items the songs to show; alternating items are split across the two rows so neither row is a
 *   run of the same album.
 * @param thumbnailOf artwork URL for an item.
 * @param onItemClick invoked with the tapped item.
 */
@Composable
fun <T> MarqueeArtworkCarousel(
    items: List<T>,
    thumbnailOf: (T) -> String?,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    cardSize: Dp = 108.dp,
) {
    if (items.isEmpty()) return
    val firstRow = remember(items) { items.filterIndexed { index, _ -> index % 2 == 0 } }
    val secondRow = remember(items) { items.filterIndexed { index, _ -> index % 2 == 1 } }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        if (firstRow.isNotEmpty()) {
            MarqueeRow(
                items = firstRow,
                thumbnailOf = thumbnailOf,
                onItemClick = onItemClick,
                cardSize = cardSize,
                direction = -1f,
            )
        }
        if (secondRow.isNotEmpty()) {
            MarqueeRow(
                items = secondRow,
                thumbnailOf = thumbnailOf,
                onItemClick = onItemClick,
                cardSize = cardSize,
                direction = 1f,
            )
        }
    }
}

/**
 * One drifting row.
 *
 * A `Row` of `(items + overscan * 2) * 3` tiles is translated as a whole. Three spans guarantee the
 * strip covers the screen twice over at any drift offset, so no gap can appear at either edge.
 *
 * @param direction -1 drifts left, +1 drifts right.
 */
@Composable
private fun <T> MarqueeRow(
    items: List<T>,
    thumbnailOf: (T) -> String?,
    onItemClick: (T) -> Unit,
    cardSize: Dp,
    direction: Float,
) {
    val density = LocalDensity.current
    val gapPx = with(density) { Spacing.small.toPx() }
    val stepPx = with(density) { cardSize.toPx() } + gapPx
    val totalPx = (items.size * stepPx).coerceAtLeast(1f)

    var drift by remember(items.size) { mutableFloatStateOf(0f) }
    var dragging by remember { mutableFloatStateOf(0f) }

    // Auto-drift: a few pixels per frame, wrapped modulo one full pass so the loop never ends.
    LaunchedEffect(items.size, direction) {
        while (true) {
            delay(16)
            if (dragging > 0.5f) continue
            drift += 1.6f * direction
            if (drift <= -totalPx) drift += totalPx
            if (drift >= totalPx) drift -= totalPx
        }
    }

    val loopCount = items.size + Overscan * 2
    val span = 3

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(cardSize + Spacing.small * 2)
                .pointerInput(items.size, direction) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragging = 1f },
                        onDragCancel = { dragging = 0f },
                        onDragEnd = { dragging = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            // Direct manipulation: the finger pushes the strip one-for-one, wrapped
                            // inside one full pass so the loop stays seamless.
                            drift = (drift + dragAmount).let { value ->
                                when {
                                    value <= -totalPx -> value + totalPx
                                    value >= totalPx -> value - totalPx
                                    else -> value
                                }
                            }
                        },
                    )
                },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier =
                Modifier.offset { IntOffset((drift - stepPx * Overscan).roundToInt(), 0) },
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            // `shift` moves whole tiles through the sequence; the fractional part stays in the offset
            // above, so motion is continuous instead of stepping one card at a time.
            val shift = kotlin.math.floor(drift / stepPx).toInt()
            repeat(loopCount * span) { slot ->
                val realIndex = Math.floorMod(slot - Overscan + shift, items.size)
                // Fixed per-index tilt: the fan effect of the original, largest at the edges. Plain
                // arithmetic, so it needs no `remember` (this lambda is not a composable scope).
                val centre = (items.size - 1) / 2f
                val tilt = ((realIndex - centre) / centre.coerceAtLeast(1f)) * 7f
                MarqueeCard(
                    item = items[realIndex],
                    thumbnailUrl = thumbnailOf(items[realIndex]),
                    cardSize = cardSize,
                    tilt = tilt,
                    // Staggered entrance keyed on the *slot*, so a looping strip does not re-animate
                    // every time an item wraps around.
                    entranceDelayMillis = (slot % loopCount) * 60L,
                    onItemClick = { onItemClick(items[realIndex]) },
                )
            }
        }
    }
}

/**
 * One artwork tile: rounded card, image, bottom scrim and a tap target.
 *
 * The entrance is a blur + fade driven by an `animateFloatAsState` over a value set after
 * [entranceDelayMillis] — the Compose form of the reference's staggered `filter: blur(...)` animation.
 */
@Composable
private fun <T> MarqueeCard(
    item: T,
    thumbnailUrl: String?,
    cardSize: Dp,
    tilt: Float,
    entranceDelayMillis: Long,
    onItemClick: () -> Unit,
) {
    var entered by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(entranceDelayMillis) {
        delay(entranceDelayMillis)
        entered = 1f
    }
    val appear by animateFloatAsState(
        targetValue = entered,
        animationSpec = tween(420),
        label = "marquee-appear",
    )
    // Perspective strength: 12× density is a close camera, strong enough to read as depth without
    // warping the far edge of the card.
    val cameraDistance = with(LocalDensity.current) { 12 * density }

    Box(
        modifier =
            Modifier
                .width(cardSize)
                .aspectRatio(1f)
                .graphicsLayer {
                    rotationY = tilt
                    // A little depth so the row reads as a fan rather than a flat strip.
                    rotationZ = tilt * 0.4f
                    this.cameraDistance = cameraDistance
                    alpha = appear
                }
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                // No ripple: these cards are moving, so an expanding surface reads as a glitch.
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onItemClick,
                ),
    ) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur((1f - appear) * 8.dp),
        )
        // Bottom scrim so the rounded edge stays visible over bright artwork.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.25f),
                        ),
                    ),
                ),
        )
    }
}
