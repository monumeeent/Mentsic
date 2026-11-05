# Implementation Plan

## Overview

Complete the UI/UX redesign of Mentsic, an Android music streaming app built with Jetpack Compose, Hilt DI, Room DB, and Media3 ExoPlayer. The goal is to finalize the design system started on the `redesign/mentsic` branch, implement the new home page view hinted at in `TempNotes.md`, clean up the settings screen, and ensure consistent application of design tokens across all UI components per `ui_rulebook.md`. This plan covers design token completion, component refactoring, screen-level redesigns, and settings cleanup.

## Context

The project is on version 13.7.0 (versionCode 153, Target SDK 36, Compile SDK 37, Min SDK 26). Key architectural characteristics:

- **Single-Activity Jetpack Compose** with Hilt DI
- **Flavor system**: FOSS (default), GMS, Izzy (F-Droid compliant)
- **Design tokens partially defined**: `GlassTokens.kt`, `IconSizes.kt`, `Alphas.kt`, `Theme.kt`, `OrbitTheme.kt`, `Font.kt`
- **Spacing.kt is incomplete**: Only defines `LetterSpacingWide`, missing the full 4dp/8dp/16dp/24dp/32dp grid from `ui_rulebook.md`
- **TempNotes.md** hints at: "A Different HomePage View" and removal of settings items (Changelog, Updater, Open Supported Links)

## Files

### New Files

