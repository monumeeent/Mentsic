/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Third mini-player design: the **capsule**.
 *
 * Where the stacked mini player is a wide pill holding thumbnail + title + three actions, the capsule
 * is deliberately quieter: a narrow floating island with the album art, one line of text, a
 * play/pause target, and a live waveform that stands in for the missing progress bar. It is designed
 * to sit *above* the notched navigation bar without competing with it — same floating language, half
 * the visual weight.
 *
 * Toggleable via [MiniPlayerCapsuleKey]; `MiniPlayer` picks this layout when it is on.
 */

package com.metrolist.music.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.constants.MiniPlayerHeight
import com.metrolist.music.ui.component.AudioVisualizer
import com.metrolist.music.ui.component.VisualizerStyle
import com.metrolist.music.utils.joinToArtistString
import com.metrolist.music.ui.theme.Spacing

/**
 * Capsule mini player.
 *
 * @param progressState shared progress source; only its `progress` is read, and only inside the
 *   draw phase of the waveform, so playback position updates never recompose this layout.
 */
@Composable
fun CapsuleMiniPlayer(
    progressState: ProgressState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    // `LocalPlayerConnection` lives in `com.metrolist.music` (declared next to the provider in
    // MainActivity), not in `ui.component` — importing it from the wrong package is what failed here.
    val playerConnection = LocalPlayerConnection.current ?: return
    val scheme = MaterialTheme.colorScheme

    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle(initialValue = null)
    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle(initialValue = false)
    val playbackState by playerConnection.playbackState.collectAsStateWithLifecycle(initialValue = Player.STATE_IDLE)

    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier.fillMaxWidth().height(MiniPlayerHeight),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium)
                    .height(56.dp)
                    .clip(shape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                scheme.surfaceContainerHigh,
                                scheme.surfaceContainer,
                            ),
                        ),
                    )
                    .border(1.dp, scheme.primary.copy(alpha = 0.22f), shape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                    .padding(start = Spacing.small, end = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Album art + play affordance, in one target.
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (playbackState == Player.STATE_ENDED) {
                                playerConnection.player.seekTo(0, 0)
                                playerConnection.player.playWhenReady = true
                            } else {
                                playerConnection.togglePlayPause()
                            }
                        },
                contentAlignment = Alignment.Center,
            ) {
                mediaMetadata?.thumbnailUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )
                }
                if (!isPlaying || playbackState == Player.STATE_ENDED) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(scheme.scrim.copy(alpha = 0.45f), CircleShape),
                    )
                    Icon(
                        painter =
                            painterResource(
                                if (playbackState == Player.STATE_ENDED) R.drawable.replay else R.drawable.play,
                            ),
                        contentDescription = null,
                        tint = scheme.onPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(Modifier.width(Spacing.small))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = mediaMetadata?.title.orEmpty(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text =
                        mediaMetadata
                            ?.artists
                            ?.joinToArtistString(", ") { it.name }
                            .orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.width(Spacing.small))

            // The waveform replaces the progress bar: 12 dots is enough to read as "alive" at this
            // size and costs almost nothing to paint.
            AudioVisualizer(
                barCount = 12,
                barSpacing = 2.dp,
                colors = listOf(scheme.primary.copy(alpha = 0.55f), scheme.primary),
                enabled = isPlaying,
                style = VisualizerStyle.DOTS,
                height = 28.dp,
                modifier = Modifier.width(64.dp),
            )
        }
    }
}


