package com.metrolist.music.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.Artist
import com.metrolist.innertube.models.ArtistItem
import com.metrolist.innertube.models.EpisodeItem
import com.metrolist.innertube.models.PlaylistItem
import com.metrolist.innertube.models.PodcastItem
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.innertube.models.YTItem
import com.metrolist.music.R
import com.metrolist.music.LocalNavController
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.db.entities.Album
import com.metrolist.music.db.entities.Song
import com.metrolist.music.extensions.getQueueWindows
import com.metrolist.music.extensions.metadata
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.models.SmartPlaylist
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.playback.queues.ListQueue
import com.metrolist.music.playback.queues.YouTubeQueue
import com.metrolist.music.ui.component.GlassCard
import com.metrolist.music.ui.component.ListeningHeatmap
import com.metrolist.music.ui.component.ListeningHeatmapCard
import com.metrolist.music.ui.component.NavigationTitle
import com.metrolist.music.ui.component.TacticalCollapsibleHeatmapHeader
import com.metrolist.music.ui.component.title
import com.metrolist.music.ui.component.mentsic.ShiningRowCard
import com.metrolist.music.ui.component.shimmer.GridItemPlaceHolder
import com.metrolist.music.ui.component.shimmer.ShimmerHost

import com.metrolist.music.ui.theme.GlassTokens
import com.metrolist.music.ui.theme.IconSizes
import com.metrolist.music.ui.theme.Spacing
import com.metrolist.music.ui.theme.developerOnlyBadgeBackground
import com.metrolist.music.ui.theme.developerOnlyBrush
import com.metrolist.music.utils.StreakState
import com.metrolist.music.utils.StreakTracker
import com.metrolist.music.utils.rememberStreakState
import com.metrolist.music.utils.rememberPreference
import com.metrolist.music.constants.HomeLayoutPresetKey
import com.metrolist.music.constants.DeveloperModeKey
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.metrolist.music.viewmodels.DailyDiscoverItem
import com.metrolist.music.viewmodels.HomeViewModel
import com.metrolist.music.viewmodels.SmartPlaylistViewModel
import java.time.LocalDate
import java.util.Calendar

/** Shuffle sampler geometry: 3 x 3 = 9 cells by default; the grid is full width. */
private const val SHUFFLE_COLUMNS = 3
private const val SHUFFLE_ROWS = 3
private const val SHUFFLE_CELLS = SHUFFLE_COLUMNS * SHUFFLE_ROWS



/** YouTube Chart geometry: 4 columns x 2 rows = 8 tiles. */
private const val TRENDING_COLUMNS = 4
private const val TRENDING_ROWS = 2
private const val TRENDING_CELLS = TRENDING_COLUMNS * TRENDING_ROWS

/** Speed Dial geometry: 4 columns x 6 rows = 24 artwork-only tiles. */
private const val SPEED_DIAL_COLUMNS = 4
private const val SPEED_DIAL_ROWS = 6
private const val SPEED_DIAL_CELLS = SPEED_DIAL_COLUMNS * SPEED_DIAL_ROWS

/**
 * Trailing inset on horizontal rows so the next card peeks in. Sized to show ~15% of a card:
 * enough to read the edge of the artwork, not enough to imply a second visible item.
 */
