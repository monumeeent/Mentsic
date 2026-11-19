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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.R
import com.metrolist.music.constants.SessionMaxMinutesKey
import com.metrolist.music.constants.HubHeatmapCollapsedKey
import com.metrolist.music.constants.HubModeEnabledKey
import com.metrolist.music.constants.HomeShowDiscoveryKey
import com.metrolist.music.constants.HomeShowPersonalSpaceKey
import com.metrolist.music.constants.HomeShowShufflesKey
import com.metrolist.music.constants.HomeShowSpeedDialKey
import com.metrolist.music.constants.HomeShowTopListenedKey
import com.metrolist.music.constants.HomeShowTrendingKey
import com.metrolist.music.constants.ShuffleColumnsKey
import com.metrolist.music.constants.ShuffleRowsKey
import com.metrolist.music.constants.SpeedDialColumnsKey
import com.metrolist.music.constants.SpeedDialRowsKey
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButton
import com.metrolist.music.ui.component.LongClickIconButton
import com.metrolist.music.ui.component.Material3SettingsGroup
import com.metrolist.music.ui.component.Material3SettingsItem
import com.metrolist.music.ui.utils.backToMain
import com.metrolist.music.utils.rememberPreference
import kotlin.math.roundToInt

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
        rememberPreference(HubHeatmapCollapsedKey, defaultValue = true)

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
                            isDeveloperOnly = true,
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
                                isDeveloperOnly = true,
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

        // Developer-only grid geometry for the two Home grids.
        val (sdColumns, onSdColumns) = rememberPreference(SpeedDialColumnsKey, 4)
        val (sdRows, onSdRows) = rememberPreference(SpeedDialRowsKey, 6)
        val (shColumns, onShColumns) = rememberPreference(ShuffleColumnsKey, 3)
        val (shRows, onShRows) = rememberPreference(ShuffleRowsKey, 3)

        // Each grid gets two independent sliders — columns and rows — instead of a combined
        // ± stepper: a stepper could only change one dimension at a time and gave no feel for the
        // range. Sliders snap to whole cells and show the live value above the track.
        Material3SettingsGroup(
            title = stringResource(R.string.grid_customisation),
            items =
                listOf(
                    gridSizeItem(
                        title = stringResource(R.string.speed_dial_grid),
                        columns = sdColumns,
                        rows = sdRows,
                        onColumns = onSdColumns,
                        onRows = onSdRows,
                        minColumns = 2,
                        maxColumns = 6,
                        minRows = 1,
                        maxRows = 10,
                    ),
                    gridSizeItem(
                        title = stringResource(R.string.shuffle_grid),
                        columns = shColumns,
                        rows = shRows,
                        onColumns = onShColumns,
                        onRows = onShRows,
                        minColumns = 2,
                        maxColumns = 6,
                        minRows = 1,
                        maxRows = 8,
                    ),
                ),
        )

        Spacer(modifier = Modifier.height(27.dp))

        // Developer-only: how long a Listen Together session may run before everyone is dropped.
        val (sessionMax, onSessionMax) = rememberPreference(SessionMaxMinutesKey, 120)
        Material3SettingsGroup(
            title = stringResource(R.string.listen_together),
            items =
                listOf(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.time_auto),
                        title = { Text(stringResource(R.string.session_max_minutes)) },
                        description = {
                            Column {
                                Text(
                                    if (sessionMax == 0) {
                                        "No limit"
                                    } else {
                                        "$sessionMax minutes"
                                    },
                                )
                                Slider(
                                    value = sessionMax.toFloat(),
                                    onValueChange = { onSessionMax(it.roundToInt()) },
                                    valueRange = 0f..360f,
                                    // 15-minute stops: 0, 15, 30 ... 360.
                                    steps = 23,
                                )
                            }
                        },
                        onClick = {},
                    ),
                ),
        )

        Spacer(modifier = Modifier.height(27.dp))

        // Additive section switches. A section renders only when its switch is on AND the active
        // preset includes it, so the presets stay meaningful coarse shortcuts.
        Material3SettingsGroup(
            title = stringResource(R.string.home_sections),
            items =
                listOf(
                    sectionSwitch(stringResource(R.string.trending_chart), HomeShowTrendingKey),
                    sectionSwitch(stringResource(R.string.speed_dial), HomeShowSpeedDialKey),
                    sectionSwitch(stringResource(R.string.shuffles), HomeShowShufflesKey),
                    sectionSwitch(stringResource(R.string.personal_space), HomeShowPersonalSpaceKey),
                    sectionSwitch(stringResource(R.string.top_listened), HomeShowTopListenedKey),
                    sectionSwitch(stringResource(R.string.home_discovery_rows), HomeShowDiscoveryKey),
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

/**
 * Grid size editor: one row-scoped slider per axis, with the current "C×R" shown as the
 * description. Sliders snap with `steps`, so every stop is a whole number of cells.
 */
@Composable
private fun gridSizeItem(
    title: String,
    columns: Int,
    rows: Int,
    onColumns: (Int) -> Unit,
    onRows: (Int) -> Unit,
    minColumns: Int,
    maxColumns: Int,
    minRows: Int,
    maxRows: Int,
) = Material3SettingsItem(
    title = { Text(title) },
    description = {
        Column {
            Text("$columns × $rows columns")

            Text(
                text = stringResource(R.string.grid_columns_value, columns),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            androidx.compose.material3.Slider(
                value = columns.toFloat(),
                onValueChange = { onColumns(it.roundToInt()) },
                valueRange = minColumns.toFloat()..maxColumns.toFloat(),
                // `steps` counts the stops *between* the endpoints.
                steps = (maxColumns - minColumns - 1).coerceAtLeast(0),
            )

            Text(
                text = stringResource(R.string.grid_rows_value, rows),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            androidx.compose.material3.Slider(
                value = rows.toFloat(),
                onValueChange = { onRows(it.roundToInt()) },
                valueRange = minRows.toFloat()..maxRows.toFloat(),
                steps = (maxRows - minRows - 1).coerceAtLeast(0),
            )
        }
    },
    onClick = {},
)

/** Switch row bound to a single boolean section preference. */
@Composable
private fun sectionSwitch(
    title: String,
    key: androidx.datastore.preferences.core.Preferences.Key<Boolean>,
) = Material3SettingsItem(
    title = { Text(title) },
    trailingContent = {
        val (checked, onChange) = rememberPreference(key, defaultValue = true)
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            thumbContent = {
                Icon(
                    painter = painterResource(if (checked) R.drawable.check else R.drawable.close),
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            },
        )
    },
    onClick = {},
)


