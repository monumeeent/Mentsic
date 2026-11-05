/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScreenRouteTest {

    @Test
    fun `main screens routes are correct`() {
        assertEquals("home", ScreenRoute.Home.route)
        assertEquals("search_input", ScreenRoute.Search.route)
        assertEquals("listen_together", ScreenRoute.ListenTogether.route)
        assertEquals("library", ScreenRoute.Library.route)
    }

    @Test
    fun `fromString resolves known routes`() {
        assertEquals(ScreenRoute.Home, ScreenRoute.Companion.fromString("home"))
        assertEquals(ScreenRoute.Search, ScreenRoute.Companion.fromString("search_input"))
        assertEquals(ScreenRoute.ListenTogether, ScreenRoute.Companion.fromString("listen_together"))
        assertEquals(ScreenRoute.Library, ScreenRoute.Companion.fromString("library"))
        assertEquals(ScreenRoute.Settings, ScreenRoute.Companion.fromString("settings"))
    }

    @Test
    fun `fromString returns null for unknown routes`() {
        assertNull(ScreenRoute.Companion.fromString("unknown_route"))
        assertNull(ScreenRoute.Companion.fromString(""))
    }

    @Test
    fun `mainScreens contains exactly four screens`() {
        assertEquals(4, ScreenRoute.MainScreens.size)
        assertEquals(ScreenRoute.Home, ScreenRoute.MainScreens[0])
        assertEquals(ScreenRoute.Search, ScreenRoute.MainScreens[1])
        assertEquals(ScreenRoute.ListenTogether, ScreenRoute.MainScreens[2])
        assertEquals(ScreenRoute.Library, ScreenRoute.MainScreens[3])
    }
}