/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.db.entities

/**
 * Result of the daily play counts query used by the Listening Heatmap.
 * Room can map this POJO directly; the ViewModel converts it to a Map.
 */
data class DailyPlayCount(
    val day: String,
    val playCount: Int,
)