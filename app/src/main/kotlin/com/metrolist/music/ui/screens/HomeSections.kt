/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.ArtistItem
import com.metrolist.innertube.models.EpisodeItem
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.innertube.models.PodcastItem
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.innertube.models.YTItem
import com.metrolist.music.LocalDatabase
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.constants.ListItemHeight
import com.metrolist.music.constants.ListThumbnailSize
import com.metrolist.music.constants.ThumbnailCornerRadius
import com.metrolist.music.db.entities.Album
import com.metrolist.music.db.entities.Artist
import com.metrolist.music.db.entities.LocalItem
import com.metrolist.music.db.entities.Playlist
import com.metrolist.music.db.entities.Song
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.models.SimilarRecommendation
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.playback.queues.ListQueue
import com.metrolist.music.playback.queues.YouTubeQueue
import com.metrolist.music.ui.component.LocalMenuState
import com.metrolist.music.ui.component.NavigationTitle
import com.metrolist.music.ui.component.RandomizeGridItem
import com.metrolist.music.ui.component.SongListItem
import com.metrolist.music.ui.component.SpeedDialGridItem
import com.metrolist.music.ui.component.YouTubeListItem
import com.metrolist.music.ui.menu.SongMenu
import com.metrolist.music.ui.menu.YouTubeAlbumMenu
import com.metrolist.music.ui.menu.YouTubeArtistMenu
import com.metrolist.music.ui.menu.YouTubePlaylistMenu
import com.metrolist.music.ui.menu.YouTubeSongMenu
import com.metrolist.music.ui.utils.SnapLayoutInfoProvider
import com.metrolist.music.viewmodels.CommunityPlaylistItem
import com.metrolist.music.viewmodels.DailyDiscoverItem
import com.metrolist.innertube.pages.ExplorePage
import com.metrolist.innertube.pages.HomePage
import kotlinx.coroutines.CoroutineScope
import kotlin.math.min

/**
 * Section composables extracted from HomeScreen.kt.
 *
 * Each composable receives only the data and callbacks it needs as parameters,
 * keeping state hoisting clean and preventing unnecessary recompositions.
 * The `ytGridItem` and `localGridItem` composable lambdas are passed as
 * parameters to avoid duplicating click/menu logic.
 */

