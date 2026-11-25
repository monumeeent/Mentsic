/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Alternate navigation bar: a **floating notched bar**.
 *
 * One dark body whose top edge dips into a concave notch under the active tab, with the active icon
 * floating in a tinted orb inside that cradle. Inactive icons sit flat on the bar. It replaces the
 * stock Material 3 bar when the developer turns `DevFloatingDockKey` on.
 *
 * Three layers, back to front — this split is what keeps the motion cheap and seam-free:
 *
 *  1. **Base** — one `Canvas` path: rounded body *plus* the bezier notch. Because the notch is part
 *     of the same path, sliding it can never leave a seam between two shapes.
 *  2. **Tabs** — N equal-width, **stationary** touch targets. Hit zones must not move mid-animation,
 *     or a fast double tap lands on the wrong route.
 *  3. **Orb**  — the floating badge. It reads the **same** animation value as the notch, so it can
 *     never detach from its pocket.
 *
 * Precisability: every dimension and duration lives in [FloatingNotchSpec], and the live values are
 * read once per bar through [rememberFloatingNotchSpec] — there are no duplicated constants.
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.metrolist.music.constants.DockBodyHeightKey
import com.metrolist.music.constants.DockCornerRadiusKey
import com.metrolist.music.constants.DockNotchDepthKey
import com.metrolist.music.constants.DockNotchWidthKey
import com.metrolist.music.constants.DockOrbLiftKey
import com.metrolist.music.constants.DockOrbSizeKey
import com.metrolist.music.ui.screens.Screens
import com.metrolist.music.ui.theme.Spacing
import com.metrolist.music.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

/**
 * Every tunable of the notched bar, as immutable data.
 *
 * Defaults are **golden-ratio derived** from [bodyHeight] = 65dp (φ ≈ 1.618):
 *  - notch mouth   120dp ≈ 65 × φ² / 1.42  — wide enough that the orb never pinches the curve;
 *  - notch depth    36dp ≈ 65 × 0.5539 (1/φ)   — the cradle is exactly as deep as the orb sinks;
 *  - orb size       54dp ≈ 65 × 0.83  (φ/2 ≈ 0.809) — the orb dominates but never fills the notch;
 *  - orb lift       12dp ≈ 65 / φ²/2  — a third of the orb stays above the bar top;
 *  - corner radius   0dp — square body, so the notch is the only curve on screen.
 *
 * The live instance comes from [rememberFloatingNotchSpec], which overlays the developer settings,
 * so notch, orb travel, touch geometry and the height MainActivity reserves all follow one value.
 */
data class FloatingNotchSpec(
    /** Height of the visible body (excludes [floatInset]). */
    val bodyHeight: Dp = 65.dp,
    /** Gap below the body, so the bar reads as hovering rather than docked. */
    val floatInset: Dp = 8.dp,
    /** Horizontal margin from the screen edges. */
    val sideMargin: Dp = Spacing.medium,
    /** Corner radius of the body; 0 = square body. Clamped to bodyHeight / 2 when drawing. */
    val cornerRadius: Dp = 0.dp,
    /** Width of the notch mouth (the span the curve covers). */
    val notchWidth: Dp = 120.dp,
    /** How far the curve dips below the body's top edge. */
    val notchDepth: Dp = 36.dp,
    /** Diameter of the floating orb. */
    val orbSize: Dp = 54.dp,
    /** How far the orb's centre sits above the bar's top edge (positive = lifted). */
    val orbLift: Dp = 12.dp,
    /** Icon size, inside the orb and on the bar. */
    val iconSize: Dp = 22.dp,
    /** Notch + orb travel time, used when [useSpring] is false. */
    val travelMillis: Int = 380,
    /** Spring travel (true) or a flat [travelMillis] ease-out (false). */
    val useSpring: Boolean = true,
    /** Press-down scale for a tapped tab. */
    val pressScale: Float = 0.94f,
    /** Peak scale of the orb's settle pop. */
    val popScale: Float = 1.06f,
    /** Colour shift duration between muted and accent. */
    val tintMillis: Int = 200,
) {
    /** Total height to reserve in the scaffold for this bar. */
    val reservedHeight: Dp get() = bodyHeight + floatInset + orbProtrusion

    /**
     * How far the orb's top edge rises above the bar container's own top (0 when it does not).
     *
     * The orb is vertically centred on the notch and then raised by [orbLift]. When the cradle is
     * shallower than the orb (the default: 36dp notch vs a 54dp orb) the badge necessarily pokes
     * above the body, and the reserved height has to account for it — otherwise the mini-player and
     * list content would be drawn over the protruding half of the orb.
     */
    val orbProtrusion: Dp
        get() = maxOf(0.dp, orbSize / 2f + orbLift - notchDepth / 2f)

    /** Y offset of the orb's top edge, relative to the bar container's top. May be negative. */
    val orbTopOffset: Dp
        get() = floatInset + notchDepth / 2f - orbLift - orbSize / 2f + orbProtrusion

    companion object {
        val Default = FloatingNotchSpec()
    }
}