private val PEEK_PADDING = 72.dp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keepListening by viewModel.keepListening.collectAsStateWithLifecycle()
    val quickPicks by viewModel.quickPicks.collectAsStateWithLifecycle()
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsStateWithLifecycle()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val refreshState = rememberPullToRefreshState()
    val selectedChip by viewModel.selectedChip.collectAsStateWithLifecycle()
    val homePage by viewModel.homePage.collectAsStateWithLifecycle()
    val shuffles by viewModel.shuffles.collectAsStateWithLifecycle()
    val topListened by viewModel.topListened.collectAsStateWithLifecycle()
    val remixes by viewModel.remixes.collectAsStateWithLifecycle()
    val todayMinutes by viewModel.todayMinutes.collectAsStateWithLifecycle()
    val trending by viewModel.trending.collectAsStateWithLifecycle()
    val heatmapPlays by viewModel.heatmapPlays.collectAsStateWithLifecycle()

    // Home layout preset. Filters which sections render; the feed itself stays one implementation.
    val (homePresetKey) = rememberPreference(HomeLayoutPresetKey, HomeLayoutPreset.Default.key)
    val homePreset = remember(homePresetKey) { HomeLayoutPreset.fromKey(homePresetKey) }

    // Fine-grained section switches, layered on top of the preset (both must allow a section).
    val (showTrending) = rememberPreference(HomeShowTrendingKey, true)
    val (showSpeedDialFlag) = rememberPreference(HomeShowSpeedDialKey, true)
    val (showShufflesFlag) = rememberPreference(HomeShowShufflesKey, true)
    val (showPersonalSpaceFlag) = rememberPreference(HomeShowPersonalSpaceKey, true)
    val (showTopListenedFlag) = rememberPreference(HomeShowTopListenedKey, true)
    val (showDiscoveryFlag) = rememberPreference(HomeShowDiscoveryKey, true)

    // Developer mode gates the Command Hub layout and the shining developer rows.
    val (developerMode) = rememberPreference(DeveloperModeKey, defaultValue = false)

    // Command Hub layout (same zone structure as the standalone hub screen) with the heatmap
    // starting collapsed. Both switches live in Settings → Tactical Hub.
    val (hubModeEnabled) = rememberPreference(HubModeEnabledKey, defaultValue = false)
    val (hubHeatmapCollapsedFlag, onHubHeatmapCollapsedFlagChange) =
        rememberPreference(HubHeatmapCollapsedKey, defaultValue = true)

    // Developer-mode grid geometry. Clamped so a bad value cannot break the layout.
    val (sdColumns) = rememberPreference(SpeedDialColumnsKey, SPEED_DIAL_COLUMNS)
    val (sdRows) = rememberPreference(SpeedDialRowsKey, SPEED_DIAL_ROWS)
    val (shuffleCols) = rememberPreference(ShuffleColumnsKey, SHUFFLE_COLUMNS)
    val (shuffleRows) = rememberPreference(ShuffleRowsKey, SHUFFLE_ROWS)

    // Auto-generated mixes (Release Radar / Weekly Mix). The generator caches for a week, so this
    // first load is not repeated when the user switches tabs.
    val smartPlaylistViewModel: SmartPlaylistViewModel = hiltViewModel()
    val smartPlaylists by smartPlaylistViewModel.playlists.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { smartPlaylistViewModel.refresh() }

    // Remixes = upcoming queue tracks + a random unlisted sample. Recomputed when the queue
    // length or the current track changes, so the row always reflects what is playing.
    LaunchedEffect(mediaMetadata?.id, playerConnection.player.mediaItemCount) {
        val upcoming =
            playerConnection.player.getQueueWindows()
                .drop(1)
                .mapNotNull { window -> window.mediaItem.metadata }
                .map { metadata ->
                    SongItem(
                        id = metadata.id,
                        title = metadata.title,
                        // MediaMetadata.Artist and innertube Artist are different types.
                        artists = metadata.artists.map { Artist(name = it.name, id = it.id) },
                        thumbnail = metadata.thumbnailUrl ?: "",
                    )
                }
        viewModel.updateRemixes(upcoming)
    }

    LaunchedEffect(Unit) { viewModel.loadHomeData() }

    // Queue tracks lead the shuffle grid, and the grid re-rolls whenever the queue changes so
    // it always samples what is playing right now alongside the rest of the library.
    val queueSnapshot = remember(mediaMetadata?.id, playerConnection.player.mediaItemCount) {
        playerConnection.player.getQueueWindows()
            .drop(1)
            .mapNotNull { window -> window.mediaItem.metadata }
            .map { metadata ->
                SongItem(
                    id = metadata.id,
                    title = metadata.title,
                    artists = metadata.artists.map { Artist(name = it.name, id = it.id) },
                    thumbnail = metadata.thumbnailUrl.orEmpty(),
                )
            }
    }

    LaunchedEffect(queueSnapshot) {
        viewModel.updateShuffles(queueSnapshot)
    }

    // "Today · N min" must advance continuously while a track is running. The view model keeps a
    // wall-clock session offset so the value grows every second; we only have to tell it when
    // playback starts and stops, and re-sync against the database periodically (the DB row lands
    // later than real playback).
    LaunchedEffect(Unit) { viewModel.startTodayMinutesTicker() }
    LaunchedEffect(isPlaying) { viewModel.setPlaybackRunning(isPlaying) }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(60_000L)
            viewModel.tickTodayMinutes()
        }
    }

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    // Same source as the Personal Space card, so the header chip never disagrees with it.
    val streakState = rememberStreakState()
    val today = remember { LocalDate.now() }
    val activeSong = remember(keepListening, quickPicks) {
        keepListening.orEmpty().firstNotNullOfOrNull { resume ->
            when (resume) {
                is Song -> ResumeTarget.SongTarget(resume)
                is Album -> ResumeTarget.AlbumTarget(resume)
                else -> null
            }
        } ?: quickPicks.orEmpty().firstOrNull()?.let(ResumeTarget::SongTarget)
    }
    val activeKeepListening = remember(keepListening) { keepListening.orEmpty().filterIsInstance<Song>() }
    // Focus mode is driven purely by the ViewModel chip, because only real InnerTube chips
    // produce filtered content.
    val isFocused = selectedChip != null
    BackHandler(enabled = isFocused) { viewModel.toggleChip(null) }

    // No infinite-scroll paging here by design: HomeViewModel.loadMoreYouTubeItems appends the
    // next InnerTube page onto the existing sections, which repeats shelf titles that InnerTube
    // already returned (e.g. "Mixed for you"), so the feed visibly duplicated itself.

    PullToRefreshBox(
        state = refreshState,
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = refreshState,
                isRefreshing = uiState.isRefreshing,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues()),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // MainActivity already supplies a global top app bar and player-aware bottom insets,
            // so the feed consumes them directly instead of adding its own status-bar padding.
            val feedInsets = LocalPlayerAwareWindowInsets.current.asPaddingValues()
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = feedInsets.calculateTopPadding() + Spacing.small,
                    bottom = feedInsets.calculateBottomPadding() + Spacing.large,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
                modifier = Modifier.fillMaxSize(),
            ) {
                // MainActivity's top app bar already shows the route title and account action, so
                // the feed starts with content instead of drawing a second greeting header.
                item(key = "new_home_greeting") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            DeveloperCreditRow()
                        }
                        StreakChip(
                            days = StreakTracker.currentForDisplay(streakState, today),
                            onClick = { navController.navigate("account") },
                        )
                    }
                }

                item(key = "new_home_filters") {
                    // InnerTube chips are the only filter the ViewModel can actually fetch, so the
                    // row is built from live chips with an always-available "All" reset.
                    val chipTitles = uiState.homePage?.chips?.map { it.title }.orEmpty()
                    HomeFilterTabs(
                        filters = listOf("All") + chipTitles,
                        selected = selectedChip?.title ?: "All",
                        onSelected = { title ->
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            if (title == "All") {
                                if (selectedChip != null) viewModel.toggleChip(null)
                            } else {
                                uiState.homePage?.chips
                                    ?.firstOrNull { it.title == title }
                                    ?.let(viewModel::toggleChip)
                            }
                        },
                    )
                }

                // Command Hub layout: the same zones as the standalone hub screen (speed dial,
                // active session, community picks, collapsible heatmap) rendered inside the
                // normal feed, so it keeps pull-to-refresh and the live ViewModel data.
                if (hubModeEnabled) {
                    item(key = "hub_zone_speed_dial") {
                        DevSectionHeader(label = stringResource(R.string.zone1_speed_dial_pager))
                        HubZoneSpeedDial(
                            items = uiState.speedDialItems,
                            onItemClick = { item ->
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                playYouTubeItem(item, playerConnection, mediaMetadata?.id) { route ->
                                    navController.navigate(route)
                                }
                            },
                        )
                    }

                    item(key = "hub_zone_active_session") {
                        DevSectionHeader(label = stringResource(R.string.zone2_active_session))
                        HubZoneActiveSession(
                            target = activeSong,
                            isPlaying = isPlaying && activeSong?.id == mediaMetadata?.id,
                            onResume = {
                                activeSong?.let { target ->
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    if (target.id == mediaMetadata?.id) {
                                        playerConnection.togglePlayPause()
                                    } else {
                                        when (target) {
                                            is ResumeTarget.SongTarget ->
                                                playerConnection.playQueue(ListQueue(target.song.title, listOf(target.song.toMediaItem())))

                                            is ResumeTarget.AlbumTarget ->
                                                navController.navigate("album/${target.album.id}")
                                        }
                                    }
                                } ?: navController.navigate("account")
                            },
                        )
                    }

                    item(key = "hub_zone_community") {
                        HomeSectionHeaderV2(title = stringResource(R.string.zone3_community_picks))
                        HubZoneCommunity(
                            items = uiState.accountPlaylists.orEmpty() +
                                uiState.dailyDiscover.orEmpty().map<DailyDiscoverItem, YTItem> { it.recommendation },
                            onItemClick = { item ->
                                playYouTubeItem(item, playerConnection, mediaMetadata?.id) { route ->
                                    navController.navigate(route)
                                }
                            },
                        )
                    }

                    item(key = "hub_zone_heatmap_header") {
                        TacticalCollapsibleHeatmapHeader(
                            isCollapsed = hubHeatmapCollapsedFlag,
                            onToggleCollapse = {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onHubHeatmapCollapsedFlagChange(!hubHeatmapCollapsedFlag)
                            },
                        )
                    }

                    if (!hubHeatmapCollapsedFlag) {
                        item(key = "hub_zone_heatmap") {
                            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.medium)) {
                                ListeningHeatmapCard(playsByDay = heatmapPlays)
                            }
                        }
                    }

                    if (developerMode) {
                        item(key = "hub_zone_dev_shining") {
                            DevShiningRows(
                                picks = quickPicks.orEmpty().take(4),
                                activeId = mediaMetadata?.id,
                                onSongClick = { playSong(it, playerConnection, mediaMetadata?.id) },
                            )
                        }
                    }
                }

                if (!isFocused && !hubModeEnabled) {
                    // Personal Space slides: streak, continue listening, top artists.
                    // Gated by the Home section switch (Settings → Home sections).
                    if (homePreset.showsPersonalSpace && showPersonalSpaceFlag) {
                        item(key = "new_home_personal_space") {
                            PersonalSpacePager(
                                streakState = streakState,
                                today = today,
                                target = activeSong,
                                isPlaying = isPlaying && activeSong?.id == mediaMetadata?.id,
                                topArtists = topListened.map { it.orderedArtists.firstOrNull()?.name.orEmpty() }
                                    .filter { it.isNotBlank() }
                                    .distinct()
                                    .take(3),
                                todayMinutes = todayMinutes,
                                playsByDay = heatmapPlays,
                                onResume = {
                                    activeSong?.let { target ->
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        if (target.id == mediaMetadata?.id) {
                                            playerConnection.togglePlayPause()
                                        } else {
                                            when (target) {
                                                is ResumeTarget.SongTarget ->
                                                    playerConnection.playQueue(ListQueue(target.song.title, listOf(target.song.toMediaItem())))

                                                is ResumeTarget.AlbumTarget ->
                                                    navController.navigate("album/${target.album.id}")
                                            }
                                        }
                                    } ?: navController.navigate("account")
                                },
                                onOpenPersonalSpace = { navController.navigate("account") },
                                onOpenListeningActivity = {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                },
                                smartPlaylists = smartPlaylists,
                                onOpenSmartPlaylist = { navController.navigate("smart_playlist/${it.id}") },
                                onPlaySmartPlaylist = { playlist ->
                                    playlist.songs.firstOrNull()?.let { first ->
                                        playerConnection.playQueue(
                                            YouTubeQueue(
                                                endpoint = WatchEndpoint(videoId = first.id),
                                                preloadItem = first.toMediaMetadata(),
                                            ),
                                        )
                                    }
                                },
                            )
                        }
                    }

                    // Developer-only surface: full-width shining rows. This section exists only
                    // while Developer Mode is on, which is why its cards can animate forever
                    // without costing regular users any frames.
                    if (developerMode) {
                        item(key = "new_home_dev_shining") {
                            DevShiningRows(
                                picks = quickPicks.orEmpty().take(4),
                                activeId = mediaMetadata?.id,
                                onSongClick = { playSong(it, playerConnection, mediaMetadata?.id) },
                            )
                        }
                    }

                    // "YouTube Chart": region-localised trending. 2 rows x 4 columns, placed
                    // above Speed Dial because it is the one section that changes day to day.
                    if (!hubModeEnabled && homePreset.showsTrending && showTrending && trending.isNotEmpty()) {
                        item(key = "new_home_trending") {
                            HomeSectionHeaderV2(title = stringResource(R.string.trending_chart))
                            TrendingChartGrid(
                                songs = trending,
                                activeId = mediaMetadata?.id,
                                isPlaying = isPlaying,
                                onSongClick = { item ->
                                    playYouTubeItem(item, playerConnection, mediaMetadata?.id) { route ->
                                        navController.navigate(route)
                                    }
                                },
                            )
                        }
                    }

                    if (!hubModeEnabled && homePreset.showsSpeedDial && showSpeedDialFlag && uiState.speedDialItems.isNotEmpty()) {
                        item(key = "new_home_speed_dial") {
                            HomeSectionHeaderV2(title = stringResource(R.string.speed_dial))
                            HomeSpeedDialGrid(
                                columns = sdColumns.coerceIn(2, 6),
                                rows = sdRows.coerceIn(1, 10),
                                items = uiState.speedDialItems,
                                activeId = mediaMetadata?.id,
                                isPlaying = isPlaying,
                                onItemClick = { item ->
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    // Speed Dial is a 1-tap shuffle surface: a song starts its radio
                                    // immediately; albums/playlists/artists route to their page
                                    // because only songs carry a playable endpoint here.
                                    playYouTubeItem(item, playerConnection, mediaMetadata?.id) { route ->
                                        navController.navigate(route)
                                    }
                                },
                            )
                        }
                    }



                    // 3x3 thumbnail-only shuffle grid, re-rolled on every refresh. The header and
                    // an empty 9-cell grid stay on screen while the library/history query is still
                    // running, so the section never silently disappears from the feed.
                    if (!hubModeEnabled && homePreset.showsShuffles && showShufflesFlag) {
                        item(key = "new_home_shuffles") {
                            HomeSectionHeaderV2(title = stringResource(R.string.shuffles))
                            ShuffleGrid(
                                columns = shuffleCols.coerceIn(2, 6),
                                rows = shuffleRows.coerceIn(1, 8),
                                songs = shuffles,
                                activeId = mediaMetadata?.id,
                                isPlaying = isPlaying,
                                onSongClick = { item ->
                                    playYouTubeItem(item, playerConnection, mediaMetadata?.id) { route ->
                                        navController.navigate(route)
                                    }
                                },
                            )
                        }
                    }

                    // Top listened: always 4 cells, padded with placeholders when short.
                    if (!hubModeEnabled && homePreset.showsTopListened && showTopListenedFlag && topListened.isNotEmpty()) {
                        item(key = "new_home_top_listened") {
                            HomeSectionHeaderV2(title = stringResource(R.string.top_listened))
                            TopListenedRow(
                                songs = topListened,
                                activeId = mediaMetadata?.id,
                                isPlaying = isPlaying,
                                onSongClick = { playSong(it, playerConnection, mediaMetadata?.id) },
                            )
                        }
                    }
                }

                // Curated bands only render in the unfiltered feed; a selected chip replaces them
                // with the filtered InnerTube sections at the bottom of this block. The discovery
                // bands are also dropped by the Compact preset, which is deliberately minimal.
                if (!isFocused && !hubModeEnabled) {
                    if (homePreset.showsDiscovery && showDiscoveryFlag) {
                    quickPicks?.takeIf { it.isNotEmpty() }?.let { songs ->
                        item(key = "new_home_quick_picks") {
                            HomeSectionHeaderV2(title = stringResource(R.string.quick_picks))
                            HomeSongRow(
                                songs = songs,
                                activeId = mediaMetadata?.id,
                                isPlaying = isPlaying,
                                onSongClick = { playSong(it, playerConnection, mediaMetadata?.id) },
                            )
                        }
                    }

                    uiState.dailyDiscover?.takeIf { it.isNotEmpty() }?.let { discover ->
                        item(key = "new_home_daily_discover") {
                            HomeSectionHeaderV2(title = stringResource(R.string.your_daily_discover))
                            HomeYouTubeRow(
                                items = discover.map { it.recommendation },
                                onItemClick = {
                                    playYouTubeItem(it, playerConnection, mediaMetadata?.id) { route -> navController.navigate(route) }
                                },
                            )
                        }
                    }

                    activeKeepListening.takeIf { it.isNotEmpty() }?.let { songs ->
                        item(key = "new_home_keep_listening") {
                            HomeSectionHeaderV2(title = stringResource(R.string.keep_listening))
                            HomeSongRow(
                                songs = songs,
                                activeId = mediaMetadata?.id,
                                isPlaying = isPlaying,
                                onSongClick = { playSong(it, playerConnection, mediaMetadata?.id) },
                            )
                        }
                    }

                    uiState.forgottenFavorites?.takeIf { it.isNotEmpty() }?.let { songs ->
                        item(key = "new_home_forgotten") {
                            HomeSectionHeaderV2(title = stringResource(R.string.forgotten_favorites))
                            HomeSongRow(
                                songs = songs,
                                activeId = mediaMetadata?.id,
                                isPlaying = isPlaying,
                                onSongClick = { playSong(it, playerConnection, mediaMetadata?.id) },
                            )
                        }
                    }

                    uiState.accountPlaylists?.takeIf { it.isNotEmpty() }?.let { playlists ->
                        item(key = "new_home_playlists") {
                            HomeSectionHeaderV2(title = stringResource(R.string.your_youtube_playlists))
                            HomeYouTubeRow(
                                items = playlists,
                                onItemClick = { item ->
                                    if (item is PlaylistItem) navController.navigate("online_playlist/${item.id}")
                                },
                            )
                        }
                    }

                    // Remixes: upcoming queue + random unlisted tracks. Filtered against the
                    // songs already shown above so the feed does not repeat itself.
                    val shownIds = buildSet {
                        addAll(shuffles.map { it.id })
                        addAll(topListened.map { it.id })
                        addAll(quickPicks.orEmpty().map { it.id })
                        addAll(activeKeepListening.map { it.id })
                    }
                    remixes.filterNot { it.id in shownIds }.takeIf { it.isNotEmpty() }?.let { items ->
                        item(key = "new_home_remixes") {
                            HomeSectionHeaderV2(title = stringResource(R.string.remixes))
                            HomeYouTubeRow(
                                items = items,
                                onItemClick = {
                                    playYouTubeItem(it, playerConnection, mediaMetadata?.id) { route -> navController.navigate(route) }
                                },
                            )
                        }
                    }
                    }
                }

                if (uiState.isLoading && uiState.isEmpty) {
                    item(key = "new_home_loading") { HomeLoadingState() }
                }

                // InnerTube sections (personalised feed and chip results).
                // Rendered once per distinct section: the ViewModel can hold repeated shelf
                // titles, and rendering each one produced duplicate rows on screen.
                val renderedSections = homePage?.sections
                    ?.filter { it.items.isNotEmpty() }
                    ?.distinctBy { it.title }
                    .orEmpty()
                renderedSections.forEachIndexed { index, section ->
                    if (hubModeEnabled) return@forEachIndexed
                    item(key = "new_home_section_$index") {
                        HomeSectionHeaderV2(title = section.title)
                        HomeYouTubeRow(
                            items = section.items,
                            onItemClick = { item ->
                                playYouTubeItem(item, playerConnection, mediaMetadata?.id) { route -> navController.navigate(route) }
                            },
                        )
                    }
                }
            }

            if (!uiState.isLoading && uiState.speedDialItems.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        uiState.speedDialItems.randomOrNull()
                            ?.let { item ->
                                playYouTubeItem(item, playerConnection, mediaMetadata?.id) { route -> navController.navigate(route) }
                            }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(Spacing.large)
                        .padding(bottom = feedInsets.calculateBottomPadding()),
                ) {
                    // Uses the app's own vector drawable: material-icons-extended does not ship
                    // Icons.Default.Shuffle, and the rest of the app draws R.drawable.shuffle too.
                    Icon(painterResource(R.drawable.shuffle), contentDescription = stringResource(R.string.shuffle))
                }
            }
        }
    }
}



