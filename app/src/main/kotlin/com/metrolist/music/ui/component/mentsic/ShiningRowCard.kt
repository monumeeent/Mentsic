/**
 * Mentsic Music — Shining horizontal row card.
 *
 * A full-width, one-line list row: leading artwork slot, title/subtitle stack and a trailing
 * action slot. The card is drawn with the theme's own surface tokens and carries an animated
 * light sweep across its whole width.
 *
 * This component is used by surfaces that only exist while Developer Mode is on (the Command Hub
 * home layout), which is why it may animate continuously: it is never part of the normal feed.
 *
 * Rulebook compliance: no raw colours are used for the surface, text or icon roles. The only
 * literal colours are the white sweep highlight and its transparent counterpart, which are
 * luminance values, not palette roles (same approach as `GlassTokens`).
 */

package com.metrolist.music.ui.component.mentsic

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.metrolist.music.ui.theme.Spacing
import com.metrolist.music.ui.theme.isShiningThemeActive

/** Height of a shining row; matches the app's standard 72dp list rhythm. */
private val RowHeight = 72.dp

/** Duration of one full sweep across the card. Slow enough to read as a highlight, not a strobe. */
private const val ShineDurationMillis = 2600

/** Peak opacity of the sweep. Kept low so text remains legible at every position. */
private const val ShinePeakAlpha = 0.22f

/**
 * Full-width horizontal card with a light sweep.
 *
 * @param title primary line (song/playlist/section name).
 * @param subtitle secondary line (artist, count or a short description); hidden when blank.
 * @param isActive marks the item that is currently playing.
 * @param artworkUrl optional artwork; a music-note glyph is drawn when null.
 * @param leadingIconRes drawn inside the artwork slot when no artwork is available.
 * @param trailingIconRes icon at the end of the row (usually a play or more-vert glyph).
 */
@Composable
fun ShiningRowCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    artworkUrl: String? = null,
    leadingIconRes: Int? = null,
    trailingIconRes: Int? = null,
    onClick: () -> Unit = {},
) {
    val shape = MaterialTheme.shapes.large
    // The sweep only animates inside the Shining theme (and the dev-only layouts that use this
    // card). Everywhere else the transition is still created but the target is a static, fully
    // transparent gradient.
    val shineEnabled = isShiningThemeActive()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(RowHeight)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (isActive) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ShineOverlay(enabled = shineEnabled)

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ArtworkSlot(
                    artworkUrl = artworkUrl,
                    iconRes = leadingIconRes,
                    isActive = isActive,
                )
                Spacer(Modifier.width(Spacing.medium))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (trailingIconRes != null) {
                    Spacer(Modifier.width(Spacing.small))
                    Icon(
                        painter = painterResource(trailingIconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

/** Circular artwork slot shared by every shining row. */
@Composable
private fun ArtworkSlot(
    artworkUrl: String?,
    iconRes: Int?,
    isActive: Boolean,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (isActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (artworkUrl != null) {
            AsyncImage(
                model = artworkUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/**
 * Diagonal light sweep travelling left to right across the card.
 *
 * Two passes are painted: a narrow bright core and a wider soft halo, so the highlight reads as
 * glass rather than a stripe. When [enabled] is false the brush is fully transparent and the
 * infinite transition still exists but is invisible.
 */
@Composable
private fun ShineOverlay(enabled: Boolean) {
    val transition = rememberInfiniteTransition(label = "shining-row")
    val progress by transition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = ShineDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shining-row-progress",
    )

    if (!enabled) return

    val highlight = Color.White
    val transparent = Color.Transparent

    // The band is positioned by the animated progress plus the card's own width, so it glides
    // edge to edge without clamping stops. `Brush.linearGradient` rejects duplicate or
    // descending stops, so the offsets are always strictly increasing — which clamping to
    // 0..1f could not guarantee near the ends of the sweep.
    val width = size.width
    val centerX = progress * width
    val bandHalfWidth = width * 0.30f
    val coreHalfWidth = width * 0.10f

    val haloBrush = Brush.horizontalGradient(
        colorStops = arrayOf(
            0f to transparent,
            ((centerX - bandHalfWidth).coerceAtLeast(0f) / width) to transparent,
            (centerX / width).coerceIn(0.001f, 0.999f) to highlight.copy(alpha = ShinePeakAlpha * 0.35f),
            ((centerX + bandHalfWidth).coerceAtMost(width) / width) to transparent,
            1f to transparent,
        ),
    )
    val coreBrush = Brush.horizontalGradient(
        colorStops = arrayOf(
            0f to transparent,
            ((centerX - coreHalfWidth).coerceAtLeast(0f) / width) to transparent,
            (centerX / width).coerceIn(0.001f, 0.999f) to highlight.copy(alpha = ShinePeakAlpha),
            ((centerX + coreHalfWidth).coerceAtMost(width) / width) to transparent,
            1f to transparent,
        ),
    )

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(haloBrush)
                .background(coreBrush),
        )
    }
}
