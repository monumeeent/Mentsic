# Orbit Music Theme Analysis

**Source:** `tests/orbit_music.apk` (v3.0.3)  
**Package:** `com.orbit.music`  
**Repository:** https://github.com/fahitakrim/my-artifacts

---

## Design Identity

Orbit Music is described as "a minimalist, privacy-first offline music player engineered with a **Nothing OS & Bauhaus-inspired visual identity**."

### Key Design Principles
- **Minimalism** — Clean, sparse UI with generous whitespace
- **Nothing OS influence** — Monochromatic base with bold accent colors
- **Bauhaus-inspired** — Geometric shapes, functional design, primary colors
- **Dot-Matrix typography** — Signature Nothing OS font style

---

## Color Palette

### Light Theme
| Token | Value | Usage |
|-------|-------|-------|
| `primary` | `#FF6750A4` | Primary actions, highlights |
| `onPrimary` | `#FFede9fe` | Text/icons on primary |
| `primaryContainer` | `#FF4f378b` | Primary containers |
| `onPrimaryContainer` | `#FFede9fe` | Text on primary containers |
| `secondary` | `#FF625B71` | Secondary actions |
| `onSecondary` | `#FFede9fe` | Text on secondary |
| `secondaryContainer` | `#FF7e748b` | Secondary containers |
| `surface` | `#FF0e0e12` | Surface backgrounds |
| `onSurface` | `#FFE6E1E5` | Text on surfaces |
| `background` | `#FF0e0e12` | App background |
| `onBackground` | `#FFE6E1E5` | Text on background |
| `error` | `#FFB00020` | Error states |
| `onError` | `#FFede9fe` | Text on error |

### Dark Theme
| Token | Value | Usage |
|-------|-------|-------|
| `primary` | `#FFB825FF` | Primary actions |
| `onPrimary` | `#FF18181C` | Text on primary |
| `primaryContainer` | `#FF4f378b` | Primary containers |
| `secondary` | `#FF8E7AC9` | Secondary actions |
| `surface` | `#FF0e0e12` | Surface backgrounds |
| `onSurface` | `#FFE6E1E5` | Text on surfaces |
| `background` | `#FF0e0e12` | App background |

### Accent Colors
| Token | Value | Usage |
|-------|-------|-------|
| `accent` | `#FFC8007C` | Accent highlights |
| `accentSecondary` | `#FFC800C5` | Secondary accent |
| `accentTertiary` | `#FFC800EF` | Tertiary accent |

---

## Typography

### Fonts
- **Inter** (`inter.ttf`, 876KB) — Body text, UI elements
- **Doto** (`doto.ttf`, 456KB) — Display/headline, dot-matrix style

### Type Scale
| Style | Font | Weight | Size |
|-------|------|--------|------|
| Hero Dot | Doto | Bold | Large display |
| Body | Inter | Regular | 16sp |
| Title | Inter | Medium | 20sp |
| Headline | Inter | SemiBold | 24sp |

---

## Layout & Components

### Spacing
- Base unit: 8dp
- Card corners: 12dp
- Screen padding: 16dp horizontal

### Components
- **Cards** — Rounded corners (12dp), subtle elevation
- **Buttons** — Pill-shaped (50% corner radius), primary accent fill
- **Icons** — 24dp, monochromatic with accent tint
- **Bars** — Thin accent-colored dividers
- **Waveform** — Audio-reactive visualizer with dot-matrix style

---

## Implementation Notes for Metrolist

### Color Mapping
| Orbit Token | Metrolist Equivalent |
|-------------|---------------------|
| `primary` | `colorScheme.primary` |
| `onPrimary` | `colorScheme.onPrimary` |
| `surface` | `colorScheme.surface` |
| `onSurface` | `colorScheme.onSurface` |
| `background` | `colorScheme.background` |
| `onBackground` | `colorScheme.onBackground` |
| `secondary` | `colorScheme.secondary` |
| `error` | `colorScheme.error` |

### Font Integration
- Add `doto.ttf` to `app/src/main/assets/fonts/`
- Add `inter.ttf` to `app/src/main/assets/fonts/`
- Update `typography` in MaterialTheme

### Key Differences
1. Orbit uses **pure black background** (`#FF0e0e12`) — Metrolist uses Material 3 defaults
2. Orbit has **bold purple/pink accent** — Metrolist uses neutral palette
3. Orbit uses **custom fonts** (Doto + Inter) — Metrolist uses system fonts
4. Orbit has **dot-matrix typography** — Metrolist uses standard Material typography