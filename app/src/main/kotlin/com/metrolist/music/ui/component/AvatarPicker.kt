/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.isSystemInDarkTheme
import com.metrolist.music.R
import com.metrolist.music.constants.DarkModeKey
import com.metrolist.music.ui.screens.settings.DarkMode
import com.metrolist.music.utils.rememberEnumPreference
import com.yalantis.ucrop.UCrop
import java.io.File

/**
 * Photo picker + square crop for the profile picture.
 *
 * Mirrors the playlist-cover flow: the system picker returns a content URI, UCrop writes a square
 * JPEG into the app's private storage, and the resulting `file://` path is what gets persisted.
 * Using a private copy (rather than the picked URI) keeps the avatar valid after the user revokes
 * photo access or the source document disappears.
 */
class AvatarPicker internal constructor(
    private val open: () -> Unit,
) {
    fun open() = open.invoke()
}

@Composable
fun rememberAvatarPicker(onCropped: (String) -> Unit): AvatarPicker {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    // DarkMode lives in AppearanceSettings; the explicit type argument keeps
    // rememberEnumPreference from failing to infer T from its defaulted parameter here.
    val (darkMode, _) = rememberEnumPreference<DarkMode>(DarkModeKey, defaultValue = DarkMode.AUTO)
    val darkTheme = darkMode == DarkMode.ON || (darkMode == DarkMode.AUTO && isSystemInDarkTheme())

    var pendingDest by remember { mutableStateOf<Uri?>(null) }

    val cropLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val output = result.data?.let { UCrop.getOutput(it) } ?: pendingDest
                // UCrop wrote straight into a private FileProvider destination, so the path is
                // already app-owned and stable; no copy is needed.
                output?.path?.let(onCropped)
            }
            pendingDest = null
        }

    val pickLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri ?: return@rememberLauncherForActivityResult

            val destFile = File(context.filesDir, "avatar.jpg")
            val destUri = FileProvider.getUriForFile(context, "${context.packageName}.FileProvider", destFile)
            pendingDest = destUri

            val options =
                UCrop.Options().apply {
                    setCompressionFormat(Bitmap.CompressFormat.JPEG)
                    setCompressionQuality(92)
                    setHideBottomControls(true)
                    setToolbarTitle(context.getString(R.string.avatar_change))
                    setStatusBarLight(!darkTheme)
                    setToolbarColor(scheme.surface.toArgb())
                    setToolbarWidgetColor(scheme.inverseSurface.toArgb())
                    setRootViewBackgroundColor(scheme.surface.toArgb())
                    setLogoColor(scheme.surface.toArgb())
                }

            val intent =
                UCrop
                    .of(uri, destUri)
                    .withAspectRatio(1f, 1f)
                    .withOptions(options)
                    .getIntent(context)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            cropLauncher.launch(intent)
        }

    return remember(pickLauncher) {
        AvatarPicker {
            pickLauncher.launch(
                PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        }
    }
}
