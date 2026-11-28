/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Extra bottom-navigation surfaces, on top of the stock bar, the notched "orb" bar and the Apple-style
 * dock:
 *
 *  - [PillNavigationBar]  — a floating compact pill of icon wells; the active tab gets a tinted well.
 *  - [GradientButtonGroupNavigationBar] — a segmented rail whose active segment carries an animated
 *    Mentsic gradient pill.
 *
 * Both follow the same rules as the other docks so they are interchangeable at every call site:
 * equal-width **stationary** touch targets, no emoji/colour literals (theme roles only), and a height
 * the caller can reserve. Neither has orb geometry — that belongs to the notched bar alone.
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.metrolist.music.ui.screens.Screens
import com.metrolist.music.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

/** Height the pill surface reserves, callers use it for `navPadding`. */
val PillNavBarHeight = 72.dp

/** Height the gradient button group reserves. */
val GradientNavBarHeight = 64.dp

/** Resolved selected index for a dock; shared by all alternate surfaces. */
private fun selectedDockIndex(
    navigationItems: List<Screens>,
    currentRoute: String?,
): Int =
    navigationItems
        .indexOfFirst { screen -> isRouteSelected(currentRoute, screen.route, navigationItems) }
        .coerceAtLeast(0)

/**
 * Compact floating pill: icon wells inside a single rounded body, the active one filled with the accent.
 *
 * Narrower than the other docks by design — it is the "minimal" option for users who want the content
 * to lead and the bar to stay out of the way.
 */
