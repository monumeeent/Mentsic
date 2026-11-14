/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.R
import com.metrolist.music.constants.DeveloperModeKey
import com.metrolist.music.ui.component.LongClickIconButton
import com.metrolist.music.ui.component.Material3SettingsGroup
import com.metrolist.music.ui.component.Material3SettingsItem
import com.metrolist.music.ui.utils.backToMain
import com.metrolist.music.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    latestVersionName: String,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    // The Developer entry only exists while the hidden mode is on.
    val developerMode by rememberPreference(DeveloperModeKey, defaultValue = false)
    val isAndroid12OrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val hasAndroidAuto = remember {
        try {
            context.packageManager.getPackageInfo(
                "com.google.android.projection.gearhead", 0
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Top
                )
            )
        )

        // User Interface Section
        Material3SettingsGroup(
            title = stringResource(R.string.settings_section_ui),
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.palette),
                    title = { Text(stringResource(R.string.appearance)) },
                    onClick = { navController.navigate("settings/appearance") }
                ),
                // Widget gallery is a developer surface: it advertises widgets whose
                // configuration is still being worked on, so it stays out of the normal UI.
                if (developerMode) {
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.grid_view),
                        title = { Text(stringResource(R.string.gallery_title)) },
                        description = { Text(stringResource(R.string.gallery_how_to)) },
                        onClick = { navController.navigate("settings/widgets") },
                    )
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Player & Content Section. These three screens expose developer-grade knobs (audio
        // pipeline, hiding filters, AI translation keys), so they are developer-mode only.
        if (developerMode) {
            Material3SettingsGroup(
                title = stringResource(R.string.settings_section_player_content),
                items = listOf(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.play),
                        title = { Text(stringResource(R.string.player_and_audio)) },
                        onClick = { navController.navigate("settings/player") }
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.language),
                        title = { Text(stringResource(R.string.content)) },
                        onClick = { navController.navigate("settings/content") }
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.translate),
                        title = { Text(stringResource(R.string.ai_lyrics_translation)) },
                        onClick = { navController.navigate("settings/ai") }
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Android Auto Section — only shown if installed, and only in developer mode.
        if (hasAndroidAuto && developerMode) {
            Material3SettingsGroup(
                title = "Android Auto",
                items = listOf(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.ic_android_auto),
                        title = { Text(stringResource(R.string.android_auto)) },
                        onClick = { navController.navigate("settings/android_auto") }
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Privacy & Security Section
        Material3SettingsGroup(
            title = stringResource(R.string.settings_section_privacy),
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.security),
                    title = { Text(stringResource(R.string.privacy)) },
                    onClick = { navController.navigate("settings/privacy") }
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Storage & Data Section
        Material3SettingsGroup(
            title = stringResource(R.string.settings_section_storage),
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.storage),
                    title = { Text(stringResource(R.string.storage)) },
                    onClick = { navController.navigate("settings/storage") }
                ),
                Material3SettingsItem(
                    icon = painterResource(R.drawable.restore),
                    title = { Text(stringResource(R.string.backup_restore)) },
                    onClick = { navController.navigate("settings/backup_restore") }
                )
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // System & About Section
        Material3SettingsGroup(
            title = stringResource(R.string.settings_section_system),
            items = buildList {
                // "Open supported links" rewrites the OS intent filters, so it stays a
                // developer-only tool: a wrong choice silently breaks YouTube link handling.
                if (isAndroid12OrLater && developerMode) {
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.link),
                            title = { Text(stringResource(R.string.default_links)) },
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                                        "package:${context.packageName}".toUri()
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    when (e) {
                                        is ActivityNotFoundException -> {
                                            Toast.makeText(
                                                context,
                                                R.string.open_app_settings_error,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                        is SecurityException -> {
                                            Toast.makeText(
                                                context,
                                                R.string.open_app_settings_error,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                        else -> {
                                            Toast.makeText(
                                                context,
                                                R.string.open_app_settings_error,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            }
                        )
                    )
                }
                if (developerMode) {
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.bug_report),
                            title = { Text(stringResource(R.string.developer_options)) },
                            onClick = { navController.navigate("settings/developer") }
                        )
                    )
                }
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.info),
                        title = { Text(stringResource(R.string.about)) },
                        onClick = { navController.navigate("settings/about") }
                    )
                )
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.settings)) },
        navigationIcon = {
            LongClickIconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        }
    )
}
