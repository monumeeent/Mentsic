/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.music.R
import com.metrolist.music.db.entities.AllTimeStats
import com.metrolist.music.db.entities.DailyListeningStats
import com.metrolist.music.ui.theme.Spacing
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Five-step intensity scale used by [MonthlyPlayHeatmap], resolved **as a percentage of the peak**.
 *
 * Absolute thresholds (5 / 15 / 35 plays) only made sense for heavy listeners: someone who plays a
 * handful of tracks a day would see every single cell stuck at level 1. Measuring each day against
 * the busiest day in the visible window means the grid always spans the full ramp for that user.
 *
 * Buckets: >0–20% → 1, >20–45% → 2, >45–75% → 3, >75% → 4. Any play at all still lights the cell,
 * so "present but quiet" never looks the same as "did not listen".
 */
private fun levelFor(plays: Int, peakPlays: Int): Int {
    if (plays <= 0) return 0
    if (peakPlays <= 0) return 1
    val fraction = plays.toFloat() / peakPlays
    return when {
        fraction <= 0.20f -> 1
        fraction <= 0.45f -> 2
        fraction <= 0.75f -> 3
        else -> 4
    }
}

/**
 * Monthly play heatmap: a calendar-grid visualisation of daily listening.
 *
 * One cell per calendar day, arranged in a real month grid (Sun–Sat), with a month switcher and an
 * all-time ribbon above it. Tapping a day opens a capsule describing that day.
 *
 * @param dailyStats per-day aggregates keyed by `yyyy-MM-dd`, from `HomeViewModel.dailyStats`.
 * @param allTime lifetime totals for the ribbon.
 * @param onOpenDay invoked with the tapped date, so the host can open that day's history (the
 *   same `eventsOnDate` query the old heatmap used) instead of doing per-cell lookups here.
 */
@Composable
fun MonthlyPlayHeatmap(
    dailyStats: Map<String, DailyListeningStats>,
    allTime: AllTimeStats,
    modifier: Modifier = Modifier,
    initialMonth: YearMonth = YearMonth.now(),
    // Compact mode drops the all-time ribbon and the day capsule: used inside the Personal Space
    // card, where the grid alone has to fit a fixed height.
    showRibbon: Boolean = true,
    onOpenDay: (LocalDate) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    var month by rememberSaveable { mutableStateOf(initialMonth.toString()) }
    val currentMonth = remember(month) { YearMonth.parse(month) }
    var selectedDay by rememberSaveable { mutableStateOf<String?>(null) }

    val byDay = remember(dailyStats) { dailyStats }

    Column(modifier = modifier.fillMaxWidth()) {
        MonthHeader(
            month = currentMonth,
            monthStats = remember(currentMonth, byDay) { statsForMonth(currentMonth, byDay) },
            canGoBack = currentMonth.isAfter(YearMonth.from(LocalDate.of(2000, 1, 1))),
            canGoForward = currentMonth.isBefore(YearMonth.now()),
            onPrevious = {
                month = currentMonth.minusMonths(1).toString()
                selectedDay = null
            },
            onNext = {
                month = currentMonth.plusMonths(1).toString()
                selectedDay = null
            },
        )

        Spacer(Modifier.height(Spacing.small))

        WeekdayHeaderRow()

        Spacer(Modifier.height(Spacing.tiny))

        AnimatedContent(
            targetState = currentMonth,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "heatmap-month",
        ) { animatedMonth ->
            val monthRows = remember(animatedMonth, byDay) {
                statsForMonth(animatedMonth, byDay)
            }
            // Peak day of the visible month: the denominator for the percentage ramp.
            val peakPlays = remember(monthRows) { monthRows.maxOfOrNull { it.playCount } ?: 0 }
            MonthGrid(
                month = animatedMonth,
                byDay = byDay,
                peakPlays = peakPlays,
                selectedDay = selectedDay,
                onSelectDay = { date ->
                    selectedDay = if (selectedDay == date.toString()) null else date.toString()
                },
            )
        }

        if (showRibbon) {
            selectedDay?.let { iso ->
                val date = remember(iso) { runCatching { LocalDate.parse(iso) }.getOrNull() }
                val stats = byDay[iso]
                if (date != null) {
                    Spacer(Modifier.height(Spacing.small))
                    DayCapsule(date = date, stats = stats, onOpenDay = onOpenDay)
                }
            }

            Spacer(Modifier.height(Spacing.medium))

            AllTimeRibbon(allTime = allTime)
        }
    }
}

