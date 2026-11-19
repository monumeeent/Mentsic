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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
 * @param weeksToShow how many week columns to render. The caller picks the window: 12 for a
 *   monthly view, fewer for a weekly one.
 * @param emptyDayAlpha opacity of a day with no plays. Raised by callers that embed the grid in a
 *   card, where the default "nearly invisible" empty tone reads as a rendering bug.
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
    emptyDayAlpha: Float = 1f,
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
    // step readable on both the light and pure-black surfaces. `emptyDayAlpha` lets a caller draw
    // idle days more firmly when the grid sits on an already-tinted card.
    val levels =
        remember(scheme.primary, scheme.surfaceContainerHighest, emptyDayAlpha) {
            listOf(
                scheme.surfaceContainerHighest.copy(alpha = emptyDayAlpha),
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

/** Window sizes offered by [ListeningHeatmapCard]. */
enum class HeatmapRange(
    val weeks: Int,
    val labelRes: Int,
) {
    /** Two weekly columns plus the current week: a "this week / last week" glance. */
    WEEKLY(2, R.string.heatmap_range_weekly),
    MONTHLY(4, R.string.heatmap_range_monthly),
}

/**
 * Listening-activity card: range selector, headline numbers, then the grid.
 *
 * Today's minutes deliberately live on the Personal Space streak card instead — this card answers
 * "how consistent was I over the week/month", not "how long have I listened today".
 */
@Composable
fun ListeningHeatmapCard(
    playsByDay: Map<String, Int>,
    modifier: Modifier = Modifier,
    initialRange: HeatmapRange = HeatmapRange.MONTHLY,
) {
    var range by rememberSaveable { mutableStateOf(initialRange) }

    val today = remember { LocalDate.now() }
    val windowStart = remember(today, range) { today.minusWeeks((range.weeks - 1).toLong()).with(DayOfWeek.MONDAY) }

    // Only days inside the selected window count towards the headline numbers, so switching
    // Weekly → Monthly changes the totals instead of leaving last month silently included.
    val windowPlays =
        remember(playsByDay, windowStart, today) {
            playsByDay.filterKeys { key ->
                runCatching {
                    val date = LocalDate.parse(key)
                    !date.isBefore(windowStart) && !date.isAfter(today)
                }.getOrDefault(false)
            }
        }
    val total = remember(windowPlays) { windowPlays.values.sum() }
    val activeDays = remember(windowPlays) { windowPlays.count { it.value > 0 } }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.heatmap_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.heatmap_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(Spacing.small))

            // Weekly / Monthly segmented switch.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                HeatmapRange.entries.forEach { option ->
                    val selected = option == range
                    FilterChip(
                        selected = selected,
                        onClick = { range = option },
                        label = {
                            Text(
                                text = stringResource(option.labelRes),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                HeatmapStat(
                    value = total.toString(),
                    label = stringResource(R.string.heatmap_stat_plays),
                    modifier = Modifier.weight(1f),
                )
                HeatmapStat(
                    value = activeDays.toString(),
                    label = stringResource(R.string.heatmap_stat_active_days),
                    modifier = Modifier.weight(1f),
                )
                HeatmapStat(
                    value = remember(total, activeDays) {
                        if (activeDays == 0) "—" else (total / activeDays).toString()
                    },
                    label = stringResource(R.string.heatmap_stat_avg),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(Spacing.medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Spacing.medium))

            ListeningHeatmap(
                playsByDay = playsByDay,
                weeksToShow = range.weeks,
                emptyDayAlpha = 0.9f,
            )
        }
    }
}

/** One headline number inside [ListeningHeatmapCard]. */
@Composable
private fun HeatmapStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(vertical = Spacing.small, horizontal = Spacing.small)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
