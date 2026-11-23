# Blueprint — Listening Together Tab, Stage Layout

Status: implemented as a developer experiment (`ListenTogetherAltUiKey`, route to toggle:
*Settings → Appearance → Tactical Hub → Stage layout*).
Source of truth:

| Layer | File |
|---|---|
| Screen + data wiring | `app/src/main/kotlin/com/metrolist/music/ui/screens/ListenTogetherScreen.kt` (`ListenTogetherScreen`, `ListenTogetherStageLayout`) |
| Stage building blocks (canvas) | `app/src/main/kotlin/com/metrolist/music/ui/component/ListenTogetherStage.kt` (`StagePulse`, `ListenerConstellation`, `BroadcastRoomCode`) |
| Session state | `com.metrolist.music.listentogether.ListenTogetherManager` → `connectionState`, `roomState`, `pendingJoinRequests`, `pendingSuggestions`, `events` |
| Navigation entry | `NavigationBuilder.kt` → `Screens.ListenTogether.route` (bottom tab) and `listen_together_from_topbar` (top-bar entry) |

---

## 1. Design intent

The default screen is a **stack of equal cards** — connection, room, listeners, requests, suggestions,
leave, settings. Every item has the same visual weight, so nothing tells the eye where to look first.

The stage layout replaces that hierarchy with three zones:

1. **Stage** — one hero surface. It answers *"am I connected and in a room?"* before the user reads
   a word, through motion (a breathing orb) rather than a status string.
2. **Cast** — the people in the room, drawn as a constellation instead of a vertical list, so the
   room feels occupied and live.
3. **Console** — everything transactional (room code, join/create fields, settings, leave).

---

## 2. Screen anatomy (top → bottom)

```
┌───────────────────────────────────────────────────────────┐
│  (16dp)                                                   │
│               ╭──── breathing rings ────╮                 │
│            (   ●  StagePulse, 200dp tall, core 16%   )    │
│               ╰──────────────────────────╯                │
│                     LISTEN TOGETHER                       │  labelMedium / onSurfaceVariant
│                    In Room · 4 listeners                  │  headlineSmall / SemiBold / onSurface
│                                                           │
│   [ CONNECT / DISCONNECT / RECONNECT ]   ← primary action │  Filled Button, 56dp, full width
│                                                           │
│   ┌── CONNECTION ──┐  ┌──── ROOM ────┐                    │  StageStatTile × 2 (weight 1f)
│   │   Connected    │  │    Active    │                    │
│   └────────────────┘  └──────────────┘                    │
│                                                           │
│        ── only when inside a room ──                      │
│   ╔═══════════════════════════════════════════════╗       │
│   ║  ROOM CODE                                    ║       │  BroadcastRoomCode
│   ║      A 7 K 2 Q 9        (scanline sweeping)   ║       │  headlineMedium / Monospace / ls 10sp
│   ╚═══════════════════════════════════════════════╝       │
│                                                           │
│        ╭──── ListenerConstellation, 160dp ────╮           │  160dp canvas, ring r = 34% min
│        │      ○   ← host (tertiary)           │           │  hollow node  = disconnected
│        │        ●  ●  ← connected (primary)   │           │  solid + halo = connected
│        ╰──────────────────────────────────────╯           │
│                                                           │
│   ┌ Pending requests · n ┐   ┌ Suggestions · n ┐          │  host only, when non-empty
│                                                           │
│   [ LEAVE ROOM ]  ← error container                       │  only inside a room
│                                                           │
│        ── only when not inside a room ──                  │
│   ╭── Identity & room card (24dp radius) ──────────╮      │
│   │  Username field                                │      │  OutlinedTextField
│   │  Room code field                               │      │  OutlinedTextField
│   │  [ JOIN ROOM ]                                 │      │  Filled Button
│   │  [ CREATE ROOM ]   ← developer mode only       │      │  Outlined Button
│   ╰────────────────────────────────────────────────╯      │
│                                                           │
│   [ ⚙ Settings ]                                          │  Outlined, → ListenTogetherSettings
│  (bottom padding = inset + 16dp + AppBarHeight)           │
└───────────────────────────────────────────────────────────┘
```

The body is a single `Column` with `verticalScroll`, `imePadding()` (keyboard-aware, because the
join/create fields live inside it) and `Arrangement.spacedBy(16dp)`. It is scroll-driven, not
paged: there is exactly one "page", and the hero must not scroll away from the orb mid-session.

---

## 3. Components

### 3.1 `StagePulse` — the breathing core

| Property | Value |
|---|---|
| Canvas height | 200dp, full width |
| Core radius | `minDimension * 0.16`, radial gradient `primary @0.9 → @0.12` at `1.7 × core` |
| Rings | 3, each offset by `1/(rings+1)` of the cycle |
| Period | `2600 − 1600 × energy`, clamped to `900..2600ms` |
| Ring alpha | `(1 − phase) × (0.18 + 0.22 × energy)`, clamped to `0..0.5` |