/** Month name, that month's totals, and the scrub chevrons. */
@Composable
private fun MonthHeader(
    month: YearMonth,
    monthStats: List<DailyListeningStats>,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val plays = remember(monthStats) { monthStats.sumOf { it.playCount } }
    val minutes = remember(monthStats) { monthStats.sumOf { it.listenMillis } / 60_000L }
    val label = remember(month) {
        "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.heatmap_month_summary, plays, minutes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        IconButton(onClick = onPrevious, enabled = canGoBack) {
            Icon(
                painter = painterResource(R.drawable.arrow_back),
                contentDescription = stringResource(R.string.heatmap_prev_month),
                tint = if (canGoBack) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                },
            )
        }
        IconButton(onClick = onNext, enabled = canGoForward) {
            Icon(
                painter = painterResource(R.drawable.navigate_next),
                contentDescription = stringResource(R.string.heatmap_next_month),
                tint = if (canGoForward) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                },
            )
        }
    }
}

/** S M T W T F S — the column captions of the calendar grid. */
@Composable
private fun WeekdayHeaderRow() {
    // Monday-first or Sunday-first depends on the locale's convention.
    val firstDay = remember {
        when (Locale.getDefault().country) {
            "US", "CA", "JP", "IN" -> java.time.DayOfWeek.SUNDAY
            else -> java.time.DayOfWeek.MONDAY
        }
    }
    val days = remember(firstDay) { (0 until 7).map { firstDay.plus(it.toLong()) } }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
        days.forEach { day ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * Current-week strip: seven cells for the week containing [today], labelled by weekday.
 *
 * The intensity ramp is measured against the busiest day **of that week**, so a light week still
 * shows contrast instead of seven identically pale boxes. Used by the Personal Space card, which
 * has room for one row but not a month grid.
 */
@Composable
fun CurrentWeekHeatmap(
    dailyStats: Map<String, DailyListeningStats>,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val weekStart = remember(today) {
        // Monday-first everywhere the app is Monday-first; Sunday-first for the locales that
        // expect it. Mirrors WeekdayHeaderRow so the strip and the month grid agree.
        val mondayFirst = when (Locale.getDefault().country) {
            "US", "CA", "JP", "IN" -> false
            else -> true
        }
        val back = if (mondayFirst) today.dayOfWeek.value - 1 else today.dayOfWeek.value % 7
        today.minusDays(back.toLong())
    }
    val days = remember(weekStart) { (0L..6L).map { weekStart.plusDays(it) } }
    val peakPlays =
        remember(days, dailyStats) {
            days.maxOfOrNull { dailyStats[it.toString()]?.playCount ?: 0 } ?: 0
        }
    val weekPlays = remember(days, dailyStats) {
        days.sumOf { dailyStats[it.toString()]?.playCount ?: 0 }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.heatmap_this_week),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.heatmap_week_plays, weekPlays),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.height(Spacing.small))

        // Weekday captions sit **above** the cells, like a calendar column header — never inside the
        // cell. Three things keep them visually separate from the date blocks:
        //   1. a fixed-height caption band (not a text line box that varies with font scale),
        //   2. a real gap (Spacing.small) between the band and the cell row, so no edge can touch,
        //   3. an explicit `includeFontPadding = false` + pinned lineHeight, which removes the extra
        //      leading that used to push the glyph down into the gap.
        val captionStyle =
            MaterialTheme.typography.labelSmall.copy(
                lineHeight = MaterialTheme.typography.labelSmall.fontSize * 1.0f,
                platformStyle =
                    androidx.compose.ui.text.PlatformTextStyle(
                        includeFontPadding = false,
                        lineHeightStyleAlignment = androidx.compose.ui.text.style.LineHeightStyleAlignment.Top,
                    ),
            )
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.tiny),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            days.forEach { date ->
                Box(
                    modifier = Modifier.weight(1f).height(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        style = captionStyle,
                        color =
                            if (date == today) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible,
                    )
                }
            }
        }

        // The gap between the weekday band and the date blocks. This is the separation that was
        // missing: previously only `Spacing.tiny` (4dp) sat between a 14dp band and a cell that can be
        // 46dp tall, which read as one merged block.
        Spacer(Modifier.height(Spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.tiny),
        ) {
            days.forEach { date ->
                HeatmapCell(
                    date = date,
                    stats = dailyStats[date.toString()],
                    peakPlays = peakPlays,
                    isSelected = false,
                    isToday = date == today,
                    isFuture = date.isAfter(today),
                    modifier = Modifier.weight(1f),
                    onClick = {},
                )
            }
        }
    }
}