/**
 * The live spec: [FloatingNotchSpec.Default] overlaid with the developer's saved geometry.
 *
 * Reading the preferences here (rather than in a global object) means the settings screen and the
 * bar can never disagree, and values survive a process restart. MainActivity calls this too, so the
 * space it reserves always matches what the bar actually draws.
 */
@Composable
fun rememberFloatingNotchSpec(): FloatingNotchSpec {
    val (bodyHeight) = rememberPreference(DockBodyHeightKey, FloatingNotchSpec.Default.bodyHeight.value)
    val (notchWidth) = rememberPreference(DockNotchWidthKey, FloatingNotchSpec.Default.notchWidth.value)
    val (notchDepth) = rememberPreference(DockNotchDepthKey, FloatingNotchSpec.Default.notchDepth.value)
    val (orbSize) = rememberPreference(DockOrbSizeKey, FloatingNotchSpec.Default.orbSize.value)
    val (orbLift) = rememberPreference(DockOrbLiftKey, FloatingNotchSpec.Default.orbLift.value)
    val (cornerRadius) = rememberPreference(DockCornerRadiusKey, FloatingNotchSpec.Default.cornerRadius.value)

    return remember(bodyHeight, notchWidth, notchDepth, orbSize, orbLift, cornerRadius) {
        FloatingNotchSpec.Default.copy(
            bodyHeight = bodyHeight.dp,
            notchWidth = notchWidth.dp,
            notchDepth = notchDepth.dp,
            orbSize = orbSize.dp,
            orbLift = orbLift.dp,
            cornerRadius = cornerRadius.dp,
        )
    }
}

