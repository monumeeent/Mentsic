/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Third mini-player design: the **pixel capsule**.
 *
 * Not a rounded pill with a progress bar — a pixel-art instrument:
 *
 *  - **Pixel frame.** The capsule is drawn on a grid, with the corners *cut out* (a chamfered
 *    octagon) instead of rounded, and a 1px inner scanline. It reads as hardware, not as a card.
 *  - **Pixel spectrum.** A column of quantised blocks that steps up and down with the analysed
 *    audio (from [VisualizerState], i.e. the real decoded signal), not a smooth curve.
 *  - **Pixel typography.** Title is uppercased and wide-tracked; the artist line is dimmed and
 *    tracked, so the pair reads like a two-line LCD.
 *  - **Pixel art.** The album art is inset into a square with corner pixels knocked out, so even
 *    the bitmap looks like it came off a 16×16 sprite sheet.
 *
 * Toggleable via [MiniPlayerCapsuleKey]; `MiniPlayer` picks this layout when it is on.
 */

package com.metrolist.music.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.constants.MiniPlayerHeight
import com.metrolist.music.playback.audio.VisualizerFrame
import com.metrolist.music.playback.audio.VisualizerState
import com.metrolist.music.ui.theme.Spacing
import com.metrolist.music.utils.joinToArtistString

/** Grid step of the whole design. Everything in the capsule is a multiple of this. */
private val Pixel = 4.dp

/** Columns of the pixel spectrum. 8 matches the analyser's band count exactly — one column per band. */
private const val SpectrumColumns = 8

/** Height of the spectrum in *block* rows; the drawing quantises amplitude to these steps. */
private const val SpectrumRows = 5

/**
 * Pixel capsule mini player.
 *
 * @param progressState shared progress source. Unused for drawing here — the spectrum carries the
 *   "is it moving" signal — but kept in the signature so both designs stay swappable.
 */
@Composable
fun CapsuleMiniPlayer(
    progressState: ProgressState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    // `LocalPlayerConnection` lives in `com.metrolist.music` (declared next to the provider in
    // MainActivity), not in `ui.component`.
    val playerConnection = LocalPlayerConnection.current ?: return
    val scheme = MaterialTheme.colorScheme

    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle(initialValue = null)
    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle(initialValue = false)
    val playbackState by playerConnection.playbackState.collectAsStateWithLifecycle(initialValue = Player.STATE_IDLE)
    val frame by VisualizerState.frame.collectAsStateWithLifecycle()

    val interactionSource = remember { MutableInteractionSource() }
    val ended = playbackState == Player.STATE_ENDED

    Box(
        modifier = modifier.fillMaxWidth().height(MiniPlayerHeight),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium)
                    .height(56.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    ),
        ) {
            // Pixel frame: chamfered corners + 1px scanline, painted behind the content.
            PixelPanel(
                modifier = Modifier.fillMaxSize(),
                fill = scheme.surfaceContainerHigh,
                outline = scheme.primary.copy(alpha = 0.55f),
                scanline = scheme.primary.copy(alpha = 0.10f),
            )

            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = Pixel, vertical = Pixel),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PixelArt(
                    url = mediaMetadata?.thumbnailUrl,
                    isPlaying = isPlaying,
                    ended = ended,
                    onClick = {
                        if (ended) {
                            playerConnection.player.seekTo(0, 0)
                            playerConnection.player.playWhenReady = true
                        } else {
                            playerConnection.togglePlayPause()
                        }
                    },
                )

                Spacer(Modifier.width(Spacing.small))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        // Uppercased + tracked: the two lines read as an LCD readout.
                        text = mediaMetadata?.title.orEmpty().uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text =
                            mediaMetadata
                                ?.artists
                                ?.joinToArtistString(", ") { it.name }
                                .orEmpty()
                                .uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.width(Spacing.small))

                PixelSpectrum(
                    frame = frame,
                    active = isPlaying && !ended,
                    modifier = Modifier.width(Pixel * SpectrumColumns * 2 + Pixel * 3).height(32.dp),
                )

                Spacer(Modifier.width(Pixel))

                // Transport glyph, drawn as a pixel arrow rather than a Material icon so the right
                // edge of the capsule stays on-grid.
                PixelTransport(
                    playing = isPlaying && !ended,
                    ended = ended,
                    onClick = {
                        if (ended) {
                            playerConnection.player.seekTo(0, 0)
                            playerConnection.player.playWhenReady = true
                        } else {
                            playerConnection.togglePlayPause()
                        }
                    },
                )
            }
        }
    }
}

/**
 * Chamfered panel: 4dp corners are removed on a diagonal, then a hairline scanline is drawn inside.
 *
 * The chamfer is the cheapest way to say "pixel" without literally drawing a sprite: a radius would
 * read as a modern card, a 45° cut reads as a tile.
 */
