/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.EventWithSong
import com.metrolist.music.playback.queues.YouTubeQueue
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HeatmapCell(
    val date: String,
    val count: Int,
    val level: Int, // 0..4 intensity bucket
)

data class HeatmapState(
    val cells: Map<String, Int> = emptyMap(),
    val maxCount: Int = 0,
    val selectedDate: String? = null,
    val selectedEvents: List<EventWithSong> = emptyList(),
    val totalListeningDays: Int = 0,
)

@HiltViewModel
class HeatmapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MusicDatabase,
) : ViewModel() {

    private val _state = MutableStateFlow(HeatmapState())
    val state: StateFlow<HeatmapState> = _state.asStateFlow()

    private val days = 30
    private val cutoffDate: String
        get() = LocalDate.now().minusDays(days.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)

    init {
        loadHeatmapData()
    }

    private fun loadHeatmapData() {
        viewModelScope.launch {
            combine(
                database.dailyPlayCounts(cutoffDate),
                database.eventCount(),
            ) { dailyCounts, totalCount ->
                val maxCount = dailyCounts.values.maxOrNull() ?: 0
                val cells = dailyCounts.mapValues { (_, count) ->
                    bucketize(count, maxCount)
                }
                _state.value = HeatmapState(
                    cells = dailyCounts,
                    maxCount = maxCount,
                    totalListeningDays = dailyCounts.size,
                )
            }.stateIn(viewModelScope, SharingStarted.Lazily, HeatmapState())
        }
    }

    fun selectDate(date: String?) {
        _state.value = _state.value.copy(selectedDate = date)
        if (date != null) {
            viewModelScope.launch {
                database.eventsOnDate(date).collect { events ->
                    _state.value = _state.value.copy(selectedEvents = events)
                }
            }
        } else {
            _state.value = _state.value.copy(selectedEvents = emptyList())
        }
    }

    private fun bucketize(count: Int, max: Int): Int {
        if (count <= 0 || max <= 0) return 0
        val ratio = count.toDouble() / max.toDouble()
        return when {
            ratio <= 0.25 -> 1
            ratio <= 0.5 -> 2
            ratio <= 0.75 -> 3
            else -> 4
        }
    }
}