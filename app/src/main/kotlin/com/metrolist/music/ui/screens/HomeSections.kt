/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens

import com.metrolist.music.ui.screens.MoodAndGenresButtonHeight
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metrolist.innertube.models.YTItem
import com.metrolist.innertube.pages.ExplorePage
import com.metrolist.innertube.pages.HomePage
import com.metrolist.music.R
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.LocalItem
import com.metrolist.music.db.entities.Song
import com.metrolist.music.models.SimilarRecommendation
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.ui.component.MenuState
import com.metrolist.music.ui.component.NavigationTitle
import com.metrolist.music.ui.screens.home.sections.HomeAccountPlaylistsSection
import com.metrolist.music.ui.screens.home.sections.HomeDailyDiscoverSection
import com.metrolist.music.ui.screens.home.sections.HomeForgottenFavoritesSection
import com.metrolist.music.ui.screens.home.sections.HomeFromTheCommunitySection
import com.metrolist.music.ui.screens.home.sections.HomeKeepListeningSection
import com.metrolist.music.ui.screens.home.sections.HomePageSectionItem
import com.metrolist.music.ui.screens.home.sections.HomeQuickPicksSection
import com.metrolist.music.ui.screens.home.sections.HomeSimilarRecommendationSection
import com.metrolist.music.ui.screens.home.sections.HomeSpeedDialSection
import com.metrolist.music.viewmodels.CommunityPlaylistItem
import com.metrolist.music.viewmodels.DailyDiscoverItem
import kotlinx.coroutines.CoroutineScope

/**
 * Section composables facade for HomeScreen.
 *
 * All implementations have been modularised into [com.metrolist.music.ui.screens.home.sections.*]
 * under strict single-responsibility principles. The functions below provide transparent
 * backwards compatibility across the codebase while routing to the new, recomposition-shielded
 * composables.
 */

@Composable
fun SpeedDialSection(
    speedDialItems: List<YTItem>,
    pinnedSpeedDialItems: List<YTItem>,
    pinnedSpeedDialItemTypes: List<String>,
    isRandomizing: Boolean,
    onRandomizeClick: () -> Unit,
    scope: CoroutineScope,
    autoRadioQueue: Boolean,
    isListenTogetherGuest: Boolean,
    mediaMetadataId: String?,
    albumId: String?,
    isPlaying: Boolean,
    playerConnection: PlayerConnection?,
    navController: NavController,
    database: MusicDatabase,
    menuState: MenuState,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    maxWidth: Dp,
) {
    HomeSpeedDialSection(
        speedDialItems = speedDialItems,
        pinnedSpeedDialItems = pinnedSpeedDialItems,
        pinnedSpeedDialItemTypes = pinnedSpeedDialItemTypes,
        isRandomizing = isRandomizing,
        onRandomizeClick = onRandomizeClick,
        scope = scope,
        autoRadioQueue = autoRadioQueue,
        isListenTogetherGuest = isListenTogetherGuest,
        mediaMetadataId = mediaMetadataId,
        albumId = albumId,
        isPlaying = isPlaying,
        playerConnection = playerConnection,
        navController = navController,
        database = database,
        menuState = menuState,
        haptic = haptic,
        maxWidth = maxWidth,
    )
}

@Composable
fun QuickPicksSection(
    quickPicks: List<Song>,
    quickPicksLazyGridState: LazyGridState,
    quickPicksSnapLayoutInfoProvider: SnapLayoutInfoProvider,
    horizontalLazyGridItemWidth: Dp,
    isPlaying: Boolean,
    mediaMetadataId: String?,
    isListenTogetherGuest: Boolean,
    autoRadioQueue: Boolean,
    database: MusicDatabase,
    menuState: MenuState,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    playerConnection: PlayerConnection?,
) {
    HomeQuickPicksSection(
        quickPicks = quickPicks,
        quickPicksLazyGridState = quickPicksLazyGridState,
        quickPicksSnapLayoutInfoProvider = quickPicksSnapLayoutInfoProvider,
        horizontalLazyGridItemWidth = horizontalLazyGridItemWidth,
        isPlaying = isPlaying,
        mediaMetadataId = mediaMetadataId,
        isListenTogetherGuest = isListenTogetherGuest,
        autoRadioQueue = autoRadioQueue,
        database = database,
        menuState = menuState,
        haptic = haptic,
        playerConnection = playerConnection,
    )
}

@Composable
fun FromTheCommunitySection(
    playlists: List<CommunityPlaylistItem>,
    navController: NavController,
    playerConnection: PlayerConnection?,
    isListenTogetherGuest: Boolean,
) {
    HomeFromTheCommunitySection(
        playlists = playlists,
        navController = navController,
        playerConnection = playerConnection,
        isListenTogetherGuest = isListenTogetherGuest,
    )
}

@Composable
fun DailyDiscoverSection(
    discoverList: List<DailyDiscoverItem>,
    playerConnection: PlayerConnection?,
    isListenTogetherGuest: Boolean,
    autoRadioQueue: Boolean,
    modifier: Modifier = Modifier,
) {
    HomeDailyDiscoverSection(
        discoverList = discoverList,
        playerConnection = playerConnection,
        isListenTogetherGuest = isListenTogetherGuest,
        autoRadioQueue = autoRadioQueue,
        modifier = modifier,
    )
}

