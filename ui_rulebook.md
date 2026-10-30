# Mentsic UI Rulebook & Design System

> Scope: Comprehensive audit of `app/src/main/kotlin/com/metrolist/music/ui` (theme + menu packages) and related UI components.
> Project: Mentsic — Android music player (Kotlin / Jetpack Compose / Material3).

---

## 1. Design Tokens & Color Palette

### 1.1 Theme Engine

Two mutually exclusive theme systems selectable at runtime via `MetrolistTheme(...)`:

- **Default (Dynamic / Material You)**: seeded color scheme via `com.materialkolor.rememberDynamicColorScheme` with `ColorSpec.SpecVersion.SPEC_2025` + `PaletteStyle.TonalSpot`. Seed token `DefaultThemeColor = Color(0xFF8B5CF6)` (violet). Supports `pureBlack` override (forces `surface`/`background` to `Color.Black`).
- **Orbit (Static)**: full static `ColorScheme` from `OrbitColors` + `orbitTypography`, enabled via `enableOrbitTheme = true`.

Dark/light resolved by `isSystemInDarkTheme()` (default param).

### 1.2 Orbit Static Palette (Hex)

Light (`OrbitColors.*Light`): primary `#6750A4`, onPrimary `#FFFFFF`, primaryContainer `#4F378B`, onPrimaryContainer `#EDE9FE`, secondary `#625B71`, secondaryContainer `#7E748B`, surface `#F8F7FA`, onSurface `#1C1B1E`, surfaceVariant `#E6E1E5`, onSurfaceVariant `#49454F`, background `#FFFFFF`, onBackground `#1C1B1E`, accent/tertiary `#C8007C`, onAccent `#FFFFFF`, error `#B00020`, onError `#FFFFFF`, outline `#79747E`, outlineVariant `#CAC4D0`, inversePrimary `#B825FF`.

Dark (`OrbitColors.*Dark`): primary `#B825FF`, onPrimary `#18181C`, primaryContainer `#4F378B`, onPrimaryContainer `#EDE9FE`, secondary `#8E7AC9`, secondaryContainer `#4F378B`, surface `#121212`, onSurface `#E6E1E5`, surfaceVariant `#2A2A2E`, onSurfaceVariant `#CAC4D0`, background `#000000`, onBackground `#E6E1E5`, accent/tertiary `#C8007C`, onAccent `#18181C`, error `#B00020`, onError `#18181C`, outline `#79747E`, outlineVariant `#49454F`, inversePrimary `#6750A4`.

### 1.3 Runtime Color Usage Rules (ui/menu)

Components consume colors exclusively via `MaterialTheme.colorScheme.*`:
- Brand actions: `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`.
- Secondary actions: `secondary`, `onSecondary`, `secondaryContainer`, `onSecondaryContainer`.
- Surfaces: `surface`, `surfaceVariant`, `surfaceContainerHigh`, `background`.
- Text: `onSurface`, `onSurfaceVariant`, `onBackground`.
- Status: `error`, `onError`, `errorContainer`, `onErrorContainer`.
- Accent: `tertiary`, `tertiaryContainer`, `onTertiary`, `onTertiaryContainer`.
- Borders: `outline`, `outlineVariant`.

Translucent overlays use `.copy(alpha=…)`:
- `primaryContainer.copy(alpha=0.45f)` (playlist row highlight)
- `primary.copy(alpha=0.1f/0.15f)` (status surfaces)
- `secondary.copy(alpha=0.15f)` (join request cards)
- `error.copy(alpha=0.1f)` (error banners)
- `surfaceVariant.copy(alpha=0.5f)` (join/create card)
- `Color.Transparent` (default row bg)
- `Color.White.copy(alpha=0.4f)` (blur/gradient slider track)
- `Color.Black` (terminal gradient stop)

### 1.4 Glassmorphism / HUD

- Translucent white `Color.White.copy(alpha=0.4f)` inactive slider track on `BLUR`/`GRADIENT` player backgrounds.
- Status chips use `primary/secondary/error.copy(alpha=0.1–0.15)` over `surfaceVariant`.
- No blur radius/shadow tokens present (gap).

### 1.5 Dynamic Player Colors

- Album art → 3-stop gradient via `PlayerColorExtractor.extractGradientColors(palette, fallback)` = `[primaryColor, primaryColor*0.6, Color.Black]`.
- Palette generated with `maximumColorCount(8)`, scored via `com.materialkolor.score.Score`.
- Fallback seed `DefaultThemeColor` (argb).
- Slider colors centralized in `PlayerSliderColors.getSliderColors(activeColor, playerBackground, useDarkTheme)`.
---

