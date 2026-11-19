/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
 * Listening heatmap modelled on the `@thilakbhat/heatmap-ui` calendar chart.
 *
 * Design parity with that component:
 *  - square cells with a fixed size and a uniform gap between them,
 *  - a four-step colour ramp (light → saturated) rather than a continuous gradient,
 *  - an explicit `empty` tone for days with no activity,
 *  - days the tracker never saw are drawn as "unknown" instead of "zero",
 *  - a legend row with "Less … More" and a unit label.
 *
 * @param playsByDay map of `yyyy-MM-dd` to play count, as returned by `DatabaseDao.dailyPlayCounts`.
 * @param weeksToShow how many week columns to render.
 * @param cellSize edge length of one day cell; the chart scales its ramp, not its cells, so the
 *   grid always reads as discrete squares.
 * @param gap space between adjacent cells.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ListeningHeatmap(
    playsByDay: Map<String, Int>,
    modifier: Modifier = Modifier,
    weeksToShow: Int = 22,
    cellSize: Int = 9,
    gap: Int = 2,
    showWeekdayRail: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    val today = remember { LocalDate.now() }

    // Monday-aligned grid: column = week, row = weekday. Ends on the current week so the rightmost
    // column is always "now", which is what the reference chart does (`to=` today).
    val startDate =
        remember(today, weeksToShow) {
            today.minusWeeks((weeksToShow - 1).toLong()).with(DayOfWeek.MONDAY)
        }

    // The first day the app could possibly have data for. Anything older is "unknown", not zero —
    // the same distinction the reference component makes with its `known` flag.
    val firstKnownDate = remember(playsByDay) {
        playsByDay.keys.minOrNull()?.let { key -> runCatching { LocalDate.parse(key) }.getOrNull() }
    }

    val maxPlays = remember(playsByDay) { playsByDay.values.maxOrNull() ?: 0 }

    // Four ramp steps: the darkest container first, then three steps of the accent colour. Alpha
    // (not distinct hues) keeps every step readable in light, dark and pure-black themes.
    val ramp =
        remember(scheme.primary, scheme.surfaceContainerHighest) {
            listOf(
                scheme.primary.copy(alpha = 0.25f),
                scheme.primary.copy(alpha = 0.50f),
                scheme.primary.copy(alpha = 0.75f),
                scheme.primary,
            )
        }
    val emptyColor = scheme.surfaceContainerHighest.copy(alpha = 0.55f)
    val unknownColor = scheme.surfaceContainerLow

    fun levelFor(count: Int): Int {
        if (count <= 0 || maxPlays <= 0) return -1
        val fraction = count.toFloat() / maxPlays
        return (fraction * 3.99f).toInt().coerceIn(0, 3)
    }

    val cell = cellSize.dp
    val gapDp = gap.dp

    // Tap-to-inspect: the reference component shows a tooltip; a phone has no hover, so the cell
    // is selected on tap and the reading is shown in the caption line below the grid.
    var selectedDate by rememberSaveable { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val railWidth = if (showWeekdayRail) 22.dp else 0.dp
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(gapDp)) {
                    if (showWeekdayRail) {
                        // Mon / Wed / Fri labels only; every row still reserves a cell so the
                        // columns line up with the weekday rail.
                        Column(
                            verticalArrangement = Arrangement.spacedBy(gapDp),
                            horizontalAlignment = Alignment.End,
                        ) {
                            listOf(
                                DayOfWeek.MONDAY,
                                DayOfWeek.TUESDAY,
                                DayOfWeek.WEDNESDAY,
                                DayOfWeek.THURSDAY,
                                DayOfWeek.FRIDAY,
                                DayOfWeek.SATURDAY,
                                DayOfWeek.SUNDAY,
                            ).forEach { day ->
                                Text(
                                    text =
                                        if (day in listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)) {
                                            day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2)
                                        } else {
                                            ""
                                        },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant,
                                    maxLines = 1,
                                    modifier = Modifier.width(railWidth - gapDp).height(cell),
                                )
                            }
                        }
                        Spacer(Modifier.width(gapDp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(gapDp)) {
                        repeat(weeksToShow) { weekIndex ->
                            Column(verticalArrangement = Arrangement.spacedBy(gapDp)) {
                                repeat(7) { dayIndex ->
                                    val date = startDate.plusDays((weekIndex * 7L) + dayIndex)
                                    val inFuture = date.isAfter(today)
                                    val isoDate = date.toString()
                                    val count = playsByDay[isoDate] ?: 0
                                    // Unknown: the day predates the oldest record we have, so the
                                    // tracker simply was not recording yet.
                                    val unknown = firstKnownDate != null && date.isBefore(firstKnownDate) && !inFuture
                                    val isToday = date == today
                                    val isSelected = selectedDate == isoDate

                                    val fill =
                                        when {
                                            inFuture -> Color.Transparent
                                            unknown -> unknownColor
                                            else -> ramp.getOrNull(levelFor(count)) ?: emptyColor
                                        }

                                    val shape = RoundedCornerShape(2.dp)
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(cell)
                                                .clip(shape)
                                                .background(fill)
                                                .then(
                                                    if (isToday || isSelected) {
                                                        Modifier.border(1.dp, scheme.primary, shape)
                                                    } else {
                                                        Modifier
                                                    },
                                                )
                                                .pointerInput(isoDate, inFuture) {
                                                    if (inFuture) return@pointerInput
                                                    detectTapGestures {
                                                        selectedDate = if (isSelected) null else isoDate
                                                    }
                                                }
                                                .semantics {
                                                    contentDescription =
                                                        when {
                                                            inFuture -> ""
                                                            unknown -> "No data for $isoDate"
                                                            else -> "$isoDate: $count plays"
                                                        }
                                                },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(Spacing.small))

        // Caption: the selected day's reading, otherwise the window total. Mirrors the tooltip the
        // reference component shows on hover.
        val caption = run {
            val selected = selectedDate
            if (selected != null) {
                val count = playsByDay[selected] ?: 0
                // `stringResource` with arguments applies the resource's own formatting, so a
                // translation can reorder the placeholders safely.
                stringResource(R.string.heatmap_play_count, count, selected)
            } else {
                val windowTotal =
                    playsByDay
                        .filterKeys { key ->
                            runCatching { !LocalDate.parse(key).isBefore(startDate) }.getOrDefault(false)
                        }.values
                        .sum()
                stringResource(R.string.heatmap_window_total, windowTotal, weeksToShow)
            }
        }
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
            maxLines = 1,
        )

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
            (ramp).forEach { colour ->
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
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.heatmap_unit_plays),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

/** Window sizes offered by [ListeningHeatmapCard]. */
enum class HeatmapRange(
    val weeks: Int,
    val labelRes: Int,
) {
    /** This week plus last week, at a glance. */
    WEEKLY(2, R.string.heatmap_range_weekly),
    MONTHLY(4, R.string.heatmap_range_monthly),
}

/**
 * Listening-activity card: range selector, headline numbers, then the grid.
 *
 * Today's minutes deliberately live on the Personal Space streak card instead — this card answers
 * "how consistent was I over the week/month", not "how long have I listened today".
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ListeningHeatmapCard(
    playsByDay: Map<String, Int>,
    modifier: Modifier = Modifier,
    initialRange: HeatmapRange = HeatmapRange.MONTHLY,
    // Larger surfaces (Stats, the full hub) have room for the long-range chart.
    weeks: Int? = null,
    showWeekdayRail: Boolean = true,
    cellSize: Int = 9,
) {
    var range by rememberSaveable { mutableStateOf(initialRange) }

    val today = remember { LocalDate.now() }
    val windowStart =
        remember(today, range, weeks) {
            today.minusWeeks(((weeks ?: range.weeks) - 1).toLong()).with(DayOfWeek.MONDAY)
        }

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

            // Weekly / Monthly segmented switch. Hidden when the caller pins a fixed window.
            if (weeks == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    HeatmapRange.entries.forEach { option ->
                        FilterChip(
                            selected = option == range,
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
            }

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
                weeksToShow = weeks ?: range.weeks,
                cellSize = cellSize,
                showWeekdayRail = showWeekdayRail,
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
