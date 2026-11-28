/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Apple-style dock: the alternate bottom navigation surface.
 *
 * The reference is the macOS/iPadOS dock — a frosted shelf of rounded icon tiles that magnify as the
 * pointer approaches. On touch there is no hover, so the magnification is driven by the **active**
 * tab instead: the selected tile is full size and lifted, its immediate neighbours are slightly
 * larger than the rest, and the remainder sit at base size. That produces the same "shelf with a
 * raised item" silhouette while remaining readable at rest.
 *
 * Structure mirrors [AppFloatingDockNavigationBar] deliberately:
 *
 *  1. **Shelf** — one rounded, blurred-surface capsule with a hairline top edge.
 *  2. **Tiles** — equal-width touch targets, stationary so a fast double tap cannot land on the wrong
 *     route.
 *  3. **Magnification** — a per-tile scale read in the *draw* phase, so moving the selection never
 *     recomposes the icon set.
 *
 * All colours come from `colorScheme` roles; nothing is hardcoded, so OLED black and the Shining
 * theme both cascade.
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.metrolist.music.constants.NavigationBarHeight
import com.metrolist.music.ui.screens.Screens
import com.metrolist.music.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/**
 * Height the Apple-style dock reserves. Equals the stock bar height, so switching styles never moves
 * the mini player or the content above the navigation area.
 */
val AppleDockNavHeight = NavigationBarHeight

/** Geometry of the dock. Kept in one object so the reserved height and the drawing cannot disagree. */
private object AppleDockSpec {
    /** Tile edge. Sized so the raised tile still fits inside [shelfHeight] without clipping. */
    val tileSize = 44.dp

    /**
     * Shelf body height. Slightly shorter than the reserved [AppleDockNavHeight] and bottom-aligned, so
     * the dock keeps its floating look while occupying exactly the same navigation footprint as the
     * stock bar.
     */
    val shelfHeight = 56.dp

    val shelfCorner = 22.dp
    val sideMargin = Spacing.medium
    val bottomGap = Spacing.small
    val iconSize = 20.dp

    /**
     * Selected tile scale (the "raised" item). Kept low now that the shelf is shorter: the tile must
     * stay inside the body, since any protrusion would break the reserved-height contract.
     */
    const val SelectedScale = 1.08f

    /** Neighbour tile scale — the gentle falloff that sells the magnification. */
    const val NeighbourScale = 1.03f
}

/**
 * Apple-style dock navigation bar.
 *
 * @param navigationItems destinations, laid out in equal slots.
 * @param onItemClick `(screen, isAlreadySelected)` — MainActivity owns "reselect scrolls to top".
 */
