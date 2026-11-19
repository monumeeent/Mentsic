/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.db.entities

/**
 * One day of listening, aggregated by `DatabaseDao.dailyListeningStats`.
 *
 * Room maps this POJO directly. Everything the heatmap needs for a single cell — brightness,
 * the day capsule's totals and the track of the day — comes from one row, so tapping a cell does
 * not trigger another query for data that was already fetched.
 */
data class DailyListeningStats(
    /** ISO day, `yyyy-MM-dd`. */
    val day: String,
    val playCount: Int,
    /** Real listening time, in milliseconds (sum of `Event.playTime`). */
    val listenMillis: Long,
    /** Song id with the most plays that day, or null when the day has no events. */
    val topSongId: String?,
)

/** Lifetime totals, aggregated by `DatabaseDao.allTimeStats`. */
data class AllTimeStats(
    val totalPlays: Int = 0,
    val totalMillis: Long = 0L,
    /** Number of distinct days with at least one play. */
    val activeDays: Int = 0,
)
