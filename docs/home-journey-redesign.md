# Home ("Camino" / "Journey") redesign

**Status:** implemented for the calendar-locked novena (v3.0.0 branch).
**Scope:** the `home` route only — `HomeScreen.kt` and the new `JourneyCard` / `JourneyStatus` / `SegmentedProgressIndicator`.
**Related:** `V3_REDESIGN_ANALYSIS.md` §4–§5 (drawer removal, content-forward home, day strip).

---

## 1. Intent

The home tab is titled **"Camino"** (es) / **"Journey"** (en) in the app bar. It is
being reframed from a single centered "Go to day X" button into a **vertically
scrolling feed**. The first card is the entry point into the daily prayer; more cards
(other devotionals, villancicos, seasonal notes) can be appended to the same column
later without restructuring the screen.

The guiding constraint from the architecture audit still holds: the biggest win is
shortening the distance between *open app* and *today's prayer*. The feed leads with
that, not with a countdown or a menu.

## 2. The card the user sees

```
Camino / Journey                                    (app bar)
┌────────────────────────────────────────────────┐
│  ● Novena en curso            (pill: dot + status label, top-left)
│                                                 │
│  Novena de Aguinaldos          (card title — replaces the old MainTitle)
│                                                 │
│  ┌───────────────────────────────────────────┐  │  inner tonal panel
│  │  Día 4 de 9              44% del camino    │  │  row 1
│  │  ▮ ▮ ▮ ▮ ▯ ▯ ▯ ▯ ▯                         │  │  row 2: segmented indicator
│  └───────────────────────────────────────────┘  │
│  ✓ Vas al día                  (only when caught up — positive only)
│  [  Continuar · Oración del día 4         →  ]   │  primary button
└────────────────────────────────────────────────┘

Ir a un día
▸ Día 1  Día 2  Día 3  [Día 4]  Día 5 …             (existing DayStrip, kept)
```

## 3. Decisions

### 3.1 The pill label is derived from state, not a fixed "Active novena"

The label reflects **where the journey is**, computed by `resolveJourneyStatus()`:

| `JourneyStatus` | When | Pill | Dot | Button |
|---|---|---|---|---|
| `InProgress` | calendar-locked devotional, today is inside its window | *Novena en curso* | primary | **Continuar · Oración del día N →** → day N |
| `NotStarted` | window has not opened yet | *Próxima novena* | muted (`onSurfaceVariant`) | **Ver la novena →** → day 1 in preview mode |
| `Completed` | all 9 days marked prayed | *Novena completada* | primary | **Leer de nuevo →** → day 1 |

"Active novena" is just what `InProgress` happens to look like — it is not a stored
flag. Everything the resolver needs is already passed into `HomeScreen`
(`currentDay`, `devotional.totalDays`, `devotional.schedule`, `completedDays`); no
ViewModel or state changes were required.

### 3.2 Two progress numbers, kept separate on purpose

The novena is **date-gated** — `Util.resolveCurrentDay(schedule)` decides which day
you are on from the calendar; you cannot pray day 5 before Dec 20 (except in preview
mode). So the card shows two independent signals rather than collapsing them:

- **"Día 4 de 9"** — the calendar position (`currentDay`). Fixed by the date.
- **"44% del camino"** — what *the user* has actually prayed:
  `completedDays.size / totalDays`, the only number they control.

If someone opens the app on Dec 19 having prayed nothing, "Día 4 de 9" is true and
"0% del camino" is also true — that honesty is the point. When the user keeps up, the
two numbers converge.

The **segmented indicator** (9 bars — a *segmented / stepped progress indicator*)
is **day-accurate, not a count**: bar _i_ is filled iff day _i_ is in
`completedDays`, today's bar is outlined when not yet prayed, the rest are dim
(`surfaceVariant`). A skipped day therefore shows as a gap, matching the checkmarks on
the `DayStrip` below. In practice the set is almost always a contiguous prefix (the
novena is prayed in order), so gaps only appear from preview-mode marking.

**Colour roles.** The `InProgress` card uses **`secondary`** (the seasonal
gold/amber accent) for the status pill (dot + label), the `% of the journey` figure,
and the filled/outlined progress bars — visually separating "where the journey is"
from the indigo **`primary`** call-to-action button. The "keeping up" affirmation
still uses `primary`. `Completed` keeps a `primary` dot; `NotStarted` a muted
`onSurfaceVariant` dot.

### 3.3 No streaks for the seasonal novena

"N days in a row" was considered and rejected **for the calendar-locked novena**:

- The window is only 9 consecutive days, so a streak is nearly identical to
  "day N of 9" — it adds no information.