@Composable
fun KeepListeningSection(
    keepListening: List<Song>,
    localGridItem: @Composable (LocalItem) -> Unit,
    currentGridHeight: Dp,
    modifier: Modifier = Modifier,
) {
    HomeKeepListeningSection(
        keepListening = keepListening,
        localGridItem = localGridItem,
        currentGridHeight = currentGridHeight,
        modifier = modifier,
    )
}

@Composable
fun AccountPlaylistsSection(
    accountPlaylists: List<YTItem>,
    accountName: String,
    accountImageUrl: String?,
    isLoggedIn: Boolean,
    ytGridItem: @Composable (YTItem) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    HomeAccountPlaylistsSection(
        accountPlaylists = accountPlaylists,
        accountName = accountName,
        accountImageUrl = accountImageUrl,
        isLoggedIn = isLoggedIn,
        ytGridItem = ytGridItem,
        navController = navController,
        modifier = modifier,
    )
}

@Composable
fun ForgottenFavoritesSection(
    forgottenFavorites: List<Song>,
    isPlaying: Boolean,
    mediaMetadataId: String?,
    isListenTogetherGuest: Boolean,
    autoRadioQueue: Boolean,
    database: MusicDatabase,
    menuState: MenuState,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    playerConnection: PlayerConnection?,
    forgottenFavoritesLazyGridState: LazyGridState,
    forgottenFavoritesSnapLayoutInfoProvider: SnapLayoutInfoProvider,
    horizontalLazyGridItemWidth: Dp,
    modifier: Modifier = Modifier,
) {
    HomeForgottenFavoritesSection(
        forgottenFavorites = forgottenFavorites,
        isPlaying = isPlaying,
        mediaMetadataId = mediaMetadataId,
        isListenTogetherGuest = isListenTogetherGuest,
        autoRadioQueue = autoRadioQueue,
        database = database,
        menuState = menuState,
        haptic = haptic,
        playerConnection = playerConnection,
        forgottenFavoritesLazyGridState = forgottenFavoritesLazyGridState,
        forgottenFavoritesSnapLayoutInfoProvider = forgottenFavoritesSnapLayoutInfoProvider,
        horizontalLazyGridItemWidth = horizontalLazyGridItemWidth,
        modifier = modifier,
    )
}

@Composable
fun SimilarRecommendationSection(
    sectionIndex: Int,
    recommendation: SimilarRecommendation,
    ytGridItem: @Composable (YTItem) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    HomeSimilarRecommendationSection(
        sectionIndex = sectionIndex,
        recommendation = recommendation,
        ytGridItem = ytGridItem,
        navController = navController,
        modifier = modifier,
    )
}

@Composable
fun HomePageSection(
    sectionIndex: Int,
    sectionData: HomePage.Section?,
    isPodcastChipActive: Boolean,
    ytGridItem: @Composable (YTItem) -> Unit,
    navController: NavController,
    playerConnection: PlayerConnection?,
    isListenTogetherGuest: Boolean,
    mediaMetadataId: String?,
    isPlaying: Boolean,
    autoRadioQueue: Boolean,
    menuState: MenuState,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    database: MusicDatabase,
    scope: CoroutineScope,
    currentGridHeight: Dp,
    horizontalLazyGridItemWidth: Dp,
    modifier: Modifier = Modifier,
) {
    HomePageSectionItem(
        sectionIndex = sectionIndex,
        sectionData = sectionData,
        isPodcastChipActive = isPodcastChipActive,
        ytGridItem = ytGridItem,
        navController = navController,
        playerConnection = playerConnection,
        isListenTogetherGuest = isListenTogetherGuest,
        mediaMetadataId = mediaMetadataId,
        isPlaying = isPlaying,
        autoRadioQueue = autoRadioQueue,
        menuState = menuState,
        haptic = haptic,
        database = database,
        scope = scope,
        currentGridHeight = currentGridHeight,
        horizontalLazyGridItemWidth = horizontalLazyGridItemWidth,
        modifier = modifier,
    )
}

@Composable
fun MoodAndGenresSection(
    explorePage: ExplorePage?,
    isPodcastChipActive: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    if (isPodcastChipActive) return

    explorePage?.moodAndGenres?.let { moodAndGenres ->
        NavigationTitle(
            title = stringResource(R.string.mood_and_genres),
            onClick = {
                navController.navigate("mood_and_genres")
            },
        )

        LazyHorizontalGrid(
            rows = GridCells.Fixed(4),
            contentPadding = PaddingValues(6.dp),
            modifier =
                Modifier
                    .height((MoodAndGenresButtonHeight + 12.dp) * 4 + 12.dp),
        ) {
            items(
                items = moodAndGenres.distinctBy { "${it.title}_${it.endpoint.browseId}_${it.endpoint.params}" },
                key = { "${it.title}_${it.endpoint.browseId}_${it.endpoint.params}" },
            ) {
                MoodAndGenresButton(
                    title = it.title,
                    onClick = {
                        navController.navigate(
                            "youtube_browse/${it.endpoint.browseId}?params=${it.endpoint.params}",
                        )
                    },
                    modifier =
                        Modifier
                            .padding(6.dp)
                            .width(180.dp),
                )
            }
        }
    }
}
