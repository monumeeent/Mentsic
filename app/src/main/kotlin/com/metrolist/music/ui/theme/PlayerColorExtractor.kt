/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette

@Immutable
data class PlayerColorPalette(
    val primary: Color,
    val secondary: Color,
    val surfaceTint: Color,
    val darkColor: Color,
    val onPrimary: Color,
)

object PlayerColorExtractor {
    fun extractColorPalette(
        palette: Palette,
        fallbackColor: Int,
        darkColor: Color = Color(0xFF000000),
    ): PlayerColorPalette {
        val ranked = palette.rankedColors(2, fallbackColor)
        val primary = Color(ranked.first())
        val secondary = ranked.getOrNull(1)?.let { Color(it) } ?: primary.copy(
            red = primary.red * 0.6f,
            green = primary.green * 0.6f,
            blue = primary.blue * 0.6f,
        )

        val luminance = (primary.red * 0.299f) + (primary.green * 0.587f) + (primary.blue * 0.114f)
        val onPrimary = if (luminance > 0.5f) Color.Black else Color.White

        return PlayerColorPalette(
            primary = primary,
            secondary = secondary,
            surfaceTint = primary.copy(alpha = 0.25f),
            darkColor = darkColor,
            onPrimary = onPrimary,
        )
    }

    fun extractGradientColors(
        palette: Palette,
        fallbackColor: Int,
        darkColor: Color = Color(0xFF000000),
    ): List<Color> {
        val extracted = extractColorPalette(palette, fallbackColor, darkColor)
        return listOf(
            extracted.primary,
            extracted.secondary,
            extracted.darkColor,
        )
    }
}
