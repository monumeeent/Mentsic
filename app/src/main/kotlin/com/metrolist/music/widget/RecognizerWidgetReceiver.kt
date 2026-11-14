/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import com.metrolist.music.MainActivity
import com.metrolist.music.R
import com.metrolist.music.recognition.RecognitionForegroundService
import timber.log.Timber
import java.io.File

/**
 * Home-screen music recognizer.
 *
 * [RecognitionForegroundService] already owns the whole recognition pipeline and persists its
 * progress into `recognizer_widget_prefs`; this provider only mirrors that state into the widget
 * and starts/stops the service when the surface is tapped. Keeping the drawing side dumb means a
 * pulse animation frame or a "no match" result never has to know about RemoteViews.
 */
class RecognizerWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { id -> render(context, appWidgetManager, id) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_TOGGLE -> toggleRecognition(context)

            // The service broadcasts these itself after each state change, so the widget can
            // repaint without polling.
            RecognitionForegroundService.ACTION_WIDGET_STATE_CHANGED,
            AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, RecognizerWidgetReceiver::class.java))
                ids.forEach { id -> render(context, manager, id) }
            }
        }
    }

    private fun toggleRecognition(context: Context) {
        val prefs = context.getSharedPreferences(RecognitionForegroundService.PREFS_NAME, Context.MODE_PRIVATE)
        val state = prefs.getInt(RecognitionForegroundService.PREF_STATE, RecognitionForegroundService.STATE_IDLE)
        val isBusy =
            state == RecognitionForegroundService.STATE_LISTENING ||
                state == RecognitionForegroundService.STATE_PROCESSING

        val action =
            if (isBusy) {
                RecognitionForegroundService.ACTION_STOP_WIDGET_RECOGNITION
            } else {
                RecognitionForegroundService.ACTION_START_WIDGET_RECOGNITION
            }

        val serviceIntent =
            Intent(context, RecognitionForegroundService::class.java).apply {
                this.action = action
            }

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }.onFailure {
            // A widget tap is a user gesture, so the FGS start is allowed; if the OS still
            // refuses (restricted background) the widget simply keeps showing its last state.
            Timber.w(it, "Recognizer widget could not start the service")
        }
    }

    /**
     * Reads the persisted state and pushes it into one widget instance.
     *
     * Called from [onUpdate] and after every service state change.
     */
    private fun render(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
        val prefs = context.getSharedPreferences(RecognitionForegroundService.PREFS_NAME, Context.MODE_PRIVATE)
        val state = prefs.getInt(RecognitionForegroundService.PREF_STATE, RecognitionForegroundService.STATE_IDLE)

        val views = RemoteViews(context.packageName, R.layout.widget_recognizer)

        val (title, subtitle) =
            when (state) {
                RecognitionForegroundService.STATE_LISTENING ->
                    context.getString(R.string.widget_recognizer_listening) to
                        context.getString(R.string.widget_recognizer_name)

                RecognitionForegroundService.STATE_PROCESSING ->
                    context.getString(R.string.widget_recognizer_processing) to
                        context.getString(R.string.widget_recognizer_name)

                RecognitionForegroundService.STATE_SUCCESS -> {
                    val title = prefs.getString(RecognitionForegroundService.PREF_SONG_TITLE, null)
                        ?: context.getString(R.string.widget_recognizer_unknown_song)
                    val artist = prefs.getString(RecognitionForegroundService.PREF_ARTIST_NAME, null)
                        ?: context.getString(R.string.widget_recognizer_unknown_artist)
                    title to artist
                }

                RecognitionForegroundService.STATE_NO_MATCH ->
                    context.getString(R.string.widget_recognizer_no_match) to
                        context.getString(R.string.widget_recognizer_tap_to_search)

                RecognitionForegroundService.STATE_ERROR -> {
                    val message = prefs.getString(RecognitionForegroundService.PREF_ERROR_MESSAGE, null)
                    context.getString(R.string.widget_recognizer_error) to
                        (message ?: context.getString(R.string.widget_recognizer_error_generic))
                }

                else ->
                    context.getString(R.string.widget_recognizer_tap_to_search) to
                        context.getString(R.string.widget_recognizer_name)
            }

        views.setTextViewText(R.id.widget_recognizer_title, title)
        views.setTextViewText(R.id.widget_recognizer_subtitle, subtitle)

        // Artwork only on a successful match; the microphone glyph is the idle/working state.
        val artPath = prefs.getString(RecognitionForegroundService.PREF_COVER_ART_PATH, null).orEmpty()
        val hasArt = state == RecognitionForegroundService.STATE_SUCCESS && artPath.isNotBlank()
        if (hasArt) {
            runCatching { BitmapFactory.decodeFile(File(artPath).absolutePath) }
                .getOrNull()
                ?.let { bitmap ->
                    views.setImageViewBitmap(R.id.widget_recognizer_art, bitmap)
                    views.setViewVisibility(R.id.widget_recognizer_art, android.view.View.VISIBLE)
                    views.setViewVisibility(R.id.widget_recognizer_icon, android.view.View.GONE)
                }
        }
        if (!hasArt) {
            views.setViewVisibility(R.id.widget_recognizer_art, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_recognizer_icon, android.view.View.VISIBLE)
        }

        // Tapping the surface toggles listening/stop. A successful result opens recognition
        // history instead, so the tap leads somewhere useful once there is something to show.
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val clickIntent =
            when (state) {
                RecognitionForegroundService.STATE_SUCCESS -> {
                    // Reuses the existing recognition entry point: MainActivity routes this action
                    // to the recognition screen, where the saved result is already listed.
                    Intent(context, MainActivity::class.java).apply {
                        action = MainActivity.ACTION_RECOGNITION
                        this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                }

                else -> {
                    Intent(context, RecognizerWidgetReceiver::class.java).apply {
                        action = ACTION_TOGGLE
                    }
                }
            }
        // A startActivity intent cannot travel through getBroadcast, so success uses the activity
        // PendingIntent and everything else toggles through this receiver.
        val pendingIntent =
            if (clickIntent.component?.className == RecognizerWidgetReceiver::class.java.name) {
                PendingIntent.getBroadcast(context, appWidgetId, clickIntent, flags)
            } else {
                PendingIntent.getActivity(context, appWidgetId, clickIntent, flags)
            }
        views.setOnClickPendingIntent(android.R.id.background, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val ACTION_TOGGLE = "com.metrolist.music.widget.recognizer.TOGGLE"
    }
}
