/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.settings

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.R
import com.metrolist.music.ui.component.LongClickIconButton
import com.metrolist.music.ui.component.mentsic.WidgetCaption
import com.metrolist.music.ui.component.mentsic.WidgetKind
import com.metrolist.music.ui.component.mentsic.WidgetPreview
import com.metrolist.music.ui.theme.Spacing
import com.metrolist.music.ui.utils.backToMain
import com.metrolist.music.widget.MusicWidgetReceiver
import com.metrolist.music.widget.RecognizerWidgetReceiver
import com.metrolist.music.widget.TurntableWidgetReceiver

/**
 * Widget gallery: lists the widgets the app ships with, shows a Compose mock-up of each, and
 * pins one to the launcher home screen in a single tap.
 *
 * Pinning goes through [AppWidgetManager.requestPinAppWidget]; launchers are allowed to refuse or
 * ignore the request, so every failure path ends in a toast instead of a dead tap.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsScreen(navController: NavController) {
    val context = LocalContext.current
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val canPin =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            appWidgetManager.isRequestPinAppWidgetSupported

    val widgets =
        remember {
            listOf(
                GalleryEntry(
                    kind = WidgetKind.PLAYER,
                    receiver = MusicWidgetReceiver::class.java,
                    titleRes = R.string.widget_music_player,
                    descriptionRes = R.string.gallery_widget_player_desc,
                ),
                GalleryEntry(
                    kind = WidgetKind.CIRCLE,
                    receiver = TurntableWidgetReceiver::class.java,
                    titleRes = R.string.widget_turntable,
                    descriptionRes = R.string.gallery_widget_turntable_desc,
                ),
                GalleryEntry(
                    kind = WidgetKind.LIST,
                    receiver = RecognizerWidgetReceiver::class.java,
                    titleRes = R.string.widget_recognizer_name,
                    descriptionRes = R.string.widget_recognizer_description,
                ),
            )
        }

    val onPin: (GalleryEntry) -> Unit = { entry ->
        val receiver = entry.receiver
        val activity = context.findActivity()
        when {
            receiver == null -> toast(context, R.string.gallery_widget_unavailable)
            !canPin || activity == null -> toast(context, R.string.gallery_widget_pin_unsupported)
            else -> {
                val successCallback =
                    android.app.PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, activity::class.java),
                        android.app.PendingIntent.FLAG_IMMUTABLE or
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT,
                    )
                val requested =
                    appWidgetManager.requestPinAppWidget(
                        ComponentName(context, receiver),
                        Bundle().apply {
                            // Ask for the widest default footprint both providers support.
                            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
                            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 140)
                        },
                        successCallback,
                    )
                if (!requested) toast(context, R.string.gallery_widget_pin_unsupported)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            contentPadding = PaddingValues(horizontal = Spacing.medium, vertical = Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top),
                    ),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.gallery_how_to),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.small),
                )
            }

            items(widgets, key = { it.titleRes }) { entry ->
                WidgetCard(
                    entry = entry,
                    pinnable = entry.receiver != null && canPin,
                    onPin = { onPin(entry) },
                )
            }
        }
    }

    TopAppBar(
        title = { Text(stringResource(R.string.gallery_title)) },
        navigationIcon = {
            LongClickIconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
private fun WidgetCard(
    entry: GalleryEntry,
    pinnable: Boolean,
    onPin: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // `clip` precedes `clickable` so the ripple stays inside the rounded preview (rulebook §5).
        WidgetPreview(
            kind = entry.kind,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .clickable(onClick = onPin),
        )
        Spacer(Modifier.height(Spacing.small))
        Text(
            text = stringResource(entry.titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        WidgetCaption(
            text = stringResource(entry.descriptionRes),
            enabled = pinnable,
        )
        Spacer(Modifier.height(Spacing.small))
        Button(
            onClick = onPin,
            enabled = pinnable,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(
                    if (pinnable) R.string.gallery_add_to_home else R.string.gallery_widget_unavailable,
                ),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = Spacing.small),
            )
        }
    }
}

private data class GalleryEntry(
    val kind: WidgetKind,
    val receiver: Class<*>?,
    val titleRes: Int,
    val descriptionRes: Int,
)

private fun toast(context: Context, messageRes: Int) {
    Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show()
}

/** Compose hosts live inside a ContextWrapper chain; the pin dialog needs the real Activity. */
private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
