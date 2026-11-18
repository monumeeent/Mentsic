/**
 * Mentsic Music — Shining theme.
 *
 * A complete, self-contained palette ("VIP vibe"): deep obsidian surfaces, champagne-gold
 * primary, warm on-surface text and a gold-tinted secondary. It is picked from the Theme screen
 * as a standalone option next to the regular palette seeds, not as a colour-harmony style —
 * picking it overrides the seed colour and the harmony, so the app looks the same regardless of
 * which palette happened to be selected before.
 *
 * Rulebook compliance: these literals live only in the theme layer (the rulebook forbids raw
 * colours inside screens/components, not inside theme definitions). Screens keep reading
 * `MaterialTheme.colorScheme` roles.
 */

package com.metrolist.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/** Seed used by the Theme screen when the Shining theme is picked. */
val ShiningSeed = Color(0xFFD4AF37)

/** Champagne gold on obsidian — dark variant, used by the app and by the theme preview. */
val ShiningDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFE8C877),
    onPrimary = Color(0xFF241C00),
    primaryContainer = Color(0xFF4A3A05),
    onPrimaryContainer = Color(0xFFFFE9A8),
    inversePrimary = Color(0xFF6F5B1A),
    secondary = Color(0xFFEDE3C8),
    onSecondary = Color(0xFF221D0E),
    secondaryContainer = Color(0xFF3B3420),
    onSecondaryContainer = Color(0xFFFFF3D2),
    tertiary = Color(0xFFBFC6D8),
    onTertiary = Color(0xFF10131C),
    tertiaryContainer = Color(0xFF2C3140),
    onTertiaryContainer = Color(0xFFDDE2F4),
    background = Color(0xFF07070A),
    onBackground = Color(0xFFF0EAE0),
    surface = Color(0xFF0A0A0E),
    onSurface = Color(0xFFF0EAE0),
    surfaceVariant = Color(0xFF1A1A22),
    onSurfaceVariant = Color(0xFFB3AC9F),
    surfaceTint = Color(0xFFE8C877),
    inverseSurface = Color(0xFFF0EAE0),
    inverseOnSurface = Color(0xFF17161B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8C8579),
    outlineVariant = Color(0xFF2E2B27),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF232229),
    surfaceDim = Color(0xFF0A0A0E),
    surfaceContainerLowest = Color(0xFF050508),
    surfaceContainerLow = Color(0xFF101017),
    surfaceContainer = Color(0xFF141419),
    surfaceContainerHigh = Color(0xFF1B1B22),
    surfaceContainerHighest = Color(0xFF24242D),
)

/** Champagne gold on warm ivory — light variant. */
val ShiningLightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF7D5F00),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE9A8),
    onPrimaryContainer = Color(0xFF241C00),
    inversePrimary = Color(0xFFE8C877),
    secondary = Color(0xFF6A5F3F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF2E6C7),
    onSecondaryContainer = Color(0xFF231B05),
    tertiary = Color(0xFF586166),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCE4EA),
    onTertiaryContainer = Color(0xFF161C21),
    background = Color(0xFFFFFBF2),
    onBackground = Color(0xFF1C1B16),
    surface = Color(0xFFFFFBF2),
    onSurface = Color(0xFF1C1B16),
    surfaceVariant = Color(0xFFEFE8DA),
    onSurfaceVariant = Color(0xFF4D4739),
    surfaceTint = Color(0xFF7D5F00),
    inverseSurface = Color(0xFF31302A),
    inverseOnSurface = Color(0xFFF4F0E6),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF7E7767),
    outlineVariant = Color(0xFFD0C8B8),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFFFBF2),
    surfaceDim = Color(0xFFDFD9CE),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBF5E9),
    surfaceContainer = Color(0xFFF5EFE3),
    surfaceContainerHigh = Color(0xFFEFE9DD),
    surfaceContainerHighest = Color(0xFFE9E4D7),
)

/**
 * True while the Shining theme is applied.
 *
 * Hosts supply it through [LocalShiningThemeActive]; it defaults to `false`, so any preview or
 * screen rendered outside the app theme is unaffected. Components use it to decide whether the
 * animated light sweep should run.
 */
val LocalShiningThemeActive = compositionLocalOf { false }

@Composable
@ReadOnlyComposable
fun isShiningThemeActive(): Boolean = LocalShiningThemeActive.current

/** Resolves the Shining palette for the current light/dark setting. */
@Composable
fun shiningColorScheme(darkTheme: Boolean = isSystemInDarkTheme()): ColorScheme =
    if (darkTheme) ShiningDarkColorScheme else ShiningLightColorScheme
