/**
 * Mentsic Music — Material 3 Expressive theme.
 *
 * Applies the production Purple color scheme (light & dark), Roboto typography,
 * M3 Expressive shapes and [MotionScheme.expressive()] motion. Every UI color is
 * resolved through [androidx.compose.material3.ColorScheme] roles — no hard-coded
 * surface/on-surface values are used inside screen or component composables.
 */

package com.metrolist.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Mentsic Expressive shapes: pill buttons, 20dp cards, 28dp dialogs. */
val MentsicShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(4.dp), // chips / small containers
    medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp), // cards
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp), // dialogs / large containers
)

/** Roboto typeface using the standard M3 weights (FontFamily.Default resolves to Roboto on Android). */
val MentsicTypography = Typography(
    fontFamily = FontFamily.Default,
    displayLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W400, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W400, fontSize = 45.sp),
    displaySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W400, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W400, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W400, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W400, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W500, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W500, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W500, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W400, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W400, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W400, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W500, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W500, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.W500, fontSize = 11.sp),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MentsicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) mentsicDarkColorScheme else mentsicLightColorScheme
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = MentsicShapes,
        typography = MentsicTypography,
        content = content,
    )
}

/** Light scheme — Purple theme (all roles per spec). */
private val mentsicLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF635A75),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFFFFD8E4),
    onTertiary = Color(0xFF31111D),
    surface = Color(0xFFFEF7FF),
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFF3EDF7),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    inverseSurface = Color(0xFF322F35),
    inverseOnSurface = Color(0xFFF5EFF7),
    inversePrimary = Color(0xFFD0BCFF),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
)

/** Dark scheme — Purple theme (all roles per spec). */
private val mentsicDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD2BCFC),
    onPrimary = Color(0xFF32226F),
    primaryContainer = Color(0xFF4C3889),
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = Color(0xFFCDC1E1),
    secondaryContainer = Color(0xFF4B425D),
    onSecondaryContainer = Color(0xFFE9DDFD),
    tertiary = Color(0xFF6C3644),
    onTertiary = Color(0xFFFFDAE1),
    surface = Color(0xFF141317),
    onSurface = Color(0xFFE4E1E7),
    surfaceVariant = Color(0xFF201F23),
    onSurfaceVariant = Color(0xFFC9C4D1),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    inverseSurface = Color(0xFFE4E1E7),
    inverseOnSurface = Color(0xFF313034),
    inversePrimary = Color(0xFF6750A4),
    outline = Color(0xFF938F9B),
    outlineVariant = Color(0xFF494550),
)