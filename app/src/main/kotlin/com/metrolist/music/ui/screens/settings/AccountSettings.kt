/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import timber.log.Timber
import com.metrolist.music.utils.reportException
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.utils.parseCookieString
import com.metrolist.music.BuildConfig
import com.metrolist.music.R
import com.metrolist.music.constants.AccountChannelHandleKey
import com.metrolist.music.constants.AccountEmailKey
import com.metrolist.music.constants.AccountNameKey
import com.metrolist.music.constants.AvatarPresetKey
import com.metrolist.music.constants.AvatarShapeKey
import com.metrolist.music.constants.DataSyncIdKey
import com.metrolist.music.constants.DeveloperModeKey
import com.metrolist.music.constants.InnerTubeAuthUserKey
import com.metrolist.music.constants.InnerTubeCookieKey
import com.metrolist.music.constants.UseLoginForBrowse
import com.metrolist.music.constants.VisitorDataKey
import com.metrolist.music.constants.YtmSyncKey
import com.metrolist.music.ui.component.AvatarPreset
import com.metrolist.music.ui.component.AvatarShape
import com.metrolist.music.ui.component.DefaultDialog
import com.metrolist.music.ui.component.InfoLabel
import com.metrolist.music.ui.component.ListeningHeatmapCard
import com.metrolist.music.ui.component.ProfileAvatar
import com.metrolist.music.ui.component.Material3SettingsGroup
import com.metrolist.music.ui.component.Material3SettingsItem
import com.metrolist.music.ui.component.TextFieldDialog
import com.metrolist.music.ui.theme.Spacing
import com.metrolist.music.utils.rememberPreference
import com.metrolist.music.viewmodels.AccountSettingsViewModel
import com.metrolist.music.viewmodels.HomeViewModel