## 2. Typography & Text Hierarchy

### 2.1 Font Families (theme/Font.kt)

| Token | Family | Weights |
|---|---|---|
| `bbhBartle` (legacy alias `bbh_bartle`) | `bbh_bartle_regular` | 400 |
| `orbitInter` | Inter | 400, 500, 600 |
| `orbitDoto` | Doto | 400, 700 |

### 2.2 Orbit Typography Scale (sp)

`orbitTypography` (enabled only when `enableOrbitTheme=true`):

| Style | Family | Weight | Size |
|---|---|---|---|
| displayLarge | orbitDoto | 700 | 57 |
| displayMedium | orbitDoto | 400 | 45 |
| displaySmall | orbitDoto | 400 | 36 |
| headlineLarge | orbitDoto | 700 | 32 |
| headlineMedium | orbitDoto | 400 | 28 |
| headlineSmall | orbitDoto | 400 | 24 |
| titleLarge | orbitDoto | 700 | 22 |
| titleMedium | orbitDoto | 400 | 18 |
| titleSmall | orbitDoto | 400 | 14 |
| bodyLarge | orbitInter | 400 | 16 |
| bodyMedium | orbitInter | 400 | 14 |
| bodySmall | orbitInter | 400 | 12 |
| labelLarge | orbitInter | 500 | 14 |
| labelMedium | orbitInter | 500 | 12 |
| labelSmall | orbitInter | 500 | 11 |

Default (non-Orbit) uses `MaterialTheme.typography` (Material3 defaults).

### 2.3 Menu Typography Usage

Menu screens use `MaterialTheme.typography.*`:
- Headers/titles: `headlineSmall`, `titleLarge`, `titleMedium`, `titleSmall`.
- Body: `bodyLarge`, `bodyMedium`, `bodySmall`.
- Captions/labels: `labelLarge`, `labelMedium`, `labelSmall`.

Observed overrides (potential violations):
- `fontSize = 18.sp` (artist/album row titles)
- `fontSize = TextUnit(12F, TextUnitType.Sp)` (note text)
- `letterSpacing = 6.sp` (room code)
- `FontFamily.Monospace` (CSV preview cells)
- `FontWeight.Bold/SemiBold/Medium` overrides on top of typography.

No explicit line-height tokens defined (rely on Material3 defaults).
---

## 3. Component Architecture & Modularity

### 3.1 File/Package Structure

- Theme tokens: `com.metrolist.music.ui.theme` (`Theme.kt`, `OrbitTheme.kt`, `Font.kt`, `bbh_bartle.kt`, `PlayerColorExtractor.kt`, `PlayerSliderColors.kt`).
- Menus/dialogs: `com.metrolist.music.ui.menu` (20 files: `*Menu.kt`, `*Dialog.kt`, `LoadingScreen.kt`).
- Reusable components: `com.metrolist.music.ui.component` (`Material3MenuGroup`, `Material3MenuItemData`, `NewAction`, `NewActionGrid`, `ListDialog`, `DefaultDialog`, `TextFieldDialog`, `ListItem`, `SongListItem`, `AlbumListItem`, `PlaylistListItem`, `YouTubeListItem`, `MediaMetadataListItem`, `VolumeSlider`, `BottomSheetState`, `CreatePlaylistDialog`, `ExportDialog`, `SortHeader`).
- Utilities: `ui/utils` (`ShowMediaInfo`, `resize`).

### 3.2 Modularity Rules (observed)

- Single-feature composables per file (e.g., `SongMenu.kt` contains `SongMenu` plus nested dialogs in `PlayerMenu.kt`).
- Menus are Composables receiving entity + `onDismiss`; state via `remember`/`rememberSaveable`/`collectAsStateWithLifecycle`.
- Shared menu primitives: `Material3MenuGroup(items)`, `NewActionGrid(actions, columns)`, `ListDialog`, `DefaultDialog`, `TextFieldDialog`.
- Dialogs wrap content in `ListDialog`/`DefaultDialog`; full-screen dialogs use `Dialog` + `DialogProperties(usePlatformDefaultWidth=false)`.
- `Material3MenuItemData` supports `title`, `description`, `icon`, `onClick`, `trailingContent`, `cardColors`.

### 3.3 Core Reusable Components & Variants