@Composable
private fun DeveloperCreditRow() {
    val uriHandler = LocalUriHandler.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { uriHandler.openUri("https://instagram.com/monumeeent") }
            .padding(vertical = Spacing.tiny),
    ) {
        Icon(
            painter = painterResource(R.drawable.instagram),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(IconSizes.Small),
        )
        Spacer(Modifier.width(Spacing.small))
        Text(
            text = "@monumeeent",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/** Compact streak readout shown on the trailing edge of the Home header. */
@Composable
private fun StreakChip(days: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.tiny),
            modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.tiny),
        ) {
            Icon(
                painter = painterResource(R.drawable.flame),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(IconSizes.Small),
            )
            Text(
                text = days.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Home filter strip. Uses a [ScrollableTabRow] rather than chips so the strip reads as navigation
 * (it swaps the feed's contents) rather than as optional filters, and matches the Command Hub.
 * Chips also wrapped badly once InnerTube returned six or more genre chips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeFilterTabs(filters: List<String>, selected: String, onSelected: (String) -> Unit) {
    val selectedIndex = filters.indexOf(selected).coerceAtLeast(0)
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = Spacing.medium,
        divider = { },
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
    ) {
        filters.forEachIndexed { index, filter ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onSelected(filter) },
                text = {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

/** Only the two keep-listening shapes the hero can render: a song or an album. */
private sealed interface ResumeTarget {
    val id: String
    val title: String
    val subtitle: String
    val artwork: String?

    data class SongTarget(val song: Song) : ResumeTarget {
        override val id = song.id
        override val title = song.title
        override val subtitle = song.orderedArtists.joinToString { it.name }
        override val artwork = song.thumbnailUrl
    }

    data class AlbumTarget(val album: Album) : ResumeTarget {
        override val id = album.id
        override val title = album.title
        override val subtitle = album.artists.joinToString { it.name }
        override val artwork = album.thumbnailUrl
    }
}

/**
 * Personal Space slides: a pager over streak, continue listening, top artists, the optional
 * Made-for-you page and the listening-activity heatmap. Each page reuses the same card chrome so
 * the strip reads as one unit.
 */
@Composable
private fun PersonalSpacePager(
    streakState: StreakState,
    today: LocalDate,
    target: ResumeTarget?,
    isPlaying: Boolean,
    topArtists: List<String>,
    todayMinutes: Long,
    playsByDay: Map<String, Int>,
    smartPlaylists: List<SmartPlaylist>,
    onResume: () -> Unit,
    onOpenPersonalSpace: () -> Unit,
    onOpenListeningActivity: () -> Unit,
    onOpenSmartPlaylist: (SmartPlaylist) -> Unit,
    onPlaySmartPlaylist: (SmartPlaylist) -> Unit,
) {
    // Page list: 0 streak, 1 continue listening, 2 top artists, 3 (conditional) Made for you,
    // then the heatmap last so the pager always ends on it.
    val hasMadeForYou = smartPlaylists.isNotEmpty()
    val pageCount = if (hasMadeForYou) 5 else 4
    // The listening-activity card is always the last page; derived rather than hard-coded so the
    // shortcut keeps working once Made for you appears.
    val listeningPage = pageCount - 1
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    // While the listening-activity page is on screen the header action must open that card, not
    // bounce back to the account screen (which lands on the streak card).
    val onListeningPage = pagerState.currentPage == listeningPage
    val headerAction =
        if (onListeningPage) onOpenListeningActivity else onOpenPersonalSpace
    val headerActionLabel =
        if (onListeningPage) R.string.listening_activity else R.string.streak_view_details

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.medium),
        ) {
            Text(
                text = stringResource(R.string.personal_space),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            // Page dots live in the header so the card itself can be shorter.
            Row(
                modifier = Modifier.padding(start = Spacing.small).weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.tiny),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pageCount) { index ->
                    Box(
                        modifier =
                            Modifier
                                .size(if (pagerState.currentPage == index) 7.dp else 5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHighest
                                    },
                                ),
                    )
                }
            }
            TextButton(
                onClick = {
                    if (!onListeningPage) scope.launch { pagerState.animateScrollToPage(listeningPage) }
                    headerAction()
                },
            ) {
                Text(
                    text = stringResource(headerActionLabel),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = Spacing.medium),
            pageSpacing = Spacing.small,
            // 116dp fits the streak / continue-listening / top-artist pages. The heatmap page needs
            // more vertical room, so the pager as a whole is sized for its tallest page and the
            // short pages centre their content inside the space.
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp),
        ) { page ->
            when (page) {
                0 -> PersonalSpaceSlide(respond = { onOpenPersonalSpace() }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.flame),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(IconSizes.Medium),
                        )
                        Spacer(Modifier.width(Spacing.small))
                        Column {
                            Text(
                                text = StreakTracker.currentForDisplay(streakState, today).toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(R.string.streak_days) + " \u00b7 " +
                                    stringResource(R.string.streak_longest) + " " + streakState.longest,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            // Today's real listening time, not just a play count.
                            Text(
                                text = stringResource(R.string.today_minutes, todayMinutes),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = Spacing.tiny),
                            )
                        }
                    }
                }

                1 -> PersonalSpaceSlide(respond = onResume) {
                    if (target != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = target.artwork,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(Spacing.small)),
                            )
                            Spacer(Modifier.width(Spacing.medium))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.continue_listening),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = target.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = target.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Surface(
                                onClick = onResume,
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(44.dp),
                            ) {
                                Icon(
                                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                                    contentDescription = null,
                                )
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = stringResource(R.string.continue_listening),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Start something new",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Pick a track to begin your personal space.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                2 -> PersonalSpaceSlide(respond = { onOpenPersonalSpace() }) {
                    Column {
                        Text(
                            text = stringResource(R.string.top_artists),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(Spacing.small))
                        if (topArtists.isEmpty()) {
                            Text(
                                text = "Listen more to see your top artists.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            topArtists.forEach { name ->
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                // Made-for-you is only reachable when it exists; otherwise the pager has four
                // pages and the heatmap occupies this slot (see the `else` branch).
                3 -> if (hasMadeForYou) {
                    PersonalSpaceSlide(respond = { smartPlaylists.firstOrNull()?.let(onPlaySmartPlaylist) }) {
                    Column {
                        Text(
                            text = stringResource(R.string.smart_playlists),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(Spacing.small))
                        smartPlaylists.forEach { playlist ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable { onOpenSmartPlaylist(playlist) }
                                        .padding(vertical = Spacing.tiny),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AsyncImage(
                                    model = playlist.thumbnailUrls.firstOrNull(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(32.dp).clip(MaterialTheme.shapes.small),
                                )
                                Text(
                                    text = playlist.type.title(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = Spacing.small).weight(1f),
                                )
                                Icon(
                                    painter = painterResource(R.drawable.play),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                    }
                } else {
                    ListeningActivitySlide(
                        playsByDay = playsByDay,
                        onOpen = onOpenListeningActivity,
                    )
                }

                else -> ListeningActivitySlide(
                    playsByDay = playsByDay,
                    onOpen = onOpenListeningActivity,
                )
            }
        }

    }
}

/**
 * Listening-activity card: a compact heatmap with a weekly / monthly window switch.
 *
 * Deliberately shows only the activity windows — no "today's minutes" readout. That number lives
 * on the streak page and, duplicated here, competed with the heatmap for the little vertical
 * space the card has.
 *
 * Clicking the card body opens the listening-activity destination; the period chips handle their
 * own clicks and do not bubble up.
 */
@Composable
private fun ListeningActivitySlide(
    playsByDay: Map<String, Int>,
    onOpen: () -> Unit,
) {
    // Two weekly columns = "this week + last week" (a glance), four monthly columns = the last
    // month. Wider windows make each cell too small to read inside the Personal Space card.
    var weekly by rememberSaveable { mutableStateOf(true) }
    val weeksToShow = if (weekly) 2 else 4

    PersonalSpaceSlide(respond = onOpen) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.heatmap_short_title),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                // Two compact period chips. They stop the touch from reaching the card's
                // click target so switching the window never navigates away.
                PeriodChip(
                    label = stringResource(R.string.heatmap_period_weekly),
                    selected = weekly,
                    onClick = { weekly = true },
                )
                Spacer(Modifier.width(Spacing.tiny))
                PeriodChip(
                    label = stringResource(R.string.heatmap_period_monthly),
                    selected = !weekly,
                    onClick = { weekly = false },
                )
            }
            Spacer(Modifier.height(Spacing.small))
            Box(
                modifier = Modifier.pointerInput(Unit) {
                    // Swallow drags so the heatmap can be inspected without paging the carousel.
                    detectHorizontalDragGestures { _, _ -> }
                },
            ) {
                ListeningHeatmap(
                    playsByDay = playsByDay,
                    weeksToShow = weeksToShow,
                    // The card already has a glass tint; a firmer empty tone keeps idle days
                    // visible instead of reading as a missing grid.
                    emptyDayAlpha = 1f,
                )
            }
        }
    }
}

