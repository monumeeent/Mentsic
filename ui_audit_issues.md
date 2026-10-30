# Mentsic UI Audit Issues

> Scope: `app/src/main/kotlin/com/metrolist/music/ui` (theme + menu + components), emphasis on `ui/theme` and `ui/menu`.

---

## A. Duplicate Token Declarations

1. Duplicate font family token: `ui/theme/bbh_bartle.kt` and `ui/theme/Font.kt` both define `bbh_bartle_regular` (400) as `bbh_bartle` and `bbhBartle`. Consolidate into `Font.kt`; delete `bbh_bartle.kt`.
2. Duplicate casing variants `bbhBartle`/`bbh_bartle`; standardize on camelCase.
3. `getOrbitColorResource` (`OrbitTheme.kt`) appears unused in scoped files; verify or remove.
4. `applyOrbitTheme` returns a fully static scheme, discarding dynamic seed; document intended precedence.

## B. Hardcoded Styling Violations

### Typography
5. `fontSize = 18.sp` overrides in `AlbumMenu.kt`, `YouTubeAlbumMenu.kt`, `SongMenu.kt`, `YouTubeSongMenu.kt` instead of `MaterialTheme.typography.titleMedium`.
6. `fontSize = TextUnit(12F, TextUnitType.Sp)` in `AddToPlaylistDialogOnline.kt` instead of `12.sp` / `labelSmall`.
7. `letterSpacing = 6.sp` in `PlayerMenu.kt` (room code) without token.
8. `FontFamily.Monospace` in `CsvColumnMappingDialog.kt` instead of a theme font token.
9. `FontWeight` overrides applied on top of typography without a defined weight scale.

### Colors
10. `Color.Transparent` used as default row background (acceptable but untokenized).
11. `Color.Black` terminal gradient stop in `PlayerColorExtractor.kt` (should be a token).
12. `Color.White.copy(alpha = 0.4f)` in `PlayerSliderColors.kt` untokenized.
13. Alpha literals (`0.1f`, `0.15f`, `0.3f`, `0.4f`, `0.45f`, `0.5f`) scattered; no alpha token scale.
14. Ensure no other hardcoded violet besides `DefaultThemeColor`.

### Strings
15. Unlocalized hardcoded strings: `"Preview"` (`CsvColumnMappingDialog`), `"Reconnect"` (`PlayerMenu`), `"Selection"` queue titles (`SelectionSongsMenu`, `SelectionMediaMetadataMenu`), `"ABCD1234"` placeholder (`PlayerMenu`).
16. Hardcoded `"auto_playlist"` id and `"Selection"` titles in logic/UI.

### Dimensions
17. Raw `dp` literals throughout menus; no spacing token scale.
18. `RoundedCornerShape(50)` magic pill radius repeated.
---

## C. Architectural Errors

20. Non-reactive remembered state: `allInLibrary`/`allLiked` in `SelectionSongsMenu.kt` and `YouTubeSelectionSongMenu.kt` computed once via `remember { mutableStateOf(...) }` without keys → stale when selection changes.
21. Mutable list in state: `notAddedList` uses `remember { mutableStateOf(mutableListOf<Song>()) }` (mutable collection in state) — not observable; lint-suppressed via `@SuppressLint("MutableCollectionMutableState")`.
22. Multiple coroutine scopes: `AlbumMenu.kt` declares two `rememberCoroutineScope()` (`scope` and `coroutineScope`).
23. Direct data-layer access in UI: Composables call `LocalDatabase`, `LocalSyncUtils`, `LocalPlayerConnection` directly; tight coupling.
24. `isGuest` logic duplicated across ~20 menu files instead of a shared helper/composition local.
25. `isPortrait` computed but unused in several menus.
26. Malformed control flow in `AddToPlaylistDialogOnline.kt` `LaunchedEffect(isVisible, playlists)`: duplicated `playlistsContainingSong = emptySet()` and misplaced braces; logic likely broken.
27. `remember { mutableStateOf(emptyList()) }` for `songs` then collected into var in `AlbumMenu`/`PlaylistMenu`.
28. `LaunchedEffect(Unit)` collecting infinite flows (downloads) without structured cancellation; may leak.
29. `produceState` for podcast and `remember` without keys in `SongMenu.kt`.
30. Empty labels in `animateFloatAsState(..., label = "")` reduce debuggability.
---

## D. Deprecations / Legacy

31. `bbh_bartle.kt` legacy file duplicates `Font.kt` (deprecated in favor of camelCase tokens).
32. `OrbitTheme.kt` static palette may be deprecated in favor of dynamic Material You; clarify usage.
33. `getOrbitColorResource` likely deprecated/unused.
34. `FontFamily.Monospace` usage in CSV preview (consider theme-provided monospace token).
35. `ExperimentalMaterial3Api` / `ExperimentalMaterial3ExpressiveApi` opt-ins scattered; track maturity.

## E. Potential Bugs

36. `SelectionSongsMenu` `allInLibrary`/`allLiked` not recomputed → wrong button labels.
37. `YouTubeSelectionSongMenu` `allLiked` checks `metadata.liked` from `SongItem` conversion; may be inaccurate.
38. `AddToPlaylistDialogOnline` duplicate dialog state machine may never trigger (`showDuplicateDialog` never set true in that file).
39. `CsvColumnMappingDialog` preview loops `previewRows.take(5)` but nests columns incorrectly (possible layout bug).
40. `PlayerMenu` `Text("Reconnect")` untranslated.
41. `LoadingScreen` uses `LinearWavyProgressIndicator` (Expressive API) — verify availability on target SDK.

## F. Recommended Refactors (Priority)

1. Remove `bbh_bartle.kt`; keep `bbhBartle` in `Font.kt`.
2. Introduce spacing/icon-size/alpha token objects in `ui/theme` (e.g., `Spacing`, `IconSizes`, `Alphas`).
3. Replace raw `fontSize = 18.sp` with `MaterialTheme.typography.titleMedium`.
4. Replace hardcoded strings with `stringResource`.
5. Fix `AddToPlaylistDialogOnline` LaunchedEffect logic.
6. Convert `allInLibrary`/`allLiked` to `derivedStateOf`.
7. Centralize `isGuest` and `isPortrait` helpers.
8. Remove duplicate `rememberCoroutineScope()` in `AlbumMenu`.
9. Audit `ExperimentalMaterial3*` opt-ins for stable replacements.
10. Add tablet/desktop breakpoints if needed.
19. Icon sizes as raw `dp` without token scale.