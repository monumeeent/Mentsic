/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.metrolist.music.R
import com.metrolist.music.models.SmartPlaylist
import com.metrolist.music.models.SmartPlaylistType
import com.metrolist.music.ui.theme.Spacing

/** Display title for a generated mix, kept next to the card so every surface agrees. */
@Composable
fun SmartPlaylistType.title(): String =
    when (this) {
        SmartPlaylistType.RELEASE_RADAR -> stringResource(R.string.smart_release_radar)
        SmartPlaylistType.WEEKLY_MIX -> stringResource(R.string.smart_weekly_mix)
    }

/** Subtitle explaining how the mix is built and when it rotates. */
@Composable
fun SmartPlaylistType.subtitle(): String =
    when (this) {
        SmartPlaylistType.RELEASE_RADAR -> stringResource(R.string.smart_release_radar_desc)
        SmartPlaylistType.WEEKLY_MIX -> stringResource(R.string.smart_weekly_mix_desc)
    }

/**
 * Horizontal row of generated mixes. The caller owns the surrounding header so this stays usable
 * on Home, in Library, and anywhere else a row is wanted.
 */
@Composable
fun SmartPlaylistRow(
    playlists: List<SmartPlaylist>,
    onPlaylistClick: (SmartPlaylist) -> Unit,
    onPlayAll: (SmartPlaylist) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        modifier = modifier,
    ) {
        items(playlists, key = { it.id }) { playlist ->
            SmartPlaylistCard(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist) },
                onPlayAll = { onPlayAll(playlist) },
            )
        }
    }
}

@Composable
private fun SmartPlaylistCard(
    playlist: SmartPlaylist,
    onClick: () -> Unit,
    onPlayAll: () -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    Column(
        // `clip` precedes `clickable` so the ripple stays inside the artwork (rulebook §5).
        modifier =
            Modifier
                .width(160.dp)
                .clip(shape)
                .clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(shape)) {
            CollageCover(thumbnailUrls = playlist.thumbnailUrls)
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.align(Alignment.BottomStart).padding(Spacing.small),
            ) {
                Text(
                    text = stringResource(R.string.smart_playlist_badge),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.tiny),
                )
            }
            IconButton(
                onClick = onPlayAll,
                modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.tiny),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.play),
                        contentDescription = stringResource(R.string.play_all),
                        modifier = Modifier.padding(Spacing.small),
                    )
                }
            }
        }
        // Two lines reserved so neighbouring cards keep aligned baselines.
        Text(
            text = playlist.type.title(),
            style = MaterialTheme.typography.bodyMedium,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = Spacing.small),
        )
        Text(
            text = playlist.type.subtitle(),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 2x2 collage when four covers exist, otherwise the first cover, otherwise a tonal placeholder. */
@Composable
private fun CollageCover(thumbnailUrls: List<String>) {
    val scheme = MaterialTheme.colorScheme
    when {
        thumbnailUrls.size >= 4 ->
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    CoverTile(thumbnailUrls[0], Modifier.weight(1f).fillMaxSize())
                    CoverTile(thumbnailUrls[1], Modifier.weight(1f).fillMaxSize())
                }
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    CoverTile(thumbnailUrls[2], Modifier.weight(1f).fillMaxSize())
                    CoverTile(thumbnailUrls[3], Modifier.weight(1f).fillMaxSize())
                }
            }

        thumbnailUrls.isNotEmpty() -> CoverTile(thumbnailUrls.first(), Modifier.fillMaxSize())

        else ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(scheme.surfaceContainerHigh, scheme.surfaceContainerLow)),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.queue_music),
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                )
            }
    }
}

@Composable
private fun CoverTile(url: String, modifier: Modifier) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}
