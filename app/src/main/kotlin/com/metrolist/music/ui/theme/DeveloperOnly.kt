/**
 * Mentsic Music — visual identity for developer-only surfaces.
 *
 * Anything that is only reachable while Developer Mode is on wears the same wash, so it is
 * obvious at a glance which parts of the UI regular users never see. Two forms are provided:
 *
 *  - [developerOnlyBrush] — a soft horizontal gradient for cards, rows and settings items.
 *  - [developerOnlyBorder] — a thin gradient outline for elements that need an edge.
 *
 * Both are built exclusively from `colorScheme` roles (tertiary → primary → secondary), which
 * keeps them inside the active theme: in the Shining theme the same wash turns gold, in a purple
 * theme it turns violet, and no raw colours leak into screens.
 *
 * Rulebook compliance: gradients are constructed from theme roles; no literal ARGB values here.
 */

package com.metrolist.music.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Opacity applied to the gradient over the normal surface, so text contrast is preserved. */
private const val DevWashAlpha = 0.22f

/**
 * Gradient wash marking a developer-only card.
 *
 * The wash is drawn *over* the item's normal container colour rather than replacing it, so text
 * and icons keep their usual contrast roles in every theme.
 */
@Composable
@ReadOnlyComposable
fun developerOnlyBrush(scheme: ColorScheme = MaterialTheme.colorScheme): Brush =
    Brush.horizontalGradient(
        colors =
            listOf(
                scheme.tertiaryContainer.copy(alpha = DevWashAlpha),
                scheme.primaryContainer.copy(alpha = DevWashAlpha),
                scheme.secondaryContainer.copy(alpha = DevWashAlpha),
            ),
    )

/** Softer variant for full-width rows, which are wider and need less tint than a small card. */
@Composable
@ReadOnlyComposable
fun developerOnlyRowBrush(scheme: ColorScheme = MaterialTheme.colorScheme): Brush =
    Brush.horizontalGradient(
        colors =
            listOf(
                scheme.tertiaryContainer.copy(alpha = DevWashAlpha * 0.7f),
                scheme.primaryContainer.copy(alpha = DevWashAlpha * 0.7f),
            ),
    )

/** Gradient outline for developer-only surfaces that already have their own fill. */
@Composable
@ReadOnlyComposable
fun developerOnlyBorder(): Brush =
    Brush.linearGradient(
        colors =
            listOf(
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.primary,
            ),
    )

/** Standard stroke width for [developerOnlyBorder]. */
val DeveloperOnlyBorderWidth = 1.dp

/**
 * Stable marker colour for "Developer" chips and labels.
 *
 * Uses `tertiary` because it is the one accent role no screen claims for regular actions.
 */
@Composable
@ReadOnlyComposable
fun developerOnlyAccent(): Color = MaterialTheme.colorScheme.tertiary

/** Background for the small "DEV" pill that labels a developer-only surface. */
@Composable
@ReadOnlyComposable
fun developerOnlyBadgeBackground(): Color =
    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
