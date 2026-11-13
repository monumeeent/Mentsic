/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.models



/**
 * One row of the weekly/monthly recap: a song and how much it was played inside the window.
 * Built from the same queries the Stats screen already uses, so both surfaces agree on the maths.
 */
data class ReportHighlight(
    val id: String,
    val title: String,
    val artistName: String,
    val thumbnailUrl: String,
    val playCount: Int,
    val minutesListened: Long,
)

/**
 * Everything the recap card renders. Kept as a plain data class so it can be compared in tests and
 * captured by the share-image renderer without touching Compose state.
 */
data class ListeningReport(
    val periodLabel: String,
    val totalPlays: Int,
    val totalMinutes: Long,
    val uniqueArtists: Int,
    val topSongs: List<ReportHighlight>,
    val topArtistNames: List<String>,
    val activeDays: Int,
    val periodDays: Int,
) {
    val isEmpty: Boolean
        get() = totalPlays == 0

    /** Average minutes per active day, used for the "you listen X min/day" line. */
    val minutesPerActiveDay: Long
        get() = if (activeDays == 0) 0 else totalMinutes / activeDays
}
