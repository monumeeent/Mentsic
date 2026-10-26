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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

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
    private val cutoffMillis: Long
        get() = LocalDate.now().minusDays(days.toLong())
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    init {
        loadHeatmapData()
    }

    private fun loadHeatmapData() {
        viewModelScope.launch {
            combine(
                database.dailyPlayCounts(cutoffMillis),
                database.eventCount(),
            ) { dailyCounts, _ ->
                val maxCount = dailyCounts.values.maxOrNull() ?: 0
                HeatmapState(
                    cells = dailyCounts,
                    maxCount = maxCount,
                    totalListeningDays = dailyCounts.size,
                )
            }.collectLatest { state ->
                _state.value = state
            }
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