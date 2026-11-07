/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.metrolist.music.ui.theme.GlassStyle
import com.metrolist.music.ui.theme.GlassTokens
import com.metrolist.music.ui.theme.glassmorphicSurface

/**
 * A reusable glassmorphic card component that applies translucent surface fill,
 * subtle border luminance, and optical clipping using [GlassTokens].
 *
 * @param modifier Compose modifier chain
 * @param shape The corner shape for clipping and border (default: RoundedCornerShape(16.dp))
 * @param style The glass style variant (default: GlassTokens.Subtle)
 * @param onClick Optional click handler; when provided, the card becomes clickable
 * @param content The card content
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    style: GlassStyle = GlassTokens.Subtle,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val cardModifier = modifier
        .clip(shape)
        .glassmorphicSurface(shape = shape, style = style)

    val contentModifier = if (onClick != null) {
        cardModifier.clickable(
            onClick = onClick,
        )
    } else {
        cardModifier
    }

    Box(
        modifier = contentModifier,
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * Modifier extension that applies glassmorphic card styling.
 */
@Composable
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(16.dp),
    style: GlassStyle = GlassTokens.Subtle,
): Modifier =
    this
        .clip(shape)
        .glassmorphicSurface(shape = shape, style = style)