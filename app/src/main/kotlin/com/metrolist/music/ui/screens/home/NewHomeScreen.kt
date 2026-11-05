/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metrolist.music.ui.component.GlassCard
import com.metrolist.music.ui.theme.Spacing

/**
 * New home page view demonstrating the redesigned design system.
 *
 * Uses [GlassCard] for card components, [Spacing] tokens for consistent layout,
 * and [MaterialTheme.colorScheme] for all colors — no hardcoded colors.
 *
 * @param modifier Compose modifier
 * @param onSectionClick Optional callback when a section header is clicked
 */
@Composable
fun NewHomeScreen(
    modifier: Modifier = Modifier,
    onSectionClick: ((String) -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
    ) {
        // Header
        Text(
            text = "For You",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = Spacing.medium),
        )

        Spacer(modifier = Modifier.height(Spacing.tiny))

        // Quick picks section (glass card)
        GlassCard(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            onClick = { onSectionClick?.invoke("quick_picks") },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Spacing.medium),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(
                    text = "Quick Picks",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.small))

        // Forgotten favorites section (glass card)
        GlassCard(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            onClick = { onSectionClick?.invoke("forgotten_favorites") },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Spacing.medium),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(
                    text = "Forgotten Favorites",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.small))

        // Daily discover section (glass card)
        GlassCard(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            onClick = { onSectionClick?.invoke("daily_discover") },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Spacing.medium),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(
                    text = "Daily Discover",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.medium))

        // Content sections using LazyColumn
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(
                listOf(
                    "Speed Dial",
                    "Similar Recommendations",
                    "Community Playlists",
                ),
                key = { it },
            ) { section ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = Spacing.tiny),
                    onClick = { onSectionClick?.invoke(section) },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(Spacing.medium),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            text = section,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}