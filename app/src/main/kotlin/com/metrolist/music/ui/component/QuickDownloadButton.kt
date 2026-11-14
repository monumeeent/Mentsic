/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadService
import com.metrolist.music.LocalDownloadUtil
import com.metrolist.music.R
import com.metrolist.music.playback.ExoDownloadService

/**
 * One-tap offline control for the currently playing track.
 *
 * Downloads used to be reachable only through the song menu — two taps and a scrolled menu for
 * the most common action while a track is on screen. This button sits inline with Share/Like and
 * mirrors the menu's state machine:
 *
 * * not downloaded → start download
 * * queued / downloading → cancel
 * * completed → remove download
 *
 * Colours are passed in because the player draws over artwork with its own contrast-adjusted
 * palette rather than the theme surface.
 */
@Composable
fun QuickDownloadButton(
    songId: String?,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    if (songId.isNullOrBlank()) return

    val context = LocalContext.current
    val downloadUtil = LocalDownloadUtil.current
    val download by downloadUtil.getDownload(songId).collectAsStateWithLifecycle(initialValue = null)

    val state = download?.state
    val isBusy = state == Download.STATE_QUEUED || state == Download.STATE_DOWNLOADING

    FilledIconButton(
        onClick = { context.toggleDownload(downloadUtil, songId, state) },
        shape = CircleShape,
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
        modifier = modifier.size(42.dp),
    ) {
        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = contentColor,
            )
        } else {
            Icon(
                painter =
                    painterResource(
                        if (state == Download.STATE_COMPLETED) R.drawable.offline else R.drawable.download,
                    ),
                contentDescription =
                    stringResource(
                        if (state == Download.STATE_COMPLETED) {
                            R.string.remove_download
                        } else {
                            R.string.action_download
                        },
                    ),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/** Single place that decides cancel-vs-start, shared by both layouts. */
private fun android.content.Context.toggleDownload(
    downloadUtil: com.metrolist.music.playback.DownloadUtil,
    songId: String,
    state: Int?,
) {
    when (state) {
        Download.STATE_COMPLETED, Download.STATE_QUEUED, Download.STATE_DOWNLOADING ->
            DownloadService.sendRemoveDownload(this, ExoDownloadService::class.java, songId, false)

        else -> downloadUtil.download(songId)
    }
}
