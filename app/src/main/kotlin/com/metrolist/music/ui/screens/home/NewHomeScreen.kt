package com.metrolist.music.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.metrolist.innertube.models.AlbumItem
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
import com.metrolist.music.LocalPlayerBottomSheetState
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.db.entities.Album
import com.metrolist.music.db.entities.Song
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.models.toMediaMetadata
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.playback.queues.ListQueue
import com.metrolist.music.playback.queues.YouTubeQueue
import com.metrolist.music.ui.component.BottomSheetState
import com.metrolist.music.ui.component.GlassCard
import com.metrolist.music.ui.component.NavigationTitle
import com.metrolist.music.ui.component.shimmer.GridItemPlaceHolder
import com.metrolist.music.ui.component.shimmer.ShimmerHost

import com.metrolist.music.ui.theme.GlassTokens
import com.metrolist.music.ui.theme.Spacing
import com.metrolist.music.viewmodels.HomeViewModel
import java.util.Calendar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHomeScreen(
    modifier: Modifier = Modifier,
    playerBottomSheetState: BottomSheetState? = LocalPlayerBottomSheetState.current,
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

    LaunchedEffect(Unit) { viewModel.loadHomeData() }

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
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
                    Column(modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)) {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Now, next, later",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                item(key = "new_home_filters") {
                    // InnerTube chips are the only filter the ViewModel can actually fetch, so the
                    // row is built from live chips with an always-available "All" reset.
                    val chipTitles = uiState.homePage?.chips?.map { it.title }.orEmpty()
                    HomeFilterRow(
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

                if (!isFocused) {
                    item(key = "new_home_hero") {
                        HomeHeroResumeCard(
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
                            onConnect = { navController.navigate("account") },
                        )
                    }

                    if (uiState.speedDialItems.isNotEmpty()) {
                        item(key = "new_home_speed_dial") {
                            HomeSectionHeaderV2(title = stringResource(R.string.speed_dial))
                            HomeSpeedDialRow(
                                items = uiState.speedDialItems.take(6),
                                activeId = mediaMetadata?.id,
                                isPlaying = isPlaying,
                                onItemClick = { item ->
                                    playYouTubeItem(item, playerConnection, mediaMetadata?.id) { route -> navController.navigate(route) }
                                },
                            )
                        }
                    }
                }

                // Curated bands only render in the unfiltered feed; a selected chip replaces them
                // with the filtered InnerTube sections at the bottom of this block.
                if (!isFocused) {
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

            if (mediaMetadata != null) {
                HomeMiniPlayerCapsule(
                    title = mediaMetadata?.title.orEmpty(),
                    artist = mediaMetadata?.artists?.joinToString { it.name }.orEmpty(),
                    isPlaying = isPlaying,
                    onClick = {
                        // MainActivity renders the player as a bottom sheet; expand it rather than
                        // pushing a duplicate route.
                        playerBottomSheetState?.expandSoft()
                    },
                    onToggle = { playerConnection.togglePlayPause() },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = feedInsets.calculateBottomPadding()),
                )
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
                        // Lift above the capsule when one is visible so the two never overlap.
                        .padding(bottom = feedInsets.calculateBottomPadding() + if (mediaMetadata != null) 80.dp else 0.dp),
                ) { Icon(Icons.Default.Shuffle, contentDescription = stringResource(R.string.shuffle)) }
            }
        }
    }
}



@Composable
private fun HomeFilterRow(filters: List<String>, selected: String, onSelected: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        items(filters, key = { it }) { filter ->
            FilterChip(
                selected = filter == selected,
                onClick = { onSelected(filter) },
                label = { Text(filter, style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
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

@Composable
private fun HomeHeroResumeCard(
    target: ResumeTarget?,
    isPlaying: Boolean,
    onResume: () -> Unit,
    onConnect: () -> Unit,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.medium).height(168.dp),
        shape = MaterialTheme.shapes.large,
        style = GlassTokens.Elevated,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(Spacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            if (target != null) {
                AsyncImage(
                    model = target.artwork,
                    contentDescription = "${target.title} artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(96.dp).clip(RoundedCornerShape(Spacing.medium)),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("CONTINUE LISTENING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(target.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        target.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(Spacing.small))
                    Text("Ready when you are", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ResumeButton(isPlaying = isPlaying, onClick = onResume)
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Text("YOUR NEXT LISTEN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("Make this space yours", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Connect your account to personalize your home.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onConnect) { Icon(Icons.Default.ArrowForward, contentDescription = "Connect") }
            }
        }
    }
}

@Composable
private fun ResumeButton(isPlaying: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.size(48.dp),
    ) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if (isPlaying) "Pause" else "Resume") }
}

@Composable
private fun HomeSectionHeaderV2(title: String) {
    // Reuses the shared home header so typography and the optional Play-all capsule stay in one
    // place; `NavigationTitle` already applies the horizontal system-bar insets.
    NavigationTitle(title = title)
}

@Composable
private fun HomeSongRow(
    songs: List<Song>,
    activeId: String?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.medium),
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
                    Icon(Icons.Default.Pause, contentDescription = "Now playing", modifier = Modifier.padding(Spacing.tiny))
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
        contentPadding = PaddingValues(horizontal = Spacing.medium),
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

@Composable
private fun HomeSpeedDialRow(items: List<YTItem>, activeId: String?, isPlaying: Boolean, onItemClick: (YTItem) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        items(items.distinctBy { it.id }, key = { it.id }) { item ->
            Surface(
                onClick = { onItemClick(item) },
                shape = MaterialTheme.shapes.medium,
                color = if (item.id == activeId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.width(156.dp).height(72.dp),
            ) {
                Row(modifier = Modifier.padding(Spacing.small), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = item.thumbnail, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small))
                    Column(modifier = Modifier.padding(start = Spacing.small).weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (item.id == activeId && isPlaying) {
                            Text("PLAYING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeMiniPlayerCapsule(title: String, artist: String, isPlaying: Boolean, onClick: () -> Unit, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.medium, vertical = Spacing.small), style = GlassTokens.Elevated, shape = RoundedCornerShape(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = Spacing.medium, vertical = Spacing.small), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onToggle) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if (isPlaying) "Pause" else "Play") }
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
