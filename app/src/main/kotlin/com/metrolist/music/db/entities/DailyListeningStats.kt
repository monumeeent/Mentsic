/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.db.entities

/**
 * One day of listening, aggregated by `DatabaseDao.dailyListeningStats`.
 *
 * Room maps this POJO directly from the aggregate query. Brightness for a cell and the day
 * capsule's totals both come from one row, so tapping a day needs no extra lookup.
 */
data class DailyListeningStats(
    /** ISO day, `yyyy-MM-dd`. */
    val day: String,
    val playCount: Int,
    /** Real listening time, in milliseconds (sum of `Event.playTime`). */
    val listenMillis: Long,
)

/** Lifetime totals, aggregated by `DatabaseDao.allTimeStats`. */
data class AllTimeStats(
    val totalPlays: Int = 0,
    val totalMillis: Long = 0L,
    /** Number of distinct days with at least one play. */
    val activeDays: Int = 0,
)
