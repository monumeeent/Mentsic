/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.music.R
import com.metrolist.music.ui.component.mentsic.ConnectedButtonGroup
import com.metrolist.music.ui.component.mentsic.MentsicButtonItem
import com.metrolist.music.ui.component.mentsic.MentsicButtonType
import com.metrolist.music.ui.component.mentsic.MentsicNavigationBar
import com.metrolist.music.ui.theme.MentsicTheme

val MoodAndGenresButtonHeight = 48.dp

@Composable
fun MoodAndGenresButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(MoodAndGenresButtonHeight)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodAndGenresScreen(
    onNavigateToHome: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    MentsicTheme(darkTheme = isSystemInDarkTheme()) {
        ScaffoldContent(
            onNavigateToHome = onNavigateToHome,
            modifier = modifier,
        )
    }
}

@Composable
private fun ScaffoldContent(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxSize()) {
            MoodAndGenresTopBar(onNavigateToHome = onNavigateToHome)
            Spacer(Modifier.height(16.dp))
            MoodAndGenresMomentsHeading()
            Spacer(Modifier.height(8.dp))
            MoodAndGenresMoodGroups()
            Spacer(Modifier.height(16.dp))
            MoodAndGenresPopularHeading()
            Spacer(Modifier.height(8.dp))
            MoodAndGenresGenreGroups()
            Spacer(Modifier.height(24.dp))
            MoodAndGenresNavigationBar()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodAndGenresTopBar(onNavigateToHome: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.mood_and_genres), style = MaterialTheme.typography.titleLarge, maxLines = 1)
        },
        navigationIcon = {
            IconButton(onClick = onNavigateToHome) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
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
private fun MoodAndGenresMomentsHeading() {
    Text(
        text = stringResource(R.string.moods_and_moments),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun MoodAndGenresMoodGroups() {
    Column(modifier = Modifier.fillMaxWidth()) {
        ConnectedButtonGroup(
            items = listOf(
                MentsicButtonItem(label = stringResource(R.string.chill), type = MentsicButtonType.Tonal),
                MentsicButtonItem(label = stringResource(R.string.focus), type = MentsicButtonType.Tonal),
            ),
            onButtonClick = { },
        )
        Spacer(Modifier.height(8.dp))
        ConnectedButtonGroup(
            items = listOf(
                MentsicButtonItem(label = stringResource(R.string.workout), type = MentsicButtonType.Tonal),
                MentsicButtonItem(label = stringResource(R.string.party), type = MentsicButtonType.Tonal),
            ),
            onButtonClick = { },
        )
        Spacer(Modifier.height(8.dp))
        ConnectedButtonGroup(
            items = listOf(
                MentsicButtonItem(label = stringResource(R.string.commute), type = MentsicButtonType.Tonal),
                MentsicButtonItem(label = stringResource(R.string.sleep), type = MentsicButtonType.Tonal),
            ),
            onButtonClick = { },
        )
    }
}

@Composable
private fun MoodAndGenresPopularHeading() {
    Text(
        text = stringResource(R.string.popular_genres),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun MoodAndGenresGenreGroups() {
    Column(modifier = Modifier.fillMaxWidth()) {
        ConnectedButtonGroup(
            items = listOf(
                MentsicButtonItem(label = stringResource(R.string.electronic), type = MentsicButtonType.Filled),
                MentsicButtonItem(label = stringResource(R.string.hip_hop), type = MentsicButtonType.Filled),
            ),
            onButtonClick = { },
        )
        Spacer(Modifier.height(8.dp))
        ConnectedButtonGroup(
            items = listOf(
                MentsicButtonItem(label = stringResource(R.string.rock_alternative), type = MentsicButtonType.Filled),
                MentsicButtonItem(label = stringResource(R.string.pop_hits), type = MentsicButtonType.Filled),
            ),
            onButtonClick = { },
        )
        Spacer(Modifier.height(8.dp))
        ConnectedButtonGroup(
            items = listOf(
                MentsicButtonItem(label = stringResource(R.string.r_b_soul), type = MentsicButtonType.Filled),
                MentsicButtonItem(label = stringResource(R.string.indie_folk), type = MentsicButtonType.Filled),
            ),
            onButtonClick = { },
        )
    }
}

@Composable
private fun MoodAndGenresNavigationBar() {
    MentsicNavigationBar(selectedIndex = 1, onDestinationClick = { })
}

