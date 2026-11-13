/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.playlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.models.SmartPlaylist
import com.metrolist.music.models.SmartPlaylistType
import com.metrolist.music.playback.queues.YouTubeQueue
import com.metrolist.music.ui.component.LongClickIconButton
import com.metrolist.music.ui.component.subtitle
import com.metrolist.music.ui.component.title
import com.metrolist.music.ui.component.YouTubeListItem
import com.metrolist.music.ui.utils.backToMain
import com.metrolist.music.ui.theme.Spacing
import com.metrolist.music.viewmodels.SmartPlaylistViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

/**
 * Detail view for a generated mix. The route mirrors `online_playlist/{playlistId}` but is handled
 * separately so the smart-playlist path never reaches the YouTube playlist fetch.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SmartPlaylistScreen(
    smartPlaylistId: String,
    navController: NavController,
    viewModel: SmartPlaylistViewModel = hiltViewModel(),
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    val type: SmartPlaylistType? = remember(smartPlaylistId) { SmartPlaylistType.fromRouteId(smartPlaylistId) }
    var playlist by remember(smartPlaylistId) { mutableStateOf<SmartPlaylist?>(null) }
    var isLoading by remember(smartPlaylistId) { mutableStateOf(true) }

    LaunchedEffect(smartPlaylistId) {
        isLoading = true
        playlist = viewModel.getById(smartPlaylistId)
        isLoading = false
    }

    val songs = playlist?.songs.orEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding =
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                    .asPaddingValues(),
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.medium),
        ) {
            item(key = "smart_header") {
                Spacer(Modifier.height(Spacing.medium))
                Text(
                    text = type?.let { it.subtitle() } ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                )
                Spacer(Modifier.height(Spacing.medium))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.play_all),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = Spacing.small),
                    )
                    IconButton(
                        onClick = {
                            val first = songs.firstOrNull() ?: return@IconButton
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            playerConnection.playQueue(
                                YouTubeQueue(
                                    endpoint = WatchEndpoint(videoId = first.id),
                                    preloadItem = first.toMediaMetadata(),
                                ),
                            )
                        },
                        enabled = songs.isNotEmpty(),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = stringResource(R.string.play_all),
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.small))
            }

            if (isLoading && songs.isEmpty()) {
                item(key = "smart_loading") {
                    Box(modifier = Modifier.fillMaxWidth().padding(Spacing.large), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (songs.isEmpty()) {
                item(key = "smart_empty") {
                    Text(
                        text = stringResource(R.string.smart_playlist_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = Spacing.large),
                    )
                }
            }

            itemsIndexed(
                items = songs,
                key = { _, song -> song.id },
            ) { index, song ->
                YouTubeListItem(
                    item = song,
                    isActive = false,
                    isPlaying = false,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    playerConnection.playQueue(
                                        YouTubeQueue(
                                            endpoint = WatchEndpoint(videoId = song.id),
                                            preloadItem = song.toMediaMetadata(),
                                        ),
                                    )
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                            ),
                    trailingContent = {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }
    }

    TopAppBar(
        title = { Text(type?.let { it.title() } ?: stringResource(R.string.smart_playlists)) },
        navigationIcon = {
            LongClickIconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
    )
}