| Path | Purpose |
|------|---------|
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\theme\Spacing.kt` (modify existing) | Complete spacing token object per ui_rulebook.md |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\screens\home\NewHomeScreen.kt` | New home page view (per TempNotes.md hint) |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\component\GlassCard.kt` | Reusable glassmorphic card component |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\component\GlassSurface.kt` | Glass surface modifier extension |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\utils\ScreenRoute.kt` | Type-safe navigation routes |

### Existing Files to Modify

| Path | Specific Changes |
|------|------------------|
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\theme\Spacing.kt` | Add `none`, `tiny`, `small`, `medium`, `large`, `huge` properties per ui_rulebook.md |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\theme\Theme.kt` | Ensure all color references use `MaterialTheme.colorScheme` tokens; remove hardcoded `Color(0xFF...)` |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\component\AppNavigation.kt` | Replace hardcoded `Color.Black`/`Color.White` with theme tokens; use `Spacing` tokens |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\component\AppNavigationRail.kt` | Same as above + use `Spacing` tokens for padding/margins |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\screens\HomeScreen.kt` | Refactor to use new design tokens; prepare for new home view |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\screens\settings\SettingsScreen.kt` | Remove Changelog, Updater, Open Supported Links entries (per TempNotes.md) |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\screens\settings\ThemeScreen.kt` | Replace hardcoded colors with theme tokens; use `Spacing` grid |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\screens\settings\AboutScreen.kt` | Remove Changelog entry |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\screens\settings\UpdaterSettings.kt` | Remove or deprecate (per TempNotes.md) |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\player\Player.kt` | Use `GlassTokens` for glass backgrounds; use `Spacing` tokens |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\player\MiniPlayer.kt` | Same as above |
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\theme\GlassTokens.kt` | Add usage examples / helper composables |
| `d:\Mentsic\app\src\main/res/values/strings.xml` | Remove strings for removed settings items |

### Files to Delete

| Path | Reason |
|------|--------|
| `d:\Mentsic\app\src\main\kotlin\com\metrolist\music\ui\screens\settings\UpdaterSettings.kt` | Per TempNotes.md: remove Updater from settings |

## Functions

### New Functions

| Name | Signature | File | Purpose |
|------|-----------|------|---------|
| `Modifier.glassCard()` | `fun Modifier.glassCard(shape: Shape = RoundedCornerShape(16.dp), style: GlassStyle = GlassTokens.Subtle): Modifier` | `GlassCard.kt` | Apply glassmorphic card styling |
| `NewHomeScreen()` | `@Composable fun NewHomeScreen(modifier: Modifier = Modifier)` | `NewHomeScreen.kt` | New home page view |
| `ScreenRoute.fromString()` | `fun String.toScreenRoute(): ScreenRoute?` | `ScreenRoute.kt` | Parse string route to type-safe enum |

### Modified Functions

| Name | File | Required Changes |
|------|------|------------------|
| `MetrolistTheme()` | `Theme.kt` | Ensure `pureBlack` and `enableOrbitTheme` cascade through all surface tokens; remove any remaining hardcoded colors |
| `glassmorphicSurface()` | `GlassTokens.kt` | Add optional `shape` parameter defaulting to `RoundedCornerShape(16.dp)` |
| `AppNavigationRail()` | `AppNavigation.kt` | Replace `Color.Black`/`Color.White.copy(alpha = ...)` with `MaterialTheme.colorScheme` tokens; use `Spacing.tiny/small/medium` |
| `AppNavigation()` | `AppNavigation.kt` | Same token replacement; use `Spacing` grid for layout |
| `ThemeScreen()` | `ThemeScreen.kt` | Replace hardcoded `Color(0xFF...)` values with `MaterialTheme.colorScheme` |
| `HomeScreen()` | `HomeScreen.kt` | Refactor grid items to use `Spacing` tokens; ensure consistent ripple clipping |

### Removed Functions

| Name | File | Migration Strategy |
|------|------|-------------------|
| None planned | N/A | N/A |

## Classes

### New Classes

| Name | File | Key Methods | Inheritance |
|------|------|-------------|-------------|
| `Spacing` | `Spacing.kt` | `none`, `tiny`, `small`, `medium`, `large`, `huge` (val properties) | `object` |
| `NewHomeScreen` | `NewHomeScreen.kt` | Composable function | `@Composable` |
| `GlassCard` | `GlassCard.kt` | Composable function | `@Composable` |
| `ScreenRoute` | `ScreenRoute.kt` | `route: String`, `toScreenRoute()` | `sealed class` |

### Modified Classes

| Name | File | Specific Modifications |
|------|------|----------------------|
| `Screens` | `Screens.kt` | Add `ScreenRoute` mapping; ensure route constants match new type-safe routes |
| `AppNavigationRail` | `AppNavigation.kt` | Replace hardcoded colors with theme tokens; use `Spacing` tokens |
| `ThemeScreen` | `ThemeScreen.kt` | Remove hardcoded colors; adopt `Spacing` grid |
| `HomeScreen` | `HomeScreen.kt` | Refactor to use design tokens consistently |

### Removed Classes

| Name | File | Replacement Strategy |
|------|------|---------------------|
| None planned | N/A | N/A |

## Dependencies

No new dependencies required. All UI upgrades use existing libraries:

- `androidx.compose.material3:material3` (1.5.0-alpha28)
- `com.materialkolor:material-kolor` (5.0.1) - for dynamic color
- `androidx.compose.animation:animation` - for motion easing

## Testing

### Test Requirements

1. **Unit tests for Spacing tokens**: Verify all spacing values match ui_rulebook.md (0, 4, 8, 16, 24, 32 dp)
2. **Unit tests for GlassTokens**: Verify alpha values, border widths, blur radii match specification
3. **Compose UI tests for NewHomeScreen**: Verify layout renders without crashes, navigation works
4. **Compose UI tests for ThemeScreen**: Verify settings items removed per TempNotes.md
5. **Snapshot tests** (if available): Verify dark theme and pure black theme rendering

### Existing Test Modifications

| Test File | Modification |
|-----------|--------------|
| `d:\Mentsic\innertube\src\test\...` | No changes needed (innertube module unaffected) |
| `d:\Mentsic\app\src\test\...` | Add tests for new `Spacing` object, `GlassCard`, `NewHomeScreen` |

### Validation Strategy

- Static analysis: Verify no hardcoded `Color(0xFF...)`, `Color.White`, `Color.Black` in screen/component Composables
- Design token audit: Verify all padding/margins use `Spacing` grid (4dp/8dp/16dp/24dp/32dp)
- Pure black compliance: Verify `surface`, `background`, `surfaceContainerLowest` through `surfaceContainerHighest` all map to black in pure black mode
- Build verification: User pushes to GitHub to trigger CI build (per AGENTS.md rule 4)

## Implementation Order

1. **Complete Spacing tokens** (`Spacing.kt`): Add missing `none`/`tiny`/`small`/`medium`/`large`/`huge` properties per ui_rulebook.md
2. **Create GlassCard component** (`GlassCard.kt`): Reusable glassmorphic card using `GlassTokens`
3. **Create ScreenRoute type-safe navigation** (`ScreenRoute.kt`): Replace raw string routes with sealed class
4. **Refactor AppNavigation/AppNavigationRail** (`AppNavigation.kt`): Replace hardcoded colors with theme tokens; use `Spacing` grid
5. **Refactor ThemeScreen** (`ThemeScreen.kt`): Remove hardcoded colors; adopt `Spacing` grid
6. **Create NewHomeScreen** (`NewHomeScreen.kt`): Implement new home page view per TempNotes.md hint
7. **Refactor HomeScreen** (`HomeScreen.kt`): Apply design tokens consistently; prepare for new home view integration
8. **Settings cleanup** (`SettingsScreen.kt`, `AboutScreen.kt`, `UpdaterSettings.kt`): Remove Changelog, Updater, Open Supported Links entries per TempNotes.md
9. **Refactor Player/MiniPlayer** (`Player.kt`, `MiniPlayer.kt`): Use `GlassTokens` for glass backgrounds; use `Spacing` tokens
10. **Design token audit**: Scan all UI components for hardcoded colors and non-standard spacing; fix violations
11. **Update strings.xml**: Remove strings for removed settings items
12. **Write tests**: Unit tests for spacing/glass tokens; Compose UI tests for new screens

## Edge Cases & Considerations

- **Pure black OLED compliance**: All surface containers must cascade to `#000000` in pure black mode per ui_rulebook.md section 1
- **Flavor differences**: Settings cleanup (Updater removal) applies to Izzy flavor; FOSS/GMS may retain Updater
- **Backward navigation**: New home screen must integrate with existing `NavController` and `Screens` sealed class
- **Theme persistence**: Dark theme / pure black / orbit theme preferences stored in DataStore must continue to work
- **Tablet/landscape**: Navigation rail vs bottom bar logic must remain functional after token refactoring