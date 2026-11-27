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
 *  - **Pixel typography.** Title is uppercased and wide-tracked; the artist line is dimmed and
 *    tracked, so the pair reads like a two-line LCD.
 *  - **Pixel art.** The album art is inset into a square with corner pixels knocked out, so even
 *    the bitmap looks like it came off a 16×16 sprite sheet.
 *
 * Toggleable via [MiniPlayerCapsuleKey]; `MiniPlayer` picks this layout when it is on.
 */

package com.metrolist.music.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.metrolist.music.LocalDatabase
import com.metrolist.music.LocalDownloadUtil
import com.metrolist.music.LocalListenTogetherManager
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.constants.MiniPlayerBackgroundStyle
import com.metrolist.music.constants.MiniPlayerBackgroundStyleKey
import com.metrolist.music.constants.MiniPlayerHeight
import com.metrolist.music.constants.SwipeSensitivityKey
import com.metrolist.music.constants.SwipeThumbnailKey
import com.metrolist.music.db.entities.Song
import com.metrolist.music.ui.theme.PlayerColorExtractor
import com.metrolist.music.ui.theme.Spacing
import com.metrolist.music.utils.joinToArtistString
import com.metrolist.music.utils.rememberEnumPreference
import com.metrolist.music.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/** Grid step of the whole design. Everything in the capsule is a multiple of this. */
private val Pixel = 4.dp

