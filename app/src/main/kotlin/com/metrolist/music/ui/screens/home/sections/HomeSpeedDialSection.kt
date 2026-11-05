/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.home.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.ArtistItem
import com.metrolist.innertube.models.EpisodeItem
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.innertube.models.PodcastItem
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.innertube.models.YTItem
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.SpeedDialItem
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.playback.queues.ListQueue
import com.metrolist.music.playback.queues.YouTubeQueue
import com.metrolist.music.ui.component.LocalMenuState
import com.metrolist.music.ui.component.MenuState
import com.metrolist.music.ui.component.RandomizeGridItem
import com.metrolist.music.ui.component.SpeedDialGridItem
import com.metrolist.music.ui.menu.YouTubeAlbumMenu
import com.metrolist.music.ui.menu.YouTubeArtistMenu
import com.metrolist.music.ui.menu.YouTubePlaylistMenu
import com.metrolist.music.ui.menu.YouTubeSongMenu
import kotlinx.coroutines.CoroutineScope

/**
 * Speed Dial paged grid section.
 *
 * Extracted from [HomeSections.kt] — logic is identical; this file is the single
 * source of truth going forward. The section header is intentionally omitted here
 * because Speed Dial occupies the top of the feed and uses the TacticalHeaderBar
 * in Hub mode and nothing in standard mode.
 *
 * Recomposition guard: [speedDialItems] and [pinnedSpeedDialItems] are stable lists
 * from StateFlow, so the pager only recomposes when their contents change.
 */
@Composable
fun HomeSpeedDialSection(
    speedDialItems: List<YTItem>,
    pinnedSpeedDialItems: List<YTItem>,
    pinnedSpeedDialItemTypes: List<String>,
    isRandomizing: Boolean,
    onRandomizeClick: () -> Unit,
    scope: CoroutineScope,
    autoRadioQueue: Boolean,
    isListenTogetherGuest: Boolean,
    mediaMetadataId: String?,
    albumId: String?,
    isPlaying: Boolean,
    playerConnection: PlayerConnection?,
    navController: NavController,
    database: MusicDatabase,
    menuState: MenuState,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    maxWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val targetItemSize = 160.dp
    val availableWidth = maxWidth - 32.dp
    val columns = (availableWidth / targetItemSize).toInt().coerceAtLeast(3)
    val rows =
        when {
            columns >= 6 -> 1
            columns >= 4 -> 2
            else -> 3
        }
    val itemsPerPage = columns * rows
    val itemWidth = availableWidth / columns

    val pagerState = rememberPagerState(pageCount = { (speedDialItems.size + itemsPerPage - 1) / itemsPerPage })

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 16.dp,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(itemWidth * rows),
        ) { page ->
            val pageStartIndex = page * itemsPerPage
            val pageItems = speedDialItems.drop(pageStartIndex).take(itemsPerPage)

            Column(modifier = Modifier.fillMaxSize()) {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until columns) {
                            val itemIndex = row * columns + col
                            val isRandomizeSlot = page == 0 && itemIndex == itemsPerPage - 1

                            if (isRandomizeSlot) {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(itemWidth)
                                            .height(itemWidth)
                                            .padding(4.dp),
                                ) {
                                    RandomizeGridItem(
                                        isLoading = isRandomizing,
                                        onClick = onRandomizeClick,
                                    )
                                }
                            } else if (itemIndex < pageItems.size) {
                                val item = pageItems[itemIndex]
                                val isPinned by database.speedDialDao
                                    .isPinned(item.id)
                                    .collectAsStateWithLifecycle(initialValue = false)

                                Box(
                                    modifier =
                                        Modifier
                                            .width(itemWidth)
                                            .height(itemWidth)
                                            .padding(4.dp),
                                ) {
                                    SpeedDialGridItem(
                                        item = item,
                                        isPinned = isPinned,
                                        isActive = item.id in listOf(albumId, mediaMetadataId),
                                        isPlaying = isPlaying,
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(12.dp))
                                                .combinedClickable(
                                                    onClick = {
                                                        when (item) {
                                                            is SongItem -> {
                                                                if (!isListenTogetherGuest) {
                                                                    playerConnection?.playQueue(
                                                                        if (autoRadioQueue) {
                                                                            YouTubeQueue(
                                                                                item.endpoint
                                                                                    ?: WatchEndpoint(videoId = item.id),
                                                                                item.toMediaMetadata(),
                                                                            )
                                                                        } else {
                                                                            ListQueue(
                                                                                title = item.title,
                                                                                items = listOf(item.toMediaItem()),
                                                                            )
                                                                        },
                                                                    )
                                                                }
                                                            }
                                                            is AlbumItem -> navController.navigate("album/${item.id}")
                                                            is ArtistItem -> navController.navigate("artist/${item.id}")
                                                            is PlaylistItem -> {
                                                                val rawType =
                                                                    pinnedSpeedDialItems
                                                                        .indexOfFirst { it.id == item.id }
                                                                        .let { idx -> if (idx >= 0) pinnedSpeedDialItemTypes[idx] else null }
                                                                val route = if (rawType == "LOCAL_PLAYLIST") "local_playlist" else "online_playlist"
                                                                navController.navigate("$route/${item.id}")
                                                            }
                                                            is PodcastItem -> navController.navigate("online_podcast/${item.id}")
                                                            is EpisodeItem -> {
                                                                if (!isListenTogetherGuest) {
                                                                    playerConnection?.playQueue(
                                                                        ListQueue(
                                                                            title = item.title,
                                                                            items = listOf(item.toMediaMetadata().toMediaItem()),
                                                                        ),
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        menuState.show {
                                                            when (item) {
                                                                is SongItem -> YouTubeSongMenu(song = item, onDismiss = menuState::dismiss)
                                                                is AlbumItem -> YouTubeAlbumMenu(albumItem = item, onDismiss = menuState::dismiss)
                                                                is ArtistItem -> YouTubeArtistMenu(artist = item, onDismiss = menuState::dismiss)
                                                                is PlaylistItem -> YouTubePlaylistMenu(playlist = item, coroutineScope = scope, onDismiss = menuState::dismiss)
                                                                is PodcastItem -> YouTubePlaylistMenu(playlist = item.asPlaylistItem(), coroutineScope = scope, onDismiss = menuState::dismiss)
                                                                is EpisodeItem -> YouTubeSongMenu(song = item.asSongItem(), onDismiss = menuState::dismiss)
                                                            }
                                                        }
                                                    },
                                                ),
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(itemWidth))
                            }
                        }
                    }
                }
            }
        }

        // Page indicator dots — only shown when there's more than one page
        if (pagerState.pageCount > 1) {
            Row(
                modifier =
                    Modifier
                        .height(24.dp)
                        .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color =
                        if (pagerState.currentPage == iteration) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        }
                    Box(
                        modifier =
                            Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(if (pagerState.currentPage == iteration) 8.dp else 6.dp),
                    )
                }
            }
        }
    }
}

