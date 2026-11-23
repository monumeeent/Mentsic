/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Alternate navigation bar: a floating expandable dock.
 *
 * Selected destination expands into a pill with its label inside it, the rest collapse to bare
 * icons, and the whole dock hovers as a rounded island instead of a full-width bar. It is an
 * experiment: the setting that turns it on lives in the developer section (`DevFloatingDockKey`)
 * and `AppNavigationBar` falls back to the stock Material 3 bar when it is off.
 *
 * Icons, labels, long-press behaviour and route matching are shared with the stock bar through
 * [isRouteSelected] / [Screens.iconIdActive] so the two can never disagree about the selected tab.
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.metrolist.music.ui.screens.Screens
import com.metrolist.music.ui.theme.Spacing

/**
 * Height the dock occupies, including the breathing room it floats in. MainActivity reserves this
 * much space instead of [com.metrolist.music.constants.NavigationBarHeight] when the dock is on.
 */
val FloatingDockHeight = 68.dp

/** Vertical inset from the bottom of the reserved area, so the dock reads as hovering. */
private val DockFloatInset = 6.dp

private val DockShape = RoundedCornerShape(28.dp)

/**
 * Floating dock navigation bar.
 *
 * @param currentRoute the navigation route, used to derive the selected destination with the same
 *   matching rules as the stock bar.
 * @param onItemClick `(screen, isAlreadySelected)` — MainActivity already handles "selected means
 *   scroll to top" with that flag, so the dock passes it straight through.
 * @param onSearchLongClick long-press on the search tab (voice/recognition), optional.
 * @param onHomeLongHold long-press on the home tab (opens the account sheet), optional.
 */
@Composable
fun AppFloatingDockNavigationBar(
    navigationItems: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onSearchLongClick: (() -> Unit)? = null,
    onHomeLongHold: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium, vertical = DockFloatInset),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(FloatingDockHeight - Spacing.medium)
                    .clip(DockShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                scheme.surfaceContainerHigh,
                                scheme.surfaceContainer,
                            ),
                        ),
                    )
                    .border(1.dp, scheme.outlineVariant.copy(alpha = 0.35f), DockShape)
                    .padding(horizontal = Spacing.small, vertical = Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.tiny),
        ) {
            navigationItems.forEach { screen ->
                val isSelected =
                    remember(currentRoute, screen.route, navigationItems) {
                        isRouteSelected(currentRoute, screen.route, navigationItems)
                    }
                val iconRes = if (isSelected) screen.iconIdActive else screen.iconIdInactive

                DockItem(
                    label = stringResource(screen.titleId),
                    iconRes = iconRes,
                    isSelected = isSelected,
                    onClick = { onItemClick(screen, isSelected) },
                    onLongClick =
                        when (screen) {
                            Screens.Search -> onSearchLongClick
                            Screens.Home -> onHomeLongHold
                            else -> null
                        },
                )
            }
        }
    }
}

/**
 * One destination. Unselected items are a 44dp icon target; the selected one grows into a pill and
 * reveals its label, which is what makes the dock readable without a separate label row.
 */
@Composable
private fun RowScope.DockItem(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
) {
    val scheme = MaterialTheme.colorScheme

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "dock-icon-tint",
    )
    val containerAlpha by animateColorAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(220),
        label = "dock-pill-alpha",
    )
    val pillWidth by animateDpAsState(
        targetValue = if (isSelected) 108.dp else 44.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "dock-pill-width",
    )

    Box(
        modifier =
            Modifier
                .weight(1f)
                .height(44.dp)
                .clip(CircleShape)
                .clickable(
                    onClickLabel = label,
                    role = Role.Tab,
                    onClick = onClick,
                )
                // Long-press is a separate gesture: clickable() takes no onLongClick parameter.
                .then(
                    if (onLongClick != null) {
                        Modifier.pointerInput(onLongClick) {
                            detectTapGestures(onLongPress = { onLongClick() })
                        }
                    } else {
                        Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier =
                Modifier
                    .width(pillWidth)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(scheme.primaryContainer.copy(alpha = containerAlpha))
                    .padding(horizontal = Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = if (isSelected) null else label,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )

            if (isSelected) {
                Spacer(Modifier.width(Spacing.small))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