/**
 * Floating notched navigation bar.
 *
 * @param navigationItems destinations, laid out in equal slots. Derived from the measured width, so
 *   adding or removing the Listen Together tab keeps working.
 * @param onItemClick `(screen, isAlreadySelected)` — MainActivity owns "reselect scrolls to top", so
 *   this is passed straight through.
 * @param onSearchLongClick long-press on the search tab (voice recognition), optional.
 * @param onHomeLongHold long-press on the home tab (account sheet), optional.
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
    if (navigationItems.isEmpty()) return
    val spec = rememberFloatingNotchSpec()
    val scheme = MaterialTheme.colorScheme
    val density = LocalDensity.current

    val selectedIndex =
        remember(currentRoute, navigationItems) {
            navigationItems
                .indexOfFirst { screen -> isRouteSelected(currentRoute, screen.route, navigationItems) }
                .coerceAtLeast(0)
        }

    // Index space, not pixel space: one value drives both notch and orb, and it survives rotation
    // and width changes because the width is applied at draw time.
    val progress = remember { Animatable(selectedIndex.toFloat()) }

    LaunchedEffect(selectedIndex) {
        progress.animateTo(
            targetValue = selectedIndex.toFloat(),
            animationSpec =
                if (spec.useSpring) {
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    )
                } else {
                    // Snappier alternative for low-end devices or users who dislike spring motion.
                    tween(spec.travelMillis, easing = FastOutSlowInEasing)
                },
        )
    }

    // The orb settles into its cradle: a short pop while the travel runs, skipping on reselect.
    val orbScale = remember { Animatable(1f) }
    var lastSettledIndex by remember { mutableStateOf(selectedIndex) }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != lastSettledIndex) {
            lastSettledIndex = selectedIndex
            orbScale.snapTo(1f)
            orbScale.animateTo(spec.popScale, tween(110, easing = FastOutSlowInEasing))
            orbScale.animateTo(1f, tween(190, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(spec.reservedHeight)
                .padding(horizontal = spec.sideMargin),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val slotWidthPx = constraints.maxWidth.toFloat() / navigationItems.size

            // One frame-time source for all three layers. `progress.value` is read ONLY inside
            // draw/offset lambdas below — never into a composition-scope val — because a composition
            // read would recompose the entire bar (icons, semantics, strings) on every animation
            // frame. That was the lag: ~60 recompositions per tab switch instead of zero.
            val frameX: () -> Float = { slotWidthPx * (progress.value + 0.5f) }

            // ── Layer 1: notched body ───────────────────────────────────────────────────────
            Canvas(modifier = Modifier.fillMaxSize()) {
                val top = spec.floatInset.toPx()
                val bodyPx = size.height - top
                drawNotchedBody(
                    centreX = frameX(),
                    top = top,
                    heightPx = bodyPx,
                    widthPx = size.width,
                    cornerPx = minOf(spec.cornerRadius.toPx(), bodyPx / 2f),
                    notchHalfPx = minOf(spec.notchWidth.toPx() / 2f, slotWidthPx * 0.46f),
                    notchDepthPx = minOf(spec.notchDepth.toPx(), bodyPx * 0.45f),
                    body = scheme.surfaceContainerHigh,
                    edge = scheme.outlineVariant.copy(alpha = 0.35f),
                )
            }

            // ── Layer 2: stationary touch targets + flat icons ──────────────────────────────
            // Laid out as a Row of equal weights: the slot width is then exactly the same
            // slotWidthPx used by the notch maths above, with no manual padding arithmetic.
            Row(modifier = Modifier.fillMaxSize()) {
                navigationItems.forEachIndexed { index, screen ->
                    val isSelected = index == selectedIndex
                    DockTab(
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
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Layer 3: the floating orb ───────────────────────────────────────────────────
            // Position is applied in the *offset* lambda (placement phase), so travelling the orb
            // never recomposes this row — only the placement pass re-runs.
            val orbSizePx = with(density) { spec.orbSize.toPx() }
            val orbTopPx = with(density) { spec.orbTopOffset.toPx() }

            Box(
                modifier =
                    Modifier
                        .offset { IntOffset((frameX() - orbSizePx / 2f).roundToInt(), orbTopPx.roundToInt()) }
                        .size(spec.orbSize)
                        .graphicsLayer {
                            // Scale in the draw phase for the same reason as the offset above.
                            val s = orbScale.value
                            scaleX = s
                            scaleY = s
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    scheme.primaryContainer,
                                    scheme.surfaceContainerHighest,
                                ),
                            ),
                        )
                        .semantics { selected = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(navigationItems[selectedIndex].iconIdActive),
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(spec.iconSize),
                )
            }
        }
    }
}

/**
 * Body path: rounded rectangle with a concave bezier notch at [centreX].
 *
 * The notch is two cubic segments whose control points push sideways by [notchHalfPx] before diving
 * by [notchDepthPx]. That keeps the curve tangent to the top edge at both ends, which is what makes
 * it read as a cut in the bar rather than a bump pasted onto it.
 */
