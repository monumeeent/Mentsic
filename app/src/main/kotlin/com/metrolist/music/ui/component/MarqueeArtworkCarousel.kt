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
 *  - **Infinite loop by modular offset.** The strip is drawn from a tiling of [tiles] repeated
 *    [repeat] times; scrolling only moves the base offset, so the loop is seamless and there is no
 *    "reached the end" state.
 *  - **Opposite directions.** Row one drifts left, row two drifts right. Each has its own offset so
 *    the two never line up.
 *  - **Drag.** A horizontal drag adds to that row's offset with the same "throw" feel as the web
 *    version — release keeps the velocity, and friction settles it back to the base drift.
 *  - **Tilt.** Each card carries a fixed rotation that increases along the row, drawn in
 *    `graphicsLayer`, which is what gives the perspective "fan" of the original.
 *
 * Nothing here is decorative-only: every tile is the user's actual song artwork and tapping it plays
 * that song, so the block is a usable surface rather than a hero banner.
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.metrolist.music.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Two-row marquee of tilted artwork cards.
 *
 * @param items the songs to show; split evenly across the two rows.
 * @param onItemClick invoked with the tapped item.
 * @param modifier outer modifier.
 */
@Composable
fun <T> MarqueeArtworkCarousel(
    items: List<T>,
    thumbnailOf: (T) -> String?,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    cardSize: androidx.compose.ui.unit.Dp = 108.dp,
) {
    if (items.isEmpty()) return
    // Alternate items into two rows so neither row is a run of the same album.
    val firstRow = remember(items) { items.filterIndexed { index, _ -> index % 2 == 0 } }
    val secondRow = remember(items) { items.filterIndexed { index, _ -> index % 2 == 1 } }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        MarqueRow(
            items = firstRow,
            thumbnailOf = thumbnailOf,
            onItemClick = onItemClick,
            cardSize = cardSize,
            direction = -1f,
        )
        MarqueRow(
            items = secondRow,
            thumbnailOf = thumbnailOf,
            onItemClick = onItemClick,
            cardSize = cardSize,
            direction = 1f,
        )
    }
}

/**
 * One drifting row.
 *
 * The row is a `LazyRow` whose scroll position auto-advances; a drag simply pauses the auto-advance and
 * hands control to the finger, then resumes. That is the closest Compose equivalent of the web
 * marquee's drag-aware behaviour, and it keeps recycling the item composables instead of drawing an
 * ever-growing strip of bitmaps.
 */
@Composable
private fun <T> MarqueRow(
    items: List<T>,
    thumbnailOf: (T) -> String?,
    onItemClick: (T) -> Unit,
    cardSize: androidx.compose.ui.unit.Dp,
    direction: Float,
) {
    if (items.isEmpty()) return
    val density = LocalDensity.current
    val gapPx = with(density) { Spacing.small.toPx() }
    val itemPx = with(density) { cardSize.toPx() } + gapPx
    var dragging by remember { mutableFloatStateOf(0f) }

    // Auto-drift. The strip is drawn as `loopCount` tiles and the whole strip slides by `drift`
    // pixels; the effect advances a few pixels per frame and wraps modulo one full pass, which is
    // what makes the loop seamless in both directions.
    var drift by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(items.size, direction) {
        val total = (items.size * itemPx).coerceAtLeast(1f)
        while (true) {
            delay(16)
            if (dragging > 0.5f) continue
            drift += 1.6f * direction
            if (drift <= -total) drift += total
            if (drift >= total) drift -= total
        }
    }
    // Tiles beyond the visible window so the wrap point is never on screen.
    val overscan = 2
    val loopCount = items.size + overscan * 2

    val tileWidth = cardSize
    val stepPx = itemPx
    val totalPx = (items.size * stepPx).coerceAtLeast(1f)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(cardSize + Spacing.small * 2)
                .clip(RoundedCornerShape(0.dp))
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
        // `span` copies are enough to cover the widest phone twice over, so the strip never shows a
        // gap at either edge however far it has drifted.
        val span = 3
        Row(
            modifier =
                Modifier.offset { IntOffset((drift - stepPx * overscan).roundToInt(), 0) },
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            repeat(loopCount * span) { slot ->
                // Which real item this slot shows: whole-tile drift is folded into the sequence, the
                // fractional part stays in the offset above, so motion looks continuous.
                val shift = kotlin.math.floor(drift / stepPx).toInt()
                val realIndex = (((slot - overscan + shift).mod(items.size)) + items.size) % items.size
                val item = items[realIndex]
            // Fixed per-index tilt: the fan effect of the original, largest at the edges of a short
            // run and smallest in the middle.
            val tilt = remember(realIndex, items.size) {
                val centre = (items.size - 1) / 2f
                ((realIndex - centre) / centre.coerceAtLeast(1f)) * 7f
            }
            // Staggered entrance: each card fades and unblurs in turn, like the reference's
            // delay: 0.3 + 0.1 * index.
            var entered by remember(realIndex) { mutableFloatStateOf(0f) }
            LaunchedEffect(realIndex) {
                delay(60L * realIndex)
                entered = 1f
            }
            val appear by animateFloatAsState(
                targetValue = entered,
                animationSpec = tween(420),
                label = "marquee-appear",
            )

            Box(
                modifier =
                    Modifier
                        .width(cardSize)
                        .aspectRatio(1f)
                        .graphicsLayer {
                            rotationY = tilt
                            // A little depth so the row reads as a fan rather than a flat strip.
                            rotationZ = tilt * 0.4f
                            cameraDistance = 12f * density.density
                            alpha = appear
                        },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    AsyncImage(
                        model = thumbnailOf(item),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .blur((1f - appear) * 8.dp),
                    )
                    // Bottom scrim so the rounded card edge stays visible over bright artwork.
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        androidx.compose.ui.graphics.Color.Transparent,
                                        MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.25f),
                                    ),
                                ),
                            ),
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                Modifier.clickableNoIndication { onItemClick(item) },
                            ),
                )
            }
        }
    }
}

/** Clickable without the ripple; these cards are moving, so a ripple reads as a glitch. */
private fun Modifier.clickableNoIndication(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(interactionSource = null, indication = null, onClick = onClick))