- **NewAction / NewActionGrid**: quick action tiles (play, shuffle, share, add, etc.) with icon (28–32dp) + label; grid columns adapt to guest mode (1–4 columns).
- **Material3MenuGroup / Material3MenuItemData**: grouped menu rows with icon, title, optional description, trailing content, optional card color override.
- **ListDialog / DefaultDialog / TextFieldDialog**: standard dialog containers.
- **AddToPlaylistDialog / AddToPlaylistDialogOnline**: playlist picker with sort chips, create-playlist button, duplicate detection.
- **ExportDialog**: CSV/M3U radio selection with share/save actions.
- **LoadingScreen**: wavy progress dialog with cancel.
- **VolumeSlider**, **BottomSheetState**.

### 3.4 Zero-Inline-Styling Policy

- Mostly adhered: colors/typography via `MaterialTheme`; spacing still uses raw `dp` literals (no spacing token scale in scoped files).
---

## 4. Layout, Spacing & Responsiveness

### 4.1 Spacing Scale (observed dp)

Common values: `4, 6, 8, 12, 16, 18, 20, 24, 28, 32, 36, 40, 48, 52, 72, 80`.
- Section padding: `padding(horizontal = 24.dp, vertical = 16.dp)` (headers), `padding(horizontal = 16.dp, vertical = 8.dp)` (buttons).
- Item spacing: `Arrangement.spacedBy(6.dp)` (chips), `8.dp`, `12.dp`, `16.dp`.
- Icon sizes: `18, 20, 24, 28, 32, 40, 48, 52`.
- Thumbnail: `ListThumbnailSize` (constant).
- Corner radii: `RoundedCornerShape(4, 8, 12, 16, 28, 50)`.

### 4.2 List/Scroll Containers

- `LazyColumn` with `contentPadding = PaddingValues(start=0, top=0, end=0, bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding())`.
- Bottom sheet collapse via `playerBottomSheetState.collapseSoft()`.

### 4.3 Responsiveness / Breakpoints

- Orientation: `LocalConfiguration.current.orientation == ORIENTATION_PORTRAIT` drives `isPortrait`; grid columns adjust (`columns = if (isGuest) 1/2 else 3/4`).
- Guest mode: `listenTogetherManager?.isInRoom == true && !isHost` reduces columns and hides host-only actions.
- No explicit tablet/desktop breakpoints in scoped files.

### 4.4 Grid/Flex Patterns

- `NewActionGrid(actions, columns)` (LazyGrid-like).
- `FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp))` for filter chips.
- `Row`/`Column` with `Arrangement.SpaceBetween/Center/spacedBy`.

---

## 5. Icons, Animations & Assets

### 5.1 Icons

- Source: Android drawable resources `R.drawable.*` rendered via `painterResource(...)` + `Icon(...)`.
- No external icon library (e.g., Remix) in scoped files.
- Common icons: play, shuffle, share, playlist_add, favorite/favorite_border, download/offline, delete, edit, search, sync, artist, album, radio, queue_music, etc.
- Icon tint: `MaterialTheme.colorScheme.onSurfaceVariant` / `onBackground` / `error` / `LocalContentColor.current`.

### 5.2 Animations

- `animateColorAsState` (row highlight, sort arrow bg/fg).
- `animateFloatAsState` with `spring(...)` / `tween(...)` (button scale, refetch rotation).
- `animateContentSize()` for expanding lyrics.
- `graphicsLayer { scaleX/scaleY/rotationZ }`.
- `LinearProgressIndicator`, `CircularProgressIndicator`, `LinearWavyProgressIndicator` (Material3 Expressive).
- Spring specs: `Spring.DampingRatioMediumBouncy`, `Spring.StiffnessMedium/MediumLow`.

### 5.3 Assets

- Fonts: `R.font.bbh_bartle_regular`, `R.font.inter`, `R.font.doto`.
- Drawables: `R.drawable.*`.

---

## 6. Theme Entry Points (API)

```kotlin
MetrolistTheme(darkTheme, pureBlack, themeColor, enableOrbitTheme, content)
OrbitTheme(darkTheme, content)
ColorScheme.applyOrbitTheme(isDark)
ColorScheme.pureBlack(apply)
Bitmap.extractThemeColor(): Color
PlayerColorExtractor.extractGradientColors(palette, fallback): List<Color>
PlayerSliderColors.getSliderColors(activeColor, playerBackground, useDarkTheme): SliderColors
ColorSaver: Saver<Color, Int>
```

---

## 7. Known Gaps (not in scoped files)

- No spacing token scale; raw `dp` literals throughout.
- No shadow/elevation/blur radius tokens.
- No dedicated icon-size token scale.
- No tablet/desktop breakpoint definitions.
- Component prop documentation not present in scoped files.
"""