/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadService
import com.metrolist.music.playback.DownloadUtil
import com.metrolist.music.playback.ExoDownloadService
import com.metrolist.music.playback.MusicService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MusicWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Only trigger update through MusicService if it's already running
        // This prevents BackgroundServiceStartNotAllowedException on Android 14+
        if (MusicService.isRunning) {
            val intent = Intent(context, MusicService::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                // Service might be restricted in background
            }
        }
        // If service is not running, widget shows default layout until user opens app
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        // Trigger widget update when size changes
        if (MusicService.isRunning) {
            val intent = Intent(context, MusicService::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                // Service might be restricted in background
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            // Quick download: the widget cannot know download state cheaply, so it just forwards
            // the id here and this receiver decides add-vs-remove from the live download manager.
            ACTION_TOGGLE_DOWNLOAD -> {
                val songId = intent.getStringExtra(EXTRA_SONG_ID).orEmpty()
                if (songId.isNotBlank()) {
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        toggleDownload(context.applicationContext, songId)
                    }
                }
            }

            ACTION_PLAY_PAUSE, ACTION_LIKE, ACTION_NEXT, ACTION_PREVIOUS -> {
                // User interactions from widget buttons can start the service
                // Android allows starting FGS from widget PendingIntent clicks
                val serviceIntent = Intent(context, MusicService::class.java).apply {
                    action = intent.action
                    putExtras(intent)
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    // Service might be restricted in background
                }
            }
        }
    }

    /**
     * Adds or removes an offline copy, then repaints the widget.
     *
     * Resolved through a Hilt entry point because a manifest receiver has no injection site.
     */
    private suspend fun toggleDownload(
        context: Context,
        songId: String,
    ) {
        val downloadUtil = EntryPointAccessors
            .fromApplication(context, DownloadEntryPoint::class.java)
            .downloadUtil()

        val existing = downloadUtil.getDownload(songId).first()
        when (existing?.state) {
            Download.STATE_COMPLETED, Download.STATE_QUEUED, Download.STATE_DOWNLOADING ->
                DownloadService.sendRemoveDownload(
                    context,
                    ExoDownloadService::class.java,
                    songId,
                    false,
                )

            else -> downloadUtil.download(songId)
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DownloadEntryPoint {
        fun downloadUtil(): DownloadUtil
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.metrolist.music.widget.PLAY_PAUSE"
        const val ACTION_TOGGLE_DOWNLOAD = "com.metrolist.music.widget.TOGGLE_DOWNLOAD"
        const val EXTRA_SONG_ID = "com.metrolist.music.widget.EXTRA_SONG_ID"
        const val ACTION_LIKE = "com.metrolist.music.widget.LIKE"
        const val ACTION_NEXT = "com.metrolist.music.widget.NEXT"
        const val ACTION_PREVIOUS = "com.metrolist.music.widget.PREVIOUS"
        const val ACTION_UPDATE_WIDGET = "com.metrolist.music.widget.UPDATE_WIDGET"
        const val ACTION_PLAY_TARGET = "com.metrolist.music.widget.PLAY_TARGET"
        const val EXTRA_WIDGET_TARGET_TYPE = "com.metrolist.music.widget.EXTRA_WIDGET_TARGET_TYPE"
        const val EXTRA_WIDGET_TARGET_ID = "com.metrolist.music.widget.EXTRA_WIDGET_TARGET_ID"
        const val TARGET_TYPE_NOW_PLAYING = "com.metrolist.music.widget.TARGET_TYPE_NOW_PLAYING"
    }
}
