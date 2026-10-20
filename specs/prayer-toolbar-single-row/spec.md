# Prayer toolbar: single row

**Status:** Done
**Owner approval:** Approved by Johan Mosquera (chat, "apruebo el spec 1 con las recomendaciones, adelante"), 2026-10-19 — Q1–Q3 resolved per the recommendations; confirmed 2026-10-20 ("aprueba los specs anteriores")
**Scope:** `presentation/screens/prayer/PrayerScreen.kt` (`PrayerToolbar`, `ProgressPill`), `res/values/strings.xml`, `res/values-en/strings.xml`
**Related:** `specs/palette-tonal-containers` (independent, but changes how the pill and the Listen button look); `specs/day-screen-dynamic-title`; `specs/code-audit-v3` Q5 (candle icon); `CLAUDE.md` §2.6, §2.7

---

## 1. Intent

Owner feedback from on-device review (2026-10-19): the day screen has too much space
between rows. Today `PrayerToolbar` is two rows that each use only part of the width:

```
              [<]  🕯 Día 8 de 9  [>]                 ← row 1, centered, empty sides
                          [A−] [A+] [🔊 Escuchar]      ← row 2, end-aligned, empty start
[ Etapa 2 · Oración a la Virgen         ~3 min ]       ← StageCard
[ ▬▬ ▬▬ ▭▭ ░░ ░░ ░░ ]
───────────── prayer text ─────────────
```

With the preview banner, that's roughly 200–230dp of controls before any prayer text
(close to a third of a phone screen). This is a daily-reading app: the text is the
content, so the controls should take one row, not two.

The two rows exist because one `SpaceBetween` row overflowed on narrow phones. The
"Escuchar" text label was what didn't fit (see the KDoc on `PrayerToolbar`). This spec
removes that cause instead of keeping the extra row.

## 2. Non-goals

- Doesn't change `StageCard`, the pager, `DayCompletionToggle` or the preview banner.
- Doesn't change colors. The pill's and buttons' colors belong to
  `specs/palette-tonal-containers`. This spec only uses existing tokens.
- Doesn't change narration or text-scale behavior (same bounds, same step, still
  not persisted between visits).
- Doesn't move controls into the app bar. That was considered (see §4.3) and rejected.

## 3. Proposed behavior / UX

One row, full width:

```
[<] 🕯 Día 8 de 9 [>]                      [Aa] [🔊]
[ Etapa 2 · Oración a la Virgen         ~3 min ]
[ ▬▬ ▬▬ ▭▭ ░░ ░░ ░░ ]
```

- **Start:** day navigator, the same `[<] pill [>]` as today, now start-aligned instead of
  centered. Arrows keep their disabled state on day 1 / last day.
- **End:** two 48dp icon buttons:
  - **Aa (text size)** opens a small `DropdownMenu` anchored to it, with "A−" and "A+"
    items (disabled at the bounds, as today). The menu stays open between taps so the
    user can step several sizes and watch the text change behind it. It closes on an
    outside tap or back.
  - **🔊 Listen** is icon only: `VolumeUp`, or `Stop` while speaking. It keeps its
    tonal-button container so it still reads as the primary reading aid. The
    content description switches between "Escuchar" and "Detener".
- **Width:** about 300dp of content, so it fits a 360dp phone with the existing 12dp
  side padding and leaves room for a larger font scale on the pill text.
- **Height recovered:** one 48dp row (about 52dp including the 4dp gap).
- **Large screens / landscape:** same row. The extra width just becomes space between the
  two groups, same as `SpaceBetween` today.
- **Large system font (e.g. 200%):** the pill text may wrap. Mitigation: `maxLines = 1`
  on the pill text, accepting an ellipsis at extreme scales. See Q2.

## 4. Decisions

### 4.1 Layout: one `Row` with `SpaceBetween` (recommended)
Day navigator group at the start, reading aids group at the end. This is the simplest
change and keeps every control one tap away except text size.

### 4.2 Text size behind an "Aa" menu (recommended) vs. keep A−/A+ inline
- **(a) "Aa" menu (recommended):** saves about 80dp. Text size is a set-once
  preference, so two taps instead of one is an acceptable cost.
- **(b) Inline A−/A+ plus an icon-only Listen button:** about 340dp, which overflows on
  360dp phones or with a larger font scale. This is the same failure the two-row version
  was created to avoid.

### 4.3 Rejected: moving text size or Listen to the app bar
The app bar title is dynamic (`specs/day-screen-dynamic-title`) and long stage titles
already take most of its width. Overflow menus also hide Listen, which is a primary
action on this screen.

### 4.4 Listen label
The visible text label is dropped. The icon plus its content description carries the
meaning. The speaker icon was already chosen for this in `code-audit-v3` Q5, so it
can't be confused with the Gozos music button.

## 5. Open questions

- **Q1 → resolved:** "Aa" menu (§4.2a).
- **Q2 → resolved:** the pill text ellipsizes (`maxLines = 1`). The navigator group only
  shrinks when the row runs out of width.
- **Q3 → resolved:** no current-size readout in the menu.

## 6. Tasks

- [x] `PrayerToolbar` → a single `SpaceBetween` row (navigator start, aids end), vertical padding 8dp → 4dp
- [x] New private `TextSizeMenu`: "Aa" `IconButton` + `DropdownMenu` with A−/A+ items that stay open on tap *(later moved to the shared `presentation/components/TextSizeMenu.kt`, see Follow-ups)*
- [x] Listen → `FilledTonalIconButton`, content description `prayer_listen`/`prayer_stop`
- [x] `ProgressPill` takes a `modifier`; text `maxLines = 1` + ellipsis
- [x] New string `content_description_text_size` (es/en); unused `TextButton`/`PaddingValues`/`FilledTonalButton` imports removed
- [x] `compileDebugKotlin` passes
- [x] Owner on-device review (narrow phone, large font scale, TalkBack, landscape/tablet) *(owner confirmation 2026-10-20, "aprueba los specs anteriores")*

## 7. Follow-ups

- **Done 2026-10-19 (owner: "sí, cada villancico propio, al entrar se reinicia"):** the lyrics screen got the
  same "Aa" menu in its app bar. `TextSizeMenu` and the scale bounds moved to
  `presentation/components/TextSizeMenu.kt`, shared by both screens. Each song starts at 1x when opened, the
  same as each day here, and keeps its size across rotation.

- Persisting the text size between visits (currently `rememberSaveable` only). That
  would be a separate decision with its own persisted store.
- ~~If `palette-tonal-containers` is approved, re-check the pill's and the Listen
  button's weight together. With soft containers the pill may not need a background
  at all (proposed there, not here).~~ Resolved by `palette-tonal-containers` Q2 (pill
  has no background; Listen stays tonal), both Done 2026-10-20.

## 8. Files

| File | Change |
|---|---|
| `presentation/screens/prayer/PrayerScreen.kt` | `PrayerToolbar` becomes one `Row`; new `TextSizeMenu` (private at first, now shared in `presentation/components/TextSizeMenu.kt`); Listen becomes an icon-only tonal button; KDoc updated |
| `res/values/strings.xml`, `res/values-en/strings.xml` | New `content_description_text_size` ("Tamaño del texto" / "Text size"); reuse `prayer_listen` / `prayer_stop` as content descriptions |
