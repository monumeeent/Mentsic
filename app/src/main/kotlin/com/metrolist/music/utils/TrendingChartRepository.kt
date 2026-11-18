/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import android.content.Context
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.pages.ChartsPage
import com.metrolist.music.constants.TrendingRefreshKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backs the "YouTube Chart" row: the trending videos for the device's country.
 *
 * InnerTube has no social-media virality feed, so this is the closest honest source — YouTube's
 * own chart, already region-localised because `YouTube.locale` is set from the device in
 * `App.kt`. No new network plumbing is needed: [YouTube.getChartsPage] takes no region argument.
 *
 * Charts move daily but not minute-to-minute, so the list is cached in memory and refreshed at
 * most once per [REFRESH_DAYS], stamped in DataStore like the smart playlists.
 */
@Singleton
class TrendingChartRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        /** How many tiles the Home row shows (2 rows x 4 columns). */
        const val ROW_SIZE = 8

        private const val REFRESH_DAYS = 1L
    }

    private var cached: List<SongItem> = emptyList()

    /** Cached snapshot for immediate display; empty until [load] has run. */
    fun cachedNow(): List<SongItem> = cached

    /**
     * Returns up to [ROW_SIZE] trending songs.
     *
     * Prefers the TRENDING section, falls back to TOP when the chart response omits it, and
     * finally to any section with songs. Returns an empty list (never throws) so the Home row can
     * simply skip rendering when offline.
     */
    suspend fun load(force: Boolean = false): List<SongItem> =
        withContext(Dispatchers.IO) {
            if (!force && cached.isNotEmpty() && !isStale()) return@withContext cached

            val page =
                YouTube
                    .getChartsPage()
                    .onFailure { Timber.w(it, "Trending: charts request failed") }
                    .getOrNull()
                    ?: return@withContext cached

            // Section titles are localised, so `chartType` is not reliable — it is derived from
            // the English word "Trending"/"Top". Prefer the typed section when it exists, but
            // fall back to merging every section so the row is never empty on a non-English
            // device.
            val sections = page.sections
            val typed =
                sections.filter { it.chartType == ChartsPage.ChartType.TRENDING }
                    .ifEmpty { sections.filter { it.chartType == ChartsPage.ChartType.TOP } }

            // Chart carousels mix songs, playlists and episodes. Requiring the typed section
            // alone produced a half-empty (or empty) row whenever that one section happened to
            // be mostly non-song entries, so the typed items lead and the other sections top the
            // row up instead of being ignored.
            val ordered = typed.flatMap { it.items } + sections.flatMap { it.items }

            val songs =
                ordered
                    .filterIsInstance<SongItem>()
                    .distinctBy { it.id }
                    .take(ROW_SIZE)

            if (songs.isNotEmpty()) {
                cached = songs
                context.safeDataStoreEdit { settings ->
                    settings[TrendingRefreshKey] = System.currentTimeMillis()
                }
            }
            // Returning `songs` (possibly empty) rather than the stale cache keeps the caller
            // honest: an empty result means "the chart really has no playable songs right now".
            songs
        }

    private fun isStale(): Boolean {
        val last = context.dataStore.get(TrendingRefreshKey, 0L)
        if (last <= 0L) return true
        val lastDate =
            Instant
                .ofEpochMilli(last)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        return ChronoUnit.DAYS.between(lastDate, LocalDate.now()) >= REFRESH_DAYS
    }
}
