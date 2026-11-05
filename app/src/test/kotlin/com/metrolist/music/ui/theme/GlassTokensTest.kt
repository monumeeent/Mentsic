/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class GlassTokensTest {

    @Test
    fun `Subtle glass style values match specification`() {
        assertEquals(0.65f, GlassTokens.Subtle.surfaceAlpha, 0.001f)
        assertEquals(0.12f, GlassTokens.Subtle.borderAlpha, 0.001f)
        assertEquals(0.75f, GlassTokens.Subtle.borderWidth.value, 0.001f)
        assertEquals(12f, GlassTokens.Subtle.blurRadius.value, 0.001f)
    }

    @Test
    fun `Elevated glass style values match specification`() {
        assertEquals(0.78f, GlassTokens.Elevated.surfaceAlpha, 0.001f)
        assertEquals(0.20f, GlassTokens.Elevated.borderAlpha, 0.001f)
        assertEquals(1f, GlassTokens.Elevated.borderWidth.value, 0.001f)
        assertEquals(24f, GlassTokens.Elevated.blurRadius.value, 0.001f)
    }

    @Test
    fun `PureBlackGlass style values match specification`() {
        assertEquals(0.85f, GlassTokens.PureBlackGlass.surfaceAlpha, 0.001f)
        assertEquals(0.08f, GlassTokens.PureBlackGlass.borderAlpha, 0.001f)
        assertEquals(0.5f, GlassTokens.PureBlackGlass.borderWidth.value, 0.001f)
        assertEquals(8f, GlassTokens.PureBlackGlass.blurRadius.value, 0.001f)
    }
}