/**
 * Pixel capsule mini player.
 *
 * @param progressState kept in the signature so both mini-player designs stay swappable; the capsule
 *   draws its own pixel frame instead of a progress arc.
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

    // Like and download state, read exactly the way the stacked mini player does, so the two designs
    // can never disagree about whether the current song is saved.
    val database = LocalDatabase.current
    val downloadUtil = LocalDownloadUtil.current
    val songId = mediaMetadata?.id
    // `database.song()` accepts a nullable id and returns Flow<Song?>, so no special-casing is
    // needed for the "nothing playing" state — the query simply yields null.
    val librarySongFlow: Flow<Song?> =
        remember(songId) { database.song(songId) }
    val librarySong by librarySongFlow.collectAsStateWithLifecycle(initialValue = null)
    val isLiked = librarySong?.song?.liked == true
    // Downloaded state comes straight off the song row, which the download worker keeps in sync — no
    // separate list query, so it costs nothing here.
    val isDownloaded = librarySong?.song?.isDownloaded == true

    val interactionSource = remember { MutableInteractionSource() }
    val ended = playbackState == Player.STATE_ENDED

    // ── Swipe to change song ────────────────────────────────────────────────────────────────────────
    // Same gesture, thresholds and preferences as the two stock mini players: one shared sensitivity
    // slider, one shared "show swipe artwork" switch, and no swiping for Listen Together guests (the
    // host owns the queue, so a local skip would be silently overridden).
    val swipeSensitivity by rememberPreference(SwipeSensitivityKey, 0.73f)
    val swipeThumbnailPref by rememberPreference(SwipeThumbnailKey, true)
    val listenTogetherManager = LocalListenTogetherManager.current
    val isListenTogetherGuest = listenTogetherManager?.let { it.isInRoom && !it.isHost } ?: false
    val swipeEnabled = swipeThumbnailPref && !isListenTogetherGuest

    val canSkipNext by playerConnection.canSkipNext.collectAsStateWithLifecycle(initialValue = false)
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsStateWithLifecycle(initialValue = false)

    val layoutDirection = LocalLayoutDirection.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Drag offset of the capsule. The whole plate slides sideways; releasing it either skips a track or
    // springs back to centre.
    val offsetXAnimatable = remember { Animatable(0f) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }
    val animationSpec = remember {
        spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
    }
    val autoSwipeThreshold = remember(swipeSensitivity) {
        (600 / (1f + kotlin.math.exp(-(-11.44748 * swipeSensitivity + 9.04945)))).roundToInt()
    }

    // ── Background style ─────────────────────────────────────────────────────────────────────────────
    // Honours the same Appearance preference as the other mini players. The pixel frame stays on top in
    // every style, so the capsule never stops looking like hardware; only the fill behind it changes.
    val miniPlayerBackground by rememberEnumPreference(
        MiniPlayerBackgroundStyleKey,
        defaultValue = MiniPlayerBackgroundStyle.DEFAULT,
    )
    val thumbnailUrl = mediaMetadata?.thumbnailUrl
    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    val context = LocalContext.current
    LaunchedEffect(thumbnailUrl, miniPlayerBackground) {
        // Only the gradient style needs an extracted palette; every other style is a flat fill.
        gradientColors = emptyList()
        if (miniPlayerBackground == MiniPlayerBackgroundStyle.GRADIENT && thumbnailUrl != null) {
            gradientColors = rememberGradientColors(context, thumbnailUrl)
        }
    }

    // Fill drawn inside the pixel frame for the current style.
    val panelFill =
        when (miniPlayerBackground) {
            MiniPlayerBackgroundStyle.DEFAULT -> scheme.surfaceContainerHigh
            MiniPlayerBackgroundStyle.TRANSPARENT -> scheme.surfaceContainerHigh.copy(alpha = 0.45f)
            MiniPlayerBackgroundStyle.BLUR -> scheme.surfaceContainerHigh
            MiniPlayerBackgroundStyle.GRADIENT -> scheme.surfaceContainerHigh
            MiniPlayerBackgroundStyle.PURE_BLACK -> Color.Black
        }

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
                    .offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) }
                    // while a swipe is in progress, tapping the plate must not open the
                    // player (the finger is dragging, not tapping), so the click is disabled during
                    // the drag and restored on release.
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = offsetXAnimatable.value.absoluteValue < 8f,
                        onClick = onClick,
                    )
                    .then(
                        if (swipeEnabled) {
                            Modifier.pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragStart = {
                                        dragStartTime = System.currentTimeMillis()
                                        totalDragDistance = 0f
                                    },
                                    onDragCancel = {
                                        coroutineScope.launch { offsetXAnimatable.animateTo(0f, animationSpec) }
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        val adjusted =
                                            if (layoutDirection == LayoutDirection.Rtl) -dragAmount else dragAmount
                                        val canGoPrev = playerConnection.player.previousMediaItemIndex != -1
                                        val canGoNext = playerConnection.player.nextMediaItemIndex != -1
                                        val tryingRight = adjusted > 0
                                        val tryingLeft = adjusted < 0
                                        val allow = (tryingLeft && canGoNext) || (tryingRight && canGoPrev)
                                        // Dragging back toward centre is always allowed, even at the end
                                        // of the queue, so an overshoot can be undone.
                                        val returning =
                                            (tryingRight && !canGoPrev && offsetXAnimatable.value < 0) ||
                                                (tryingLeft && !canGoNext && offsetXAnimatable.value > 0)
                                        if (allow || returning) {
                                            totalDragDistance += adjusted.absoluteValue
                                            coroutineScope.launch {
                                                offsetXAnimatable.snapTo(offsetXAnimatable.value + adjusted)
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        val dragDuration = System.currentTimeMillis() - dragStartTime
                                        val velocity = if (dragDuration > 0) totalDragDistance / dragDuration else 0f
                                        val currentOffset = offsetXAnimatable.value
                                        val minDistanceThreshold = 50f
                                        val velocityThreshold = (swipeSensitivity * -8.25f) + 8.5f
                                        val shouldChangeSong =
                                            (currentOffset.absoluteValue > minDistanceThreshold && velocity > velocityThreshold) ||
                                                (currentOffset.absoluteValue > autoSwipeThreshold)

                                        if (shouldChangeSong) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (currentOffset > 0 && canSkipPrevious) {
                                                playerConnection.player.seekToPreviousMediaItem()
                                            } else if (currentOffset <= 0 && canSkipNext) {
                                                playerConnection.player.seekToNext()
                                            }
                                        }
                                        coroutineScope.launch { offsetXAnimatable.animateTo(0f, animationSpec) }
                                    },
                                )
                            }
                        } else {
                            Modifier
                        },
                    ),
        ) {
            // Style layer, painted *under* the frame so the chamfer and scanline always stay crisp.
            when (miniPlayerBackground) {
                MiniPlayerBackgroundStyle.BLUR -> {
                    if (thumbnailUrl != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().blur(40.dp),
                        )
                        Box(Modifier.fillMaxSize().background(scheme.surfaceContainerHighest.copy(alpha = 0.35f)))
                    }
                }

                MiniPlayerBackgroundStyle.GRADIENT -> {
                    val colors =
                        if (gradientColors.isNotEmpty()) {
                            gradientColors
                        } else {
                            listOf(scheme.surfaceContainerHigh, scheme.surfaceContainer)
                        }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(colors))
                            .background(scheme.surfaceContainerLowest.copy(alpha = 0.15f)),
                    )
                }

                else -> Unit
            }

            // Pixel frame: chamfered corners + 1px scanline, painted over the style layer.
            PixelPanel(
                modifier = Modifier.fillMaxSize(),
                fill = panelFill,
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

            // Swipe affordance: the skip sprite fades in on the side the plate is being pushed toward,
            // brightening as the drag approaches the threshold — the same feedback the stock mini
            // player gives, drawn in the capsule's own pixel language.
            if (swipeEnabled && offsetXAnimatable.value.absoluteValue > 24f) {
                val progressAlpha =
                    (offsetXAnimatable.value.absoluteValue / autoSwipeThreshold).coerceIn(0f, 1f)
                Box(
                    modifier =
                        Modifier
                            .align(
                                if (offsetXAnimatable.value > 0f) Alignment.CenterStart else Alignment.CenterEnd,
                            ).padding(horizontal = Spacing.medium)
                            .size(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    PixelSprite(
                        sprite = if (offsetXAnimatable.value > 0f) SpriteSkipPrevious else SpriteSkipNext,
                        color = scheme.onSurface.copy(alpha = progressAlpha),
                        size = 14.dp,
                    )
                }
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

/** Right-pointing chevron pair: "next track" while swiping left. */
private val SpriteSkipNext: Sprite =
    listOf(
        "#..#.",
        "##.##",
        "#..#.",
        "##.##",
        "#..#.",
    )