@Composable
fun AccountSettings(
    navController: NavController,
    onClose: () -> Unit,
    latestVersionName: String
) {
    val context = LocalContext.current

    val (accountNamePref, onAccountNameChange) = rememberPreference(AccountNameKey, "")
    val (accountEmail, onAccountEmailChange) = rememberPreference(AccountEmailKey, "")
    val (accountChannelHandle, onAccountChannelHandleChange) = rememberPreference(AccountChannelHandleKey, "")
    val (innerTubeCookie, onInnerTubeCookieChange) = rememberPreference(InnerTubeCookieKey, "")
    val (visitorData, onVisitorDataChange) = rememberPreference(VisitorDataKey, "")
    val (dataSyncId, onDataSyncIdChange) = rememberPreference(DataSyncIdKey, "")
    val (authUser, onAuthUserChange) = rememberPreference(InnerTubeAuthUserKey, "0")

    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val (useLoginForBrowse, onUseLoginForBrowseChange) = rememberPreference(UseLoginForBrowse, true)
    val (ytmSync, onYtmSyncChange) = rememberPreference(YtmSyncKey, true)
    // Gates the raw token/credential rows below.
    val (developerMode) = rememberPreference(DeveloperModeKey, defaultValue = false)

    val homeViewModel: HomeViewModel = hiltViewModel()
    val accountSettingsViewModel: AccountSettingsViewModel = hiltViewModel()
    val accountName by homeViewModel.accountName.collectAsStateWithLifecycle()
    val accountImageUrl by homeViewModel.accountImageUrl.collectAsStateWithLifecycle()

    var showToken by remember { mutableStateOf(false) }
    var showTokenEditor by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // ---- Profile picture -------------------------------------------------------------
    val (avatarPresetKey, onAvatarPresetChange) = rememberPreference(AvatarPresetKey, "")
    val (avatarShapeKey, onAvatarShapeChange) = rememberPreference(AvatarShapeKey, AvatarShape.Default.key)
    val avatarShape = remember(avatarShapeKey) { AvatarShape.fromKey(avatarShapeKey) }
    var showShapeDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    if (showAvatarDialog) {
        DefaultDialog(
            onDismiss = { showAvatarDialog = false },
            title = { Text(stringResource(R.string.avatar_choose)) },
            content = {
                // Grid of built-in presets plus a "use account picture" entry.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 4,
                ) {
                    AvatarPreset.entries.forEach { preset ->
                        Box(
                            modifier =
                                Modifier
                                    .padding(vertical = 6.dp)
                                    .clip(avatarShape.toShape())
                                    .clickable {
                                        onAvatarPresetChange(preset.key)
                                        showAvatarDialog = false
                                    },
                        ) {
                            ProfileAvatar(
                                presetKey = preset.key,
                                accountImageUrl = accountImageUrl,
                                shape = avatarShape,
                                size = 56.dp,
                            )
                        }
                    }
                }
            },
        )
    }

    if (showShapeDialog) {
        DefaultDialog(
            onDismiss = { showShapeDialog = false },
            title = { Text(stringResource(R.string.avatar_shape)) },
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AvatarShape.entries.forEach { shape ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onAvatarShapeChange(shape.key)
                                        showShapeDialog = false
                                    }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ProfileAvatar(
                                presetKey = avatarPresetKey,
                                accountImageUrl = accountImageUrl,
                                shape = shape,
                                size = 36.dp,
                            )
                            Text(
                                text = shape.label(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f).padding(start = 12.dp),
                            )
                            if (shape == avatarShape) {
                                Icon(
                                    painter = painterResource(R.drawable.check),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onClose) {
                Icon(painterResource(R.drawable.close), contentDescription = null)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Logout confirmation dialog
        if (showLogoutDialog) {
            DefaultDialog(
                onDismiss = { showLogoutDialog = false },
                title = { Text(stringResource(R.string.logout_dialog_title)) },
                content = {
                    Text(
                        text = stringResource(R.string.logout_dialog_message),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                },
                buttons = {
                    TextButton(
                        onClick = {
                            Timber.d("[LOGOUT_CLEAR] User chose to clear data")
                            scope.launch {
                                try {
                                    Timber.d("[LOGOUT_CLEAR] Starting clear and logout process")
                                    // Forget account first (stops all sync), then clear data.
                                    // This prevents background syncs from re-adding songs.
                                    accountSettingsViewModel.logoutAndClearLibraryData(context)
                                    Timber.d("[LOGOUT_CLEAR] Library data cleared and account forgotten")
                                } catch (e: Exception) {
                                    Timber.e(e, "[LOGOUT_CLEAR] Error clearing library data, proceeding with logout")
                                    reportException(e)
                                }
                                onInnerTubeCookieChange("")
                                Timber.d("[LOGOUT_CLEAR] Logout complete")
                                showLogoutDialog = false
                                onClose()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.logout_clear))
                    }
                    TextButton(
                        onClick = {
                            Timber.d("[LOGOUT_KEEP] User chose to keep data")
                            scope.launch {
                                Timber.d("[LOGOUT_KEEP] Starting logout process (keeping data)")
                                accountSettingsViewModel.logoutKeepData(context, onInnerTubeCookieChange)
                                Timber.d("[LOGOUT_KEEP] Logout complete")
                                showLogoutDialog = false
                                onClose()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.logout_keep))
                    }
                }
            )
        }

        if (showTokenEditor) {
            val text = """
                ***INNERTUBE COOKIE*** =$innerTubeCookie
                ***VISITOR DATA*** =$visitorData
                ***DATASYNC ID*** =$dataSyncId
                ***AUTH USER*** =$authUser
                ***ACCOUNT NAME*** =$accountNamePref
                ***ACCOUNT EMAIL*** =$accountEmail
                ***ACCOUNT CHANNEL HANDLE*** =$accountChannelHandle
            """.trimIndent()

            TextFieldDialog(
                initialTextFieldValue = TextFieldValue(text),
                onDone = { data ->
                    var cookie = ""
                    var visitorDataValue = ""
                    var dataSyncIdValue = ""
                    var authUserValue = "0"
                    var accountNameValue = ""
                    var accountEmailValue = ""
                    var accountChannelHandleValue = ""

                    data.split("\n").forEach {
                        when {
                            it.startsWith("***INNERTUBE COOKIE*** =") -> cookie = it.substringAfter("=")
                            it.startsWith("***VISITOR DATA*** =") -> visitorDataValue = it.substringAfter("=")
                            it.startsWith("***DATASYNC ID*** =") -> dataSyncIdValue = it.substringAfter("=")
                            it.startsWith("***AUTH USER*** =") -> authUserValue = it.substringAfter("=")
                            it.startsWith("***ACCOUNT NAME*** =") -> accountNameValue = it.substringAfter("=")
                            it.startsWith("***ACCOUNT EMAIL*** =") -> accountEmailValue = it.substringAfter("=")
                            it.startsWith("***ACCOUNT CHANNEL HANDLE*** =") -> accountChannelHandleValue = it.substringAfter("=")
                        }
                    }
                    // Write all credentials atomically to DataStore and wait for completion
                    // before restarting, preventing the race condition where the process
                    // would be killed before async DataStore coroutines finished writing.
                    accountSettingsViewModel.saveTokenAndRestart(
                        context = context,
                        cookie = cookie,
                        visitorData = visitorDataValue,
                        dataSyncId = dataSyncIdValue,
                        authUser = authUserValue,
                        accountName = accountNameValue,
                        accountEmail = accountEmailValue,
                        accountChannelHandle = accountChannelHandleValue,
                    )
                },
                onDismiss = { showTokenEditor = false },
                singleLine = false,
                maxLines = 20,
                isInputValid = { fullText ->
                    // Extract the cookie value from the formatted template line,
                    // then validate it separately — avoids the bug where parseCookieString
                    // received the entire multi-line template and failed to find "SAPISID"
                    // as a key because the "***INNERTUBE COOKIE*** =" prefix shadowed it.
                    val cookieLine = fullText.lines()
                        .find { it.startsWith("***INNERTUBE COOKIE*** =") }
                    val cookieValue = cookieLine?.substringAfter("***INNERTUBE COOKIE*** =")?.trim() ?: ""
                    cookieValue.isNotEmpty() && "SAPISID" in parseCookieString(cookieValue)
                },
                extraContent = {
                    Spacer(Modifier.height(8.dp))
                    InfoLabel(text = stringResource(R.string.token_adv_login_description))
                }
            )
        }

        // Listening activity. Lives here because "my listening data" is an account-shaped
        // question; the heatmap had no reachable home before (its Home header rendered without
        // a grid). Collapsed by default: it is tall and pushes the profile controls off-screen.
        val heatmapPlays by homeViewModel.heatmapPlays.collectAsStateWithLifecycle()
        var heatmapExpanded by rememberSaveable { mutableStateOf(false) }

        Material3SettingsItem(
            icon = painterResource(R.drawable.stats),
            title = { Text(stringResource(R.string.heatmap_title)) },
            description = {
                Text(
                    if (heatmapExpanded) {
                        stringResource(R.string.heatmap_collapse_hint)
                    } else {
                        stringResource(R.string.heatmap_expand_hint)
                    },
                )
            },
            trailingContent = {
                Icon(
                    painter = painterResource(
                        if (heatmapExpanded) R.drawable.expand_less else R.drawable.expand_more,
                    ),
                    contentDescription = null,
                )
            },
            onClick = { heatmapExpanded = !heatmapExpanded },
        )

        if (heatmapExpanded) {
            ListeningHeatmapCard(playsByDay = heatmapPlays)
        }

        Spacer(Modifier.height(Spacing.medium))

        // Profile picture: choose one of the built-in avatars and pick its mask. Presets only —
        // no gallery upload, so there is no photo permission and no orphaned file to clean up.
        if (isLoggedIn || avatarPresetKey.isNotBlank()) {
            Material3SettingsGroup(
                title = stringResource(R.string.avatar_section),
                items =
                    buildList {
                        add(
                            Material3SettingsItem(
                                title = { Text(stringResource(R.string.avatar_choose)) },
                                icon = painterResource(R.drawable.person),
                                onClick = { showAvatarDialog = true },
                            ),
                        )
                        add(
                            Material3SettingsItem(
                                title = { Text(stringResource(R.string.avatar_shape)) },
                                description = { Text(avatarShape.label()) },
                                icon = painterResource(R.drawable.crop),
                                onClick = { showShapeDialog = true },
                            ),
                        )
                        if (avatarPresetKey.isNotBlank()) {
                            add(
                                Material3SettingsItem(
                                    title = { Text(stringResource(R.string.avatar_reset)) },
                                    description = { Text(stringResource(R.string.avatar_reset_desc)) },
                                    icon = painterResource(R.drawable.close),
                                    onClick = { onAvatarPresetChange("") },
                                ),
                            )
                        }
                    },
            )

            Spacer(Modifier.height(8.dp))
        }

        Material3SettingsGroup(
            items = listOfNotNull(
                Material3SettingsItem(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom picture wins over the account image; the mask comes from
                            // the avatar-shape preference so both surfaces stay in sync.
                            if (isLoggedIn || avatarPresetKey.isNotBlank()) {
                                ProfileAvatar(
                                    presetKey = avatarPresetKey,
                                    accountImageUrl = accountImageUrl,
                                    shape = avatarShape,
                                    size = 40.dp,
                                )

                                Spacer(Modifier.width(12.dp))
                            }

                            Text(
                                text = if (isLoggedIn) accountName else stringResource(R.string.login),
                            )
                        }
                    },
                    icon = if (!isLoggedIn) painterResource(R.drawable.login) else null,
                    trailingContent = {
                        if (isLoggedIn) {
                            OutlinedButton(
                                onClick = {
                                    Timber.d("[LOGOUT] User clicked logout button, showing dialog")
                                    showLogoutDialog = true
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(stringResource(R.string.action_logout))
                            }
                        }
                    },
                    onClick = {
                        onClose()
                        if (isLoggedIn) {
                            navController.navigate("account")
                        } else {
                            navController.navigate("login")
                        }
                    }
                ),
                if (isLoggedIn) {
                    Material3SettingsItem(
                        title = { Text(stringResource(R.string.switch_youtube_channel)) },
                        icon = painterResource(R.drawable.account),
                        onClick = {
                            onClose()
                            navController.navigate("switch_channel")
                        },
                    )
                } else null,
            ),
            useLowContrast = true
        )

        Spacer(Modifier.height(8.dp))

        Material3SettingsGroup(
            items = listOfNotNull(
                // Raw session-token inspection is a developer tool: it exposes the account
                // credential, and a hand-edited cookie breaks the whole streaming engine.
                if (developerMode) {
                    Material3SettingsItem(
                        title = {
                            Text(
                                when {
                                    !isLoggedIn -> stringResource(R.string.advanced_login)
                                    showToken -> stringResource(R.string.token_shown)
                                    else -> stringResource(R.string.token_hidden)
                                }
                            )
                        },
                        icon = painterResource(R.drawable.token),
                        onClick = {
                            if (!isLoggedIn) showTokenEditor = true
                            else if (!showToken) showToken = true
                            else showTokenEditor = true
                        }
                    )
                } else null,
                // Account-only rows: hidden entirely when signed out rather than shown disabled,
                // because a greyed switch with no explanation reads as a bug.
                if (isLoggedIn) {
                    Material3SettingsItem(
                        title = { Text(stringResource(R.string.more_content)) },
                        icon = painterResource(R.drawable.cached),
                        trailingContent = {
                            Switch(
                                checked = useLoginForBrowse,
                                onCheckedChange = {
                                    YouTube.useLoginForBrowse = it
                                    onUseLoginForBrowseChange(it)
                                },
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (useLoginForBrowse) R.drawable.check else R.drawable.close
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )
                        },
                    )
                } else null,
                if (isLoggedIn) {
                    Material3SettingsItem(
                        title = { Text(stringResource(R.string.yt_sync)) },
                        icon = painterResource(R.drawable.cached),
                        trailingContent = {
                            Switch(
                                checked = ytmSync,
                                onCheckedChange = onYtmSyncChange,
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (ytmSync) R.drawable.check else R.drawable.close
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )
                        },
                    )
                } else null
            ),
            useLowContrast = true
        )

        Spacer(Modifier.height(12.dp))

        Material3SettingsGroup(
            items = buildList {
                // Discord / Last.fm / Listen Together are developer-mode entry points.
                if (developerMode) {
                    add(
                        Material3SettingsItem(
                            title = { Text(stringResource(R.string.integrations)) },
                            icon = painterResource(R.drawable.integration),
                            onClick = {
                                onClose()
                                navController.navigate("settings/integrations")
                            }
                        )
                    )
                }
                // Listening stats were only reachable from the top-app-bar icon, which is easy
                // to miss. The Account sheet is where users look for "my listening data".
                add(
                    Material3SettingsItem(
                        title = { Text(stringResource(R.string.stats)) },
                        description = { Text(stringResource(R.string.stats_account_desc)) },
                        icon = painterResource(R.drawable.stats),
                        onClick = {
                            onClose()
                            navController.navigate("stats")
                        }
                    )
                )
                add(
                    Material3SettingsItem(
                        title = { Text(stringResource(R.string.settings)) },
                        icon = painterResource(R.drawable.settings),
                        // No update badge: auto-update is deliberately disabled for now and will
                        // ship as a GitHub-release download later.
                        onClick = {
                            onClose()
                            navController.navigate("settings")
                        }
                    )
                )
            },
            useLowContrast = true
        )
    }
}
