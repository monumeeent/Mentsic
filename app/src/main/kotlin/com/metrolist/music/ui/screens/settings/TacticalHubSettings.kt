/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.R
import com.metrolist.music.constants.HubHeatmapCollapsedKey
import com.metrolist.music.constants.HubModeEnabledKey
import com.metrolist.music.ui.component.LongClickIconButton
import com.metrolist.music.ui.component.Material3SettingsGroup
import com.metrolist.music.ui.component.Material3SettingsItem
import com.metrolist.music.ui.utils.backToMain
import com.metrolist.music.utils.rememberPreference

/**
 * Settings for the Tactical Command Hub — the alternative Home layout defined by feature #28.
 *
 * Until this screen existed, [HubModeEnabledKey] was read by the home feed but never *written*
 * anywhere, so the hub and its listening heatmap were unreachable. The route is developer-gated,
 * matching how the rest of the app treats unfinished surfaces.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticalHubSettings(navController: NavController) {
    val (hubEnabled, onHubEnabledChange) = rememberPreference(HubModeEnabledKey, defaultValue = false)
    val (heatmapCollapsed, onHeatmapCollapsedChange) =
        rememberPreference(HubHeatmapCollapsedKey, defaultValue = false)

    Column(
        modifier =
            Modifier
                .windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                    ),
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top),
            ),
        )

        Material3SettingsGroup(
            title = stringResource(R.string.tactical_hub),
            items =
                buildList {
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.grid_view),
                            title = { Text(stringResource(R.string.tactical_hub_enable)) },
                            description = {
                                Text(
                                    stringResource(
                                        if (hubEnabled) {
                                            R.string.tactical_hub_enabled_desc
                                        } else {
                                            R.string.tactical_hub_disabled_desc
                                        },
                                    ),
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = hubEnabled,
                                    onCheckedChange = onHubEnabledChange,
                                    thumbContent = {
                                        Icon(
                                            painter =
                                                painterResource(
                                                    id = if (hubEnabled) R.drawable.check else R.drawable.close,
                                                ),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    },
                                )
                            },
                            onClick = { onHubEnabledChange(!hubEnabled) },
                        ),
                    )
                    // The heatmap switch only means something on the hub screen itself.
                    if (hubEnabled) {
                        add(
                            Material3SettingsItem(
                                icon = painterResource(R.drawable.stats),
                                title = { Text(stringResource(R.string.hub_heatmap_collapsed)) },
                                description = { Text(stringResource(R.string.hub_heatmap_collapsed_desc)) },
                                trailingContent = {
                                    Switch(
                                        checked = heatmapCollapsed,
                                        onCheckedChange = onHeatmapCollapsedChange,
                                        thumbContent = {
                                            Icon(
                                                painter =
                                                    painterResource(
                                                        id = if (heatmapCollapsed) R.drawable.check else R.drawable.close,
                                                    ),
                                                contentDescription = null,
                                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                            )
                                        },
                                    )
                                },
                                onClick = { onHeatmapCollapsedChange(!heatmapCollapsed) },
                            ),
                        )
                    }
                },
        )

        Spacer(modifier = Modifier.height(27.dp))

        // Plain-language description of the four zones, so "where / how to use" is answered
        // on the screen that turns the mode on.
        Material3SettingsGroup(
            title = stringResource(R.string.tactical_hub_zones),
            items =
                listOf(
                    zoneItem(painterResource(R.drawable.grid_view), stringResource(R.string.hub_zone_speed_dial)),
                    zoneItem(painterResource(R.drawable.play), stringResource(R.string.hub_zone_active_session)),
                    zoneItem(painterResource(R.drawable.language), stringResource(R.string.hub_zone_filters)),
                    zoneItem(painterResource(R.drawable.stats), stringResource(R.string.hub_zone_heatmap)),
                ),
        )

        Spacer(modifier = Modifier.height(27.dp))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.tactical_hub)) },
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

/** Non-interactive row describing one zone of the hub layout. */
@Composable
private fun zoneItem(icon: Painter, text: String) =
    Material3SettingsItem(
        icon = icon,
        title = { Text(text) },
        onClick = {},
    )


