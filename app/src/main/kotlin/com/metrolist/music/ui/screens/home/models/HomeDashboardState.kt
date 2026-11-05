/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.home.models

import com.metrolist.innertube.models.YTItem
import com.metrolist.innertube.pages.ExplorePage
import com.metrolist.innertube.pages.HomePage
import com.metrolist.music.db.entities.LocalItem
import com.metrolist.music.db.entities.Song
import com.metrolist.music.db.entities.SpeedDialItem
import com.metrolist.music.models.SimilarRecommendation
import com.metrolist.music.viewmodels.CommunityPlaylistItem
import com.metrolist.music.viewmodels.DailyDiscoverItem

/**
 * Immutable snapshot of all Home screen data, consumed by [HomeScreen] in a single
 * `collectAsStateWithLifecycle` call. Using a data class here makes structural equality
 * checks cheap and prevents spurious recompositions — Compose will only recompose when
 * a field value actually changes.
 *
 * All nullable lists start as `null` (not yet loaded) and transition to an empty list
 * or a populated list once the ViewModel emits them.
 */
data class HomeDashboardState(
    // --- Playback quick-access grids ---
    val quickPicks: List<Song>? = null,
    val forgottenFavorites: List<Song>? = null,
    val keepListening: List<LocalItem>? = null,

    // --- Speed Dial (pinned + algorithmic) ---
    val speedDialItems: List<YTItem> = emptyList(),
    val pinnedSpeedDialItems: List<SpeedDialItem> = emptyList(),

    // --- Algorithmic / personalisation sections ---
    val dailyDiscover: List<DailyDiscoverItem>? = null,
    val similarRecommendations: List<SimilarRecommendation>? = null,
    val accountPlaylists: List<YTItem>? = null,
    val communityPlaylists: List<CommunityPlaylistItem>? = null,

    // --- Home / Explore page (InnerTube) ---
    val homePage: HomePage? = null,
    val explorePage: ExplorePage? = null,

    // --- Account metadata ---
    val accountName: String = "Guest",
    val accountImageUrl: String? = null,

    // --- Loading states ---
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isRandomizing: Boolean = false,
) {
    /** True when no content has arrived yet (all lists null). */
    val isEmpty: Boolean
        get() =
            quickPicks == null &&
                forgottenFavorites == null &&
                keepListening == null &&
                speedDialItems.isEmpty() &&
                dailyDiscover == null &&
                homePage == null

    companion object {
        val Loading = HomeDashboardState(isLoading = true)
    }
}

