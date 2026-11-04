/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class GlassStyle(
    val surfaceAlpha: Float,
    val borderAlpha: Float,
    val borderWidth: Dp = 1.dp,
    val blurRadius: Dp = 16.dp,
)

object GlassTokens {
    val Subtle = GlassStyle(
        surfaceAlpha = 0.65f,
        borderAlpha = 0.12f,
        borderWidth = 0.75.dp,
        blurRadius = 12.dp,
    )

    val Elevated = GlassStyle(
        surfaceAlpha = 0.78f,
        borderAlpha = 0.20f,
        borderWidth = 1.dp,
        blurRadius = 24.dp,
    )

    val PureBlackGlass = GlassStyle(
        surfaceAlpha = 0.85f,
        borderAlpha = 0.08f,
        borderWidth = 0.5.dp,
        blurRadius = 8.dp,
    )
}

/**
 * Applies a layered glassmorphic surface with translucent background fill,
 * optical clipping, and subtle border luminance.
 */
@Composable
fun Modifier.glassmorphicSurface(
    shape: Shape,
    style: GlassStyle = GlassTokens.Subtle,
    baseColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    borderColor: Color = MaterialTheme.colorScheme.onSurface,
): Modifier {
    val surfaceColor = baseColor.copy(alpha = style.surfaceAlpha)
    val strokeColor = borderColor.copy(alpha = style.borderAlpha)

    return this
        .clip(shape)
        .background(surfaceColor, shape)
        .border(
            width = style.borderWidth,
            color = strokeColor,
            shape = shape,
        )
}

