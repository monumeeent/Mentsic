/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.home.sections

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metrolist.music.R
import com.metrolist.music.constants.ListItemHeight
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.Song
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.playback.queues.ListQueue
import com.metrolist.music.playback.queues.YouTubeQueue
import com.metrolist.music.ui.component.MenuState
import com.metrolist.music.ui.component.SongListItem
import com.metrolist.music.ui.menu.SongMenu
import com.metrolist.music.ui.screens.home.components.HomeSectionHeader
import kotlin.math.min

/**
 * Forgotten Favorites horizontal grid + Keep Listening grid section.
 *
 * Both sections share the same SongListItem pattern and are co-located here to
 * avoid duplicating nearly identical composable structure.
 *
 * Keep Listening uses a lambda-based [localGridItem] composable parameter so the
 * parent ([HomeScreen]) owns the click / menu logic and this section stays
 * data-agnostic for local vs. YouTube items.
 */

@Composable
fun HomeForgottenFavoritesSection(
    forgottenFavorites: List<Song>,
    forgottenFavoritesLazyGridState: LazyGridState,
    forgottenFavoritesSnapLayoutInfoProvider: androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider,
    horizontalLazyGridItemWidth: Dp,
    isPlaying: Boolean,
    mediaMetadataId: String?,
    isListenTogetherGuest: Boolean,
    autoRadioQueue: Boolean,
    database: MusicDatabase,
    menuState: MenuState,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    playerConnection: PlayerConnection?,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(R.string.forgotten_favorites)

    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxWidth()) {
        HomeSectionHeader(
            title = title,
            onPlayAllClick =
                if (!isListenTogetherGuest) {
                    {
                        playerConnection?.playQueue(
                            ListQueue(
                                title = title,
                                items = forgottenFavorites.distinctBy { it.id }.map { it.toMediaItem() },
                            ),
                        )
                    }
                } else {
                    null
                },
        )

        val rows = min(4, forgottenFavorites.size)
        LazyHorizontalGrid(
            state = forgottenFavoritesLazyGridState,
            rows = GridCells.Fixed(rows),
            contentPadding =
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Horizontal)
                    .asPaddingValues(),
            flingBehavior =
                androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
                    forgottenFavoritesSnapLayoutInfoProvider,
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(ListItemHeight * rows),
        ) {
            items(
                items = forgottenFavorites.distinctBy { it.id },
                key = { "ff_${it.id}" },
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
                            .clip(RoundedCornerShape(12.dp))
                            .combinedClickable(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    if (!isListenTogetherGuest) {
                                        if (song!!.id == mediaMetadataId) {
                                            playerConnection?.togglePlayPause()
                                        } else {
                                            playerConnection?.playQueue(
                                                if (autoRadioQueue) {
                                                    YouTubeQueue.radio(song!!.toMediaMetadata())
                                                } else {
                                                    ListQueue(
                                                        title = song!!.title,
                                                        items = listOf(song!!.toMediaItem()),
                                                    )
                                                },
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
fun HomeKeepListeningSection(
    keepListening: List<Song>,
    localGridItem: @Composable (com.metrolist.music.db.entities.LocalItem) -> Unit,
    currentGridHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val rows = if (keepListening.size > 6) 2 else 1

    androidx.compose.foundation.layout.Column(
        modifier =
            modifier
                .fillMaxWidth()
                .height(
                    (
                        currentGridHeight +
                            with(androidx.compose.ui.platform.LocalDensity.current) {
                                androidx.compose.material3.MaterialTheme.typography.bodyLarge.lineHeight
                                    .toDp() * 2 +
                                    androidx.compose.material3.MaterialTheme.typography.bodyMedium.lineHeight
                                        .toDp() * 2
                            }
                    ) * rows,
                ),
    ) {
        HomeSectionHeader(title = stringResource(R.string.keep_listening))

        LazyHorizontalGrid(
            state = androidx.compose.runtime.remember("keep_listening_grid") {
                LazyGridState()
            },
            rows = GridCells.Fixed(rows),
            contentPadding =
                WindowInsets.systemBars
                    .only(WindowInsetsSides.Horizontal)
                    .asPaddingValues(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(keepListening.distinctBy { it.id }, key = { "kl_${it.id}" }) {
                localGridItem(it)
            }
        }
    }
}

