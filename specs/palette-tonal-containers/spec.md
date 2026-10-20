# Palette: tonal containers and a color-role hierarchy

**Status:** Done
**Owner approval:** Approved by Johan Mosquera (chat, "si, ajusta y apruebo, implementa"), 2026-10-19, including the per-screen role audit (§3.3) and the adjusted secondary rule (§3.2). Q1–Q5 resolved per the recommendations (§5); confirmed 2026-10-20 ("aprueba los specs anteriores")
**Scope:** `ui/theme/Color.kt`; call sites re-weighted per §3.3: `presentation/screens/home/JourneyCard.kt`, `presentation/screens/prayer/PrayerScreen.kt` (`ProgressPill`), `presentation/components/GozosScreen.kt`, `presentation/screens/lyrics/SongListScreen.kt`, `presentation/components/NovenAppBar.kt`. Indirectly every screen, the bottom nav / rail, and the Glance widget (`widget/NovenaWidget.kt` reuses `LightColorScheme`/`DarkColorScheme`)
**Related:** `specs/material3-expressive-pass`, `specs/m3-visual-polish-pass` (where the current "Novena Contemplative MD3" palette came from); `specs/code-audit-v3` P4-7 (card gradient), P1-11 (widget theming); `specs/prayer-toolbar-single-row`; `CLAUDE.md` §2.7

---

## 1. Intent

Owner feedback from on-device review (2026-10-19): colors compete for attention on the
Home cards and the day screen.

The root cause is the palette itself, not individual screens. In Material 3, the
`*Container` roles are meant to be quiet, light tones (lightness of about 85–90% in the
light theme) for chips, pills, badges, tonal buttons and the nav indicator. In
`Color.kt` they're saturated mid-tones:

| Role | Current | HSL lightness | Typical M3 |
|---|---|---|---|
| `primaryContainer` | #3F51B5 (vivid indigo) | ~48% | ~90% |
| `secondaryContainer` | #FE932C (vivid orange) | ~58% | ~90% |
| `tertiaryContainer` | #73498B (vivid violet) | ~42% | ~90% |

Text on them only just passes AA (about 4.5:1). Every component that's supposed to be
secondary ends up shouting. Observed effects:

- **Day screen:** five saturated accents above the prayer text: violet preview
  banner, orange day pill, orange Listen button, amber stage segments, orange
  "marked as prayed" chip. The pill isn't tappable but is the loudest element, and it
  has the same color as Listen, which is a button.
