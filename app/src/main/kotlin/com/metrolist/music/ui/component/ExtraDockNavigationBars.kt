/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Extra bottom-navigation surfaces, on top of the stock bar, the notched "orb" bar and the Apple-style
 * dock:
 *
 *  - [PillNavigationBar]  — a floating compact pill of icon wells; the active tab gets a tinted well.
 *  - [GlassNavigationBar] — a wide frosted bar with one highlight capsule that slides under the active
 *                           tab.
 *
 * Both follow the same rules as the other docks so they are interchangeable at every call site:
 * equal-width **stationary** touch targets, no emoji/colour literals (theme roles only), and a height
 * the caller can reserve. Neither has orb geometry — that belongs to the notched bar alone.
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.metrolist.music.ui.screens.Screens
import com.metrolist.music.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

/** Height the pill surface reserves, callers use it for `navPadding`. */
val PillNavBarHeight = 72.dp

/** Height the glass surface reserves. */
val GlassNavBarHeight = 76.dp

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
 * Glass bar: one wide frosted body with a single highlight capsule that slides under the active tab.
 *
 * The capsule is positioned in the offset lambda from the animation value, so travelling between tabs
 * repaints one strip instead of recomposing the icon set.
 */
@Composable
fun GlassNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onSearchLongClick: (() -> Unit)? = null,
    onHomeLongHold: (() -> Unit)? = null,
) {
    if (navigationItems.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val selectedIndex = remember(currentRoute, navigationItems) {
        selectedDockIndex(navigationItems, currentRoute)
    }

    // Index-space animation driving the highlight; the pixel position is resolved at layout time.
    val highlight by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "glass-highlight",
    )

    Box(
        modifier = modifier.fillMaxWidth().height(GlassNavBarHeight),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.small, vertical = Spacing.small)
                    .height(60.dp),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val slotWidthPx = constraints.maxWidth.toFloat() / navigationItems.size

                // Frosted body.
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                    drawRoundRect(
                        brush =
                            Brush.verticalGradient(
                                listOf(
                                    scheme.surfaceContainerHigh.copy(alpha = 0.96f),
                                    scheme.surfaceContainer.copy(alpha = 0.90f),
                                ),
                            ),
                        cornerRadius = r,
                    )
                    drawRoundRect(
                        color = scheme.outlineVariant.copy(alpha = 0.30f),
                        cornerRadius = r,
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }

                // Sliding highlight capsule.
                val capsuleWidth = with(density) { 64.dp.toPx() }
                val capsuleHeight = with(density) { 44.dp.toPx() }
                Box(
                    modifier =
                        Modifier
                            .offset {
                                IntOffset(
                                    (slotWidthPx * (highlight + 0.5f) - capsuleWidth / 2f).roundToInt(),
                                    ((constraints.maxHeight - capsuleHeight) / 2f).roundToInt(),
                                )
                            }
                            .width(64.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        scheme.primaryContainer,
                                        scheme.primaryContainer.copy(alpha = 0.70f),
                                    ),
                                ),
                            ),
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
                            wellSize = 44.dp,
                            iconSize = 22.dp,
                            // The capsule behind the tab already provides the highlight.
                            selectedWell = Color.Transparent,
                            unselectedWell = Color.Transparent,
                            selectedTint = scheme.onPrimaryContainer,
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
