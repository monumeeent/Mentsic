/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.home.sections

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.innertube.models.YTItem
import com.metrolist.innertube.pages.ExplorePage
import com.metrolist.innertube.pages.HomePage
import com.metrolist.music.R
import com.metrolist.music.constants.ListItemHeight
import com.metrolist.music.constants.ListThumbnailSize
import com.metrolist.music.constants.ThumbnailCornerRadius
import com.metrolist.music.db.entities.Album
import com.metrolist.music.db.entities.Artist
import com.metrolist.music.db.entities.Playlist
import com.metrolist.music.db.entities.Song
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.models.SimilarRecommendation
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.playback.queues.ListQueue
import com.metrolist.music.playback.queues.YouTubeQueue
import com.metrolist.music.ui.component.MenuState
import com.metrolist.music.ui.component.NavigationTitle
import com.metrolist.music.ui.component.YouTubeListItem
import com.metrolist.music.ui.menu.YouTubeSongMenu

/**
 * Algorithmic sections — Account Playlists, Similar Recommendations,
 * HomePage sections (InnerTube), and Mood & Genres.
 *
 * These sections all share a "title + LazyRow/Grid" structure and
 * delegate item rendering to [ytGridItem] lambdas from HomeScreen
 * to centralise click/menu handling.
 */

/** Account's personalised playlist mixes row. */
@Composable
fun HomeAccountPlaylistsSection(
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
            onClick = { navController.navigate("account") },
        )

        LazyRow(
            contentPadding =
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Horizontal)
                    .asPaddingValues(),
        ) {
            items(
                items = accountPlaylists.distinctBy { it.id },
                key = { "acct_pl_${it.id}" },
            ) { item ->
                ytGridItem(item)
            }
        }
    }
}

/** Similar to [title] recommendation row. */
@Composable
fun HomeSimilarRecommendationSection(
    sectionIndex: Int,
    recommendation: SimilarRecommendation,
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
        modifier = modifier,
        contentPadding =
            WindowInsets.systemBars
                .only(WindowInsetsSides.Horizontal)
                .asPaddingValues(),
    ) {
        items(recommendation.items.distinctBy { it.id }, key = { "sim_${sectionIndex}_${it.id}" }) { item ->
            ytGridItem(item)
        }
    }
}

/** InnerTube home page section (generic title + items). */
@Composable
fun HomePageSectionItem(
    sectionIndex: Int,
    sectionData: HomePage.Section?,
    isPodcastChipActive: Boolean,
    ytGridItem: @Composable (YTItem) -> Unit,
    navController: NavController,
    playerConnection: PlayerConnection?,
    isListenTogetherGuest: Boolean,
    mediaMetadataId: String?,
    isPlaying: Boolean,
    autoRadioQueue: Boolean,
    menuState: MenuState,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    database: com.metrolist.music.db.MusicDatabase? = null,
    scope: kotlinx.coroutines.CoroutineScope? = null,
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
                            endpoint.browseId == "FEmusic_moods_and_genres" ->
                                navController.navigate("mood_and_genres")

                            endpoint.browseId.startsWith("FEmusic_library_non_music_audio") ||
                                endpoint.browseId.startsWith("FEmusic_non_music_audio") ->
                                navController.navigate("youtube_browse/${endpoint.browseId}")

                            endpoint.params != null ->
                                navController.navigate(
                                    "youtube_browse/${endpoint.browseId}?params=${endpoint.params}",
                                )

                            else ->
                                navController.navigate("browse/${endpoint.browseId}")
                        }
                    }
                },
            onPlayAllClick =
                if (hasPlayableSongs && !isListenTogetherGuest) {
                    {
                        playerConnection?.playQueue(
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
                    key = { "hp_${sectionIndex}_song_${it.id}" },
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
                                            playerConnection?.playQueue(
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
                                                },
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
                    key = { "hp_${sectionIndex}_item_${it.id}" },
                ) { item: YTItem ->
                    ytGridItem(item)
                }
            }
        }
    }
}

/** Mood and genres chip grid — delegates to existing composable in HomeSections.kt. */
@Composable
fun HomeMoodAndGenresSection(
    explorePage: ExplorePage?,
    isPodcastChipActive: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    if (isPodcastChipActive) return
    com.metrolist.music.ui.screens.MoodAndGenresSection(
        explorePage = explorePage,
        isPodcastChipActive = isPodcastChipActive,
        navController = navController,
        modifier = modifier,
    )
}