/** Mirror of [SpriteSkipNext]: "previous track" while swiping right. */
private val SpriteSkipPrevious: Sprite =
    listOf(
        ".#..#",
        "##.##",
        ".#..#",
        "##.##",
        ".#..#",
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
        // `this.size` is DrawScope's pixel size; the bare `size` identifier is the Dp parameter from
        // the outer scope, which shadows it and would try to call `Dp.width` as a function.
        val canvasWidth = this.size.width
        val canvasHeight = this.size.height
        val cell = minOf(canvasWidth / columns, canvasHeight / rows)
        val originX = (canvasWidth - cell * columns) / 2f
        val originY = (canvasHeight - cell * rows) / 2f

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

/**
 * Cover-art gradient for the capsule's GRADIENT background style.
 *
 * Deliberately a plain suspend function rather than a composable `LaunchedEffect` body: the capsule
 * only calls it from one place, and pulling the palette inside the composable would drag the Coil
 * request and the Palette builder into the composition lambda (and recompose on every colour change).
 *
 * The extraction is the *same* routine the stock mini player uses, so switching between designs with
 * the gradient style selected produces the same colours instead of a second, slightly different ramp.
 * Any failure (offline, decode error) yields an empty list and the caller falls back to surface tones.
 */
private suspend fun rememberGradientColors(context: android.content.Context, thumbnailUrl: String): List<Color> =
    withContext(Dispatchers.IO) {
        runCatching {
            val request =
                ImageRequest.Builder(context)
                    .data(thumbnailUrl)
                    .size(100, 100)
                    .allowHardware(false)
                    .build()
            val bitmap = context.imageLoader.execute(request).image?.toBitmap() ?: return@runCatching emptyList()
            val palette =
                withContext(Dispatchers.Default) {
                    Palette.from(bitmap)
                        .maximumColorCount(8)
                        .resizeBitmapArea(100 * 100)
                        .generate()
                }
            PlayerColorExtractor.extractGradientColors(
                palette = palette,
                fallbackColor = 0xFF000000.toInt(),
                darkColor = Color.Black,
            )
        }.getOrDefault(emptyList())
    }
