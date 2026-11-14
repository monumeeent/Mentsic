/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.recognition

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.metrolist.music.R

/**
 * Quick Settings tile: pull down the shade, tap, and the song around you is identified.
 *
 * The tile deliberately does **not** own the recognition flow. It hands off to
 * [RecognitionLaunchActivity], which already solves both branches — start the foreground service
 * when RECORD_AUDIO is granted, or open the in-app permission/recognition screen when it is not.
 * That keeps a single implementation of the permission dance.
 *
 * On Android 14+ an app can only start a foreground service of type `microphone` from an
 * eligible state; a user tap on a tile counts, so the hand-off is legal.
 */
@RequiresApi(Build.VERSION_CODES.N)
class RecognitionTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            label = getString(R.string.widget_recognizer_name)
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()

        // Mark the tile busy while the recognition activity spins up, then reset. The activity
        // finishes immediately, so this is purely short-lived feedback.
        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            updateTile()
        }

        val launch =
            Intent(this, RecognitionLaunchActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ requires an explicit PendingIntent for tile-initiated activity starts.
            startActivityAndCollapse(
                android.app.PendingIntent.getActivity(
                    this,
                    0,
                    launch,
                    android.app.PendingIntent.FLAG_IMMUTABLE or
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(launch)
        }

        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }
}
