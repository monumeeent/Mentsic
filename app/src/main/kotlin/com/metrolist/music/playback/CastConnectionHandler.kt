package com.metrolist.music.playback

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CastConnectionHandler(
    private val context: Context,
    private val scope: CoroutineScope,
    private val musicService: MusicService,
) {
    private val _isCasting = MutableStateFlow(false)
    val isCasting: StateFlow<Boolean> = _isCasting.asStateFlow()

    val castDeviceName: StateFlow<String?> = MutableStateFlow(null).asStateFlow()
    val castPosition: StateFlow<Long> = MutableStateFlow(0L).asStateFlow()
    val castDuration: StateFlow<Long> = MutableStateFlow(0L).asStateFlow()
    val castIsPlaying: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
    val castIsBuffering: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()

    val isSyncingFromCast: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()

    fun play() {}
    fun pause() {}
    fun seekTo(positionMs: Long) {}
    fun skipToNext() {}
    fun skipToPrevious() {}
    fun release() {}
    fun disconnect() {}
    fun initialize() {}
    fun navigateToMediaIfInQueue(mediaId: String): Boolean = false
    fun loadMedia(mediaId: String) {}
}