- **Home card:** the "Active" badge (`primaryContainer`) weighs as much as the
  "Continue" CTA (`primary`). The NotStarted/Completed CTAs are large orange tonal
  buttons, louder than the in-progress CTA, so button weight changes by state for no
  reason. All of this sits on a hardcoded two-hue gradient whose amber (#D97706) isn't
  even in the palette (P4-7).
- **Bottom nav / rail:** the selected-tab indicator uses `secondaryContainer`, i.e.
  vivid orange (inferred from M3 defaults; to confirm on device).
- **Lyrics:** no color, which is correct for long-form reading. The song list's icon
  sits on a vivid indigo circle (`primaryContainer`).

**The hues aren't the problem.** Indigo, amber and violet fit a contemplative,
candlelit, liturgical app. This spec keeps them and fixes their tones.

## 2. Non-goals

- No new hues and no rebrand.
- Doesn't turn on dynamic color (`dynamicColor = false` stays). That's a separate
  product decision.
- Doesn't restructure the lyrics content (marking choruses, stanza spacing). That
  needs `songs.json` changes; see Follow-ups. *(Done later the same day without a
  content change; see Follow-ups.)*
- Doesn't change layout. Toolbar layout is `specs/prayer-toolbar-single-row`.
- Doesn't redo the widget's layout. It just inherits the new tones (see §4.4).

## 3. Proposed behavior / UX

### 3.1 New tonal containers (same seeds)
Regenerate the container/on-container pairs with Material Theme Builder from the
current seeds (`primary` #24389C, `secondary` #904D00, `tertiary` #593172). Final
values, generated with Material Color Utilities (contrast of on-container text in
brackets):

| Role | Light (tone 90 / 10) | Dark (tone 30 / 90) |
|---|---|---|
| `primaryContainer` / `on…` | #DEE0FF / #00105B (13.2:1) | #283CA0 / #DEE0FF (7.2:1) |
| `secondaryContainer` / `on…` | #FFDCC3 / #2F1500 (13.3:1) | #6E3900 / #FFDCC3 (7.2:1) |
| `tertiaryContainer` / `on…` | #F4D9FF / #2E0348 (13.3:1) | #5D3576 / #F4D9FF (7.3:1) |

`primary`, `secondary`, `tertiary`, surfaces and error stay as they are.

### 3.2 One job per hue

| Hue | Used for | Not for |
|---|---|---|
| **Indigo (`primary`)** | The one main action per screen ("Continue", "Preview novena", the Gozos music toggle), text actions in dialogs and footers | Status, badges, decoration, progress |
| **Amber, strong (`secondary`)** | Progress only: segments, day/percent labels, "keeping up", completed, a read gozo | Actions |
| **Amber, soft (`secondaryContainer`)** | Progress chips **and** secondary tonal actions (Listen, "Read again", nav indicator), following M3's own component defaults | Large decorative surfaces |
| **Violet (`tertiary` / `tertiaryContainer`)** | Informational notices and meditative accents: preview banner, "Active" badge, "upcoming" state, gozo numbers, decorative icon circles | Actions |
| **Vivid orange #FE932C** | Kept only as a deliberate accent, the named token `CandleAccent` (the candle on the day pill) | Surfaces, text |

Rules: nothing informational may outweigh the screen's primary action, and each screen
ends up with a single indigo element (its main action).

The amber rule is split by tone because every M3 tonal component (`FilledTonalButton`,
`FilterChip`, the nav indicator) defaults to `secondaryContainer`. A strict "amber =
progress only" would mean fighting the components. With soft containers (§3.1) the
strong progress amber and the soft action amber no longer look alike.

### 3.3 Per-screen role audit (2026-10-19) and the resulting call-site changes

Audited every screen, including the colors M3 components apply by default
(`Button` → `primary`; tonal button, `FilterChip`, nav indicator → `secondaryContainer`).
Net finding: indigo is overused and violet is almost unused (only the preview banner), so
indigo was doing violet's job. Amber was mostly right.

| Screen | Element | Before | ✓/✗ | After |
|---|---|---|---|---|
| Home card | "Continue day N" button | `primary` | ✓ | — |
| Home card | "Keeping up" ✓ | `primary` | ✗ progress, sits right above the CTA | `secondary` |
| Home card | "Active" badge | `primaryContainer` | ✗ informational, as heavy as the CTA | `tertiaryContainer` |
| Home card | "Completed" pill dot | `primary` | ✗ progress | `secondary` |
| Home card | "Upcoming" pill dot | `onSurfaceVariant` | ~ indistinct | `tertiary` |
| Home card | "Preview" CTA (not started) | tonal (`secondaryContainer`) | ✗ only action on the card | filled `primary` (`Button`) |
| Home card | "Read again" CTA (completed) | tonal | ✓ optional action | — |
| Home card | Background gradient | hardcoded hexes + `isSystemInDarkTheme()` | ✗ P4-7 | `primaryContainer` → `secondaryContainer` with alpha, from `MaterialTheme` |
| Day screen | Day pill | `secondaryContainer` background | ~ right role, looks tappable | no background; `secondary` text, `CandleAccent` candle |
| Day screen | Listen, stage segments, preview banner, day chip, prayer text | — | ✓ | — (tones fixed by §3.1) |
| Gozos | Gozo numbers | `primary` | ✗ decorative | `tertiary` |
| Gozos | Read ✓ (`FilledIconButton`) | `primary` | ✗ progress; also inconsistent with the amber day chip | `secondary` / `onSecondary` |
| Gozos | Music toggle (playing) | `primary` | ✓ the screen's action | — |
| Carols list | Icon circle | `primaryContainer` | ✗ decorative | `tertiaryContainer` |
| Lyrics | Text | `onBackground` | ✓ reading text stays neutral | — |
| More / About | Icons, text | neutral | ✓ settings-like screens stay neutral | — |
| App bar | "NovenApp" label | `secondary` | ~ not progress; the logo already brands | `onSurfaceVariant` |
| Nav bar / rail | Selected indicator | `secondaryContainer` | ✓ M3 default | — (soft via §3.1) |

### 3.4 Verification
Before/after screenshots of every main screen in light and dark (Home with each
`JourneyStatus`, day screen with and without the banner, Gozos, carols list and lyrics,
More, the widget), and on Android 12 and 14+. Every text-on-container pair rechecked
for ≥ 4.5:1 contrast. The `:compose-preview` module's multi-theme previews are the first
check before any on-device look.

## 4. Decisions

### 4.1 Keep the hues, fix the tones (recommended)
Alternatives:
- **(b)** An entirely new palette. Rejected: the hues are fine, and changing them throws
  away the seasonal identity built in `material3-expressive-pass`.
- **(c)** Only patch the call sites (pill, badge) and leave the palette as is.
  Rejected: every future tonal component (chips, nav, buttons) would keep coming out
  loud.

### 4.2 Generate, don't hand-tune
Use Material Theme Builder output for all container pairs, so light and dark stay
tonally consistent. The current dark theme is derived by `lerp` from light values; the
new dark containers come from the same generated scale instead.

### 4.3 Keep vivid orange as a named accent
For example `CandleAccent`, so the warmth isn't lost entirely, but only where it's
chosen on purpose.

### 4.4 The widget inherits it
`NovenaWidget` already uses `LightColorScheme`/`DarkColorScheme`, so it picks up the new
tones with no code change. P1-11 (widget dark mode/dynamic colors) is otherwise
unchanged by this spec.

## 5. Open questions

- **Q1 → resolved:** keep the hues, regenerate the tones.
- **Q2 → resolved:** the day pill has no background.
- **Q3 → resolved:** "Preview" (not started) becomes a filled `primary` button. "Read
  again" (completed) stays tonal, since it's optional once the path is finished (§3.3).
- **Q4 → resolved:** the vivid orange survives only as `CandleAccent`, on the day
  pill's candle.
- **Q5 → resolved:** before the season. The toolbar spec already shipped; one full
  on-device pass is required before release.

## 6. Tasks

- [x] Container/on-container pairs regenerated with Material Color Utilities
      (`materialyoucolor`, HCT tonal palettes from the current seeds; light = tone 90/10,
      dark = tone 30/90). Dark non-container roles keep their previous values, now derived
      from named private constants instead of the light container values.
- [x] `CandleAccent` token
- [x] `DarkTertiary` → generated tone 80 #E5B5FF (owner OK 2026-10-19, "sí, aplica el #E5B5FF"): the derived value was 3.8:1 as text on the dark surface once Gozos numbers started using it; now 7.8:1. A deliberate exception to "dark non-container roles keep their values"
- [x] Call sites per §3.3
- [x] `compileDebugKotlin` passes
- [x] Owner on-device pass: light and dark, every screen in §3.4, widget *(owner confirmation 2026-10-20, "aprueba los specs anteriores")*

## 7. Follow-ups

- ~~Lyrics structure~~ → done 2026-10-19 without a content change (owner: "hazlo directamente, apruebo la propuesta"). `LyricsScreen` splits stanzas on blank lines, shows the artist with a `tertiary` note, and marks the chorus (the single most-repeated stanza, ≥ 3 times, no tie) in `tertiary` italics. The chorus rule was chosen by the owner over "any repeated stanza", which highlighted up to 9/10 stanzas. Detected in 9 of 12 songs. None in Noche de Paz, El Tamborilero or Hacia Belén.
- ~~P1-11: real `GlanceTheme`/dynamic colors for the widget.~~ Already resolved
  2026-09-22 (`code-audit-v3` §9.8): `GlanceTheme` with the app's brand palette, option
  (a). Dynamic color was deliberately not chosen, matching the app (`dynamicColor = false`).
- Re-evaluate `dynamicColor` as a user option once the base palette is right.

## 8. Files

| File | Change |
|---|---|
| `ui/theme/Color.kt` | New container/on-container values (light and dark); named vivid accent; dark containers from the generated scale instead of `lerp` |
| `ui/theme/Theme.kt` | Only if a new token needs wiring |
| `presentation/screens/home/JourneyCard.kt` | Gradient from theme tokens (P4-7); `ActiveBadge` re-weighted; CTA per Q3 |
| `presentation/screens/prayer/PrayerScreen.kt` | `ProgressPill` per Q2 |
| `presentation/screens/lyrics/SongListScreen.kt` | None expected (inherits); verify |
| `widget/NovenaWidget.kt` | None (inherits); verify visually |
