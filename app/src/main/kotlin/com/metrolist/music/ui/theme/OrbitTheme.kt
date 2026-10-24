/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

object OrbitColors {
    // Light theme colors
    val primaryLight = Color(0xFF6750A4)
    val onPrimaryLight = Color(0xFFEDE9FE)
    val primaryContainerLight = Color(0xFF4F378B)
    val onPrimaryContainerLight = Color(0xFFEDE9FE)

    val secondaryLight = Color(0xFF625B71)
    val onSecondaryLight = Color(0xFFEDE9FE)
    val secondaryContainerLight = Color(0xFF7E748B)
    val onSecondaryContainerLight = Color(0xFFEDE9FE)

    val surfaceLight = Color(0xFF0E0E12)
    val onSurfaceLight = Color(0xFFE6E1E5)
    val surfaceVariantLight = Color(0xFF49454F)
    val onSurfaceVariantLight = Color(0xFFCAC4D0)

    val backgroundLight = Color(0xFF0E0E12)
    val onBackgroundLight = Color(0xFFE6E1E5)

    val accentLight = Color(0xFFC8007C)
    val onAccentLight = Color(0xFF18181C)

    val errorLight = Color(0xFFB00020)
    val onErrorLight = Color(0xFFEDE9FE)

    val outlineLight = Color(0xFF79747E)
    val outlineVariantLight = Color(0xFF49454F)

    val inverseSurfaceLight = Color(0xFFE6E1E5)
    val inverseOnSurfaceLight = Color(0xFF0E0E12)
    val inversePrimaryLight = Color(0xFFB825FF)

    // Dark theme colors
    val primaryDark = Color(0xFFB825FF)
    val onPrimaryDark = Color(0xFF18181C)
    val primaryContainerDark = Color(0xFF4F378B)
    val onPrimaryContainerDark = Color(0xFFEDE9FE)

    val secondaryDark = Color(0xFF8E7AC9)
    val onSecondaryDark = Color(0xFF18181C)
    val secondaryContainerDark = Color(0xFF4F378B)
    val onSecondaryContainerDark = Color(0xFFEDE9FE)

    val surfaceDark = Color(0xFF0E0E12)
    val onSurfaceDark = Color(0xFFE6E1E5)
    val surfaceVariantDark = Color(0xFF49454F)
    val onSurfaceVariantDark = Color(0xFFCAC4D0)

    val backgroundDark = Color(0xFF0E0E12)
    val onBackgroundDark = Color(0xFFE6E1E5)

    val accentDark = Color(0xFFC8007C)
    val onAccentDark = Color(0xFF18181C)

    val errorDark = Color(0xFFB00020)
    val onErrorDark = Color(0xFFEDE9FE)

    val outlineDark = Color(0xFF79747E)
    val outlineVariantDark = Color(0xFF49454F)

    val inverseSurfaceDark = Color(0xFFE6E1E5)
    val inverseOnSurfaceDark = Color(0xFF0E0E12)
    val inversePrimaryDark = Color(0xFF6750A4)
}

val orbitLightColorScheme = lightColorScheme(
    primary = OrbitColors.primaryLight,
    onPrimary = OrbitColors.onPrimaryLight,
    primaryContainer = OrbitColors.primaryContainerLight,
    onPrimaryContainer = OrbitColors.onPrimaryContainerLight,
    secondary = OrbitColors.secondaryLight,
    onSecondary = OrbitColors.onSecondaryLight,
    secondaryContainer = OrbitColors.secondaryContainerLight,
    onSecondaryContainer = OrbitColors.onSecondaryContainerLight,
    tertiary = OrbitColors.accentLight,
    onTertiary = OrbitColors.onAccentLight,
    background = OrbitColors.backgroundLight,
    onBackground = OrbitColors.onBackgroundLight,
    surface = OrbitColors.surfaceLight,
    onSurface = OrbitColors.onSurfaceLight,
    surfaceVariant = OrbitColors.surfaceVariantLight,
    onSurfaceVariant = OrbitColors.onSurfaceVariantLight,
    error = OrbitColors.errorLight,
    onError = OrbitColors.onErrorLight,
    outline = OrbitColors.outlineLight,
    outlineVariant = OrbitColors.outlineVariantLight,
    inverseSurface = OrbitColors.inverseSurfaceLight,
    inverseOnSurface = OrbitColors.inverseOnSurfaceLight,
    inversePrimary = OrbitColors.inversePrimaryLight,
)

val orbitDarkColorScheme = darkColorScheme(
    primary = OrbitColors.primaryDark,
    onPrimary = OrbitColors.onPrimaryDark,
    primaryContainer = OrbitColors.primaryContainerDark,
    onPrimaryContainer = OrbitColors.onPrimaryContainerDark,
    secondary = OrbitColors.secondaryDark,
    onSecondary = OrbitColors.onSecondaryDark,
    secondaryContainer = OrbitColors.secondaryContainerDark,
    onSecondaryContainer = OrbitColors.onSecondaryContainerDark,
    tertiary = OrbitColors.accentDark,
    onTertiary = OrbitColors.onAccentDark,
    background = OrbitColors.backgroundDark,
    onBackground = OrbitColors.onBackgroundDark,
    surface = OrbitColors.surfaceDark,
    onSurface = OrbitColors.onSurfaceDark,
    surfaceVariant = OrbitColors.surfaceVariantDark,
    onSurfaceVariant = OrbitColors.onSurfaceVariantDark,
    error = OrbitColors.errorDark,
    onError = OrbitColors.onErrorDark,
    outline = OrbitColors.outlineDark,
    outlineVariant = OrbitColors.outlineVariantDark,
    inverseSurface = OrbitColors.inverseSurfaceDark,
    inverseOnSurface = OrbitColors.inverseOnSurfaceDark,
    inversePrimary = OrbitColors.inversePrimaryDark,
)

fun ColorScheme.applyOrbitTheme(isDark: Boolean): ColorScheme {
    return if (isDark) orbitDarkColorScheme else orbitLightColorScheme
}

@Composable
fun getOrbitColorResource(id: Int): Color {
    return Color(colorResource(id))
}

@Composable
fun OrbitTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) orbitDarkColorScheme else orbitLightColorScheme,
        content = content
    )
}
