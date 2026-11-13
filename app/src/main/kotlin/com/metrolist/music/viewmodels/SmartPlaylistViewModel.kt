/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metrolist.music.models.SmartPlaylist
import com.metrolist.music.models.SmartPlaylistType
import com.metrolist.music.utils.SmartPlaylistGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns the generated mixes for the Home row, the Library section and the detail screen.
 *
 * One instance per screen is fine: [SmartPlaylistGenerator] is a singleton and keeps the mixes in
 * memory, so the second consumer reuses the first one's result instead of refetching.
 */
@HiltViewModel
class SmartPlaylistViewModel @Inject constructor(
    private val generator: SmartPlaylistGenerator,
) : ViewModel() {
    private val _playlists = MutableStateFlow<List<SmartPlaylist>>(emptyList())
    val playlists = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    /** Loads (or reuses) both mixes. Safe to call from a `LaunchedEffect(Unit)`. */
    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _playlists.value = generator.load(force = force)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Single mix by its stable id, for the detail screen. */
    suspend fun getById(id: String): SmartPlaylist? {
        val type = SmartPlaylistType.fromRouteId(id) ?: return null
        val existing = generator.cachedNow().firstOrNull { it.type == type }
        if (existing != null && existing.isPlayable) return existing
        return generator.load().firstOrNull { it.type == type }
    }
}
