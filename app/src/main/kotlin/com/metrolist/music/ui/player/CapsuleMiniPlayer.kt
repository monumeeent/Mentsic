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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.metrolist.music.LocalDatabase
import com.metrolist.music.LocalDownloadUtil
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.constants.MiniPlayerHeight
import com.metrolist.music.playback.audio.VisualizerFrame
import com.metrolist.music.playback.audio.VisualizerState
import com.metrolist.music.ui.theme.Spacing
import com.metrolist.music.utils.joinToArtistString
import kotlinx.coroutines.flow.flowOf

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

    // Like and download state, read exactly the way the stacked mini player does, so the two designs
    // can never disagree about whether the current song is saved.
    val database = LocalDatabase.current
    val downloadUtil = LocalDownloadUtil.current
    val songId = mediaMetadata?.id
    // `database.song()` is a Flow<Song?>; a null id has no row, so substitute an empty flow rather
    // than collecting a query that can never return.
    val librarySongFlow =
        remember(songId) {
            if (songId == null) flowOf<Song?>(null) else database.song(songId)
        }
    val librarySong by librarySongFlow.collectAsStateWithLifecycle(initialValue = null)
    val isLiked = librarySong?.song?.liked == true
    // Downloaded state comes straight off the song row, which the download worker keeps in sync — no
    // separate list query, so it costs nothing here.
    val isDownloaded = librarySong?.song?.isDownloaded == true

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

                // Fixed 56dp slot: the spectrum is decorative, so it is the element that gives up
                // width when the text needs it.
                PixelSpectrum(
                    frame = frame,
                    active = isPlaying && !ended,
                    modifier = Modifier.width(56.dp).height(30.dp),
                )

                Spacer(Modifier.width(Pixel))

                // Control cluster, all hand-drawn pixel sprites: heart, play/pause, download.
                PixelHeart(
                    songId = mediaMetadata?.id,
                    liked = isLiked,
                    onClick = { playerConnection.service.toggleLike() },
                )

                PixelPlayPause(
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

                PixelDownload(
                    downloaded = isDownloaded,
                    onClick = {
                        // The MediaMetadata overload needs no `Song` row, so this works even for a
                        // track that has never been opened before.
                        mediaMetadata?.let { metadata -> downloadUtil.download(metadata) }
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

/**
 * A pixel sprite: rows of `#`/`.` characters, one character per cell.
 *
 * Sprites are declared as text so the shapes stay readable in source and can be edited cell by
 * cell, instead of being spelled out as coordinate pairs where a single wrong `to` is invisible.
 */
private typealias Sprite = List<String>

private val SpritePause: Sprite =
    listOf(
        "#..#",
        "#..#",
        "#..#",
        "#..#",
    )

private val SpritePlay: Sprite =
    listOf(
        "#...",
        "##..",
        "###.",
        "####",
        "###.",
        "##..",
        "#...",
    )

private val SpriteReplay: Sprite =
    listOf(
        ".###",
        "#...",
        "#...",
        "#..#",
        ".##.",
    )

/** Filled heart: the classic 8×7 sprite silhouette. */
private val SpriteHeartFilled: Sprite =
    listOf(
        ".##..##.",
        "########",
        "########",
        "########",
        ".######.",
        "..####..",
        "...##...",
    )

/** Outline heart: same silhouette, cells knocked out so it reads as "not liked yet". */
private val SpriteHeartOutline: Sprite =
    listOf(
        ".##..##.",
        "#..#..#.",
        "#......#",
        "#......#",
        ".#....#.",
        "..#..#..",
        "...##...",
    )

/** Down arrow into a tray: filled when the track is on disk. */
private val SpriteDownloadFilled: Sprite =
    listOf(
        "...#...",
        "...#...",
        "...#...",
        ".#####.",
        "..###..",
        "...#...",
        "#.....#",
        ".#####.",
    )

/** Same arrow, hollow tray: not downloaded. */
private val SpriteDownloadOutline: Sprite =
    listOf(
        "...#...",
        "...#...",
        "...###.",
        ".#####.",
        "..###..",
        "...#...",
        "#.....#",
        "#.....#",
    )

/**
 * Renders a [Sprite] into a square canvas, preserving the grid.
 *
 * Cells are drawn at 82% of the step so adjacent pixels keep a hairline gap — the gap is what makes
 * a small sprite read as pixels rather than as a solid blob.
 */
@Composable
private fun PixelSprite(
    sprite: Sprite,
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val columns = sprite.maxOfOrNull { it.length }?.coerceAtLeast(1) ?: 1
        val rows = sprite.size.coerceAtLeast(1)
        val cell = minOf(size.width / columns, size.height / rows)
        val originX = (size.width - cell * columns) / 2f
        val originY = (size.height - cell * rows) / 2f

        sprite.forEachIndexed { y, row ->
            row.forEachIndexed { x, ch ->
                if (ch != '.' && !ch.isWhitespace()) {
                    drawRect(
                        color = color,
                        topLeft = Offset(originX + x * cell, originY + y * cell),
                        size = Size(cell * 0.82f, cell * 0.82f),
                    )
                }
            }
        }
    }
}

/** Shared target for the three control sprites: 36dp hit area, 16dp artwork. */
@Composable
private fun PixelControl(
    onClick: () -> Unit,
    contentDescription: String?,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(36.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                .clickable(onClick = onClick)
                .semantics { if (contentDescription != null) this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** Pixel heart: filled `error` when liked, outlined `onSurfaceVariant` when not. */
@Composable
private fun PixelHeart(
    songId: String?,
    liked: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    PixelControl(
        onClick = { if (songId != null) onClick() },
        contentDescription = if (liked) "Remove from favourites" else "Add to favourites",
    ) {
        PixelSprite(
            sprite = if (liked) SpriteHeartFilled else SpriteHeartOutline,
            color = if (liked) scheme.error else scheme.onSurfaceVariant.copy(alpha = 0.85f),
            size = 16.dp,
        )
    }
}

/** Pixel play/pause/replay, matching the capsule's stepped language. */
@Composable
private fun PixelPlayPause(
    playing: Boolean,
    ended: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    PixelControl(
        onClick = onClick,
        contentDescription = if (playing) "Pause" else "Play",
    ) {
        PixelSprite(
            sprite =
                when {
                    ended -> SpriteReplay
                    playing -> SpritePause
                    else -> SpritePlay
                },
            color = scheme.onSurface,
            size = 16.dp,
        )
    }
}

/**
 * Pixel download icon.
 *
 * Shows the downloaded state but only starts a download: removing a download is a destructive action
 * that belongs in the song menu, not on a miniature control.
 */
@Composable
private fun PixelDownload(
    downloaded: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    PixelControl(
        onClick = { if (!downloaded) onClick() },
        contentDescription = if (downloaded) "Downloaded" else "Download",
    ) {
        PixelSprite(
            sprite = if (downloaded) SpriteDownloadFilled else SpriteDownloadOutline,
            color = if (downloaded) scheme.primary else scheme.onSurfaceVariant.copy(alpha = 0.85f),
            size = 16.dp,
        )
    }
}
