/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.metrolist.music.LocalChangelogState
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.R
import com.metrolist.music.constants.LauncherIconVariantKey
import com.metrolist.music.recognition.RecognitionOverlay
import com.metrolist.music.ui.component.LongClickIconButton
import com.metrolist.music.ui.component.Material3SettingsGroup
import com.metrolist.music.ui.component.Material3SettingsItem
import com.metrolist.music.ui.theme.Spacing
import com.metrolist.music.ui.utils.backToMain
import com.metrolist.music.utils.LAUNCHER_ICON_DYNAMIC
import com.metrolist.music.utils.LAUNCHER_ICON_STATIC
import com.metrolist.music.utils.applyLauncherIconVariant
import com.metrolist.music.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Hidden developer area, unlocked by typing the secret code in the search bar.
 * The route itself is guarded in NavigationBuilder, so a stale deep link bounces back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperSettings(navController: NavController) {
    val showChangelog = LocalChangelogState.current

    Column(
        Modifier
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                ),
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.medium),
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top),
            ),
        )

        // Quick hub: every developer-grade screen reachable in one tap, so the hidden area is
        // a real index instead of two links and a "coming soon" placeholder.
        Material3SettingsGroup(
            title = stringResource(R.string.developer_options),
            items =
                listOf(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.play),
                        title = { Text(stringResource(R.string.player_and_audio)) },
                        onClick = { navController.navigate("settings/player") },
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.language),
                        title = { Text(stringResource(R.string.content)) },
                        onClick = { navController.navigate("settings/content") },
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.translate),
                        title = { Text(stringResource(R.string.developer_ai_lyrics)) },
                        onClick = { navController.navigate("settings/ai") },
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.integration),
                        title = { Text(stringResource(R.string.integrations)) },
                        onClick = { navController.navigate("settings/integrations") },
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.grid_view),
                        title = { Text(stringResource(R.string.gallery_title)) },
                        onClick = { navController.navigate("settings/widgets") },
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.grid_view),
                        title = { Text(stringResource(R.string.tactical_hub_dark)) },
                        onClick = { navController.navigate("settings/appearance/tactical") },
                    ),
                ),
        )

        Spacer(Modifier.height(Spacing.medium))

        // The recogniser overlay needs a system-only permission; there is no in-app grant.
        val context = LocalContext.current
        var canDraw by remember { mutableStateOf(RecognitionOverlay.canDraw(context)) }
        LaunchedEffect(Unit) { canDraw = RecognitionOverlay.canDraw(context) }

        Material3SettingsGroup(
            title = stringResource(R.string.recognizer_overlay_title),
            items = listOf(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.ic_widget_mic),
                    title = { Text(stringResource(R.string.recognizer_overlay_enable)) },
                    description = {
                        Text(
                            stringResource(
                                if (canDraw) {
                                    R.string.recognizer_overlay_on
                                } else {
                                    R.string.recognizer_overlay_off
                                },
                            ),
                        )
                    },
                    // Always opens the system toggle: it is the only place this can be changed,
                    // and re-opening it lets the user revoke as well as grant.
                    onClick = { RecognitionOverlay.requestPermission(context) },
                ),
            ),
        )

        Spacer(Modifier.height(Spacing.medium))

        // Launcher icon: the real logo ships enabled by default. The themed adaptive icon stays
        // reachable here so it can be checked side by side, and applying it persists across
        // restarts (the manifest only carries the default).
        val (launcherIconVariant, onLauncherIconVariantChange) =
            rememberPreference(LauncherIconVariantKey, LAUNCHER_ICON_STATIC)
        var pendingLauncherVariant by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(pendingLauncherVariant) {
            val target = pendingLauncherVariant ?: return@LaunchedEffect
            // Component changes restart the process on some launchers, so persistence comes first.
            onLauncherIconVariantChange(target)
            withContext(Dispatchers.IO) { applyLauncherIconVariant(context, target) }
            pendingLauncherVariant = null
        }

        Material3SettingsGroup(
            title = stringResource(R.string.launcher_icon_section),
            items =
                listOf(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.grid_view),
                        title = { Text(stringResource(R.string.launcher_icon_use_real)) },
                        isDeveloperOnly = true,
                        isDeveloperOnly = true,
                        description = { Text(stringResource(R.string.launcher_icon_use_real_desc)) },
                        trailingContent = {
                            Switch(
                                checked = launcherIconVariant == LAUNCHER_ICON_STATIC,
                                onCheckedChange = { useReal ->
                                    pendingLauncherVariant =
                                        if (useReal) LAUNCHER_ICON_STATIC else LAUNCHER_ICON_DYNAMIC
                                },
                            )
                        },
                        onClick = {
                            pendingLauncherVariant =
                                if (launcherIconVariant == LAUNCHER_ICON_STATIC) {
                                    LAUNCHER_ICON_DYNAMIC
                                } else {
                                    LAUNCHER_ICON_STATIC
                                }
                        },
                    ),
                ),
        )

        Spacer(Modifier.height(Spacing.medium))

        Material3SettingsGroup(
            title = stringResource(R.string.developer_diagnostics),
            items =
                listOf(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.stats),
                        title = { Text(stringResource(R.string.stats)) },
                        description = { Text(stringResource(R.string.developer_stats_desc)) },
                        onClick = { navController.navigate("stats") },
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.update),
                        title = { Text(stringResource(R.string.developer_changelog)) },
                        onClick = { showChangelog.value = true },
                    ),
                ),
        )

        Spacer(Modifier.height(Spacing.medium))

        Text(
            text = stringResource(R.string.developer_unlock_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.medium))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.developer_options)) },
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