@Composable
private fun PixelPanel(
    modifier: Modifier = Modifier,
    fill: Color,
    outline: Color,
    scanline: Color,
) {
    Canvas(modifier = modifier) {
        val c = Pixel.toPx()
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(c, 0f)
            lineTo(w - c, 0f)
            lineTo(w, c)
            lineTo(w, h - c)
            lineTo(w - c, h)
            lineTo(c, h)
            lineTo(0f, h - c)
            lineTo(0f, c)
            close()
        }

        drawPath(path = path, color = fill)
        drawPath(path = path, color = outline, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))

        // Inner scanline: one dot every two steps, fading toward the right. This is the "texture"
        // that sells the pixel look at a glance.
        var x = c
        while (x < w - c) {
            drawRect(
                color = scanline.copy(alpha = scanline.alpha * (1f - x / w)),
                topLeft = Offset(x, h - 2.dp.toPx()),
                size = Size(2.dp.toPx(), 2.dp.toPx()),
            )
            x += Pixel.toPx()
        }
    }
}

/** Album art in a pixel frame: square, corner pixels knocked out, steppped play indicator. */
@Composable
private fun PixelArt(
    url: String?,
    isPlaying: Boolean,
    ended: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.size(40.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = 6.dp.toPx()
            val path = Path().apply {
                moveTo(c, 0f)
                lineTo(size.width - c, 0f)
                lineTo(size.width, c)
                lineTo(size.width, size.height - c)
                lineTo(size.width - c, size.height)
                lineTo(c, size.height)
                lineTo(0f, size.height - c)
                lineTo(0f, c)
                close()
            }
            drawPath(path = path, color = scheme.surfaceContainerHighest)
        }

        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().padding(3.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(1.dp)),
            )
        }

        if (!isPlaying || ended) {
            Canvas(modifier = Modifier.size(14.dp)) {
                val step = size.width / 4f
                // 3×3 pixel glyph: play triangle, or a replay bracket when the track has ended.
                val glyph =
                    if (ended) {
                        listOf(
                            Triple(2, 0, true), Triple(1, 0, true), Triple(0, 0, true),
                            Triple(0, 1, true), Triple(0, 2, true),
                            Triple(1, 2, true), Triple(2, 2, true),
                        )
                    } else {
                        listOf(
                            Triple(0, 0, true), Triple(0, 1, true), Triple(0, 2, true),
                            Triple(1, 1, true), Triple(2, 1, true),
                            Triple(1, 0, true), Triple(1, 2, true),
                        )
                    }
                glyph.forEach { (x, y, _) ->
                    drawRect(
                        color = scheme.onPrimary,
                        topLeft = Offset(x * step, y * step),
                        size = Size(step, step),
                    )
                }
            }
        }
    }
}

/**
 * Quantised spectrum: one column per analyser band, each a stack of [SpectrumRows] blocks.
 *
 * Quantising is deliberate — a smooth curve would fight the pixel language, and an LCD-style step
 * meter is what makes a handful of blocks read as a level. Blocks light bottom-up, tinted from
 * `primary` to `tertiary` by row so the stack has its own little gradient.
 */
@Composable
private fun PixelSpectrum(
    frame: VisualizerFrame,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Canvas(modifier = modifier) {
        val gap = Pixel.toPx() * 0.5f
        val colW = (size.width - gap * (SpectrumColumns - 1)) / SpectrumColumns
        val rowH = (size.height - gap * (SpectrumRows - 1)) / SpectrumRows

        for (col in 0 until SpectrumColumns) {
            val band = frame.bands.getOrNull(col) ?: 0f
            // Beat pulse lifts the whole column, so the meter visibly jumps on a transient.
            val energy = if (active) (band + frame.pulse * 0.30f).coerceIn(0f, 1f) else 0f
            val litRows = if (energy <= 0.001f) 0 else ((energy * SpectrumRows) + 0.35f).toInt().coerceIn(1, SpectrumRows)

            val x = col * (colW + gap)
            for (row in 0 until SpectrumRows) {
                // Row 0 is the bottom of the stack.
                val y = size.height - (row + 1) * rowH - row * gap
                val lit = row < litRows
                val t = row.toFloat() / (SpectrumRows - 1).coerceAtLeast(1)
                val color =
                    if (lit) {
                        androidx.compose.ui.graphics.lerp(scheme.primary, scheme.tertiary, t)
                    } else {
                        scheme.onSurfaceVariant.copy(alpha = 0.10f)
                    }
                drawRect(
                    color = color.copy(alpha = if (lit) 0.65f + 0.35f * t else 1f),
                    topLeft = Offset(x, y),
                    size = Size(colW, rowH),
                )
            }
        }
    }
}

/** Stepped play/pause/replay glyph, built from squares so it stays on the pixel grid. */
@Composable
private fun PixelTransport(
    playing: Boolean,
    ended: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.size(28.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val cell = size.width / 4f
            val cells: List<Pair<Int, Int>> =
                when {
                    ended ->
                        listOf(2 to 0, 1 to 0, 0 to 0, 0 to 1, 0 to 2, 1 to 2, 2 to 2)
                    playing -> listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 2 to 0, 2 to 1, 2 to 2, 2 to 3)
                    else -> listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 1 to 1, 1 to 2, 2 to 1, 2 to 2)
                }
            cells.forEach { (x, y) ->
                drawRect(
                    color = scheme.onSurface,
                    topLeft = Offset(x * cell, y * cell),
                    size = Size(cell * 0.8f, cell * 0.8f),
                )
            }
        }
    }
}
