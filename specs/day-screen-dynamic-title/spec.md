# DayScreen app-bar title follows the current pager page

**Status:** Done
**Owner approval:** Approved by Johan Mosquera (chat, "aprueba las 1,2"), 2026-09-17; confirmed working on-device 2026-09-20 ("aprobar todos excepto el splash art")
**Scope:** `MainActivity.kt` (`NovenAppBar` invocation for `DayScreen.route`), `presentation/screens/prayer/PrayerScreen.kt`
**Related:** `V3_REDESIGN_ANALYSIS.md` §7 Phase 3 (visual refresh) — new item, added to the plan at the owner's request (2026-09-17); precedent: `currentSongTitle` hoisting already used for `LyricsViewScreen` in `MainActivity.kt`

---

## 1. Intent

Right now the app bar on `DayScreen` always shows the same static label (`R.string.app_bar_section_reading`, e.g. "Reading"/"Lectura") no matter which of the day's stages (reflection, prayer to Mary, prayer to Joseph, Gozos, prayer to the Child Jesus...) the user has swiped to in `PrayerScreen`'s `HorizontalPager`. Surfacing the actual stage name in the app bar (e.g. "Oración a San José", "Gozos") gives the user orientation without needing to look at the in-page title text, and matches how a physical novena booklet's running header would work.

## 2. Non-goals

- Does not change the content or order of the pager's pages.
- Does not change `PrayerToolbar`'s own in-page day/stage indicator — this is purely about the top-level `Scaffold` app bar shown in `MainActivity.kt`.

## 3. Proposed behavior / UX

As the user swipes between pages in `PrayerScreen`, the app bar title updates to that page's own title:
- `Prayer.Simple` / `Prayer.WithImage` pages → their existing `title: String` field.
- `Prayer.AllGozos` page → the existing `gozos_title` string resource (already used inside `PrayerScreen` as `gozosLabel`).

This is exactly the same shape of behavior the app already has for `LyricsViewScreen`, whose title is hoisted into `currentSongTitle` in `MyApp()` and passed down.

## 4. Decisions

- **Mechanism:** mirror the existing `currentSongTitle` pattern — hoist a `currentStageTitle` (or reuse/rename generically, since two nearly-identical hoisted-title variables would be a little repetitive) piece of state in `MyApp()`, pass a `onStageTitleChange: (String) -> Unit` callback into `PrayerScreen`, and call it from the existing `LaunchedEffect(pagerState.currentPage)` block (`PrayerScreen.kt:90`) alongside the narrator-stop call already there.
- **Fallback:** while `PrayerScreen` hasn't yet reported a title (first composition), keep showing the current static `app_bar_section_reading` string so there's no blank/flashing app-bar title on entry.

## 5. Open questions

- None blocking — mechanism is a direct precedent-following change.

## 6. Tasks

- [x] Hoisted `currentStageTitle` (nullable) in `MyApp()` (`MainActivity.kt`), mirroring `currentSongTitle`
- [x] `AppNavHost` takes `onStageTitleUpdated`, passed through to `PrayerScreen`'s new `onStageTitleChange`
- [x] `PrayerScreen`'s existing `LaunchedEffect(pagerState.currentPage)` now also reports `currentStage.stageTitle(gozosLabel)`
- [x] `DayScreen.route`'s `NovenAppBar` uses `currentStageTitle ?: stringResource(R.string.app_bar_section_reading)` — static fallback preserved
- [x] Verified `:app:assembleDebug` builds successfully
- [x] Owner to confirm the app-bar title updates correctly while swiping through a day's pages *(confirmed 2026-09-20)*

## 7. Follow-ups

- Consider whether the two now-similar hoisted-title state variables (`currentSongTitle`, this new one) are worth unifying into one small "current top-bar subtitle" concept in `MyApp()` — minor readability cleanup, not required for this spec to ship. *(Partly done by `code-audit-v3` P1-6: both titles now share the `ReportedTitle(entryId, title)` type and only show while their back-stack entry is current, so a new day never inherits the previous day's title. They're still two variables.)*

## 8. Files

| File | Change |
|---|---|
| `MainActivity.kt` | Hoist stage-title state; pass callback into `PrayerScreen`; use it for `DayScreen.route`'s `NovenAppBar` |
| `presentation/screens/prayer/PrayerScreen.kt` | Accept `onStageTitleChange` callback; invoke on page change |
