# Day-to-day navigation: remove DayStrip, sequential prev/next in DayScreen

**Status:** Done
**Owner approval:** Approved by Johan Mosquera across chat (2026-09-17): chips removal confirmed, Option 4 (sequential arrows, no picker) chosen, forward navigation beyond "today" confirmed ("Sí, permitamos avanzar más allá de hoy"); confirmed working on-device 2026-09-20 ("aprobar todos excepto el splash art")
**Scope:** `presentation/screens/home/HomeScreen.kt`, `presentation/components/DayStrip.kt` (deleted), `presentation/screens/prayer/PrayerScreen.kt`, `presentation/AppNavHost.kt`, `presentation/navigation/NavigationScreen.kt` (referenced, unchanged), string resources
**Related:** `V3_REDESIGN_ANALYSIS.md` §5 B.10 (superseded by this spec); `docs/home-journey-redesign.md`; CLAUDE.md §2.6 (no streak/loss framing — respected: forward browsing is a preview, not a lock)

---

## 1. Intent (final, after discussion)

This spec started as "per-day locked/empty state for future days" (B.10) but was substantially reshaped through conversation with the owner into a broader decision: **remove the `DayStrip` day-chip row from Home entirely**, since its two functions (progress-at-a-glance, jump to an arbitrary day) are now redundant with `JourneyCard`'s `SegmentedProgressIndicator` (progress) and no longer needed once "today" via the card is the only expected entry point. In its place, `DayScreen` gets simple sequential ◀/▶ navigation, matching the owner's stated intent: read the novena day after day, with jumping to a specific day only mattering for catching up on a missed day (going backward) or reading ahead (going forward) — both served by stepping one day at a time from wherever the user currently is.

The owner also described a longer-term direction — devotionals usable "any time," where the user picks their own start date (a future `DevotionalSchedule.SelfPaced`, already anticipated in `JourneyStatus.kt`'s own doc comment). That is explicitly **out of scope here** per `CLAUDE.md` §2.8 ("don't build ahead of content") — this spec only changes day-to-day navigation UI, which is orthogonal to how a devotional's schedule assigns calendar dates to day numbers, and works the same regardless of which schedule type is active.

## 2. Non-goals

- No `DevotionalSchedule.SelfPaced` or custom start-date feature — noted as a distinct future spec, not started here.
- No change to how `JourneyCard`/`JourneyStatus` resolve "today" from the calendar — unchanged.
- No streak/loss framing anywhere in the new arrows (per CLAUDE.md §2.6) — advancing past "today" is a neutral preview, not an achievement or a violation.

## 3. Proposed behavior / UX (implemented)

- **Home:** `JourneyCard` is the only entry point into a day (unchanged from before). The day-chip row and its "Ir a un día" label are gone.
- **DayScreen:** a ◀ and ▶ `IconButton` flank the existing "Día N/9" pill in `PrayerToolbar`.
  - ◀ is disabled (not hidden) at day 1.
  - ▶ is disabled (not hidden) at the devotional's last day (`totalDays`) — otherwise **always enabled**, including past the calendar-resolved "today." Stepping ahead of today shows the existing `PreviewBanner` ("Estás viendo un adelanto...") inside `PrayerScreen`, which was already built but rarely reachable before this change (previously only `DayStrip`'s locked/Toast behavior prevented reaching it in normal use).
  - Tapping an arrow replaces the current `DayScreen` back-stack entry (`popUpTo(DayScreen.route) { inclusive = true }`, `launchSingleTop = true`) rather than pushing a new one, so repeated stepping doesn't pile up the back stack — the top-bar back button always returns directly to Home.

## 4. Decisions

- **Boundary is purely ordinal** (`1..totalDays`), not calendar-aware — deliberately, so the same arrows work unchanged if a future schedule type (self-paced, evergreen) is added; no calendar-lock logic to rewrite later.
- **A comment marks the toggle point for future re-locking**, per the owner's request, at `AppNavHost.kt` where `onNextDay` is computed: if forward browsing should later be capped at "today" instead of the full range, that comment explains exactly what to change (cap the bound at `actualCurrentDay ?: devotionalMeta.totalDays`, and add an explicit block signal like the old `DayStrip` Toast instead of silently disabling the arrow).
- **Dead code removed, not just unused:** `DayStrip.kt` was deleted entirely (not left dangling) per the "don't leave unused code" guidance — along with its now-orphaned strings (`text_dia`, `text_dia_locked`, `day_strip_locked_message`, `journey_all_days_label`) in both `values/strings.xml` and `values-en/strings.xml`. Two new strings were added instead: `content_description_previous_day`, `content_description_next_day`.
- Doc-comment references to `DayStrip` in `MainActivity.kt` and `SegmentedProgressIndicator.kt` were updated so nothing points at a deleted file.

## 5. Open questions

None — resolved through conversation.

## 6. Tasks

- [x] Removed `DayStrip` usage and its label from `HomeScreen.kt`; deleted `DayStrip.kt`
- [x] Cleaned up now-orphaned string resources in both locales; added the two new content-description strings
- [x] Added `NavHostController.navigateToDevotionalDay()` helper in `AppNavHost.kt` (replaces the current DayScreen entry instead of stacking)
- [x] Computed `onPreviousDay`/`onNextDay` in `AppNavHost.kt`'s `DayScreen` composable, clamped to `1..totalDays`, with the re-locking comment requested by the owner
- [x] Added ◀/▶ `IconButton`s to `PrayerToolbar` in `PrayerScreen.kt`, flanking the existing day-progress pill
- [x] Updated stale `DayStrip` doc-comment references in `MainActivity.kt` and `SegmentedProgressIndicator.kt`
- [x] Verified `:app:assembleDebug` builds successfully
- [x] **Fix (2026-09-17):** owner reported the single-row toolbar overflowed on narrow phones - the added arrows squeezed the text-size/listen controls, wrapping the listen button's label vertically. Split `PrayerToolbar` into two rows (day navigator centered on top, reading aids right-aligned below) so neither group competes for width with the other.
- [x] Owner to visually confirm the two-row toolbar looks right and that stepping past the last real day shows the preview banner as expected *(confirmed 2026-09-20; the two-row toolbar was later replaced by a single row in `specs/prayer-toolbar-single-row`, Done 2026-10-20)*

## 7. Follow-ups

- A `DevotionalSchedule.SelfPaced` (or similar user-chosen-start) feature, if/when the owner wants to pursue the "usable any time" direction — deserves its own spec given it touches the schedule model, notifications, widget, and the Dec 25 celebration trigger.
- Noticed but out of scope: `home_explore_all_days` string resource appears unused in code (predates this change, unrelated to `DayStrip`) — flagging for a future cleanup pass, not touched here per scope discipline.

## 8. Files

| File | Change |
|---|---|
| `presentation/screens/home/HomeScreen.kt` | Removed `DayStrip` usage/label, unused import |
| `presentation/components/DayStrip.kt` | Deleted |
| `presentation/screens/prayer/PrayerScreen.kt` | Added `onPreviousDay`/`onNextDay` params; ◀/▶ arrows in `PrayerToolbar` |
| `presentation/AppNavHost.kt` | Added `navigateToDevotionalDay()` helper; computed prev/next bounds with re-locking comment |
| `MainActivity.kt`, `presentation/components/SegmentedProgressIndicator.kt` | Updated stale doc comments referencing deleted `DayStrip` |
| `values/strings.xml`, `values-en/strings.xml` | Removed 4 orphaned strings, added 2 new ones |