/** The 7-column calendar for one month, padded with blank slots before day 1. */
@Composable
private fun MonthGrid(
    month: YearMonth,
    byDay: Map<String, DailyListeningStats>,
    peakPlays: Int,
    selectedDay: String?,
    onSelectDay: (LocalDate) -> Unit,
) {
    val firstDay = remember { month.atDay(1) }
    // Number of blank cells before day 1, honouring the locale's first weekday.
    val leadingBlanks = remember(month) {
        val dayOfWeek = firstDay.dayOfWeek.value // 1 = Monday … 7 = Sunday
        val mondayFirst = when (Locale.getDefault().country) {
            "US", "CA", "JP", "IN" -> false
            else -> true
        }
        if (mondayFirst) dayOfWeek - 1 else dayOfWeek % 7
    }
    val today = remember { LocalDate.now() }

    val cells = remember(month) { month.lengthOfMonth() }
    val rows = remember(leadingBlanks, cells) { (leadingBlanks + cells + 6) / 7 }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
        var day = 1
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.tiny),
            ) {
                repeat(7) { colIndex ->
                    val slotIndex = rowIndex * 7 + colIndex
                    if (slotIndex < leadingBlanks || day > cells) {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = month.atDay(day)
                        val iso = date.toString()
                        HeatmapCell(
                            date = date,
                            stats = byDay[iso],
                            peakPlays = peakPlays,
                            isSelected = selectedDay == iso,
                            isToday = date == today,
                            isFuture = date.isAfter(today),
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectDay(date) },
                        )
                        day++
                    }
                }
            }
        }
    }
}

/**
 * One day cell: fill intensity encodes that day's play count.
 *
 * Layering is deliberate, back to front: **density fill → contrast scrim → day number**. The
 * intensity ramp reaches full `primary`, which is exactly where `onSurfaceVariant` text used to
 * vanish. The scrim is a translucent veil derived from `surfaceContainerLowest` / `onSurface` so the
 * date number is legible on every intensity level, in every palette (including OLED black and the
 * gold Shining theme) and in both light and dark.
 */