/** Small pill used for the weekly / monthly window switch on the heatmap card. */
@Composable
private fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = Spacing.small, vertical = 2.dp),
        )
    }
}

/** Shared card chrome for a Personal Space page. */
@Composable
private fun PersonalSpaceSlide(respond: () -> Unit, content: @Composable () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.large).clickable { respond() },
        shape = MaterialTheme.shapes.large,
        style = GlassTokens.Elevated,
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(Spacing.large),
            contentAlignment = Alignment.CenterStart,
        ) {
            content()
        }
    }
}

@Composable
private fun HomeSectionHeaderV2(title: String) {
    // Reuses the shared home header so typography and the optional Play-all capsule stay in one
    // place; `NavigationTitle` already applies the horizontal system-bar insets.
    NavigationTitle(title = title)
}

/**
 * Command Hub zone 1: dense artwork grid (same tile shape as the dev Speed Dial grid, but with a
 * fixed 3-column layout so the hub stays visually distinct from the regular feed).
 */
@Composable
private fun HubZoneSpeedDial(items: List<YTItem>, onItemClick: (YTItem) -> Unit) {
    HomeSpeedDialGrid(
        columns = 3,
        rows = 3,
        items = items,
        activeId = null,
        isPlaying = false,
        onItemClick = onItemClick,
    )
}

