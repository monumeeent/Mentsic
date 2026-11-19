/**
 * Mentsic Music — Shining row card.
 *
 * A full-width horizontal row that *marks itself as developer-only*: the container carries the
 * shared developer-only gradient wash (see `ui/theme/DeveloperOnly.kt`) plus a slow light sweep.
 * Regular users never see these rows, so the extra tint is a signal — "this section exists only
 * in Developer Mode" — rather than decoration on normal content.
 *
 * Design notes:
 *  - The gradient comes from theme roles only, so it follows the active palette (gold in the
 *    Shining theme, violet in the default one) instead of hard-coding a colour.
 *  - The sweep is a single `horizontalGradient` whose animated offset moves across the card. Its
 *    stops are computed from fractions of the measured width and are always strictly increasing,
 *    because `Brush.horizontalGradient` rejects duplicate/descending offsets.
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.metrolist.music.R
import com.metrolist.music.ui.theme.Spacing
import com.metrolist.music.ui.theme.developerOnlyAccent
import com.metrolist.music.ui.theme.developerOnlyBadgeBackground
import com.metrolist.music.ui.theme.developerOnlyRowBrush

/** Height of a shining row; matches the app's standard 72dp list rhythm. */
private val RowHeight = 76.dp

/** Duration of one full sweep across the card. Slow enough to read as a highlight, not a strobe. */
private const val ShineDurationMillis = 3200

/** Peak opacity of the sweep. Kept low so text remains legible at every position. */
private const val ShinePeakAlpha = 0.18f

/**
 * Full-width horizontal card that visually flags a developer-only feature.
 *
 * @param title primary line (song/playlist/section name).
 * @param subtitle secondary line (artist, count or a short description); hidden when blank.
 * @param isActive marks the item that is currently playing.
 * @param artworkUrl optional artwork; [leadingIconRes] is drawn when null.
 * @param leadingIconRes drawn inside the artwork slot when no artwork is available.
 * @param trailingIconRes icon at the end of the row (usually a play or more-vert glyph).
 * @param showDevBadge draws the small "DEV" pill; on by default because this card is only ever
 *   used for developer-only content.
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
    showDevBadge: Boolean = true,
    onClick: () -> Unit = {},
) {
    val shape = MaterialTheme.shapes.large
    // The wash is drawn over the normal container colour, so text keeps its usual contrast.
    val wash = developerOnlyRowBrush()

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
            // 1. Developer-only gradient wash (static identity).
            Box(modifier = Modifier.fillMaxSize().background(wash))
            // 2. Animated sweep on top of the wash (motion identity).
            ShineOverlay()

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
                if (showDevBadge) {
                    Spacer(Modifier.width(Spacing.small))
                    DevBadge()
                }
                if (trailingIconRes != null) {
                    Spacer(Modifier.width(Spacing.small))
                    Icon(
                        painter = painterResource(trailingIconRes),
                        contentDescription = null,
                        tint = developerOnlyAccent(),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

/** Small "DEV" pill that labels a developer-only row. */
@Composable
private fun DevBadge() {
    Surface(
        shape = CircleShape,
        color = developerOnlyBadgeBackground(),
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Text(
            text = stringResource(R.string.dev_badge),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.small, vertical = 2.dp),
        )
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
                tint = developerOnlyAccent(),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/**
 * Light sweep travelling left to right across the card.
 *
 * A single gradient is used with five stops that follow the animated position; the stops are
 * clamped into 0..0.95f and then forced to be strictly increasing, which is what
 * `Brush.horizontalGradient` requires.
 */
@Composable
private fun ShineOverlay() {
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

    if (progress < 0f || progress > 1f) return
    val stops = buildShineStops(center = progress)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.horizontalGradient(colorStops = stops)),
    )
}

/**
 * Builds a strictly increasing stop list for the sweeping highlight.
 *
 * All offsets are fractions of the card width (0 = left edge, 1 = right edge). When the band has
 * slid off the card the computed offsets collapse onto the same value, which `horizontalGradient`
 * rejects, so each offset that fails to advance on its predecessor is nudged forward by a small
 * epsilon. The 0.95f ceiling leaves room for those nudges.
 */
private fun buildShineStops(center: Float): Array<Pair<Float, Color>> {
    fun stop(offset: Float, color: Color): Pair<Float, Color> =
        offset.coerceIn(0f, 0.95f) to color

    val normalized = listOf(
        stop(center - 0.30f, Color.Transparent),
        stop(center - 0.10f, Color.Transparent),
        stop(center, Color.White.copy(alpha = ShinePeakAlpha)),
        stop(center + 0.10f, Color.Transparent),
        stop(center + 0.30f, Color.Transparent),
    ).sortedBy { it.first }

    var previous = 0f
    return normalized.map { (offset, color) ->
        val next = if (offset > previous) offset else previous + 0.004f
        previous = next
        next to color
    }.toTypedArray()
}