@Composable
fun SpeedDialSection(
    speedDialItems: List<YTItem>,
    pinnedSpeedDialItems: List<YTItem>,
    isRandomizing: Boolean,
    onRandomizeClick: () -> Unit,
    scope: CoroutineScope,
    autoRadioQueue: Boolean,
    isListenTogetherGuest: Boolean,
    mediaMetadataId: String?,
    albumId: String?,
    isPlaying: Boolean,
    playerConnection: LocalPlayerConnection,
    navController: NavController,
    database: LocalDatabase,
    menuState: LocalMenuState,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    maxWidth: Dp,
) {
    val targetItemSize = 160.dp
    val availableWidth = maxWidth - 32.dp
    val columns = (availableWidth / targetItemSize).toInt().coerceAtLeast(3)
    val rows =
        if (columns >= 6) {
            1
        } else if (columns >= 4) {
            2
        } else {
            3
        }
    val itemsPerPage = columns * rows
    val itemWidth = availableWidth / columns

    val pagerState = rememberPagerState(pageCount = { (speedDialItems.size + itemsPerPage - 1) / itemsPerPage })

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
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
                                                .combinedClickable(
                                                    onClick = {
                                                        when (item) {
                                                            is SongItem -> {
                                                                if (!isListenTogetherGuest) {
                                                                    playerConnection.playQueue(
                                                                        if (autoRadioQueue) {
                                                                            YouTubeQueue(
                                                                                item.endpoint
                                                                                    ?: WatchEndpoint(
                                                                                        videoId = item.id,
                                                                                    ),
                                                                                item.toMediaMetadata(),
                                                                            )
                                                                        } else {
                                                                            ListQueue(
                                                                                title = item.title,
                                                                                items = listOf(item.toMediaItem()),
                                                                            )
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                            is AlbumItem -> navController.navigate("album/${item.id}")
                                                            is ArtistItem -> navController.navigate("artist/${item.id}")
                                                            is PlaylistItem -> {
                                                                val rawType =
                                                                    pinnedSpeedDialItems
                                                                        .find { it.id == item.id }?.type
                                                                val route = if (rawType == "LOCAL_PLAYLIST") "local_playlist" else "online_playlist"
                                                                navController.navigate("$route/${item.id}")
                                                            }
                                                            is PodcastItem -> navController.navigate("online_podcast/${item.id}")
                                                            is EpisodeItem -> {
                                                                if (!isListenTogetherGuest) {
                                                                    playerConnection.playQueue(
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
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    Box(
                        modifier =
                            Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun QuickPicksSection(
    quickPicks: List<Song>,
    quickPicksLazyGridState: LazyGridState,
    quickPicksSnapLayoutInfoProvider: SnapLayoutInfoProvider,
    horizontalLazyGridItemWidth: Dp,
    isPlaying: Boolean,
    mediaMetadataId: String?,
    isListenTogetherGuest: Boolean,
    autoRadioQueue: Boolean,
    database: LocalDatabase,
    menuState: LocalMenuState,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    playerConnection: LocalPlayerConnection,
) {
    val quickPicksTitle = stringResource(R.string.quick_picks)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = quickPicksTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Surface(
                onClick = {
                    if (!isListenTogetherGuest) {
                        playerConnection.playQueue(
                            ListQueue(
                                title = quickPicksTitle,
                                items = quickPicks.distinctBy { it.id }.map { it.toMediaItem() },
                            ),
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play All",
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Play All",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }

        LazyHorizontalGrid(
            state = quickPicksLazyGridState,
            rows = GridCells.Fixed(4),
            flingBehavior = rememberSnapFlingBehavior(quickPicksSnapLayoutInfoProvider),
            contentPadding =
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Horizontal)
                    .asPaddingValues(),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(ListItemHeight * 4),
        ) {
            items(
                items = quickPicks.distinctBy { it.id },
                key = { "home_quickpick_${it.id}" },
            ) { originalSong ->
                val song by database
                    .song(originalSong.id)
                    .collectAsStateWithLifecycle(initialValue = originalSong)

                SongListItem(
                    song = song!!,
                    showInLibraryIcon = true,
                    isActive = song!!.id == mediaMetadataId,
                    isPlaying = isPlaying,
                    isSwipeable = false,
                    trailingContent = {
                        IconButton(
                            onClick = {
                                menuState.show {
                                    SongMenu(
                                        originalSong = song!!,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = null,
                            )
                        }
                    },
                    modifier =
                        Modifier
                            .width(horizontalLazyGridItemWidth)
                            .combinedClickable(
                                onClick = {
                                    if (!isListenTogetherGuest) {
                                        if (song!!.id == mediaMetadataId) {
                                            playerConnection.togglePlayPause()
                                        } else {
                                            playerConnection.playQueue(
                                                if (autoRadioQueue) {
                                                    YouTubeQueue.radio(song!!.toMediaMetadata())
                                                } else {
                                                    ListQueue(
                                                        title = song!!.title,
                                                        items = listOf(song!!.toMediaItem()),
                                                    )
                                                }
                                            )
                                        }
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuState.show {
                                        SongMenu(
                                            originalSong = song!!,
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                },
                            ),
                )
            }
        }
    }
}

@Composable
fun FromTheCommunitySection(
    playlists: List<CommunityPlaylistItem>,
    navController: NavController,
    playerConnection: LocalPlayerConnection,
    isListenTogetherGuest: Boolean,
) {
    NavigationTitle(
        title = stringResource(R.string.from_the_community),
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(playlists, key = { "community_playlist_${it.playlist.id}" }) { item ->
            CommunityPlaylistCard(
                item = item,
                onClick = {
                    navController.navigate("online_playlist/${item.playlist.id.removePrefix("VL")}")
                },
                onSongClick = { song ->
                    if (!isListenTogetherGuest) {
                        playerConnection.playQueue(
                            YouTubeQueue(
                                song.endpoint ?: WatchEndpoint(videoId = song.id),
                                song.toMediaMetadata(),
                            ),
                        )
                    }
                },
            )
        }
    }
}

@Composable
fun DailyDiscoverSection(
    discoverList: List<DailyDiscoverItem>,
    playerConnection: LocalPlayerConnection,
    isListenTogetherGuest: Boolean,
    autoRadioQueue: Boolean,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(R.string.your_daily_discover)
    NavigationTitle(
        title = title,
        onPlayAllClick = {
            val queueItems =
                discoverList.mapNotNull {
                    (it.recommendation as? SongItem)?.toMediaMetadata()
                }

            if (queueItems.isNotEmpty()) {
                playerConnection.playQueue(
                    ListQueue(
                        title = title,
                        items = queueItems.map { it.toMediaItem() },
                    ),
                )
            }
        },
    )

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(340.dp)
                .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        val carouselState = rememberCarouselState { discoverList.size }
        HorizontalMultiBrowseCarousel(
            state = carouselState,
            preferredItemWidth = 320.dp,
            itemSpacing = 16.dp,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(320.dp),
        ) { i ->
            val item = discoverList[i]
            DailyDiscoverCard(
                dailyDiscover = item,
                onClick = {
                    if (!isListenTogetherGuest) {
                        val song = item.recommendation as? SongItem
                        val mediaMetadata = song?.toMediaMetadata()
                        if (mediaMetadata != null) {
                            playerConnection.playQueue(
                                if (autoRadioQueue) {
                                    YouTubeQueue(
                                        song.endpoint ?: WatchEndpoint(videoId = song.id),
                                        mediaMetadata,
                                    )
                                } else {
                                    ListQueue(
                                        title = song.title,
                                        items = listOf(song.toMediaItem()),
                                    )
                                }
                            )
                        }
                    }
                },
                modifier = modifier.maskClip(MaterialTheme.shapes.extraLarge),
            )
        }
    }
}

@Composable
fun KeepListeningSection(
    keepListening: List<Song>,
    localGridItem: @Composable (LocalItem) -> Unit,
    currentGridHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val rows = if (keepListening.size > 6) 2 else 1
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(
                (
                    currentGridHeight +
                        with(LocalDensity.current) {
                            MaterialTheme.typography.bodyLarge.lineHeight
                                .toDp() * 2 +
                            MaterialTheme.typography.bodyMedium.lineHeight
                                .toDp() * 2
                        }
                ) * rows,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.keep_listening),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        LazyHorizontalGrid(
            state = remember("keep_listening_grid") { LazyGridState() },
            rows = GridCells.Fixed(rows),
            contentPadding =
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Horizontal)
                    .asPaddingValues(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(keepListening.distinctBy { it.id }, key = { "home_keep_listening_${it.id}" }) {
                localGridItem(it)
            }
        }
    }
}

@Composable
fun AccountPlaylistsSection(
    accountPlaylists: List<YTItem>,
    accountName: String,
    accountImageUrl: String?,
    isLoggedIn: Boolean,
    ytGridItem: @Composable (YTItem) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val url = if (isLoggedIn) accountImageUrl else null
    Column(modifier = modifier.fillMaxWidth()) {
        NavigationTitle(
            label = stringResource(R.string.mixes),
            title = accountName,
            thumbnail = {
                if (url != null) {
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalContext.current)
                                .data(url)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .diskCacheKey(url)
                                .crossfade(false)
                                .build(),
                        placeholder = painterResource(id = R.drawable.person),
                        error = painterResource(id = R.drawable.person),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .size(ListThumbnailSize)
                                .clip(CircleShape),
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.person),
                        contentDescription = null,
                        modifier = Modifier.size(ListThumbnailSize),
                    )
                }
            },
            onClick = {
                navController.navigate("account")
            },
        )

        LazyRow(
            contentPadding =
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Horizontal)
                    .asPaddingValues(),
        ) {
            items(
                items = accountPlaylists.distinctBy { it.id },
                key = { "home_account_playlist_${it.id}" },
            ) { item ->
                ytGridItem(item)
            }
        }
    }
}

@Composable
fun ForgottenFavoritesSection(
    forgottenFavorites: List<Song>,
    isPlaying: Boolean,
    mediaMetadataId: String?,
    isListenTogetherGuest: Boolean,
    autoRadioQueue: Boolean,
    database: LocalDatabase,
    menuState: LocalMenuState,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    playerConnection: LocalPlayerConnection,
    forgottenFavoritesLazyGridState: LazyGridState,
    forgottenFavoritesSnapLayoutInfoProvider: SnapLayoutInfoProvider,
    horizontalLazyGridItemWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val forgottenFavoritesTitle = stringResource(R.string.forgotten_favorites)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = forgottenFavoritesTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Surface(
                onClick = {
                    if (!isListenTogetherGuest) {
                        playerConnection.playQueue(
                            ListQueue(
                                title = forgottenFavoritesTitle,
                                items = forgottenFavorites.distinctBy { it.id }.map { it.toMediaItem() },
                            ),
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play All",
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Play All",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }

        val rows = min(4, forgottenFavorites.size)
        LazyHorizontalGrid(
            state = forgottenFavoritesLazyGridState,
            rows = GridCells.Fixed(rows),
            contentPadding =
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Horizontal)
                    .asPaddingValues(),
            flingBehavior =
                rememberSnapFlingBehavior(forgottenFavoritesSnapLayoutInfoProvider),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(ListItemHeight * rows),
        ) {
            items(
                items = forgottenFavorites.distinctBy { it.id },
                key = { "home_forgotten_${it.id}" },
            ) { originalSong ->
                val song by database
                    .song(originalSong.id)
                    .collectAsStateWithLifecycle(initialValue = originalSong)

                SongListItem(
                    song = song!!,
                    showInLibraryIcon = true,
                    isActive = song!!.id == mediaMetadataId,
                    isPlaying = isPlaying,
                    isSwipeable = false,
                    trailingContent = {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    SongMenu(
                                        originalSong = song!!,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = null,
                            )
                        }
                    },
                    modifier =
                        Modifier
                            .width(horizontalLazyGridItemWidth)
                            .combinedClickable(
                                onClick = {
                                    if (!isListenTogetherGuest) {
                                        if (song!!.id == mediaMetadataId) {
                                            playerConnection.togglePlayPause()
                                        } else {
                                            playerConnection.playQueue(
                                                if (autoRadioQueue) {
                                                    YouTubeQueue.radio(song!!.toMediaMetadata())
                                                } else {
                                                    ListQueue(
                                                        title = song!!.title,
                                                        items = listOf(song!!.toMediaItem()),
                                                    )
                                                }
                                            )
                                        }
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuState.show {
                                        SongMenu(
                                            originalSong = song!!,
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                },
                            ),
                )
            }
        }
    }
}

@Composable
fun SimilarRecommendationSection(
    sectionIndex: Int,
    recommendation: com.metrolist.music.models.SimilarRecommendation,
    ytGridItem: @Composable (YTItem) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    NavigationTitle(
        label = stringResource(R.string.similar_to),
        title = recommendation.title.title,
        thumbnail =
            recommendation.title.thumbnailUrl?.let { thumbnailUrl ->
                {
                    val shape =
                        if (recommendation.title is Artist) {
                            CircleShape
                        } else {
                            RoundedCornerShape(ThumbnailCornerRadius)
                        }
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(ListThumbnailSize)
                                .clip(shape),
                    )
                }
            },
        onClick = {
            when (recommendation.title) {
                is Song -> navController.navigate("album/${recommendation.title.album!!.id}")
                is Album -> navController.navigate("album/${recommendation.title.id}")
                is Artist -> navController.navigate("artist/${recommendation.title.id}")
                is Playlist -> {}
            }
        },
    )

    LazyRow(
        contentPadding =
            WindowInsets.systemBars
                .only(WindowInsetsSides.Horizontal)
                .asPaddingValues(),
    ) {
        items(recommendation.items.distinctBy { it.id }, key = { "home_similar_${it.id}" }) { item ->
            ytGridItem(item)
        }
    }
}

@Composable
fun HomePageSection(
    sectionIndex: Int,
    sectionData: HomePage.Section?,
    isPodcastChipActive: Boolean,
    ytGridItem: @Composable (YTItem) -> Unit,
    navController: NavController,
    playerConnection: LocalPlayerConnection,
    isListenTogetherGuest: Boolean,
    mediaMetadataId: String?,
    isPlaying: Boolean,
    autoRadioQueue: Boolean,
    menuState: LocalMenuState,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    database: LocalDatabase,
    scope: CoroutineScope,
    currentGridHeight: Dp,
    horizontalLazyGridItemWidth: Dp,
    modifier: Modifier = Modifier,
) {
    if (isPodcastChipActive) return
    if (sectionData == null) return

    val sectionSongs = sectionData.items.filterIsInstance<SongItem>()
    val hasPlayableSongs = sectionSongs.isNotEmpty()
    val isSongsOnlySection =
        sectionData.items.isNotEmpty() &&
            sectionData.items.all { it is SongItem }

    Column(modifier = modifier.fillMaxWidth()) {
        NavigationTitle(
            title = sectionData.title,
            label = sectionData.label,
            thumbnail =
                sectionData.thumbnail?.let { thumbnailUrl ->
                    {
                        val shape =
                            if (sectionData.endpoint?.isArtistEndpoint == true) {
                                CircleShape
                            } else {
                                RoundedCornerShape(ThumbnailCornerRadius)
                            }
                        AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .size(ListThumbnailSize)
                                    .clip(shape),
                        )
                    }
                },
            onClick =
                sectionData.endpoint?.let { endpoint ->
                    {
                        when {
                            endpoint.browseId == "FEmusic_moods_and_genres" -> {
                                navController.navigate("mood_and_genres")
                            }

                            endpoint.browseId.startsWith("FEmusic_library_non_music_audio") ||
                                endpoint.browseId.startsWith("FEmusic_non_music_audio") -> {
                                navController.navigate("youtube_browse/${endpoint.browseId}")
                            }

                            endpoint.params != null -> {
                                navController.navigate(
                                    "youtube_browse/${endpoint.browseId}?params=${endpoint.params}",
                                )
                            }

                            else -> {
                                navController.navigate("browse/${endpoint.browseId}")
                            }
                        }
                    }
                },
            onPlayAllClick =
                if (hasPlayableSongs && !isListenTogetherGuest) {
                    {
                        playerConnection.playQueue(
                            ListQueue(
                                title = sectionData.title,
                                items = sectionSongs.map { it.toMediaMetadata().toMediaItem() },
                            ),
                        )
                    }
                } else {
                    null
                },
        )

        if (isSongsOnlySection) {
            LazyHorizontalGrid(
                state = remember("section_${sectionIndex}_grid") { LazyGridState() },
                rows = GridCells.Fixed(4),
                contentPadding =
                    WindowInsets.systemBars
                        .only(WindowInsetsSides.Horizontal)
                        .asPaddingValues(),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(ListItemHeight * 4),
            ) {
                items(
                    items = sectionSongs.distinctBy { it.id },
                    key = { "home_section_${sectionIndex}_song_${it.id}" },
                ) { song ->
                    YouTubeListItem(
                        item = song,
                        isActive = song.id == mediaMetadataId,
                        isPlaying = isPlaying,
                        isSwipeable = false,
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    menuState.show {
                                        YouTubeSongMenu(
                                            song = song,
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                },
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.more_vert),
                                    contentDescription = null,
                                )
                            }
                        },
                        modifier =
                            Modifier
                                .width(horizontalLazyGridItemWidth)
                                .combinedClickable(
                                    onClick = {
                                        if (!isListenTogetherGuest) {
                                            playerConnection.playQueue(
                                                if (autoRadioQueue) {
                                                    YouTubeQueue(
                                                        song.endpoint ?: WatchEndpoint(videoId = song.id),
                                                        song.toMediaMetadata(),
                                                    )
                                                } else {
                                                    ListQueue(
                                                        title = song.title,
                                                        items = listOf(song.toMediaItem()),
                                                    )
                                                }
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            YouTubeSongMenu(
                                                song = song,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    },
                                ),
                    )
                }
            }
        } else {
            LazyRow(
                contentPadding =
                    WindowInsets.systemBars
                        .only(WindowInsetsSides.Horizontal)
                        .asPaddingValues(),
            ) {
                items(
                    items = sectionData.items.distinctBy { it.id },
                    key = { "home_section_${sectionIndex}_item_${it.id}" },
                ) { item ->
                    ytGridItem(item)
                }
            }
        }
    }
}

@Composable
fun MoodAndGenresSection(
    explorePage: ExplorePage?,
    isPodcastChipActive: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    if (isPodcastChipActive) return

    explorePage?.moodAndGenres?.let { moodAndGenres ->
        NavigationTitle(
            title = stringResource(R.string.mood_and_genres),
            onClick = {
                navController.navigate("mood_and_genres")
            },
        )

        LazyHorizontalGrid(
            rows = GridCells.Fixed(4),
            contentPadding = PaddingValues(6.dp),
            modifier =
                Modifier
                    .height((MoodAndGenresButtonHeight + 12.dp) * 4 + 12.dp),
        ) {
            items(moodAndGenres.distinctBy { "${it.title}_${it.endpoint.browseId}_${it.endpoint.params}" }, key = { "${it.title}_${it.endpoint.browseId}_${it.endpoint.params}" }) {
                MoodAndGenresButton(
                    title = it.title,
                    onClick = {
                        navController.navigate(
                            "youtube_browse/${it.endpoint.browseId}?params=${it.endpoint.params}",
                        )
                    },
                    modifier =
                        Modifier
                            .padding(6.dp)
                            .width(180.dp),
                )
            }
        }
    }
}