/**
 * Command Hub zone 2: the single "active session" card — the track you would continue with.
 *
 * Kept to one card (rather than a carousel) because the hub is a glanceable summary: one tap
 * resumes, and everything else is one level down.
 */
@Composable
private fun HubZoneActiveSession(
    target: ResumeTarget?,
    isPlaying: Boolean,
    onResume: () -> Unit,
) {
    Surface(
        onClick = onResume,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium)
            .height(96.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                if (target?.artwork != null) {
                    AsyncImage(
                        model = target.artwork,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Spacer(Modifier.width(Spacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.keep_listening),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = target?.title ?: stringResource(R.string.zone2_active_session),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = target?.subtitle.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(Spacing.small))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(44.dp),
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = stringResource(R.string.play_pause),
                    modifier = Modifier.padding(Spacing.small),
                )
            }
        }
    }
}

/**
 * Command Hub zone 3: community picks and account playlists as full-width rows.
 *
 * These are ordinary user data (your playlists / daily-discover picks), not developer-only
 * surfaces, so the rows deliberately opt out of the DEV wash.
 */
@Composable
private fun HubZoneCommunity(items: List<YTItem>, onItemClick: (YTItem) -> Unit) {
    val rows = items.distinctBy { it.id }.take(5)
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        if (rows.isEmpty()) {
            ShiningRowCard(
                title = stringResource(R.string.zone3_community_picks),
                subtitle = stringResource(R.string.dev_shining_rows_loading_desc),
                leadingIconRes = R.drawable.playlist_play,
                isDeveloperOnly = false,
            )
        } else {
            rows.forEach { item ->
                ShiningRowCard(
                    title = item.title,
                    subtitle = when (item) {
                        is PlaylistItem -> stringResource(R.string.curated_by_neon_rider)
                        is AlbumItem -> stringResource(R.string.filter_albums)
                        is ArtistItem -> stringResource(R.string.filter_artists)
                        else -> stringResource(R.string.zone3_community_picks)
                    },
                    artworkUrl = item.thumbnail,
                    leadingIconRes = R.drawable.playlist_play,
                    trailingIconRes = R.drawable.play,
                    isDeveloperOnly = false,
                    onClick = { onItemClick(item) },
                )
            }
        }
    }
}

