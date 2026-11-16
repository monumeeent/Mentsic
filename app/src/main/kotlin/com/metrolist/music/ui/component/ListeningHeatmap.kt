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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.metrolist.music.R
import com.metrolist.music.ui.theme.Spacing
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Listening activity over the last [weeksToShow] weeks, GitHub-contribution style.
 *
 * @param playsByDay map of `yyyy-MM-dd` to play count, as returned by `DatabaseDao.dailyPlayCounts`.
 *
 * Days with no entry render at the lowest intensity; intensity steps up through four tonal levels
 * derived from `colorScheme.primary`, so the heatmap follows the user's accent colour and pure
 * black automatically. Empty days are still drawn so the grid keeps a stable shape.
 */
@Composable
fun ListeningHeatmap(
    playsByDay: Map<String, Int>,
    modifier: Modifier = Modifier,
    weeksToShow: Int = 18,
) {
    val scheme = MaterialTheme.colorScheme
    val today = remember { LocalDate.now() }

    // Monday-aligned grid: column = week, row = weekday.
    val startDate =
        remember(today, weeksToShow) {
            today.minusWeeks((weeksToShow - 1).toLong()).with(DayOfWeek.MONDAY)
        }

    val maxPlays = remember(playsByDay) { playsByDay.values.maxOrNull() ?: 0 }

    // Four intensity steps, plus the empty tone. Alphas rather than distinct colours keeps every
    // step readable on both the light and pure-black surfaces.
    val levels =
        remember(scheme.primary) {
            listOf(
                scheme.surfaceContainerHighest,
                scheme.primary.copy(alpha = 0.28f),
                scheme.primary.copy(alpha = 0.52f),
                scheme.primary.copy(alpha = 0.76f),
                scheme.primary,
            )
        }

    fun levelFor(count: Int): Int {
        if (count <= 0 || maxPlays <= 0) return 0
        // Bucket the day against the busiest day so a light week still shows contrast.
        val fraction = count.toFloat() / maxPlays
        return (1 + (fraction * 3.99f).toInt()).coerceIn(1, 4)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
            // Weekday rail. Only Mon/Wed/Fri are labelled; all seven rows still get a cell so the
            // grid aligns with the columns.
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.tiny),
                horizontalAlignment = Alignment.End,
            ) {
                listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                    .forEach { day ->
                        Text(
                            text =
                                if (day in listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)) {
                                    day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2)
                                } else {
                                    ""
                                },
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.height(CELL_SIZE),
                        )
                    }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
                repeat(weeksToShow) { weekIndex ->
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
                        repeat(7) { dayIndex ->
                            val date = startDate.plusDays((weekIndex * 7L) + dayIndex)
                            val inFuture = date.isAfter(today)
                            val count = playsByDay[date.toString()] ?: 0

                            Box(
                                modifier =
                                    Modifier
                                        .size(CELL_SIZE)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (inFuture) {
                                                // Future days stay invisible rather than reading as
                                                // "zero plays", which would falsely imply inactivity.
                                                scheme.surfaceContainerLow.copy(alpha = 0.25f)
                                            } else {
                                                levels[levelFor(count)]
                                            },
                                        )
                                        .semantics {
                                            contentDescription =
                                                "$date: " + count + " " + "plays"
                                        },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(Spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.heatmap_less),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(Spacing.tiny))
            levels.forEach { colour ->
                Box(
                    modifier =
                        Modifier
                            .padding(horizontal = 1.dp)
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colour),
                )
            }
            Spacer(Modifier.width(Spacing.tiny))
            Text(
                text = stringResource(R.string.heatmap_more),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

private val CELL_SIZE = 11.dp

/**
 * Card wrapper used by the Account screen: title, total, divider, then the grid.
 */
@Composable
fun ListeningHeatmapCard(
    playsByDay: Map<String, Int>,
    modifier: Modifier = Modifier,
) {
    val total = remember(playsByDay) { playsByDay.values.sum() }
    val activeDays = remember(playsByDay) { playsByDay.count { it.value > 0 } }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Text(
                text = stringResource(R.string.heatmap_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = pluralPlays(total) + " · " + pluralActiveDays(activeDays),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Spacing.medium))

            ListeningHeatmap(playsByDay = playsByDay)
        }
    }
}

@Composable
private fun pluralPlays(count: Int): String =
    androidx.compose.ui.res.pluralStringResource(R.plurals.plays_count, count, count)

@Composable
private fun pluralActiveDays(count: Int): String =
    androidx.compose.ui.res.pluralStringResource(R.plurals.active_days_count, count, count)
