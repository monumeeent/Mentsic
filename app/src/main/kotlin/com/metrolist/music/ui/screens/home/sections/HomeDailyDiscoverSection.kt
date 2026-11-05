/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.home.sections

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.innertube.models.YTItem
import com.metrolist.music.R
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.playback.queues.ListQueue
import com.metrolist.music.playback.queues.YouTubeQueue
import com.metrolist.music.ui.component.NavigationTitle
import com.metrolist.music.viewmodels.CommunityPlaylistItem
import com.metrolist.music.viewmodels.DailyDiscoverItem
import com.metrolist.music.ui.screens.DailyDiscoverCard
import com.metrolist.music.ui.screens.CommunityPlaylistCard

/**
 * Daily Discover immersive carousel section.
 *
 * Design signature: full-bleed card carousel with album art backdrop,
 * gradient scrim, and dynamic "because you listened to" metadata text.
 * This is the hero moment of the feed — intentionally the most visually
 * differentiated section.
 */
@Composable
fun HomeDailyDiscoverSection(
    discoverList: List<DailyDiscoverItem>,
    playerConnection: PlayerConnection?,
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
                playerConnection?.playQueue(
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
            modifier
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
                            playerConnection?.playQueue(
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
                                },
                            )
                        }
                    }
                },
                modifier = Modifier.clip(MaterialTheme.shapes.extraLarge),
            )
        }
    }
}

/**
 * From The Community playlist cards row.
 */
@Composable
fun HomeFromTheCommunitySection(
    playlists: List<CommunityPlaylistItem>,
    navController: androidx.navigation.NavController,
    playerConnection: PlayerConnection?,
    isListenTogetherGuest: Boolean,
    modifier: Modifier = Modifier,
) {
    NavigationTitle(title = stringResource(R.string.from_the_community))

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
    ) {
        items(playlists, key = { "community_${it.playlist.id}" }) { item ->
            CommunityPlaylistCard(
                item = item,
                onClick = {
                    navController.navigate("online_playlist/${item.playlist.id.removePrefix("VL")}")
                },
                onSongClick = { song ->
                    if (!isListenTogetherGuest) {
                        playerConnection?.playQueue(
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