- Streaks carry loss framing ("you broke your streak"). Nagging someone for missing a
  day of a prayer tradition is tonally wrong for this app.

Reinforcement is **positive-only**: a small "Vas al día" / "You're keeping up" line
appears when `prayedCount >= currentDay`, and *nothing* negative appears when the user
is behind.

A streak model is still appropriate for a **future evergreen daily devotional**
(e.g. a daily rosary prayed over months) — see §4.

### 3.4 Snow animation and DayStrip retained

- The full-screen `animation_snow_falling` Lottie is **kept** behind the feed; the
  cards are opaque `ElevatedCard` surfaces so text contrast is unaffected. It could
  later be scoped to a bounded header for battery, but that was not done here to avoid
  removing seasonal charm without a decision.
- `DayStrip` (the tappable "jump to any day" row) is **kept** below the card under an
  "Ir a un día" heading. The card's segmented indicator is a glanceable summary; the
  strip is the interactive control. They are complementary, not redundant.
- The Dec 25 `CelebrationAnimation` path is unchanged.

## 4. Temporal model — current and future

`DevotionalSchedule` already splits into:

- **`FixedRange`** — calendar-locked. Day N is decided by today's date. The Christmas
  novena. Communal and date-locked *by design* — not a limitation to remove.
- **`Evergreen`** — no day progression (a daily rosary: there is just "today's").

Missing, and intentionally **not built yet**:

- **`SelfPaced`** — a novena the user starts themselves; "day N" advances from *their*
  completions (`completedDays.size + 1`) with a one-day-per-calendar-day gate. When the
  first self-paced devotional is authored, add:
  1. `DevotionalSchedule.SelfPaced` (+ a `hasBegun` / start-date concept in
     `DevotionalProgressStore`),
  2. a `JourneyStatus.NotBegun` case and a resolver branch,
  3. a "Begin the novena" button state on `JourneyCard`.

Building this now — with no content behind it — is the speculative work
`V3_REDESIGN_ANALYSIS.md` warns against, so `JourneyStatus` is shaped to make it an
additive change.

## 4a. Previewing the seasonal UI off-season (debug builds)

The novena's states only appear Dec 16–25, which makes them hard to check the rest of
the year. **More ▸ 🐞 Debug · forced date** (visible only when `BuildConfig.DEBUG`)
switches the app's idea of "today" between the real date and any novena day / Christmas
Day. The choice persists across restarts; "Real date" turns it off.

- `debug/DebugClock.kt` — the in-memory override. `Util.currentDate()` routes every
  date calculation through `DebugClock.today()`.
- `debug/DebugSettings.kt` — SharedPreferences persistence, all entry points gated on
  `BuildConfig.DEBUG`. `hydrate()` runs once from `NovenAppApplication`.
- `MainActivity` re-runs `MainViewModel.refreshCurrentDay()` whenever the Novena tab
  comes to the front, so the switch (and real midnight rollovers) take effect without
  an app restart.

Release builds compile all of this but it is fully inert — nothing writes the pref and
`DebugClock.set()` ignores non-debug callers.

## 5. Known follow-ups

- **Per-season progress keying.** `DevotionalProgressStore` keys completed days by
  devotional id only, with no year/season. Last December's completions persist into
  the next season and would show as pre-filled progress on day 1. Needs a season key
  (e.g. `completed_days_<id>_<startYear>`).
- **Dead code.** `MainTitle` and `CountdownToDate` are no longer used by `HomeScreen`
  (the title moved into the card; the countdown into the `NotStarted` state). Left in
  place for now; remove once nothing else references them.
- **`NotStarted` date format.** Uses `FormatStyle.LONG` (includes the year). Fine, but
  a year-less format would read better ("16 de diciembre").
- **Feed content.** Only the `JourneyCard` exists. The "other sections of interest"
  cards (villancicos shortcut, etc.) are the next feed items to add.

## 6. Files

| File | Role |
|---|---|
| `presentation/screens/home/JourneyStatus.kt` | `JourneyStatus` sealed interface + `resolveJourneyStatus()` |
| `presentation/screens/home/JourneyCard.kt` | the card UI for all three states |
| `presentation/components/SegmentedProgressIndicator.kt` | the 9-bar progress row |
| `presentation/screens/home/HomeScreen.kt` | feed layout (was a centered column) |
| `debug/DebugClock.kt`, `debug/DebugSettings.kt` | debug-only "forced date" for previewing the seasonal UI |
| `res/values/strings.xml`, `res/values-en/strings.xml` | `journey_*` strings |
