/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component.mentsic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.metrolist.music.R
import com.metrolist.music.ui.theme.Spacing

/** The widgets that ship with the app and can be pinned from Settings → Widgets. */
enum class WidgetKind {
    PLAYER,
    CIRCLE,
    LIST,
}

/**
 * Purely decorative mock-up of a home-screen widget. Drawn with Compose instead of inflating the
 * real RemoteViews so the gallery costs nothing at runtime and still follows the design tokens
 * ([MaterialTheme.colorScheme] only, 4dp spacing grid).
 */
@Composable
fun WidgetPreview(
    kind: WidgetKind,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val backdrop =
        Brush.linearGradient(
            listOf(scheme.surfaceContainerHigh, scheme.surfaceContainerLow),
        )

    Box(
        modifier =
            modifier
                .aspectRatio(if (kind == WidgetKind.CIRCLE) 1f else 1.35f)
                .clip(MaterialTheme.shapes.large)
                .background(backdrop)
                .padding(Spacing.medium),
        contentAlignment = Alignment.Center,
    ) {
        when (kind) {
            WidgetKind.PLAYER ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(scheme.primary.copy(alpha = 0.35f)),
                        )
                        Column(
                            modifier = Modifier.padding(start = Spacing.small),
                            verticalArrangement = Arrangement.spacedBy(Spacing.tiny),
                        ) {
                            Bar(widthFraction = 0.55f, height = 7.dp, color = scheme.onSurface)
                            Bar(widthFraction = 0.35f, height = 5.dp, color = scheme.onSurfaceVariant)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PreviewDot(R.drawable.skip_previous)
                        PreviewDot(R.drawable.play, filled = true)
                        PreviewDot(R.drawable.skip_next)
                        PreviewDot(R.drawable.favorite)
                    }
                }

            WidgetKind.CIRCLE ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(scheme.secondaryContainer)
                            .padding(Spacing.medium),
                    contentAlignment = Alignment.Center,
                ) {
                    // Inner ring reads as the record label area of the turntable widget.
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(scheme.surfaceContainerHighest.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(shape = CircleShape, color = scheme.primary, modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.play),
                                    contentDescription = null,
                                    tint = scheme.onPrimary,
                                    modifier = Modifier.padding(Spacing.small),
                                )
                            }
                        }
                    }
                }
                }

            WidgetKind.LIST ->
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    Bar(widthFraction = 0.4f, height = 8.dp, color = scheme.onSurface)
                    repeat(3) { index ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(20.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(
                                            if (index == 0) {
                                                scheme.tertiaryContainer
                                            } else {
                                                scheme.surfaceContainerHighest
                                            },
                                        ),
                            )
                            Column(
                                modifier = Modifier.padding(start = Spacing.small),
                                verticalArrangement = Arrangement.spacedBy(Spacing.tiny),
                            ) {
                                Bar(widthFraction = 0.5f, height = 5.dp, color = scheme.onSurface)
                                Bar(widthFraction = 0.28f, height = 4.dp, color = scheme.onSurfaceVariant)
                            }
                        }
                    }
                }
        }
    }
}

@Composable
private fun PreviewDot(icon: Int, filled: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = if (filled) scheme.primary else Color.Transparent,
        modifier = Modifier.size(if (filled) 32.dp else 24.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (filled) scheme.onPrimary else scheme.onSurfaceVariant,
            modifier = Modifier.padding(Spacing.tiny),
        )
    }
}

/** Decorative bar used inside the mock-ups; drawn as a surface so it neither leaks a font size. */
@Composable
private fun Bar(widthFraction: Float, height: Dp, color: Color) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth(widthFraction)
                .height(height)
                .clip(CircleShape)
                .background(color),
    )
}

/** Caption under a widget card. Muted when the widget cannot be pinned. */
@Composable
fun WidgetCaption(text: String, enabled: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color =
            if (enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}
