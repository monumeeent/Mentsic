/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.music.viewmodels.HeatmapState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val heatmapColors = listOf(
    Color(0xFF161B22), // 0 - empty
    Color(0xFF0E4429), // 1
    Color(0xFF196127), // 2
    Color(0xFF26A65A), // 3
    Color(0xFF3FB950), // 4 - max
)

@Composable
fun ListeningHeatmap(
    state: HeatmapState,
    onDaySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = remember { generateDayCells() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Listening Heatmap",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${state.totalListeningDays} active days in the last 30",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Less",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    heatmapColors.forEach { color ->
                        Spacer(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color, RoundedCornerShape(3.dp)),
                        )
                    }
                    Text(
                        "More",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = 7,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.height(180.dp),
                userScrollEnabled = false,
            ) {
                items(days) { day ->
                    HeatmapCell(
                        day = day,
                        count = state.cells[day] ?: 0,
                        level = bucketizeLevel(state.cells[day] ?: 0, state.maxCount),
                        isSelected = state.selectedDate == day,
                        onClick = { onDaySelected(if (state.selectedDate == day) null else day) },
                    )
                }
            }

            if (state.selectedDate != null && state.selectedEvents.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Played on ${formatDate(state.selectedDate)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                state.selectedEvents.take(3).forEach { eventWithSong ->
                    Text(
                        text = "• ${eventWithSong.event.songId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeatmapCell(
    day: String,
    count: Int,
    level: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val baseColor = heatmapColors[level]
    val displayColor = if (isSelected) MaterialTheme.colorScheme.primary else baseColor

    Spacer(
        modifier = Modifier
            .size(16.dp)
            .background(displayColor, RoundedCornerShape(4.dp))
            .let {
                if (count > 0) {
                    it.clickable(onClick = onClick)
                } else {
                    it
                }
            },
    )
}

private fun generateDayCells(): List<String> {
    val today = LocalDate.now()
    return (0 until 30).map { today.minusDays(it.toLong()) }.reversed().map { it.format(DateTimeFormatter.ISO_LOCAL_DATE) }
}

private fun formatDate(date: String): String {
    return try {
        LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    } catch (e: Exception) {
        date
    }
}

private fun bucketizeLevel(count: Int, max: Int): Int {
    if (count <= 0 || max <= 0) return 0
    val ratio = count.toDouble() / max.toDouble()
    return when {
        ratio <= 0.25 -> 1
        ratio <= 0.5 -> 2
        ratio <= 0.75 -> 3
        else -> 4
    }
}