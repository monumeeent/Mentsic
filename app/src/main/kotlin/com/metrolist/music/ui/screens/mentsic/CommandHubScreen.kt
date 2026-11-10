/**
 * Mentsic Music — Tactical Command Hub screen (Material 3 Expressive).
 *
 * Scrollable body with tactical header, speed dial pager, active session card,
 * community recommendations, and listening heatmap. The top app bar and navigation
 * bar stay fixed while the body scrolls vertically.
 */

package com.metrolist.music.ui.screens.mentsic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icons
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.Tabs
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.music.R
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.ui.component.mentsic.ConnectedButtonGroup
import com.metrolist.music.ui.component.mentsic.MentsicButtonItem
import com.metrolist.music.ui.component.mentsic.MentsicButtonType
import com.metrolist.music.ui.component.mentsic.MentsicNavigationBar
import com.metrolist.music.ui.theme.MentsicTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandHubScreen(
    onNavigateToHome: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    MentsicTheme(darkTheme = androidx.compose.foundation.isSystemInDarkTheme()) {
        val haptics = LocalHapticFeedback.current
        val playerConnection = LocalPlayerConnection.current
        val activeTab by remember { mutableStateOf(0) }

        ScaffoldContent(
            haptics = haptics,
            playerConnection = playerConnection,
            activeTab = activeTab,
@Composable
private fun ScaffoldContent(
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    playerConnection: com.metrolist.music.playback.PlayerConnection?,
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = modifier.fillMaxSize()) {
            CommandHubTopBar(onNavigateToHome = onNavigateToHome)
            Spacer(Modifier.height(16.dp))
            CommandHubTabs(activeTab = activeTab, onTabSelected = onTabSelected)
            Spacer(Modifier.height(16.dp))
            CommandHubSpeedDialHeading()
            Spacer(Modifier.height(8.dp))
            CommandHubFocusButtons()
            Spacer(Modifier.height(16.dp))
            CommandHubActiveSessionHeading()
            Spacer(Modifier.height(8.dp))
            CommandHubActiveSessionCard()
            Spacer(Modifier.height(16.dp))
            CommandHubCommunityList()
            Spacer(Modifier.height(24.dp))
            CommandHubHeatmapCard()
            Spacer(Modifier.height(24.dp))
            CommandHubNavigationBar()
        }
    }
}

@Composable
private fun CommandHubTopBar(onNavigateToHome: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.tactical_hub), style = MaterialTheme.typography.titleLarge, maxLines = 1)
        },
        navigationIcon = {
            IconButton(onClick = onNavigateToHome) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
            }
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier.height(64.dp),
    )
}

@Composable
private fun CommandHubTabs(activeTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        stringResource(R.string.all_streams),
        stringResource(R.string.podcasts),
        stringResource(R.string.community),
    )
    Tabs(
        selectedTabIndex = activeTab,
        onTabSelected = onTabSelected,
        divider = { },
    ) {
        tabs.forEachIndexed { index, label ->
            Tab(
                selected = index == activeTab,
                onClick = { onTabSelected(index) },
                text = { Text(text = label, style = MaterialTheme.typography.titleSmall) },
            )
        }
    }
}
            onTabSelected = { activeTab = it },
            onNavigateToHome = onNavigateToHome,
        )
    }
}
@Composable
private fun CommandHubSpeedDialHeading() {
    Text(
        text = stringResource(R.string.zone1_speed_dial_pager),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun CommandHubFocusButtons() {
    ConnectedButtonGroup(
        items = listOf(
            MentsicButtonItem(label = stringResource(R.string.electronic_focus), type = MentsicButtonType.Tonal),
            MentsicButtonItem(label = stringResource(R.string.deep_bass), type = MentsicButtonType.Tonal),
        ),
        onButtonClick = { },
    )
}

@Composable
private fun CommandHubActiveSessionHeading() {
    Text(
        text = stringResource(R.string.zone2_active_session),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun CommandHubActiveSessionCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.keep_listening_daft_punk),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
@Composable
private fun CommandHubCommunityList() {
    val items = listOf(
        CommunityItem(
            title = stringResource(R.string.synthwave_2088),
            subtitle = stringResource(R.string.curated_by_neon_rider),
            icon = Icons.Rounded.PlaylistPlay,
            trailing = Icons.Rounded.FavoriteBorder,
        ),
        CommunityItem(
            title = stringResource(R.string.justice_radio),
            subtitle = stringResource(R.string.recommended_based_on_recent),
            icon = Icons.Rounded.Radio,
            trailing = Icons.Rounded.MoreVert,
        ),
        CommunityItem(
            title = stringResource(R.string.forgotten_favorite_get_lucky),
            subtitle = stringResource(R.string.played_82_times),
            icon = Icons.Rounded.History,
            trailing = Icons.Rounded.PlayArrow,
        ),
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
            CommandHubCommunityItem(item = item, onClick = { })
        }
    }
}

@Composable
private fun CommandHubCommunityItem(item: CommunityItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
            Text(text = item.subtitle, style = MaterialTheme.typography.bodyMedium, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(imageVector = item.trailing, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CommandHubHeatmapCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Insights,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.zone4_listening_heatmap),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.weekly_peak_activity),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CommandHubNavigationBar() {
    MentsicNavigationBar(selectedIndex = 0, onDestinationClick = { })
}

private data class CommunityItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.ImageVector,
    val trailing: androidx.compose.ui.graphics.ImageVector,
)
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.random_access_memories_paused),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}