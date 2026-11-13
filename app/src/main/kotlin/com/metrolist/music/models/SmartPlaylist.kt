/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.models

import com.metrolist.innertube.models.SongItem

/** The auto-generated mixes offered by the smart-playlist engine. */
enum class SmartPlaylistType {
    /** New releases from the artists the user follows ("Release Radar"). */
    RELEASE_RADAR,

    /** Fresh picks derived from the user's most played tracks ("Weekly Mix"). */
    WEEKLY_MIX,
}

/**
 * A smart playlist as shown in the UI. Deliberately virtual: nothing is written to the media
 * database, so there is no migration, no sync conflict, and the content can be refreshed freely.
 *
 * @param id stable key used for list keys and navigation arguments.
 * @param type which generator produced this list.
 * @param title localised title resolved by the caller.
 * @param description short subtitle ("Updated every Monday", etc.).
 * @param songs the generated tracks, already de-duplicated and shuffled by the generator.
 * @param thumbnailUrls up to four artwork URLs used to build a collage cover.
 */
data class SmartPlaylist(
    val id: String,
    val type: SmartPlaylistType,
    val title: String,
    val description: String,
    val songs: List<SongItem>,
    val thumbnailUrls: List<String> = emptyList(),
) {
    val isPlayable: Boolean
        get() = songs.isNotEmpty()

    companion object {
        fun idFor(type: SmartPlaylistType): String =
            when (type) {
                SmartPlaylistType.RELEASE_RADAR -> "smart_release_radar"
                SmartPlaylistType.WEEKLY_MIX -> "smart_weekly_mix"
            }

        fun typeFromId(id: String): SmartPlaylistType? =
            SmartPlaylistType.entries.firstOrNull { idFor(it) == id }
    }
}