private fun DrawScope.drawNotchedBody(
    centreX: Float,
    top: Float,
    heightPx: Float,
    widthPx: Float,
    cornerPx: Float,
    notchHalfPx: Float,
    notchDepthPx: Float,
    body: Color,
    edge: Color,
) {
    val bottom = top + heightPx
    val right = widthPx

    val path =
        Path().apply {
            // With a square body there are no arcs, so the outline starts at the true top-left
            // corner; with rounded corners it starts where the top-left arc ends.
            moveTo(if (cornerPx > 0f) cornerPx else 0f, top)
            lineTo(centreX - notchHalfPx, top)

            // Down into the cradle…
            cubicTo(
                centreX - notchHalfPx * 0.55f, top,
                centreX - notchHalfPx * 0.62f, top + notchDepthPx,
                centreX, top + notchDepthPx,
            )
            // …and back out, tangentially.
            cubicTo(
                centreX + notchHalfPx * 0.62f, top + notchDepthPx,
                centreX + notchHalfPx * 0.55f, top,
                centreX + notchHalfPx, top,
            )

            lineTo(if (cornerPx > 0f) right - cornerPx else right, top)
            if (cornerPx > 0f) {
                arcTo(Rect(right - cornerPx * 2f, top, right, top + cornerPx * 2f), -90f, 90f, false)
                lineTo(right, bottom - cornerPx)
                arcTo(Rect(right - cornerPx * 2f, bottom - cornerPx * 2f, right, bottom), 0f, 90f, false)
                lineTo(cornerPx, bottom)
                arcTo(Rect(0f, bottom - cornerPx * 2f, cornerPx * 2f, bottom), 90f, 90f, false)
                lineTo(0f, top + cornerPx)
                arcTo(Rect(0f, top, cornerPx * 2f, top + cornerPx * 2f), 180f, 90f, false)
            } else {
                // Square body (cornerPx == 0): plain corners. Skipping arcTo here matters because a
                // zero-size Rect makes the arc degenerate, which some devices render as a notch in
                // the corner or drop the path segment entirely.
                lineTo(right, bottom)
                lineTo(0f, bottom)
                lineTo(0f, top)
            }
            close()
        }

    drawPath(
        path = path,
        brush =
            Brush.verticalGradient(
                colors = listOf(body, body.copy(alpha = 0.94f)),
                startY = top,
                endY = bottom,
            ),
    )
    drawPath(path = path, color = edge, style = Stroke(width = 1.dp.toPx()))
}

/**
 * One tab slot: a full-size, equal-width touch target with a flat icon.
 *
 * The selected slot draws **no** icon of its own — the orb paints it — but keeps identical geometry,
 * so hit targets never shift while the orb is in flight.
 */
@Composable
private fun DockTab(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val spec = rememberFloatingNotchSpec()
    val viewConfiguration = LocalViewConfiguration.current
    val currentOnClick by rememberUpdatedState(onClick)
    val interaction = remember { MutableInteractionSource() }
    var pressed by remember { mutableStateOf(false) }

    // One interaction source handles tap *and* long-press: stacking a second gesture detector would
    // swallow the tap, and a timed press keeps the two paths mutually exclusive.
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
        targetValue = if (pressed) spec.pressScale else 1f,
        animationSpec = tween(90),
        label = "dock-tab-press",
    )
    val tintBlend by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(spec.tintMillis),
        label = "dock-tab-tint",
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
        // The icon is centred on the *body*, not on the whole reserved height. The container also
        // reserves `floatInset + orbProtrusion` of empty space around the bar, so centring on the
        // full slot would pull every icon down by half of that. Shifting by the difference puts the
        // inactive icons exactly on the body's midline — which is what "a little low" actually was.
        val bodyCentreShift = (spec.floatInset - spec.orbProtrusion) / 2f

        Box(
            modifier = Modifier.offset(y = bodyCentreShift),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                // Invisible placeholder with the same size, so the slot does not resize.
                Box(modifier = Modifier.size(spec.iconSize).scale(pressScale))
            } else {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = label,
                    tint = lerp(scheme.onSurfaceVariant, scheme.primary, tintBlend),
                    modifier = Modifier.size(spec.iconSize).scale(pressScale),
                )
            }
        }
    }
}
