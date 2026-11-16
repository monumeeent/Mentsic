/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.home

import com.metrolist.music.R

/**
 * Which sections the Home feed renders.
 *
 * A preset is a *filter* over the existing feed rather than a second layout implementation: the
 * feed already composes its sections conditionally, so switching preset is a few booleans instead
 * of duplicate screens. HUB is handled separately (it swaps in the Command Hub entirely).
 */
enum class HomeLayoutPreset(
    val key: String,
) {
    /** Everything: Trending, Speed Dial, Shuffles, Top Listened, Quick Picks, Discover, … */
    FEED("feed"),

    /** Just the two quick-access grids, for a one-screen Home. */
    COMPACT("compact"),

    /** Zone-based Command Hub (falls back to FEED when it is not enabled). */
    HUB("hub"),

    /** Library-first: Top Listened and your playlists lead, discovery rows dropped. */
    LIBRARY("library"),
    ;

    val labelRes: Int
        get() =
            when (this) {
                FEED -> R.string.preset_feed
                COMPACT -> R.string.preset_compact
                HUB -> R.string.preset_hub
                LIBRARY -> R.string.preset_library
            }

    val descriptionRes: Int
        get() =
            when (this) {
                FEED -> R.string.preset_feed_desc
                COMPACT -> R.string.preset_compact_desc
                HUB -> R.string.preset_hub_desc
                LIBRARY -> R.string.preset_library_desc
            }

    // --- Section switches -------------------------------------------------------------

    val showsTrending: Boolean get() = this == FEED || this == LIBRARY
    val showsSpeedDial: Boolean get() = this != HUB
    val showsShuffles: Boolean get() = this == FEED || this == COMPACT
    val showsPersonalSpace: Boolean get() = this == FEED
    val showsDiscovery: Boolean get() = this == FEED
    val showsTopListened: Boolean get() = this == FEED || this == LIBRARY

    companion object {
        val Default = FEED

        fun fromKey(key: String?): HomeLayoutPreset = entries.firstOrNull { it.key == key } ?: Default
    }
}
