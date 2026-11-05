/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.home.sections

import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.remember
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

/**
 * Quick Picks horizontal song grid section.
 *
 * Performance notes:
 * - Each grid item observes its own song flow (`database.song(id)`) so only the
 *   individual item recomposes when its metadata changes — not the entire grid.
 * - Stable keys `"qp_${it.id}"` prevent item recompositions on list identity change.
 * - [isActive] and [isPlaying] are plain booleans, not StateFlow, to avoid
 *   converting state at the item level.
 */
@Composable
fun HomeQuickPicksSection(
    quickPicks: List<Song>,
    quickPicksLazyGridState: LazyGridState,
    quickPicksSnapLayoutInfoProvider: androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider,
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
    val quickPicksTitle = stringResource(R.string.quick_picks)

    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxWidth()) {
        HomeSectionHeader(
            title = quickPicksTitle,
            onPlayAllClick =
                if (!isListenTogetherGuest) {
                    {
                        playerConnection?.playQueue(
                            ListQueue(
                                title = quickPicksTitle,
                                items = quickPicks.distinctBy { it.id }.map { it.toMediaItem() },
                            ),
                        )
                    }
                } else {
                    null
                },
        )

        LazyHorizontalGrid(
            state = quickPicksLazyGridState,
            rows = GridCells.Fixed(4),
            flingBehavior =
                androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
                    quickPicksSnapLayoutInfoProvider,
                ),
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
                key = { "qp_${it.id}" },
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

