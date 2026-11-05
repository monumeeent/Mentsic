/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class SpacingTest {

    @Test
    fun `spacing values match ui_rulebook.md grid`() {
        assertEquals(Dp(0f), Spacing.none)
        assertEquals(Dp(4f), Spacing.tiny)
        assertEquals(Dp(8f), Spacing.small)
        assertEquals(Dp(16f), Spacing.medium)
        assertEquals(Dp(24f), Spacing.large)
        assertEquals(Dp(32f), Spacing.huge)
    }

    @Test
    fun `spacing values follow 4dp grid`() {
        val values = listOf(
            Spacing.none, Spacing.tiny, Spacing.small,
            Spacing.medium, Spacing.large, Spacing.huge,
        )
        values.forEach { dp ->
            assertEquals(0f, dp.value % 4f, 0.001f)
        }
    }
}