@Composable
fun AppleDockNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onSearchLongClick: (() -> Unit)? = null,
    onHomeLongHold: (() -> Unit)? = null,
) {
    if (navigationItems.isEmpty()) return
    val scheme = MaterialTheme.colorScheme

    val selectedIndex =
        remember(currentRoute, navigationItems) {
            navigationItems
                .indexOfFirst { screen -> isRouteSelected(currentRoute, screen.route, navigationItems) }
                .coerceAtLeast(0)
        }

    Box(
        // The caller reserves bottomInset + AppleDockNavHeight; fill that box and bottom-align the
        // shelf inside it, rather than hard-coding the height here (which shifted the whole bar).
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = AppleDockSpec.sideMargin),
        contentAlignment = Alignment.BottomCenter,
    ) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(AppleDockSpec.shelfHeight)
                    .padding(bottom = AppleDockSpec.bottomGap)
                    // Bottom-aligned inside the reserved NavigationBarHeight box: the reserved space is
                    // what has to match the stock bar, the shelf is free to look shorter.
                    .align(Alignment.BottomCenter),
        ) {
            // Frosted shelf: a translucent surface plus a soft top-edge highlight, which is what makes
            // the dock read as glass sitting above the content rather than a painted bar.
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = AppleDockSpec.shelfCorner.toPx()
                drawRoundRect(
                    brush =
                        Brush.verticalGradient(
                            listOf(
                                scheme.surfaceContainerHigh.copy(alpha = 0.96f),
                                scheme.surfaceContainer.copy(alpha = 0.92f),
                            ),
                        ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                )
                // Hairline top edge + faint inner outline.
                drawRoundRect(
                    color = scheme.outlineVariant.copy(alpha = 0.35f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                    style = Stroke(width = 1.dp.toPx()),
                )
                drawLine(
                    brush =
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, scheme.onSurface.copy(alpha = 0.08f), Color.Transparent),
                        ),
                    start = Offset(radius, 1.dp.toPx()),
                    end = Offset(size.width - radius, 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                navigationItems.forEachIndexed { index, screen ->
                    val isSelected = index == selectedIndex
                    // Distance from the selection drives the magnification falloff.
                    val distance = kotlin.math.abs(index - selectedIndex)
                    val targetScale =
                        when {
                            isSelected -> AppleDockSpec.SelectedScale
                            distance == 1 -> AppleDockSpec.NeighbourScale
                            else -> 1f
                        }
                    val scale by animateFloatAsState(
                        targetValue = targetScale,
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        label = "dock-tile-scale",
                    )

                    DockTile(
                        label = stringResource(screen.titleId),
                        iconRes = if (isSelected) screen.iconIdActive else screen.iconIdInactive,
                        isSelected = isSelected,
                        scale = scale,
                        onClick = { onItemClick(screen, isSelected) },
                        onLongClick =
                            when (screen) {
                                Screens.Search -> onSearchLongClick
                                Screens.Home -> onHomeLongHold
                                else -> null
                            },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** One dock tile: a rounded icon well that lifts and tints when it is the active destination. */
@Composable
private fun DockTile(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    scale: Float,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val viewConfiguration = LocalViewConfiguration.current
    val currentOnClick by rememberUpdatedState(onClick)
    val interaction = remember { MutableInteractionSource() }
    var pressed by remember { mutableStateOf(false) }

    // One interaction source handles tap *and* long-press: a second gesture detector would swallow
    // the tap.
    LaunchedEffect(interaction, onLongClick) {
        interaction.interactions.collectLatest { press ->
            when (press) {
                is PressInteraction.Press -> {
                    pressed = true
                    if (onLongClick != null) {
                        delay(viewConfiguration.longPressTimeoutMillis)
                        onLongClick()
                    }
                }

                is PressInteraction.Release, is PressInteraction.Cancel -> pressed = false
            }
        }
    }

    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(90, easing = FastOutSlowInEasing),
        label = "dock-tile-press",
    )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClickLabel = label,
                    role = Role.Tab,
                    onClick = { currentOnClick() },
                )
                .semantics { selected = isSelected },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(AppleDockSpec.tileSize)
                    .scale(scale * pressScale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) {
                            Brush.verticalGradient(
                                listOf(
                                    scheme.primaryContainer,
                                    scheme.primaryContainer.copy(alpha = 0.72f),
                                ),
                            )
                        } else {
                            // Inactive tiles are a translucent well, so the frosted shelf shows through.
                            Brush.verticalGradient(
                                listOf(
                                    scheme.surfaceContainerHighest.copy(alpha = 0.55f),
                                    scheme.surfaceContainerHighest.copy(alpha = 0.30f),
                                ),
                            )
                        },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = if (isSelected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                modifier = Modifier.size(AppleDockSpec.iconSize),
            )
        }

        // Selected label: a small caption tucked inside the bottom of the raised tile, the way the
        // dock names the hovered app. It must live *inside* the tile now that the shelf is 48dp — a
        // caption below the tile would fall off the body and break the reserved height.
        if (isSelected) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.82f,
                ),
                fontWeight = FontWeight.SemiBold,
                color = scheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}