/**
 * Section header for developer-only content.
 *
 * Same rhythm as [HomeSectionHeaderV2] but with the shared dev gradient behind a "DEV" pill, so
 * the whole block reads as one dev-only surface rather than as a normal feed section.
 */
@Composable
private fun DevSectionHeader(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(developerOnlyBrush())
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = developerOnlyBadgeBackground(),
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ) {
            Text(
                text = stringResource(R.string.dev_badge),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = Spacing.small, vertical = 2.dp),
            )
        }
        Spacer(Modifier.width(Spacing.small))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Developer-only "Shining" list: one full-width animated card per item.
 *
 * Mounted only when Developer Mode is on (see the caller), so the continuous light sweep never
 * runs for regular users. The section is intentionally not part of any preset and is not
 * affected by the per-section switches — it is a dev surface, not a feed section.
 */
@Composable
private fun DevShiningRows(
    picks: List<Song>,
    activeId: String?,
    onSongClick: (Song) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        DevSectionHeader(label = stringResource(R.string.dev_shining_rows))
        if (picks.isEmpty()) {
            ShiningRowCard(
                title = stringResource(R.string.dev_shining_rows_loading),
                subtitle = stringResource(R.string.dev_shining_rows_loading_desc),
                leadingIconRes = R.drawable.music_note,
            )
        } else {
            picks.forEach { song ->
                ShiningRowCard(
                    title = song.title,
                    subtitle = song.orderedArtists.joinToString { it.name },
                    artworkUrl = song.thumbnailUrl,
                    leadingIconRes = R.drawable.music_note,
                    trailingIconRes = if (song.id == activeId) R.drawable.pause else R.drawable.play,
                    isActive = song.id == activeId,
                    onClick = { onSongClick(song) },
                )
            }
        }
    }
}

