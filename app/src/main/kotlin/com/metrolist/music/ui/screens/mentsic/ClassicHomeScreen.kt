/**
 * Mentsic Music — Classic Home screen (Material 3 Expressive).
 *
 * Production homescreen layout: dynamic greeting header, chips filter row, speed dial
 * pager/actions, quick picks grid, daily discover carousel, and shuffle FAB.
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.graphics.vector.ImageVector
import androidx.compose.material3.Icons
import com.metrolist.music.ui.component.mentsic.ConnectedButtonGroup
import com.metrolist.music.ui.component.mentsic.ConnectedChipGroup
import com.metrolist.music.ui.component.mentsic.MentsicButtonItem
import com.metrolist.music.ui.component.mentsic.MentsicButtonType
import com.metrolist.music.ui.component.mentsic.MentsicChipItem
import com.metrolist.music.ui.component.mentsic.MentsicNavigationBar
import com.metrolist.music.ui.theme.MentsicTheme
import com.metrolist.music.R
import com.metrolist.music.LocalPlayerConnection

@Composable
fun ClassicHomeScreen(
    onNavigateToHub: () -> Unit = {},
    onNavigateToExplore: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    MentsicTheme(darkTheme = androidx.compose.foundation.isSystemInDarkTheme()) {
        val haptics = LocalHapticFeedback.current
        val playerConnection = LocalPlayerConnection.current

        val greeting by remember {
            derivedGreeting()
        }

        ScaffoldContent(
            greeting = greeting,
            haptics = haptics,
            playerConnection = playerConnection,
            onNavigateToHub = onNavigateToHub,
            onNavigateToExplore = onNavigateToExplore,
        )
    }
}

@Composable
private fun ScaffoldContent(
    greeting: String,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    playerConnection: com.metrolist.music.playback.PlayerConnection?,
    onNavigateToHub: () -> Unit,
    onNavigateToExplore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.padding(horizontal = 16.dp),
        ) {
            item {
                ClassicHomeTopBar(
                    greeting = greeting,
                    onAccountClick = onNavigateToHub,
                    onNotificationsClick = { haptics.performHapticFeedback(HapticFeedbackType.Click) },
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                ClassicHomeChips()
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                ClassicHomeSpeedDialHeading()
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                ClassicHomeSpeedDialActions()
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                QuickPicksSongList()
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                ClassicHomeDailyDiscoverRow()
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                ClassicHomeExploreButton()
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                ClassicHomeNavigationBar()
            }
        }
    }
}

private fun derivedGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour in 5 until 12 -> stringResource(R.string.good_morning)
        hour in 12 until 17 -> stringResource(R.string.good_afternoon)
        hour in 17 until 21 -> stringResource(R.string.good_evening)
        else -> stringResource(R.string.good_night)
    }
}

@Composable
@Composable
private fun QuickPicksSongList() {
    val songs = listOf(
        QuickPicksSongItem(id = "starboy", title = stringResource(R.string.starboy), subtitle = stringResource(R.string.the_weeknd) + " • " + stringResource(R.string.daft_punk)),
        QuickPicksSongItem(id = "midnight_city", title = stringResource(R.string.midnight_city), subtitle = stringResource(R.string.m83) + " • " + stringResource(R.string.hurry_up_we_are_dreaming)),
        QuickPicksSongItem(id = "blinding_lights", title = stringResource(R.string.blinding_lights), subtitle = stringResource(R.string.the_weeknd) + " • " + stringResource(R.string.after_hours)),
        QuickPicksSongItem(id = "instant_crush", title = stringResource(R.string.instant_crush), subtitle = stringResource(R.string.daft_punk) + " • " + stringResource(R.string.julian_casablancas)),
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.quick_picks),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ConnectedButtonGroup(
                    items = listOf(
                        MentsicButtonItem(label = stringResource(R.string.quick_picks), type = MentsicButtonType.Filled),
                        MentsicButtonItem(label = stringResource(R.string.play_all), type = MentsicButtonType.Tonal),
                    ),
                    onButtonClick = { },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        QuickPicksSongItems(items = songs)
    }
}

@Composable
private fun QuickPicksSongItems(items: List<QuickPicksSongItem>) {
    Column(modifier = Modifier.height(200.dp)) {
        items.forEach { song ->
            QuickPicksSongItemContent(item = song, onClick = { })
        }
    }
}

@Composable
private fun QuickPicksSongItemContent(item: QuickPicksSongItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.Rounded.MoreVert,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
    }
}
    @Composable
private fun ClassicHomeChips() {
    ConnectedChipGroup(
        items = listOf(
            MentsicChipItem(label = stringResource(R.string.all), selected = true),
            MentsicChipItem(label = stringResource(R.string.energize)),
            MentsicChipItem(label = stringResource(R.string.workout)),
            MentsicChipItem(label = stringResource(R.string.podcasts)),
        ),
        selectedIndex = 0,
        onChipClick = { },
    )
}

@Composable
private fun ClassicHomeSpeedDialHeading() {
    Text(
        text = stringResource(R.string.speed_dial),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun ClassicHomeSpeedDialActions() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            ConnectedButtonGroup(
                items = listOf(
                    MentsicButtonItem(label = stringResource(R.string.daily_mix_1), type = MentsicButtonType.Tonal),
                    MentsicButtonItem(label = stringResource(R.string.liked_songs), type = MentsicButtonType.Tonal),
                ),
                onButtonClick = { },
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            ConnectedButtonGroup(
                items = listOf(
                    MentsicButtonItem(label = stringResource(R.string.rock_classics), type = MentsicButtonType.Tonal),
                    MentsicButtonItem(label = stringResource(R.string.randomize), type = MentsicButtonType.Filled),
                ),
                onButtonClick = { },
            )
        }
    }
}
    @Composable
private fun ClassicHomeTopBar(
    greeting: String,
    onAccountClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = greeting,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
            )
        },
        navigationIcon = {
            IconButton(onClick = onAccountClick) {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = stringResource(R.string.account),
                    modifier = Modifier.size(48.dp),
                )
            }
        },
        actions = {
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = stringResource(R.string.notifications),
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