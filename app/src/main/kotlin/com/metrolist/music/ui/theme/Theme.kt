/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme

import android.graphics.Bitmap
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import com.materialkolor.score.Score

val DefaultThemeColor = Color(0xFF8B5CF6)

@Composable
fun MetrolistTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    enableOrbitTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val baseColorScheme = rememberDynamicColorScheme(
        seedColor = themeColor,
        isDark = darkTheme,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        style = PaletteStyle.TonalSpot,
    )

    val colorScheme = remember(baseColorScheme, pureBlack, darkTheme) {
        if (darkTheme && pureBlack) {
            baseColorScheme.pureBlack(true)
        } else {
            baseColorScheme
        }
    }

    val finalColorScheme = remember(colorScheme, enableOrbitTheme, darkTheme) {
        if (enableOrbitTheme) {
            colorScheme.applyOrbitTheme(darkTheme)
        } else {
            colorScheme
        }
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = if (enableOrbitTheme) orbitTypography else MaterialTheme.typography,
        content = content,
    )
}

val orbitTypography = Typography(
    displayLarge = TextStyle(fontFamily = orbitDoto, fontWeight = FontWeight.W700, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = orbitDoto, fontWeight = FontWeight.W400, fontSize = 45.sp),
    displaySmall = TextStyle(fontFamily = orbitDoto, fontWeight = FontWeight.W400, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = orbitDoto, fontWeight = FontWeight.W700, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = orbitDoto, fontWeight = FontWeight.W400, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = orbitDoto, fontWeight = FontWeight.W400, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = orbitDoto, fontWeight = FontWeight.W700, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = orbitDoto, fontWeight = FontWeight.W400, fontSize = 18.sp),
    titleSmall = TextStyle(fontFamily = orbitDoto, fontWeight = FontWeight.W400, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = orbitInter, fontWeight = FontWeight.W400, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = orbitInter, fontWeight = FontWeight.W400, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = orbitInter, fontWeight = FontWeight.W400, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = orbitInter, fontWeight = FontWeight.W500, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = orbitInter, fontWeight = FontWeight.W500, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = orbitInter, fontWeight = FontWeight.W500, fontSize = 11.sp),
)

fun Bitmap.extractThemeColor(): Color = Color(
    Palette.from(this)
        .maximumColorCount(8)
        .generate()
        .rankedColors(1, DefaultThemeColor.toArgb())
        .first()
)

internal fun Palette.rankedColors(
    desiredColorCount: Int,
    fallbackColor: Int,
): List<Int> = Score.score(
    swatches.associate { it.rgb to it.population },
    desiredColorCount,
    fallbackColor,
    true,
)

fun ColorScheme.pureBlack(apply: Boolean) =
    if (apply) copy(
        surface = Color.Black,
        background = Color.Black,
    ) else this

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)

    override fun SaverScope.save(value: Color): Int = value.toArgb()
}