`energy` is derived from the session, not configured:

| Connection state | Room | Energy | Reads as |
|---|---|---|---|
| `CONNECTING` / `RECONNECTING` | – | 0.90 | nervous, fast |
| `ERROR` | – | 1.00 | maximum agitation |
| `CONNECTED` | yes | 0.35 | calm, alive |
| `CONNECTED` | no | 0.22 | idle but present |
| `DISCONNECTED` | – | 0.10 | almost still |

Rule: the pulse **never strobes** (period floor 900ms) and never reaches full opacity, so it can sit
permanently on screen without becoming a distraction.

### 3.2 `BroadcastRoomCode` — the room code as a signal

- 18dp rounded surface, `surfaceContainerHighest @0.6`, 1dp `primary @0.45` border.
- `headlineMedium`, `FontFamily.Monospace`, `Bold`, `letterSpacing = 10sp`, centred.
- A 2400ms linear scanline (`primary @0.22`) sweeps horizontally behind the glyphs.
- Empty state renders `· · · ·` — never an empty box.

### 3.3 `ListenerConstellation` — the cast

- 160dp canvas; ring radius `minDimension * 0.34`; node radius 9dp.
- One full revolution per 60s plus a per-index offset so nodes never align.
- **Host** uses `tertiary`, everyone else `primary`.
- Connected → solid node with a `2.1 ×` halo at 0.22 alpha; disconnected → 1.5dp hollow stroke.
- A thin tether line runs from each node to the centre (0.22 alpha connected / 0.08 disconnected).
- There is a deliberate trade-off here: **canvas nodes are not focusable**. Names therefore live in
  the stacked screen, and the constellation is decorative + glanceable only.

### 3.4 `StageStatTile`

20dp-radius `surfaceContainerLow` card, `labelMedium` caption over `titleMedium` value, two per row
(`weight(1f)` each): **Connection** (`Disconnected … Connected`) and **Room** (`None` / `Active`).

---

## 4. State matrix

| Connection | In room | Host | Stage shows |
|---|---|---|---|
| `DISCONNECTED` | no | – | core still · **Connect** · identity card · settings |
| `CONNECTING` / `RECONNECTING` | no | – | core nervous · **Reconnect** · identity card |
| `ERROR` | no | – | core agitated · **Connect** · identity card |
| `CONNECTED` | no | – | core calm · **Disconnect** · identity card |
| `CONNECTED` | yes | no | core warm · broadcast code · cast · **Leave room** · settings |
| `CONNECTED` | yes | yes | as above **+** invite/copy (dev), pending requests, pending suggestions |

Host-only blocks are gated exactly like the stacked screen: `isHost && …isNotEmpty()` for the queues,
`isHost && developerMode` for invite/copy.

---

## 5. Events handled by the screen (not the layout)

`ListenTogetherManager.events` is collected once in `ListenTogetherScreen`, so both layouts behave
identically:

| Event | Effect |
|---|---|
| `SessionEnded(TIME_LIMIT / ALONE)` | Toast, clear create/join progress |
| `JoinRejected(reason)` | Localised error string, clear progress |
| `JoinApproved` | Clear error + progress |
| `RoomCreated(code)` | Copy the code to the clipboard |

---

## 6. Rules this layout must keep

1. **No literals.** Every colour is a `MaterialTheme.colorScheme` role; the gold *Shining* palette
   and OLED black must work with no extra branches.
2. **No raw font sizes.** Only `MaterialTheme.typography` + explicit letter spacing for the code.
3. **4dp grid.** All gaps use `Spacing.*` or `16.dp` multiples already used by the screen.
4. **Voice-over parity.** Every interactive element keeps a real `contentDescription`; nothing may
   exist *only* as canvas pixels to a screen reader.
5. **No WebView / three.js.** The stage is native Compose; adding a JS renderer for decoration would
   cost ~600 KB of APK for no behaviour.

---

## 7. Rollout plan

| Step | State |
|---|---|
| 1. Building blocks extracted to `ListenTogetherStage.kt` | done |
| 2. `ListenTogetherAltUiKey` preference + stage screen | done |
| 3. Dev toggle in *Tactical Hub* settings | done |
| 4. Accessibility pass on the constellation (text list fallback for TalkBack) | next |
| 5. Per-listener tap targets on the ring (menu: kick / block / transfer host) | next |
| 6. Promote `ListenTogetherAltUiKey` out of developer mode once 4–5 land | later |

Step 5 needs host privileges and the existing `UserActionDialog`; it is the one functional gap
between the stacked screen and the stage (motivation for keeping the toggle dev-only for now).
