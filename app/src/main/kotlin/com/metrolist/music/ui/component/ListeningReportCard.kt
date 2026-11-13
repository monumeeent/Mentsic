/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.metrolist.music.models.ListeningReport
import com.metrolist.music.ui.theme.Spacing

/**
 * Recap summary shown at the top of the Stats screen: three headline numbers, the top five tracks
 * and the top artists, plus a share action. Purely presentational — [onShare] owns the bitmap work.
 */
@Composable
fun ListeningReportCard(
    report: ListeningReport,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Text(
                text = stringResource(R.string.report_title),
                style = MaterialTheme.typography.titleLarge,
                color = scheme.onSurface,
            )
            Text(
                text = report.periodLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.medium))

            if (report.isEmpty) {
                Text(
                    text = stringResource(R.string.report_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
                return@Column
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                StatBlock(
                    value = report.totalMinutes.toString(),
                    label = stringResource(R.string.report_minutes),
                    modifier = Modifier.weight(1f),
                )
                StatBlock(
                    value = report.totalPlays.toString(),
                    label = stringResource(R.string.report_plays),
                    modifier = Modifier.weight(1f),
                )
                StatBlock(
                    value = report.uniqueArtists.toString(),
                    label = stringResource(R.string.report_artists),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(Spacing.medium))
            HorizontalDivider(color = scheme.outlineVariant)
            Spacer(Modifier.height(Spacing.small))

            Text(
                text = stringResource(R.string.report_top_tracks),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary,
            )
            report.topSongs.take(5).forEachIndexed { index, highlight ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.tiny),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(36.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(
                                    Brush.linearGradient(
                                        listOf(scheme.surfaceContainerHighest, scheme.surfaceContainer),
                                    ),
                                ),
                    ) {
                        AsyncImage(
                            model = highlight.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = Spacing.small)) {
                        Text(
                            text = "${index + 1}. ${highlight.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = highlight.artistName,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = "${highlight.playCount}×",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.primary,
                    )
                }
            }

            if (report.topArtistNames.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.small))
                Text(
                    text = stringResource(R.string.report_top_artists),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.primary,
                )
                Text(
                    text = report.topArtistNames.take(4).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Spacing.tiny),
                )
            }

            Spacer(Modifier.height(Spacing.small))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text =
                        stringResource(R.string.report_active_days) + ": " +
                            "${report.activeDays}/${report.periodDays}",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onShare) {
                    Icon(
                        painter = painterResource(R.drawable.share),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.report_share),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = Spacing.small),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Placeholder row used while the report is still loading. */
@Composable
fun ListeningReportPlaceholder(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(0.5f)
                        .height(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            )
            Spacer(Modifier.height(Spacing.small))
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(0.3f)
                        .height(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            )
        }
    }
}
