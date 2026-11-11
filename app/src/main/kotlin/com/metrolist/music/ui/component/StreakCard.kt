/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.music.R
import com.metrolist.music.ui.theme.IconSizes
import com.metrolist.music.ui.theme.Spacing
import com.metrolist.music.utils.StreakDay

/**
 * Daily listening streak summary, shown at the top of Personal Space.
 *
 * Compose port of the `StreakCard` React component: header with flame + title and a link
 * action, the current streak, a Monday-first week strip, longest/total stats, and a
 * collapsible "How do streaks work?" section.
 */
@Composable
fun StreakCard(
    week: List<StreakDay>,
    currentStreak: Int,
    longestStreak: Int,
    total: Int,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.streak_title),
    actionLabel: String = stringResource(R.string.streak_view_details),
    onActionClick: (() -> Unit)? = null,
    showHowItWorks: Boolean = true,
    howItWorksTitle: String = stringResource(R.string.streak_how_it_works),
    howItWorksItems: List<String> = listOf(
        stringResource(R.string.streak_rule_1),
        stringResource(R.string.streak_rule_2),
        stringResource(R.string.streak_rule_3),
    ),
    defaultHowItWorksOpen: Boolean = false,
) {
    var isHowItWorksOpen by rememberSaveable { mutableStateOf(defaultHowItWorksOpen) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Streak summary card" },
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            // Header: flame + title, link action on the trailing edge.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                Icon(
                    painter = painterResource(R.drawable.flame),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(IconSizes.Medium),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = { if (onActionClick != null) onActionClick() else isHowItWorksOpen = !isHowItWorksOpen },
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(Spacing.small))

            // Current streak, e.g. "12 days".
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = currentStreak.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(Spacing.small))
                Text(
                    text = stringResource(R.string.streak_days),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.small),
                )
            }

            Spacer(Modifier.height(Spacing.medium))

            StreakWeekStrip(week = week)

            Spacer(Modifier.height(Spacing.medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(Spacing.medium))

            // Longest streak / total.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Streak stats" },
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.streak_longest),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = longestStreak.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.width(Spacing.tiny))
                        Text(
                            text = stringResource(R.string.streak_days),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = stringResource(R.string.streak_total),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = total.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (showHowItWorks) {
                Spacer(Modifier.height(Spacing.medium))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(Spacing.medium))

                val chevronRotation by animateFloatAsState(
                    targetValue = if (isHowItWorksOpen) 180f else 0f,
                    label = "streakChevron",
                )

                Surface(
                    onClick = { isHowItWorksOpen = !isHowItWorksOpen },
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { stateDescription = if (isHowItWorksOpen) "Expanded" else "Collapsed" },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = howItWorksTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            painter = painterResource(R.drawable.expand_more),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(IconSizes.Medium)
                                .rotate(chevronRotation),
                        )
                    }
                }

                if (isHowItWorksOpen) {
                    Spacer(Modifier.height(Spacing.medium))
                    Column(
                        modifier = Modifier.padding(horizontal = Spacing.small),
                        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                    ) {
                        howItWorksItems.forEachIndexed { index, item ->
                            // Mirrors the React card: check, flame, refresh.
                            val icon = when (index) {
                                0 -> R.drawable.check
                                1 -> R.drawable.flame
                                else -> R.drawable.refresh
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                                Icon(
                                    painter = painterResource(icon),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(IconSizes.Small),
                                )
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Monday-to-Sunday strip; active days are filled with the primary color. */
@Composable
private fun StreakWeekStrip(week: List<StreakDay>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        week.forEach { day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.tiny),
            ) {
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .size(IconSizes.ExtraLarge)
                        .clip(CircleShape)
                        .background(
                            when {
                                day.isActive -> MaterialTheme.colorScheme.primary
                                day.isFuture -> MaterialTheme.colorScheme.surfaceContainerLow
                                else -> MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                        )
                        .then(
                            // Today gets a ring so it is findable even when inactive.
                            if (day.isToday) {
                                Modifier.border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape,
                                )
                            } else {
                                Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (day.isActive) {
                        Icon(
                            painter = painterResource(R.drawable.check),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(IconSizes.Small),
                        )
                    }
                }
            }
        }
    }
}