@Composable
private fun HomeSongRow(
    songs: List<Song>,
    activeId: String?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
) {
    LazyRow(
        // Extra trailing padding lets the next card peek in, which signals the row scrolls.
        contentPadding = PaddingValues(start = Spacing.medium, end = PEEK_PADDING),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        items(songs.distinctBy { it.id }, key = { it.id }) { song ->
            HomeSongCard(song, song.id == activeId, isPlaying && song.id == activeId, onSongClick)
        }
    }
}

@Composable
private fun HomeSongCard(song: Song, isActive: Boolean, isPlaying: Boolean, onClick: (Song) -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Column(
        // `clip` precedes `clickable` so the ripple stays inside the rounded artwork (rulebook §5).
        modifier = Modifier.width(132.dp).clip(shape).clickable { onClick(song) },
    ) {
        Box {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = "${song.title}, ${song.orderedArtists.joinToString { it.name }}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(132.dp).clip(shape),
            )
            if (isPlaying) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.small).size(28.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.pause),
                        contentDescription = "Now playing",
                        modifier = Modifier.padding(Spacing.tiny),
                    )
                }
            }
        }
        Text(
            song.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = Spacing.small),
        )
        Text(
            song.orderedArtists.joinToString { it.name },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HomeYouTubeRow(items: List<YTItem>, onItemClick: (YTItem) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(start = Spacing.medium, end = PEEK_PADDING),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        items(items.distinctBy { it.id }, key = { it.id }) { item ->
            Column(modifier = Modifier.width(160.dp).clip(MaterialTheme.shapes.medium).clickable { onItemClick(item) }) {
                AsyncImage(
                    model = item.thumbnail,
                    contentDescription = "${item.title} artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(112.dp).clip(MaterialTheme.shapes.medium),
                )
                // Reserve two lines so cards in a row keep aligned baselines regardless of title length.
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Spacing.small),
                )
            }
        }
    }
}

/**
 * "YouTube Chart": 4 columns x 2 rows of artwork-only tiles.
 *
 * Same visual family as Speed Dial, capped at eight items so it never competes with it for
 * vertical space. Hidden entirely when the chart request fails, which is why the caller checks
 * for an empty list rather than rendering a placeholder.
 */
@Composable
private fun TrendingChartGrid(
    songs: List<SongItem>,
    activeId: String?,
    isPlaying: Boolean,
    onSongClick: (SongItem) -> Unit,
) {
    val cells = songs.take(TRENDING_CELLS)
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        repeat(TRENDING_ROWS) { rowIndex ->
            val rowItems = cells.drop(rowIndex * TRENDING_COLUMNS).take(TRENDING_COLUMNS)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                rowItems.forEach { song ->
                    ArtworkTile(
                        title = song.title,
                        thumbnail = song.thumbnail,
                        isActive = song.id == activeId,
                        isPlaying = isPlaying,
                        modifier = Modifier.weight(1f),
                        onClick = { onSongClick(song) },
                    )
                }
                repeat(TRENDING_COLUMNS - rowItems.size) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    )
                }
            }
        }
    }
}

/** Shared artwork-only tile: used by the Trending and Speed Dial grids. */
@Composable
private fun ArtworkTile(
    title: String,
    thumbnail: String?,
    isActive: Boolean,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = thumbnail,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (isActive) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f)),
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.align(Alignment.Center).size(24.dp),
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = stringResource(R.string.play_pause),
                    modifier = Modifier.padding(4.dp),
                )
            }
        }
    }
}

