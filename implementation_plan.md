# Implementation Plan — Production Homescreen Design Specification (`tests/DesignHS.md`)

## Goal
Create `tests/DesignHS.md` — a design specification document that records the current production Homescreen layout "same to same" (exactly as applied in the app). Documentation only: no production code, UI, or behaviour is changed.

## Source of truth
- `d:/Mentsic/app/src/main/kotlin/com/metrolist/music/ui/screens/HomeScreen.kt` (~2300 lines) — production `HomeScreen` Composable
- `d:/Mentsic/app/src/main/kotlin/com/metrolist/music/ui/screens/home/sections/*.kt` — section composables
- `d:/Mentsic/app/src/main/kotlin/com/metrolist/music/ui/screens/home/components/*.kt` — `HomeSectionHeader`, `HomePlaceholders`
- `d:/Mentsic/ui_rulebook.md` — design tokens (Spacing, GlassTokens, typography, interaction rules)
- Referenced types (read-only): `HomeDashboardState`, `Spacing`, `GlassTokens`/`GlassStyle`, `GlassCard`, `HomeSectionHeader`, `HomePlaceholders`, section composables

## Deliverables
| File | Action | Purpose |
|------|--------|---------|
| `d:/Mentsic/tests/DesignHS.md` | Create | Design spec documenting applied production Homescreen layout, section by section |
| `d:/Mentsic/implementation_plan.md` | Create | This plan |

No existing files are modified, moved, or deleted (the stale placeholder `tests/DesignHS.md` is replaced).

## Implementation order
1. [x] Re-read `HomeScreen.kt` in full (all 2300 lines) — section ordering, modifiers, heights, paddings, interaction logic
2. [x] Re-read each home section composable and component — Speed Dial pager, Quick Picks grid, Forgotten Favorites, Keep Listening, Daily Discover carousel, From The Community, Account Playlists, Similar Recommendations, Home Page sections, Mood & Genres
3. [x] Cross-check claims against sources for "same to same" accuracy (no invented measurements/colours/behaviour); confirm `NewHomeScreen.kt` is not referenced as design content
4. [ ] Draft `tests/DesignHS.md`: Overview, Source of Truth, Layout Structure, Per-Section Specs, Design Tokens, Interaction/Haptics, Placeholders/Loading, State Model, Traceability table, Accuracy notes
5. [ ] Review draft against source files; correct discrepancies
6. [ ] Save `tests/DesignHS.md` and hand off to the user

## Testing / validation
- Validate `tests/DesignHS.md` renders as markdown (headings, tables, fenced code blocks)
- Cross-check every layout claim against `HomeScreen.kt`, sections, components, `ui_rulebook.md`
- Confirm no `NewHomeScreen.kt` design content is referenced (redesign/demo is out of scope)

## Constraints (AGENTS.md)
- No local Gradle/Android build commands; verification via static analysis + CI on push
- Git Bash compatible terminal commands only
