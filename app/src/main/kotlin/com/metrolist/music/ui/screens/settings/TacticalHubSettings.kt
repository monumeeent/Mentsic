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
import com.metrolist.music.constants.DevFloatingDockKey
import com.metrolist.music.constants.AppleDockNavKey
import com.metrolist.music.constants.PersonalSpaceTiltFactorKey
import com.metrolist.music.constants.PersonalSpaceTiltKey
import com.metrolist.music.constants.DockOrbLiftKey
import com.metrolist.music.constants.DockOrbSizeKey
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
    // Developer-only alternate navigation bar (floating dock). Read by MainActivity.
    val (floatingDock, onFloatingDockChange) =
        rememberPreference(DevFloatingDockKey, defaultValue = false)
    // Apple-style magnifying dock: the other alternate bottom bar.
    val (appleDock, onAppleDockChange) = rememberPreference(AppleDockNavKey, defaultValue = false)
    // Personal Space tilt treatment and its strength.
    val (personalSpaceTilt, onPersonalSpaceTiltChange) =
        rememberPreference(PersonalSpaceTiltKey, defaultValue = true)
    val (personalSpaceTiltFactor, onPersonalSpaceTiltFactorChange) =
        rememberPreference(PersonalSpaceTiltFactorKey, defaultValue = 8f)

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
            isDeveloperGroup = true,
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
                    // Navigation surface: the stock bar or the floating dock. User-facing now — the
                    // toggle works with developer mode off, so the row carries no DEV wash.
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.nav_bar),
                            title = { Text(stringResource(R.string.floating_dock_navbar)) },
                            description = { Text(stringResource(R.string.floating_dock_navbar_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = floatingDock,
                                    onCheckedChange = onFloatingDockChange,
                                    thumbContent = {
                                        Icon(
                                            painter =
                                                painterResource(
                                                    id = if (floatingDock) R.drawable.check else R.drawable.close,
                                                ),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    },
                                )
                            },
                            onClick = { onFloatingDockChange(!floatingDock) },
                        ),
                    )
                    // Apple-style magnifying dock. Mutually exclusive with the notched dock above:
                    // only one alternate bottom bar can be the active surface.
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.nav_bar),
                            title = { Text(stringResource(R.string.apple_dock_navbar)) },
                            description = { Text(stringResource(R.string.apple_dock_navbar_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = appleDock,
                                    onCheckedChange = { on ->
                                        onAppleDockChange(on)
                                        // Turning the Apple dock on turns the notched dock off.
                                        if (on && floatingDock) onFloatingDockChange(false)
                                    },
                                    thumbContent = {
                                        Icon(
                                            painter =
                                                painterResource(
                                                    id = if (appleDock) R.drawable.check else R.drawable.close,
                                                ),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    },
                                )
                            },
                            onClick = {
                                val next = !appleDock
                                onAppleDockChange(next)
                                if (next && floatingDock) onFloatingDockChange(false)
                            },
                        ),
                    )
                    // Personal Space cards: tilt/perspective treatment while touched.
                    add(
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.grid_view),
                            title = { Text(stringResource(R.string.personal_space_tilt)) },
                            description = { Text(stringResource(R.string.personal_space_tilt_desc)) },
                            trailingContent = {
                                Switch(
                                    checked = personalSpaceTilt,
                                    onCheckedChange = onPersonalSpaceTiltChange,
                                    thumbContent = {
                                        Icon(
                                            painter =
                                                painterResource(
                                                    id = if (personalSpaceTilt) R.drawable.check else R.drawable.close,
                                                ),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    },
                                )
                            },
                            onClick = { onPersonalSpaceTiltChange(!personalSpaceTilt) },
                        ),
                    )
                    if (personalSpaceTilt) {
                        add(
                            dockSlider(
                                title = stringResource(R.string.personal_space_tilt_strength),
                                value = personalSpaceTiltFactor,
                                onChange = onPersonalSpaceTiltFactorChange,
                                range = 2f..16f,
                            ),
                        )
                    }
                    // The mini-player style is chosen in Appearance → Mini player (a single row that
                    // owns both STANDARD and CAPSULE), so this screen no longer duplicates the switch.
                },
        )

        Spacer(modifier = Modifier.height(27.dp))

        // Geometry for the floating notched bar. The frame (height, notch width/depth, corner radius)
        // is fixed in FloatingNotchSpec and shown below read-only; only the orb — size and lift — is
        // an actual, user-visible preference.
        if (floatingDock) {
            val (dockOrbSize, onDockOrbSize) = rememberPreference(DockOrbSizeKey, 54f)
            val (dockOrbLift, onDockOrbLift) = rememberPreference(DockOrbLiftKey, 36f)

            Material3SettingsGroup(
                title = stringResource(R.string.floating_dock_orb),
                items =
                    listOf(
                        dockSlider(
                            title = stringResource(R.string.dock_orb_size),
                            value = dockOrbSize,
                            onChange = onDockOrbSize,
                            range = 36f..64f,
                        ),
                        dockSlider(
                            title = stringResource(R.string.dock_orb_lift),
                            value = dockOrbLift,
                            onChange = onDockOrbLift,
                            range = 0f..36f,
                        ),
                    ),
            )

            Spacer(modifier = Modifier.height(27.dp))

            // Reference readout of the fixed frame: discoverable, but not a control.
            Material3SettingsGroup(
                title = stringResource(R.string.floating_dock_geometry),
                items =
                    listOf(
                        fixedGeometryItem(stringResource(R.string.dock_body_height), "48 dp"),
                        fixedGeometryItem(stringResource(R.string.dock_notch_width), "180 dp"),
                        fixedGeometryItem(stringResource(R.string.dock_notch_depth), "48 dp"),
                        fixedGeometryItem(stringResource(R.string.dock_corner_radius), "8 dp"),
                    ),
            )

            Spacer(modifier = Modifier.height(27.dp))
        }

        // Developer-only grid geometry for the two Home grids. Defaults mirror the constants in
        // NewHomeScreen (Speed Dial 6x3, Shuffles 5x2) so the sliders open on the live values.
        val (sdColumns, onSdColumns) = rememberPreference(SpeedDialColumnsKey, 6)
        val (sdRows, onSdRows) = rememberPreference(SpeedDialRowsKey, 3)
        val (shColumns, onShColumns) = rememberPreference(ShuffleColumnsKey, 5)
        val (shRows, onShRows) = rememberPreference(ShuffleRowsKey, 2)

        // Each grid gets two independent sliders — columns and rows — instead of a combined
        // ± stepper: a stepper could only change one dimension at a time and gave no feel for the
        // range. Sliders snap to whole cells and show the live value above the track.
        Material3SettingsGroup(
            title = stringResource(R.string.grid_customisation),
            isDeveloperGroup = true,
            items =
                listOf(
                    gridSizeItem(
                        title = stringResource(R.string.speed_dial_grid),
                        columns = sdColumns,
                        rows = sdRows,
                        onColumns = onSdColumns,
                        onRows = onSdRows,
                        minColumns = 2,
                        maxColumns = 8,
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
            isDeveloperGroup = true,
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

/**
 * One dp slider for the floating dock orb. The live value is printed beside the title in dp so the
 * tuning is precise instead of "a bit more than last time".
 */
@Composable
private fun dockSlider(
    title: String,
    value: Float,
    onChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
) = Material3SettingsItem(
    title = { Text("$title · ${value.roundToInt()}dp") },
    description = {
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            // One stop per 1dp step, so a slider release lands on a whole dp.
            steps = (range.endInclusive - range.start).roundToInt() - 1,
        )
    },
    onClick = {},
)

/**
 * Read-only row for a fixed dock constant (bar height, notch size, corner radius).
 *
 * These are no longer preferences: the bar and the space MainActivity reserves for it both come
 * from the same `FloatingNotchSpec`, so a value changed here could only ever disagree with the
 * drawing. The row still shows the number, so the geometry is documented where users look for it.
 */
@Composable
private fun fixedGeometryItem(
    title: String,
    value: String,
) = Material3SettingsItem(
    title = { Text(title) },
    trailingContent = {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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