@Composable
private fun HeatmapCell(
    date: LocalDate,
    stats: DailyListeningStats?,
    peakPlays: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val plays = stats?.playCount ?: 0
    val level = levelFor(plays, peakPlays)

    // Ramp built from theme roles, so it follows the palette (and turns gold in Shining).
    val fill =
        when {
            isFuture -> Color.Transparent
            level == 0 -> scheme.surfaceContainerHighest.copy(alpha = 0.35f)
            level == 1 -> scheme.primary.copy(alpha = 0.20f)
            level == 2 -> scheme.primary.copy(alpha = 0.45f)
            level == 3 -> scheme.primary.copy(alpha = 0.75f)
            else -> scheme.primary
        }

    // Level 4 "peak session" gets a glow: a soft accent halo drawn behind the cell.
    val glowAlpha by animateFloatAsState(
        targetValue = if (level == 4) 0.45f else 0f,
        animationSpec = tween(400),
        label = "heatmap-glow",
    )

    // Compact calendar cells are ~34-46dp wide: a single line of `labelSmall` fits everywhere and
    // never wraps the date onto a second row.
    val numberStyle = MaterialTheme.typography.labelSmall.copy(lineHeight = 11.sp)

    // Legend-style contrast: the label sits on whichever of onSurface / onPrimary reads against the
    // fill, rather than on the fill itself. Light fills get onSurface, inky fills get onPrimary.
    val numberColor =
        when {
            isFuture -> scheme.onSurfaceVariant.copy(alpha = 0.30f)
            level >= 3 -> scheme.onPrimary
            level == 0 -> scheme.onSurfaceVariant.copy(alpha = 0.55f)
            else -> scheme.onSurface.copy(alpha = 0.85f)
        }

    // Top scrim: an alpha wash over the fill that darkens (dark theme) or lightens (light theme)
    // the density colour under the date. Derived from surface roles, never a literal black/white.
    val scrimColor =
        if (scheme.onSurface.luminance() > 0.5f) {
            scheme.onSurface.copy(alpha = 0.06f)
        } else {
            scheme.surfaceContainerLowest.copy(alpha = 0.28f)
        }

    val shape = RoundedCornerShape(4.dp)

    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .pointerInput(isFuture, date) {
                    if (isFuture) return@pointerInput
                    detectTapGestures { onClick() }
                }
                .semantics {
                    contentDescription =
                        if (isFuture) {
                            ""
                        } else {
                            "$date: $plays plays"
                        }
                },
        contentAlignment = Alignment.Center,
    ) {
        if (glowAlpha > 0f) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(scheme.primary.copy(alpha = glowAlpha * 0.5f)),
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(shape)
                    .background(fill)
                    .then(
                        when {
                            isSelected || isToday -> Modifier.border(1.5.dp, scheme.primary, shape)
                            level == 0 -> Modifier.border(1.dp, scheme.outlineVariant.copy(alpha = 0.6f), shape)
                            else -> Modifier
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (!isFuture) {
                // Layer 1: contrast scrim over the density fill, under the number.
                Box(
                    modifier =
                        Modifier
                            // BoxScope member: fills the parent cell without importing anything.
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, scrimColor),
                                ),
                            ),
                )

                // Layer 2: the date, always on top. Weak shadows are intentionally avoided — the
                // colour switch above handles contrast without an extra draw pass per cell.
                Text(
                    text = date.dayOfMonth.toString(),
                    style = numberStyle,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = numberColor,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

/** Glass capsule describing the selected day. */
@Composable
private fun DayCapsule(
    date: LocalDate,
    stats: DailyListeningStats?,
    onOpenDay: (LocalDate) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val plays = stats?.playCount ?: 0
    val minutes = (stats?.listenMillis ?: 0L) / 60_000L
    val label = remember(date) {
        date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault()))
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceContainerHigh,
        onClick = { onOpenDay(date) },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    // Subtle glass sheen so the capsule reads as an overlay, not as another grid row.
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                scheme.primary.copy(alpha = 0.07f),
                                Color.Transparent,
                            ),
                        ),
                    ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        maxLines = 1,
                    )
                    Text(
                        text =
                            if (plays == 0) {
                                // A real zero, not missing data: the app was opened but nothing played.
                                stringResource(R.string.heatmap_day_empty)
                            } else {
                                stringResource(R.string.heatmap_day_summary, plays, minutes)
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (plays > 0) {
                    Spacer(Modifier.width(Spacing.small))
                    Surface(
                        shape = CircleShape,
                        color = scheme.primary,
                        contentColor = scheme.onPrimary,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = stringResource(R.string.heatmap_open_day),
                            modifier = Modifier.padding(Spacing.small),
                        )
                    }
                }
            }
        }
    }
}

/** Lifetime totals under the grid. */
@Composable
private fun AllTimeRibbon(allTime: AllTimeStats) {
    val hours = remember(allTime.totalMillis) { allTime.totalMillis / 3_600_000.0 }
    val hoursText = remember(hours) { String.format(Locale.getDefault(), "%.1f", hours) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        RibbonStat(
            value = allTime.activeDays.toString(),
            label = stringResource(R.string.heatmap_active_days),
            modifier = Modifier.weight(1f),
        )
        RibbonStat(
            value = allTime.totalPlays.toString(),
            label = stringResource(R.string.heatmap_total_plays),
            modifier = Modifier.weight(1f),
        )
        RibbonStat(
            value = hoursText,
            label = stringResource(R.string.heatmap_hours),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RibbonStat(
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
                overflow = TextOverflow.Ellipsis,
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

/** Rows of [dailyStats] that fall inside [month], used for the header summary. */
private fun statsForMonth(
    month: YearMonth,
    byDay: Map<String, DailyListeningStats>,
): List<DailyListeningStats> {
    val prefix = "%04d-%02d-".format(month.year, month.monthValue)
    return byDay.filterKeys { it.startsWith(prefix) }.values.toList()
}
