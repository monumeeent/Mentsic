# Mentsic Design System & UI Rulebook

## 1. Core Principles
- **No Hardcoded Colors**: Always reference tokens from `MaterialTheme.colorScheme` (or `LocalContentColor`). Never use raw `Color(0xFF...)`, `Color.White`, or `Color.Black` directly inside screen or component Composables.
- **No Arbitrary Font Sizes**: Always use `MaterialTheme.typography` styles (`displayLarge` down to `labelSmall`). Never declare raw `fontSize = 14.sp` in new UI components.
- **Consistent Spacing Grid**: All padding, margins, and gaps must follow standard 4dp/8dp intervals defined in `Spacing`:
  - `Spacing.none = 0.dp`
  - `Spacing.tiny = 4.dp`
  - `Spacing.small = 8.dp`
  - `Spacing.medium = 16.dp`
  - `Spacing.large = 24.dp`
  - `Spacing.huge = 32.dp`
- **OLED Pure Black Compliance**: The pure black mode must cascade across all container levels (`surface`, `background`, `surfaceContainerLowest`, `surfaceContainerLow`, `surfaceContainer`, `surfaceContainerHigh`, `surfaceDim`). Do not write ad-hoc `if (pureBlack) Color.Black else ...` checks when proper token mapping can resolve the surface color automatically.

---

## 2. Color Scheme Tokens & Roles
| Role | Purpose | Dark Theme Standard | OLED Pure Black Standard |
|---|---|---|---|
| `colorScheme.surface` | Base app background | `#121212` / Tonal dark | `#000000` |
| `colorScheme.background` | Scaffolding background | `#121212` / Tonal dark | `#000000` |
| `colorScheme.surfaceContainerLowest` | Recessed areas / background wells | `#0E0E0E` | `#000000` |
| `colorScheme.surfaceContainerLow` | Subtle card background | `#171717` | `#080808` |
| `colorScheme.surfaceContainer` | Standard cards & sheet surfaces | `#1D1D1D` | `#101010` (or translucent glass) |
| `colorScheme.surfaceContainerHigh` | Elevated dialogs & top bars | `#242424` | `#161616` |
| `colorScheme.surfaceContainerHighest` | Floating pills & active controls | `#2E2E2E` | `#1E1E1E` |
| `colorScheme.primary` | Key brand accent / active highlights | Tonal spot derived | Dynamic accent |
| `colorScheme.onSurface` | Primary text & high-emphasis icons | High contrast white (`0.95`) | Pure white (`#FFFFFF`) |
| `colorScheme.onSurfaceVariant` | Secondary text & subtitles | Muted text (`0.70`) | Muted text (`0.70`) |

---

## 3. Glassmorphism Tokens (`GlassTokens`)
Used for floating player capsules, contextual menus, and ambient overlays:
- **`GlassTokens.Subtle`**:
  - `surfaceAlpha`: `0.65f`
  - `borderAlpha`: `0.12f`
  - `borderWidth`: `0.75.dp`
  - `blurRadius`: `12.dp`
- **`GlassTokens.Elevated`**:
  - `surfaceAlpha`: `0.78f`
  - `borderAlpha`: `0.20f`
  - `borderWidth`: `1.dp`
  - `blurRadius`: `24.dp`
- **`GlassTokens.PureBlackGlass`**:
  - `surfaceAlpha`: `0.85f` (Pitch black with subtle border luminance)
  - `borderAlpha`: `0.08f`
  - `borderWidth`: `0.5.dp`
  - `blurRadius`: `8.dp`

---

## 4. Typography Scale Hierarchy
- `typography.headlineLarge` / `headlineMedium`: Section headers ("Quick Picks", "Forgotten Favorites").
- `typography.titleLarge` / `titleMedium`: Primary song titles, artist page headings.
- `typography.bodyLarge` / `bodyMedium`: Secondary metadata (artist name, album, view counts).
- `typography.labelMedium` / `labelSmall`: Timers, track counters, badges, and chips.

---

## 5. Micro-Interactions & Tactile Feedback
- **Bounded Ripples**: All clickable cards must use `.clip(shape)` before `.clickable` to avoid rectangular spill on rounded elements.
- **Haptic Anchors**:
  - Song selection & playback toggle: `HapticFeedbackType.SegmentTick` / light tick.
  - Long-press contextual menus: `HapticFeedbackType.LongPress`.
  - Swipe-to-skip threshold: subtle haptic confirmation.
- **Motion Easing**:
  - All shared axis & sheet transitions must use `CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)` (Material 3 Expressive easing) or `spring(dampingRatio = Spring.DampingRatioLowBouncy)`.

