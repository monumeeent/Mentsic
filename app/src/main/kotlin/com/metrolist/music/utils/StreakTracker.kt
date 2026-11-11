/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Daily listening streak.
 *
 * A day counts once the user plays at least one song. The streak is stored per device
 * (DataStore), so it works without an account. All date maths is done on [LocalDate] in the
 * device time zone, so "today" flips at local midnight.
 */
data class StreakState(
    val current: Int = 0,
    val longest: Int = 0,
    val total: Int = 0,
    val history: Set<LocalDate> = emptySet(),
) {
    /** True when the user has already listened today. */
    fun isActiveOn(day: LocalDate): Boolean = day in history
}

/** One cell of the week strip rendered by the streak card. */
data class StreakDay(
    val date: LocalDate,
    val label: String,
    val isActive: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean,
)

object StreakTracker {
    private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /** Monday-first initials, matching the card's `startOfWeek={1}`. */
    private val DAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")

    /**
     * Records a song play for [today].
     *
     * - Already counted today -> returns the state unchanged (idempotent, so every play in a
     *   session does not inflate the streak).
     * - Played yesterday -> streak continues (+1).
     * - Gap of one or more days -> streak restarts at 1.
     */
    fun recordPlay(state: StreakState, today: LocalDate): StreakState {
        if (state.isActiveOn(today)) return state

        val continued = state.history.contains(today.minusDays(1))
        val current = if (continued) state.current + 1 else 1
        val history = state.history + today

        return state.copy(
            current = current,
            longest = maxOf(state.longest, current),
            total = state.total + 1,
            history = history,
        )
    }

    /**
     * Recomputes the *displayed* current streak for [today].
     *
     * The stored [StreakState.current] is only updated when a song is played, so a user who
     * skipped yesterday would otherwise still see their old streak. If the last active day is
     * older than yesterday the streak is broken and shows 0 until the next play.
     */
    fun currentForDisplay(state: StreakState, today: LocalDate): Int {
        if (state.history.isEmpty()) return 0
        if (state.isActiveOn(today) || state.isActiveOn(today.minusDays(1))) return state.current
        return 0
    }

    /** The Monday-to-Sunday week containing [today]. */
    fun weekDays(state: StreakState, today: LocalDate): List<StreakDay> {
        val monday = today.minusDays((today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
        return (0L..6L).map { offset ->
            val date = monday.plusDays(offset)
            StreakDay(
                date = date,
                label = DAY_LABELS[offset.toInt()],
                isActive = state.isActiveOn(date),
                isToday = date == today,
                isFuture = date.isAfter(today),
            )
        }
    }

    /** Days between the first and last recorded play, inclusive. */
    fun spanInDays(state: StreakState): Long {
        val first = state.history.minOrNull() ?: return 0
        val last = state.history.maxOrNull() ?: return 0
        return ChronoUnit.DAYS.between(first, last) + 1
    }

    // --- DataStore (de)serialisation -------------------------------------------------------

    fun encodeHistory(history: Set<LocalDate>): String =
        history.sorted().joinToString(",") { it.format(ISO) }

    fun decodeHistory(raw: String?): Set<LocalDate> =
        raw.orEmpty()
            .split(',')
            .mapNotNull { entry ->
                entry.trim().takeIf { it.isNotEmpty() }?.let {
                    runCatching { LocalDate.parse(it, ISO) }.getOrNull()
                }
            }
            .toSet()
}