/**
 * Speed Dial: a dense 4-column grid of artwork-only shortcuts (24 items, 6 rows).
 *
 * The previous 156x72dp pill row fitted barely two items per screen width and carried a title
 * block that made each entry tall. Titles are dropped deliberately — the artwork is the identifier
 * and tapping starts shuffle playback immediately.
 */
@Composable
private fun HomeSpeedDialGrid(
    columns: Int = SPEED_DIAL_COLUMNS,
    rows: Int = SPEED_DIAL_ROWS,
    items: List<YTItem>,
    activeId: String?,
    isPlaying: Boolean,
    onItemClick: (YTItem) -> Unit,
) {
    val wanted = columns * rows
    // No cycling: duplicates across the two grids were the reported bug. The pool is already
    // tier-filled by the view model, so unfilled cells here mean the library really is that small.
    val cells = items.distinctBy { it.id }.take(wanted)
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        repeat(rows) { rowIndex ->
            val rowItems = cells.drop(rowIndex * columns).take(columns)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                rowItems.forEach { item ->
                    ArtworkTile(
                        title = item.title,
                        thumbnail = item.thumbnail,
                        isActive = item.id == activeId,
                        isPlaying = isPlaying,
                        modifier = Modifier.weight(1f),
                        onClick = { onItemClick(item) },
                    )
                }
                // Keep the last row flush when it has fewer tiles than the column count.
                repeat(columns - rowItems.size) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    )
                }
            }
        }
    }
}



/**
 * Compact shuffle sampler: 3 columns x 2 rows (6 cells).
 *
 * A LazyVerticalGrid cannot be nested in the feed's LazyColumn, so this is a fixed Column of two
 * Rows. It used to be 3x3, which put a large block of artwork between the user and the real
 * content; the grid only needs to offer a quick tap-in, not be a destination.
 */
@Composable
private fun ShuffleGrid(
    columns: Int = SHUFFLE_COLUMNS,
    rows: Int = SHUFFLE_ROWS,
    songs: List<YTItem>,
    activeId: String?,
    isPlaying: Boolean,
    onSongClick: (YTItem) -> Unit,
) {
    val wanted = columns * rows
    // No cycling: a repeated cover would look like a broken grid. The view model already fills
    // from queue -> remixes -> recent -> library, so `songs` is as long as it can be.
    val cells = songs.take(wanted)
    Column(
        modifier =
            Modifier
                // Full width: the old 260dp cap made the grid render as a narrow strip pinned to
                // the left edge, which is what made it look "stuck at 50%" and unorganised.
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        // Always `rows` full rows. Building rows from `chunked(n)` produced no rows at all for an
        // empty list, which collapsed the section to a bare header while the pool loaded.
        repeat(rows) { rowIndex ->
            val rowSongs = cells.drop(rowIndex * columns).take(columns)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.tiny)) {
                rowSongs.forEach { song ->
                    val isActive = song.id == activeId
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onSongClick(song) },
                    ) {
                        AsyncImage(
                            model = song.thumbnail,
                            contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        if (isActive && isPlaying) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                shape = CircleShape,
                                modifier =
                                    Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(Spacing.tiny)
                                        .size(20.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.pause),
                                    contentDescription = stringResource(R.string.pause),
                                    modifier = Modifier.padding(2.dp),
                                )
                            }
                        }
                    }
                }
                // Missing cells keep their footprint as placeholders so the grid stays aligned.
                repeat(columns - rowSongs.size) {
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    )
                }
            }
        }
    }
}

/**
 * Top listened row: exactly 4 cells. Missing entries render as placeholders so the row
 * never collapses to a different width.
 */
@Composable
private fun TopListenedRow(
    songs: List<Song>,
    activeId: String?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Same right inset as the peeking rows above so the 4th cell lines up with theirs.
            .padding(start = Spacing.medium, end = PEEK_PADDING),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        repeat(4) { index ->
            val song = songs.getOrNull(index)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (song != null) {
                            Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onSongClick(song) }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    contentAlignment = Alignment.Center,
                ) {
                    if (song != null) {
                        AsyncImage(
                            model = song.thumbnailUrl,
                            contentDescription = "${song.title}, ${song.orderedArtists.joinToString { it.name }}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        if (song.id == activeId && isPlaying) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(Spacing.tiny)
                                    .size(24.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.pause),
                                    contentDescription = "Now playing",
                                    modifier = Modifier.padding(Spacing.tiny),
                                )
                            }
                        }
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.music_note),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(IconSizes.Medium),
                        )
                    }
                }
                Text(
                    text = song?.title.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Spacing.tiny),
                )
            }
        }
    }
}

@Composable
private fun HomeLoadingState() {
    Column(modifier = Modifier.fillMaxWidth().padding(Spacing.medium), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        repeat(3) {
            // Real shimmer placeholders (not blank boxes) so loading matches the feed rhythm.
            ShimmerHost(modifier = Modifier.fillMaxWidth().height(112.dp)) {
                GridItemPlaceHolder()
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
    }
}

private fun playSong(song: Song, playerConnection: PlayerConnection, activeId: String?) {
    if (song.id == activeId) playerConnection.togglePlayPause()
    else playerConnection.playQueue(ListQueue(song.title, listOf(song.toMediaItem())))
}

private fun playYouTubeItem(
    item: YTItem,
    playerConnection: PlayerConnection,
    activeId: String?,
    onOpen: (String) -> Unit,
) {
    when (item) {
        is SongItem -> {
            if (item.id == activeId) playerConnection.togglePlayPause()
            else playerConnection.playQueue(YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.toMediaMetadata()))
        }

        is EpisodeItem -> playerConnection.playQueue(ListQueue(item.title, listOf(item.toMediaMetadata().toMediaItem())))
        is AlbumItem -> onOpen("album/${item.id}")
        is PlaylistItem -> onOpen("online_playlist/${item.id}")
        is ArtistItem -> onOpen("artist/${item.id}")
        is PodcastItem -> onOpen("online_podcast/${item.id}")
    }
}