@Composable
fun PillNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onSearchLongClick: (() -> Unit)? = null,
    onHomeLongHold: (() -> Unit)? = null,
) {
    if (navigationItems.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    val selectedIndex = remember(currentRoute, navigationItems) {
        selectedDockIndex(navigationItems, currentRoute)
    }

    Box(
        modifier = modifier.fillMaxWidth().height(PillNavBarHeight),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier =
                Modifier
                    .padding(bottom = Spacing.small)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                scheme.surfaceContainerHigh.copy(alpha = 0.98f),
                                scheme.surfaceContainer.copy(alpha = 0.94f),
                            ),
                        ),
                    )
                    .padding(horizontal = Spacing.tiny, vertical = Spacing.tiny),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationItems.forEachIndexed { index, screen ->
                val isSelected = index == selectedIndex
                SimpleDockTab(
                    label = stringResource(screen.titleId),
                    iconRes = if (isSelected) screen.iconIdActive else screen.iconIdInactive,
                    isSelected = isSelected,
                    onClick = { onItemClick(screen, isSelected) },
                    onLongClick =
                        when (screen) {
                            Screens.Search -> onSearchLongClick
                            Screens.Home -> onHomeLongHold
                            else -> null
                        },
                    wellSize = 44.dp,
                    iconSize = 21.dp,
                    selectedWell = scheme.primaryContainer,
                    unselectedWell = Color.Transparent,
                    selectedTint = scheme.onPrimaryContainer,
                    unselectedTint = scheme.onSurfaceVariant.copy(alpha = 0.8f),
                    // Equal-width slots: without a weight the row would collapse to icon widths and
                    // the touch targets would be uneven.
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Mentsic gradient navigation bar.
 *
 * The reference is a "gradient button group": a rounded segmented control whose selected segment is
 * filled with a moving multi-stop gradient while the rest stay flat ghosts on the same surface. This
 * is the Mentsic-flavoured variant — same concept, built from theme roles so it turns gold in the
 * Shining theme and stays black in OLED.
 *
 * Structure:
 *  1. **Rail** — one rounded container, flat surface, hairline outline.
 *  2. **Gradient pill** — the selected segment. Its fill is an animated horizontal gradient whose hue
 *     offset drifts slowly, so the active tab shimmers instead of sitting static.
 *  3. **Segments** — equal-width stationary touch targets; the selected one draws no well (the pill
 *     behind it is the highlight).
 *
 * The animation is a single `rememberInfiniteTransition` float driving the gradient's start offset;
 * it is read in the draw phase, so it never recomposes the icon row.
 */
@Composable
fun GradientButtonGroupNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onSearchLongClick: (() -> Unit)? = null,
    onHomeLongHold: (() -> Unit)? = null,
) {
    if (navigationItems.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    // A `Density` receiver is required for `Dp.toPx()`; at the layout level (outside a draw scope)
    // there is none in scope, so it is captured once here.
    val density = LocalDensity.current
    val selectedIndex =
        remember(currentRoute, navigationItems) {
            selectedDockIndex(navigationItems, currentRoute)
        }

    // Slow, continuous drift of the gradient stops. Infinite rather than animated-on-change, because
    // the point is ambient movement, not feedback.
    val shimmer = rememberInfiniteTransition(label = "gradient-dock")
    val shift by shimmer.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(5200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "gradient-dock-shift",
    )

    // Slide between segments, in index space; the pixel position is resolved at layout time.
    val highlight by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "gradient-dock-highlight",
    )

    Box(
        modifier = modifier.fillMaxWidth().height(GradientNavBarHeight),
        contentAlignment = Alignment.BottomCenter,
    ) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.small, vertical = Spacing.small)
                    .height(48.dp),
        ) {
            val slotWidthPx = constraints.maxWidth.toFloat() / navigationItems.size
            val railCorner =
                with(density) {
                    androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
                }

            // 1. Rail.
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = scheme.surfaceContainer,
                    cornerRadius = railCorner,
                )
                drawRoundRect(
                    color = scheme.outlineVariant.copy(alpha = 0.35f),
                    cornerRadius = railCorner,
                    style = Stroke(width = 1.dp.toPx()),
                )
            }

            // 2. Animated gradient pill under the active segment.
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val pillWidth = with(LocalDensity.current) { 64.dp.toPx() }
                val pillHeight = with(LocalDensity.current) { 38.dp.toPx() }
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .drawBehind {
                                val left = slotWidthPx * (highlight + 0.5f) - pillWidth / 2f
                                val top = (size.height - pillHeight) / 2f
                                val corner = androidx.compose.ui.geometry.CornerRadius(pillHeight / 2f)

                                // The gradient's window slides with `shift`, so the colour travels
                                // through the pill instead of the pill changing colour.
                                val stops =
                                    listOf(
                                        scheme.primary,
                                        scheme.tertiary,
                                        scheme.secondary,
                                        scheme.primary,
                                    )
                                drawRoundRect(
                                    brush =
                                        Brush.horizontalGradient(
                                            colors = stops,
                                            startX = left - pillWidth * (0.2f + shift),
                                            endX = left + pillWidth * (1.8f + shift),
                                        ),
                                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                                    size = androidx.compose.ui.geometry.Size(pillWidth, pillHeight),
                                    cornerRadius = corner,
                                )
                            },
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    navigationItems.forEachIndexed { index, screen ->
                        val isSelected = index == selectedIndex
                        SimpleDockTab(
                            label = stringResource(screen.titleId),
                            iconRes = if (isSelected) screen.iconIdActive else screen.iconIdInactive,
                            isSelected = isSelected,
                            onClick = { onItemClick(screen, isSelected) },
                            onLongClick =
                                when (screen) {
                                    Screens.Search -> onSearchLongClick
                                    Screens.Home -> onHomeLongHold
                                    else -> null
                                },
                            wellSize = 38.dp,
                            iconSize = 20.dp,
                            // The gradient pill is the highlight; the tab adds no second well.
                            selectedWell = Color.Transparent,
                            unselectedWell = Color.Transparent,
                            selectedTint = scheme.onPrimary,
                            unselectedTint = scheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

/**
 * One tab for the alternate surfaces: equal-weight slot, centred icon well.
 *
 * Tap and long-press share one interaction source because a second gesture detector would swallow the
 * tap. Nothing here consumes pointer events, so the host's click handling stays authoritative.
 */
@Composable
private fun SimpleDockTab(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    wellSize: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    selectedWell: Color,
    unselectedWell: Color,
    selectedTint: Color,
    unselectedTint: Color,
    modifier: Modifier = Modifier,
) {
    val viewConfiguration = LocalViewConfiguration.current
    val currentOnClick by rememberUpdatedState(onClick)
    val interaction = remember { MutableInteractionSource() }
    var pressed by remember { mutableStateOf(false) }

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
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = tween(90, easing = FastOutSlowInEasing),
        label = "simple-dock-press",
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
                    .size(wellSize)
                    .graphicsLayer {
                        scaleX = pressScale
                        scaleY = pressScale
                    }
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (isSelected) selectedWell else unselectedWell),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = if (isSelected) selectedTint else unselectedTint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
