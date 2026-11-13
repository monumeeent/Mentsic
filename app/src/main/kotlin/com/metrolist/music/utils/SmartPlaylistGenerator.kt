/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import android.content.Context
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.Album
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.Artist
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.WatchEndpoint
import com.metrolist.music.constants.SmartPlaylistRefreshKey
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.models.SmartPlaylist
import com.metrolist.music.models.SmartPlaylistType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the two auto-generated mixes without writing anything to the media database.
 *
 * * **Release Radar** — the InnerTube new-releases grid, filtered down to the artists the user
 *   follows. The followed-artist list only exists for signed-in accounts, so the builder returns
 *   `null` when the bookmark table is empty and the UI hides the row entirely.
 * * **Weekly Mix** — seeds taken from the most played tracks, expanded through each seed's radio
 *   ("up next") endpoint, then de-duplicated and capped. A failed expansion degrades to the seeds
 *   themselves instead of an empty screen.
 *
 * The results are virtual playlists: keeping them out of the database avoids a migration and any
 * conflict with the YouTube sync engine. They live in memory and are regenerated once
 * [REFRESH_DAYS] have passed since the last refresh, which is stamped in DataStore so the cadence
 * survives process death.
 */
@Singleton
class SmartPlaylistGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MusicDatabase,
) {
    companion object {
        /** Every generated mix rotates once a week. */
        private const val REFRESH_DAYS = 7L
        private const val RELEASE_RADAR_ALBUMS = 12
        private const val WEEKLY_MIX_SEEDS = 5
        private const val WEEKLY_MIX_LIMIT = 40
    }

    private val cached = LinkedHashMap<SmartPlaylistType, SmartPlaylist>()

    /** Reads the cache without touching the network; used on first composition. */
    fun cachedNow(): List<SmartPlaylist> = cached.values.toList()

    /** Drops the in-memory mixes so the next [load] regenerates them. */
    fun invalidate() {
        cached.clear()
    }

    /**
     * Returns both mixes, regenerating them when stale or when [force] is set. Missing sources make
     * the list absent rather than empty, so callers can simply skip empty results.
     */
    suspend fun load(force: Boolean = false): List<SmartPlaylist> =
        withContext(Dispatchers.IO) {
            if (!force && cached.isNotEmpty() && !isStale()) return@withContext cachedNow()

            val regenerated =
                coroutineScope {
                    val radar = async { runCatching { buildReleaseRadar() }.getOrNull() }
                    val weekly = async { runCatching { buildWeeklyMix() }.getOrNull() }
                    listOfNotNull(weekly.await(), radar.await())
                }

            cached.clear()
            regenerated.forEach { cached[it.type] = it }

            // Only stamp the refresh when a mix was actually produced: a failed offline load must
            // not suppress the next attempt for a whole week.
            if (regenerated.any { it.isPlayable }) {
                context.safeDataStoreEdit { settings ->
                    settings[SmartPlaylistRefreshKey] = System.currentTimeMillis()
                }
            }
            regenerated
        }

    private fun isStale(): Boolean {
        val lastRefresh = context.dataStore.get(SmartPlaylistRefreshKey, 0L)
        return SmartPlaylist.isStale(lastRefresh, refreshDays = REFRESH_DAYS)
    }

    private suspend fun buildReleaseRadar(): SmartPlaylist? {
        val followed = database.bookmarkedArtistEntitiesByNameAsc()
        if (followed.isEmpty()) return null

        val followedIds = followed.map { it.id }.toSet()
        val followedNames = followed.map { it.name.lowercase() }.toSet()

        val albums =
            YouTube
                .newReleaseAlbums()
                .onFailure { Timber.w(it, "Release Radar: newReleaseAlbums failed") }
                .getOrNull()
                .orEmpty()

        val fromFollowedArtists =
            albums
                .filter { album ->
                    album.artists.orEmpty().any { artist ->
                        (artist.id != null && artist.id in followedIds) ||
                            artist.name.lowercase() in followedNames
                    }
                }.distinctBy { it.playlistId }

        val tracks =
            fromFollowedArtists
                .take(RELEASE_RADAR_ALBUMS)
                .flatMap { album -> albumTracks(album) }
                .distinctBy { it.id }

        return SmartPlaylist(
            id = SmartPlaylist.idFor(SmartPlaylistType.RELEASE_RADAR),
            type = SmartPlaylistType.RELEASE_RADAR,
            title = "",
            description = "",
            songs = tracks,
            thumbnailUrls = fromFollowedArtists.take(4).map { it.thumbnail },
        )
    }

    private suspend fun albumTracks(album: AlbumItem): List<SongItem> {
        val page =
            YouTube
                .album(album.playlistId)
                .onFailure { Timber.d(it, "Release Radar: album ${album.playlistId} unavailable") }
                .getOrNull()
                ?: return emptyList()

        val albumRef = Album(id = album.id, name = album.title)
        return page.songs.map { song -> song.copy(album = song.album ?: albumRef) }
    }

    private suspend fun buildWeeklyMix(): SmartPlaylist {
        val seeds =
            database
                .mostPlayedSongs(
                    fromTimeStamp = LocalDateTime.now().minusWeeks(4),
                    limit = WEEKLY_MIX_SEEDS,
                ).first()

        val seedItems =
            seeds.map { song ->
                SongItem(
                    id = song.song.id,
                    title = song.song.title,
                    artists = song.artists.map { Artist(name = it.name, id = it.id) },
                    album = song.song.albumId?.let { id -> Album(id = id, name = song.song.albumName.orEmpty()) },
                    duration = song.song.duration.takeIf { it > 0 },
                    thumbnail = song.song.thumbnailUrl.orEmpty(),
                )
            }

        // One radio page per seed is enough for a 40-track mix and keeps the request count low.
        val expansions =
            coroutineScope {
                seedItems
                    .map { seed ->
                        async {
                            YouTube
                                .next(WatchEndpoint(videoId = seed.id))
                                .onFailure { Timber.d(it, "Weekly Mix: radio for ${seed.id} failed") }
                                .getOrNull()
                                ?.items
                                .orEmpty()
                        }
                    }.map { it.await() }
            }.flatten()

        val seedIds = seedItems.map { it.id }.toSet()
        // Seeded by the day so a mix is stable within a day but different next week.
        val random = Random(LocalDate.now().toEpochDay())

        val discovered =
            expansions
                .filterNot { it.isEpisode }
                .filterNot { it.id in seedIds }
                .distinctBy { it.id }
                .shuffled(random)

        val tracks = (discovered + seedItems).distinctBy { it.id }.take(WEEKLY_MIX_LIMIT)

        return SmartPlaylist(
            id = SmartPlaylist.idFor(SmartPlaylistType.WEEKLY_MIX),
            type = SmartPlaylistType.WEEKLY_MIX,
            title = "",
            description = "",
            songs = tracks,
            thumbnailUrls = tracks.map { it.thumbnail }.filter { it.isNotBlank() }.take(4),
        )
    }
}
