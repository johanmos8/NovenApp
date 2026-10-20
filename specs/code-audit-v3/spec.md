# Code audit v3.0.0 — bugs, performance, security and best practices

**Status:** In Progress — blocks A and B, plus several `v3-bugfix-batch`/§5-dependent
items, are implemented and build clean; owner has not yet done on-device testing of
this batch, so nothing here is `Done` (see `CLAUDE.md` §1.1.6).
**Owner approval:** Johan Mosquera — approved incrementally during the 2026-09-22
session: "Arregla los 7 bugs primero con mayor rigor..." authorized implementing
P0-2, P0-3, P0-4, P2-5, P4-5, P1-7, P3-4; the Q1/Q6/Q8/Q9/Q10 answers below (same
session) authorized implementing P0-1, P1-2, P1-4, P2-6, P3-3, plus the incidental
P1-1 and P4-9 fixes made while touching that code. After the second-pass re-validation
(§9), "adelante con Q8 opción (a) junto con P1-5" authorized R1 + P1-5.
**Scope:** the whole app (`:app`), focused on the uncommitted changes on branch `3.0.0`
(devotional catalog, generic content model, reminders, widget)
**Related:** `CLAUDE.md` §1 (workflow) and §2 (principles), `specs/devotional-catalog-selection`,
`specs/generic-devotional-content-model`, `specs/smart-prayer-reminder`,
`docs/home-journey-redesign.md`

---

## 1. Intent

Collects in one document what the full code review of 2026-09-22 found before the
v3.0.0 batch is committed. It covers silent bugs, performance, security, UI defects,
and Kotlin/Compose best-practice issues. The app is seasonal: anything broken in
December can't be fixed in time for that season. So the most urgent items are the bugs
that only show up in the second season or on real devices (last year's progress,
notifications, rotation).

This document is a **prioritized inventory**, not an implementation plan. Each finding
has an ID (`P0-1`, `P1-3`…) so it can be approved, dismissed, or moved into its own spec
independently.

## 2. Non-goals

- Doesn't implement anything. No code has been touched.
- Doesn't redesign screens or add features. Where a fix involves a product decision, it
  stays open in §5.
- Doesn't replace per-fix specs. The large findings (P0-1, P0-2/P0-3, the serialization
  migration, the layering refactor) should each get their own spec before
  implementation (see §4.1).

## 3. Findings (ordered by priority)

Priority legend:

| Priority | Meaning |
|---|---|
| **P0** | Bug with user-visible or data impact; fix before the December season. |
| **P1** | Minor bug, UI or accessibility defect; worth fixing in v3.0.x. |
| **P2** | Performance. |
| **P3** | Security and build hardening. |
| **P4** | Architecture, technical debt and cleanup. |

---

### P0 — Serious silent bugs

#### P0-1 · Novena progress never resets from one year to the next

- **Files:** `data/progress/DevotionalProgressStore.kt:36-37`,
  `presentation/screens/home/JourneyStatus.kt:68-72`,
  `reminder/EveningCheckInWorker.kt:44-45`, `debug/DebugNotificationTrigger.kt:41-44`
- **What happens:** the DataStore key is `completed_days_<devotionalId>`, with no year.
  Days marked in 2025 stay marked in 2026.
- **Scenario:** the user completes all 9 days in 2025.
  - From then on, `resolveJourneyStatus` checks `prayedAll` **before** `currentDay`, so
    the Home card shows "Completed" all year. In December 2026 it never switches to
    "In progress" and never shows the countdown.
  - `EveningCheckInWorker` skips the 8pm check-in because `day in completedDays` is
    already true from the previous year.
- **Proposed fix:** the decision is open in §5 (Q1).
- **✅ Fixed 2026-09-22** (per Q1 answer, option (b) plus the Evergreen exception).
  `Util.resolveCycleKey(schedule, today)` computes a stable key for "which occurrence
  of this devotional is relevant now" (the ISO start date of the current/next window
  for `FixedRange`; the constant `"evergreen"` for `Evergreen`, so a future daily
  Rosary never auto-resets — only a manual reset would clear it, per the owner's
  explicit distinction). `DevotionalProgressStore` now stores `(completedDays,
  cycleKey)` and stays "dumb"; `Util.resolveEffectiveCompletedDays` treats stored days
  as stale (returns empty) once the stored `cycleKey` no longer matches. No migration
  step for pre-existing keyless data: it will simply read as stale on first load,
  which is the same outcome as the chosen "reset" answer.

#### P0-2 · A notification choice made before the novena starts is lost

- **Files:** `reminder/NovenaReminderScheduler.kt:53-57`,
  `data/notifications/DevotionalNotificationStore.kt`
- **What happens:** `scheduleIfNeeded` clears a `FixedRange` devotional's manual override
  whenever `resolveCurrentDay == null`, which is **both before and after** the window.
  `specs/devotional-catalog-selection` §4.3 says it's only cleared once the window
  **closes**.
- **Scenario:** in November the user long-presses to turn notifications on. They see the
  "Active" badge and the Toast. They close and reopen the app: the choice is gone and so
  is the badge. The same happens if they turn it off in November: in December it's back
  on by default.
- **Proposed fix:** only clear the override once the devotional is past this cycle's
  window (today > end of window and the next one hasn't started), recording which cycle
  the override covers. What should happen to a choice made before the window is open in
  §5 (Q2).
- **✅ Fixed 2026-09-22** (per Q2 answer: a choice made before the window opens carries
  into that upcoming window). Same cycle-key mechanism as P0-1:
  `DevotionalNotificationStore` now stores `(enabled, cycleKey)`;
  `Util.isNotifyingEffective` only honors the stored override while its `cycleKey`
  still equals `Util.resolveCycleKey(schedule, today)` — since that key already
  resolves to the *next upcoming* window before it opens, a November toggle survives
  into December without a separate "is it before/after the window" branch.

#### P0-3 · Turning notifications on after 9am fires them immediately (and duplicated)

- **Files:** `presentation/MainViewModel.kt:94-104`,
  `reminder/NovenaReminderScheduler.kt:71-87, 127-130`
- **What happens:** `toggleNotification` enqueues with `REPLACE` and `from = today`. If
  the target time has already passed, `delayUntil` returns `Duration.ZERO`.
- **Scenario:** on December 18 at 21:00 the user turns notifications on and immediately
  gets **two** notifications (the 9am reminder and the 8pm check-in). Toggling off and
  back on in the afternoon repeats the day's reminder even though it already arrived at
  9am.
- **Proposed fix:** if today's slot has already passed, schedule for tomorrow (or the
  next window start). That way `ZERO` only applies to genuinely overdue work. Use
  `ExistingWorkPolicy.KEEP` when turning on if work is already enqueued.
  Also, `toggleNotification` uses `LocalDate.now()` rather than `Util.currentDate()`:
  confirm whether that's intentional, as it is in the workers.
- **✅ Fixed 2026-09-22.** `NovenaReminderScheduler.enqueue()` now loops (bounded to 400
  attempts) advancing the candidate date and re-resolving `resolveTargetDate()` each
  iteration until the target hour is genuinely in the future — including jumping to
  the devotional's next occurrence if bumping the date lands outside the current
  `FixedRange` window. `LocalDate.now()` was left as-is in `toggleNotification` (not
  flagged as wrong by the owner during the fix pass; still open if it should switch to
  `Util.currentDate()` for consistency with the debug-date override used elsewhere).

#### P0-4 · The notification/widget intent is re-processed when the Activity is recreated

- **Files:** `MainActivity.kt:66-89, 157-162`, `presentation/MainViewModel.kt:106-118`
- **What happens:** `onCreate` always runs `intent.extractPendingTarget()`, including
  when `savedInstanceState != null`. `onPendingTargetConsumed` clears the Compose state,
  but the extras stay on the intent. `GoToDay` navigates without `launchSingleTop`.
- **Scenario:** the user taps the day-3 notification and lands on DayScreen. On rotation
  or a system theme change, navigation restores DayScreen **and** navigates again:
  DayScreen(3) ends up on the back stack twice and "back" returns to the same day. If
  they had already gone back to Home, rotating reopens day 3.
- **Proposed fix:** only process the intent when `savedInstanceState == null` (and in
  `onNewIntent`), and strip the extras once consumed (`intent.removeExtra(...)`). Add
  `launchSingleTop` to `GoToDay`.
- **✅ Fixed 2026-09-22.** `onCreate` now guards `intent.extractPendingTarget()` behind
  `if (savedInstanceState == null)`; `onPendingTargetConsumed` also calls
  `intent.removeExtra(EXTRA_TARGET_DAY)` and `intent.removeExtra(EXTRA_TARGET_DEVOTIONAL_ID)`.
  `GoToDay` navigation now passes `launchSingleTop = true`.

#### P0-5 · Carols: the previous song flashes, and a "TODO(fix)" text can show

- **Files:** `presentation/AppNavHost.kt:184-214`,
  `presentation/screens/lyrics/LyricsViewModel.kt:22-36`
- **What happens:** `selectedSong` is a `StateFlow` that keeps the last song and is never
  cleared. The new one is set in a `LaunchedEffect`, which runs **after** the first
  frame.
- **Scenario:** the user opens "Noche de paz", goes back and opens "Tutaina". For the
  first frame they see the title and lyrics of "Noche de paz". If the `songID` doesn't
  exist, the screen shows the developer text **"TODO(fix)"** (line 212).
- **Proposed fix:** resolve the song synchronously from the navigation argument (or
  clear `selectedSong` on exit) and show an empty state using a `strings.xml` text.
- **✅ Fixed 2026-09-24** (block C, per the Q11 answer). The shared `selectedSong`
  `StateFlow` and `getSongById` are gone. The lyrics route resolves its song with
  `remember(songID) { lyricsViewModel.findSong(context, it) }`, which loads the list
  first if needed (e.g. a restore straight onto this screen), so the first frame
  already shows the right song. A missing id shows `MissingContentMessage` with the
  new `error_song_unavailable` string instead of `"TODO(fix)"`. The two
  `Log.d("Test")` calls in this route went with it (part of P3-2). Reading the JSON on
  the main thread is unchanged (P2-1).

#### P0-6 · English narration uses a Spanish voice

- **Files:** `presentation/screens/prayer/PrayerNarrator.kt:51`
- **What happens:** `engine.language = Locale.forLanguageTag("es-CO")` is hardcoded, but
  content loads in English when the device language is `en`
  (`DevotionalRepositoryImpl.kt:23`).
- **Scenario:** an English user taps "Listen" and hears English text pronounced by a
  Spanish voice. Also, if the voice pack is missing (`LANG_MISSING_DATA`), the button
  does nothing and gives no feedback.
- **Proposed fix:** use the same language the content was loaded in (pass it to the
  narrator), check the result of `setLanguage`, and tell the user if the voice isn't
  available.
- **✅ Fixed 2026-09-24** (block C). The repository's "English content for `en`, Spanish
  for everything else" rule is now a single function, `contentLanguageFor`
  (`DevotionalRepositoryImpl.kt`), also used by `PrayerNarrator`, so the voice can't
  drift from the text. `PrayerScreen` takes the device language and passes it to
  `rememberPrayerNarrator`. The narrator tries `Locale.ENGLISH` for English content,
  and `es-CO` then any `es` voice for Spanish, checking each `setLanguage` result.
  `speak()` returns a `SpeakResult`. `VoiceUnavailable` (no TTS engine, or no usable
  voice) shows the new `prayer_voice_unavailable` Toast. A tap while the engine is
  still starting up does nothing, same as before.

#### P0-7 · "Rate this app" can crash the app

- **Files:** `presentation/screens/more/MoreScreen.kt:218-223`
- **What happens:** `startActivity` with `market://` has no `try/catch` and no fallback.
- **Scenario:** on devices without the Play Store (Huawei, emulators without Google
  Play, some tablets) it throws `ActivityNotFoundException` and the app crashes.
- **Proposed fix:** catch the exception and fall back to
  `https://play.google.com/store/apps/details?id=…`.
- **✅ Fixed 2026-09-24** (block C). `openPlayStore` catches `ActivityNotFoundException`
  on `market://` and opens the web listing. If there's no browser either, the tap does
  nothing instead of crashing.

---

### P1 — Minor bugs, UI and accessibility

| ID | Finding | Files | Proposed fix |
|---|---|---|---|
| P1-1 | `LottieAnimationRender` takes a `modifier` but uses `Modifier.fillMaxSize()`, so the balloons' offset on December 25 is never applied. | `screens/home/HomeScreen.kt:151-158, 115` | Use `modifier.fillMaxSize()`. **✅ Fixed 2026-09-22** (incidentally, while rewriting this function for Q9/P2-6). |
| P1-2 | If **any** devotional is on its celebration day, Home shows only the animation and gives no access to any devotional's prayers. | `screens/home/HomeScreen.kt:60, 67-70` | Pending decision (§5 Q6). **✅ Fixed 2026-09-22.** Per the Q6 answer, the celebration animation now renders in the same background slot the snow otherwise occupies; the devotionals feed (`Column`) always renders on top, so Dec 25 no longer hides any card. |
| P1-3 | The top bar's Notifications and Profile buttons do nothing (`onClick = {}` by default; `MainActivity` passes no handler). "Profile" makes no sense in an app without accounts. | `components/NovenAppBar.kt:42-43, 79-95`, `MainActivity.kt:175-183` | Pending decision (§5 Q7). Per the owner's correction this session, the icons are **not** to be removed — the Notifications icon is meant to eventually open a list of active-notification devotionals, a feature not yet specced. Still open: what (if anything) the Profile icon should do. |
| P1-4 | The `POST_NOTIFICATIONS` permission is requested **every time** Home comes back into view (the `LaunchedEffect(Unit)` restarts on each tab switch), not once per launch as the comment says. It's requested on first launch with no explanation. | `screens/home/HomeScreen.kt:123-148` | Request it only once (remember that it was requested) or when a notification is turned on. The exact timing is in §5 (Q8). **✅ Fixed 2026-09-22** (per Q8 answer). `rememberNotificationPermissionRequester()` returns a lambda invoked only from the long-press handler when `!wasNotifying`, replacing the old unconditional `LaunchedEffect(Unit)`. |
| P1-5 | The Toast says "activated: you'll get alerts" even when the notification permission is denied, and the "Active" badge still shows. | `screens/home/HomeScreen.kt:94-101` | Check the permission when turning on; if denied, request it or explain how to enable it in Settings. **✅ Fixed 2026-09-22** (with R1, see §9.5). Turning on now checks `NotificationManagerCompat.areNotificationsEnabled()`: if enabled → "activated" Toast; if the Android 13+ permission is missing → the system dialog is shown and the Toast comes from its result; otherwise (denied for good, or blocked in system Settings) → a new `devotional_notifications_blocked` Toast pointing to Settings. The "Active" badge itself is unchanged (R1 option (c) wasn't chosen). |
| P1-6 | `currentStageTitle` isn't reset to `null` when leaving DayScreen, so entering another day briefly shows the title of the previous day's last page. | `MainActivity.kt:142-144, 184-187` | Set it to `null` on route change or reset it per navigation entry. **✅ Fixed 2026-09-24** (see §9.10). |
| P1-7 | The card uses `combinedClickable(onClick = {})`: tapping it shows the touch ripple and does nothing. The ripple isn't clipped to the rounded shape (the modifier is applied outside `ElevatedCard`). There's no `onLongClickLabel`, so the long press can't be discovered with TalkBack. | `screens/home/HomeScreen.kt:93-101`, `screens/home/JourneyCard.kt:113` | Add `onLongClickLabel`, clip to the card's `shape`, and decide what a plain tap does (§5 Q7). **✅ Fixed 2026-09-22** for the ripple clipping and `onLongClickLabel` (approved with the "7 bugs" fix pass). What a plain tap should do is still open — currently still a no-op `onClick = {}`. |
| P1-8 | Missing plurals: "Faltan 1 días" / "1 days to go". There's no `<plurals>` anywhere in the project. | `res/values/strings.xml:63`, `res/values-en/strings.xml:61`, `widget/NovenaWidget.kt:115`, `screens/home/JourneyCard.kt:355` | Move to `<plurals>` and `pluralStringResource`. Also, `NotStartedBody` reuses a widget string: split them. **✅ Fixed 2026-09-24** (see §9.10). |
| P1-9 | `"%1$s activada"` hardcodes the grammatical gender and doesn't fit masculine devotional names ("Rosario activada"). | `res/values/strings.xml:84-85` | Reword without gender ("Avisos activados para %1$s"). **✅ Fixed 2026-09-24** (see §9.10). |
| P1-10 | The Gozos music and the voice narration keep playing with the app in the background. Audio focus isn't requested. | `components/GozosScreen.kt:249-269`, `screens/prayer/PrayerNarrator.kt` | Pause on `ON_STOP` (`LifecycleEventEffect`) and request audio focus. **✅ Fixed 2026-09-24** (see §9.10). |
| P1-11 | The widget has hardcoded colors, so it has no dark mode or dynamic colors. It picks the devotional with `DebugClock` but computes the day with `LocalDate.now()`. | `widget/NovenaWidget.kt:50, 58, 76, 85, 95` | Use `GlanceTheme` and a single date source. |
| P1-12 | Hardcoded UI text: `"Villancicos"` as the initial title and `"TODO(fix)"` (see P0-5). The debug texts (`MoreScreen.kt:103-189`) are acceptable since they're gated by `BuildConfig.DEBUG`; the `"Dec 25"` preset label is hardcoded even though its date comes from the catalog. | `MainActivity.kt:141`, `AppNavHost.kt:212`, `MoreScreen.kt:105` | Move to `strings.xml`. **✅ Fixed 2026-09-24** (see §9.10). |
| P1-13 | A fire icon (`LocalFireDepartment`) on the "Day N of 9" pill. It's the typical streak symbol, in tension with `CLAUDE.md` §2.6. The "Listen" button uses `MusicNote`, which is easy to confuse with the Gozos music button. | `screens/prayer/PrayerScreen.kt:442, 415` | Pending decision (§5 Q5). |
| P1-14 | `LyricsScreen` has no horizontal padding and a fixed `fontSize = 18.sp`; the `songTitle` and `onBack` parameters are unused. | `screens/lyrics/LyricsScreen.kt:19-37` | Use the theme typography, add padding, and remove the unused parameters. **✅ Fixed 2026-09-24** (see §9.10). |

---

### P2 — Performance

| ID | Finding | Files | Proposed fix |
|---|---|---|---|
| P2-1 | `loadSongsFromJson` reads and parses the asset **during composition**, on the main thread, on every recomposition. `onSongTitleUpdated` also changes state during composition. | `AppNavHost.kt:169, 205`, `LyricsViewModel.kt:25-31` | Load once in the ViewModel's `init` on `Dispatchers.IO`, expose a `StateFlow`, and update the title in a `LaunchedEffect`. **✅ Fixed 2026-09-24** (see §9.10). |
| P2-2 | The novena JSON is re-read and re-parsed on every previous/next day arrow: each day is a new navigation entry with its own `produceState`, and a new `Gson()` is created on every load. | `AppNavHost.kt:114-118`, `DevotionalRepositoryImpl.kt:21-31` | Cache in the repository by `(devotionalId, language)` and reuse a single `Gson` instance. **✅ Fixed 2026-09-24** (see §9.10). |
| P2-3 | `MediaPlayer.create` runs on the main thread every time the Gozos page composes, even if the user never plays the music. | `components/GozosScreen.kt:251-256` | Create the player on the first play tap. **✅ Fixed 2026-09-24** (see §9.10). |
| P2-4 | `CountdownToDate` uses `LaunchedEffect(key1 = remainingTime)`, which restarts every second. It's dead code anyway (P4-6). | `components/CountdownToDate.kt:37-42` | Delete it (P4-6) or key on `schedule`. **✅ Fixed 2026-09-24:** file deleted (owner: "si no se usa borralo"). |
| P2-5 | `homeDevotionals` is collected with `collectAsState`, which stays active with the app in the background. | `AppNavHost.kt:61, 232-233` | Use `collectAsStateWithLifecycle` (add `lifecycle-runtime-compose`). **✅ Fixed 2026-09-22** (approved with the "7 bugs" fix pass). Added `androidx-lifecycle-runtime-compose`; `homeDevotionals` and `DayScreen`'s completed-days flow now use `collectAsStateWithLifecycle()`. |
| P2-6 | The snow Lottie animation loops forever behind Home: it drains battery and ignores the reduce-animations setting. | `screens/home/HomeScreen.kt:72, 151-158` | Pause when "Remove animations" is on, and consider stopping after N loops (visual decision: §5 Q9). **✅ Fixed 2026-09-22** (per Q9 answer: stop after a time budget, not a loop count). `LottieAnimationRender` now loops for up to `maxLoopDuration` (30s default) via `rememberLottieAnimatable()`, and skips animating entirely when `Settings.Global.ANIMATOR_DURATION_SCALE == 0f` (system "Remove animations" on). |

---

### P3 — Security and build

Overall risk is **low**: no network, no accounts, no sensitive data. What's already
right: `MainActivity` is exported but `AppNavHost.kt:101` validates the incoming day and
`DevotionalCatalog.findById` falls back to a default if the id doesn't exist; the
`PendingIntent`s use `FLAG_IMMUTABLE`; `novenapp.jks` and `local.properties` are in
`.gitignore` and aren't in the repository.

| ID | Finding | Files | Proposed fix |
|---|---|---|---|
| P3-1 | `isMinifyEnabled = false` in release: no obfuscation, no code or resource shrinking, and the debug code ships in the APK. **Turning it on without rules breaks Gson** for the `DevotionalContent`/`Song` models. | `app/build.gradle.kts` (`release` block), `app/proguard-rules.pro` | Enable R8 together with `@Keep`/rules for the models, or do it after migrating to kotlinx.serialization (P4-1). It touches release config, so it needs explicit approval (`CLAUDE.md` §1.1.5). |
| P3-2 | `Log.d("Test", …)` calls remain in production code. | `AppNavHost.kt:106, 190, 202` | Remove them. **✅ Fixed 2026-09-24** (see §9.10). |
| P3-3 | `backup_rules.xml` and `data_extraction_rules.xml` are the empty templates: everything is backed up without a decision (including `debug_settings`). | `res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml` | Include progress and notification preferences, exclude `debug_settings` (§5 Q10). **✅ Fixed 2026-09-22** (per Q10 answer: yes, back up). Both files now `<include>` the two DataStore preference files (`devotional_progress.preferences_pb`, `devotional_notifications.preferences_pb`) and `<exclude>` `debug_settings.xml`. |
| P3-4 | An unstructured `CoroutineScope(Dispatchers.Default)` with no exception handling in `Application.onCreate`. A DataStore or WorkManager error there crashes the app on launch. | `NovenAppApplication.kt:23-25` | Add a `SupervisorJob` and `CoroutineExceptionHandler`, or move scheduling into a startup worker. **✅ Fixed 2026-09-22** (approved with the "7 bugs" fix pass). `startupScope` now combines `SupervisorJob()` with a `CoroutineExceptionHandler` that logs instead of crashing. |

---

### P4 — Architecture, best practices and cleanup

| ID | Finding | Files | Proposed fix |
|---|---|---|---|
| P4-1 | **Gson ignores Kotlin default values.** `DevotionalContent` has no no-arg constructor, so Gson creates the object without calling a constructor: if a JSON omits `everyDayBlocks` or `closingBlocks`, they end up `null` despite `= emptyList()`, and a missing `title` also ends up `null` even though its type doesn't allow it. The result is an NPE at `DevotionalDayMapper.kt:44`. It doesn't happen today (both JSON files are complete, validated), but it's latent for every new devotional. | `model/DevotionalContent.kt`, `mapper/DevotionalDayMapper.kt:44`, `LyricsViewModel.kt:27` | Migrate to kotlinx.serialization, which honors default values and nullability, or validate the JSON on load. Deserves its own spec. |
| P4-2 | **The data layer depends on the presentation layer.** `DevotionalMeta`, `DevotionalSchedule`, `DevotionalCatalog` and `DevotionalContent` live in `presentation/model` but are imported by `data/`, `reminder/`, `widget/` and `debug/`. | `presentation/model/**`, `data/devotional/*`, `reminder/*`, `widget/*` | Move them to a `domain/` or `data/model` package. It's only a package move. |
| P4-3 | The ViewModel takes a `Context` parameter (`toggleNotification`). `NovenaReminderScheduler` and `DebugNotificationTrigger` are `object`s that fetch their own dependencies from Koin (`KoinComponent`), in tension with the "everything goes through the DI graph" principle (`CLAUDE.md` §2.4). They're also hard to test. | `MainViewModel.kt:94`, `reminder/NovenaReminderScheduler.kt:37-40`, `debug/DebugNotificationTrigger.kt` | Turn the scheduler into a class injected with `applicationContext` and use it from the ViewModel. |
| P4-4 | `Util.resolveNextOccurrence` and `Util.calculateTimeRemaining` don't handle windows that cross the year boundary (e.g. Dec 28–Jan 5), unlike `resolveCurrentDay`. `calculateTimeRemaining` also mixes `DebugClock` with `System.currentTimeMillis()`. It's latent with the current novena and goes against the per-devotional date config principle (§2.2). | `presentation/util/Util.kt:55-64, 97-110` | Rewrite with the same strategy as `resolveCurrentDay` and cover it with tests (P4-8). |
| P4-5 | The Home sort order is duplicated: `HOME_SORT_ORDER` and the `pickFeaturedDevotional` comparator are copies and can drift apart. | `MainViewModel.kt:120-129`, `Util.kt:87-95` | Extract a single comparator. **✅ Fixed 2026-09-22** (approved with the "7 bugs" fix pass). `Util.devotionalFeatureOrder` is now the single comparator; `MainViewModel.HOME_SORT_ORDER` and `Util.pickFeaturedDevotional` both delegate to it. |
| P4-6 | **Dead code:** `MainTitle.kt`, **`MainTitle.kt~` (a backup tracked in git)**, `CountdownToDate.kt`, `NovenaAction.GoHome`, `Util.calculateTimeRemaining`, the `text_navidad_en*` strings. While the image-less blocks are in use, `buildDevotionalDayPrayers`, `ReadingWithImageScreen` and `DevotionalMeta.images` are also unused (depends on §5 Q3). `DevotionalDayContent.day` is ignored (the day is looked up by index). | `presentation/components/MainTitle.kt(~)`, `components/CountdownToDate.kt`, `NovenaAction.kt:5`, `Util.kt:97`, `strings.xml:7-8`, `AppNavHost.kt:123-124`, `mapper/DevotionalDayMapper.kt:21-25`, `model/DevotionalContent.kt:18` | Delete once approved; deleting files requires a separate confirmation (`CLAUDE.md` §1.1.5). |
| P4-7 | The card gradient's hardcoded colors violate `CLAUDE.md` §2.7. It also uses `isSystemInDarkTheme()` instead of the app's actual theme. | `screens/home/JourneyCard.kt:76-91` | Derive them from `colorScheme.primaryContainer` and `secondaryContainer`. |
| P4-8 | There are no real tests (only `ExampleUnitTest`). The date logic (`Util`), `resolveJourneyStatus` and the mapper are pure functions that are easy to test. | `app/src/test/**` | Add unit tests, starting with the P0-1, P0-2 and P4-4 cases. |
| P4-9 | Text styling: a fixed `18.sp` instead of the theme typography; `Arrangement.Center` inside `verticalScroll` has no effect; `TextAlign.Justify` hurts readability at large font sizes. | `screens/prayer/ReadingScreen.kt:49-58`, `ReadingWithImageScreen.kt:107-114` | Use a scaled `bodyLarge` and left alignment (the visual change is in §5 Q9). **Partially fixed 2026-09-22**: `TextAlign.Justify` → `TextAlign.Start` in both files (per Q9). The fixed `18.sp`/theme-typography and the dead `Arrangement.Center` are still open. |
| P4-10 | Compose details: `PrayerFooter` rebuilds its list on every recomposition and uses `SpaceBetween` inside `horizontalScroll`, where it has no effect; `!!` on Compose state (`PrayerFooter.kt:57`, `AppNavHost.kt:205-208`); `ReadingWithImageScreen` has unused imports and uses `LocalConfiguration.screenHeightDp` instead of `BoxWithConstraints`; `PreviewApp` applies the theme twice. | `screens/prayer/PrayerFooter.kt`, `ReadingWithImageScreen.kt`, `MainActivity.kt:307-314` | Minor cleanup. |
| P4-11 | Build: `sourceCompatibility`/`jvmTarget` at 1.8 (17 recommended); deprecated `kotlinOptions` block; `versionCode = 12` set by hand while `VERSION_NAME` comes from `gradle.properties`. | `app/build.gradle.kts` | Update. It touches the build, so it needs approval. |
| P4-12 | `CLAUDE.md` §2.7 mentions the `RedChristmas`/`GreenChristmas`/`GoldChristmas` tokens, which no longer exist in `ui/theme/Color.kt`. | `CLAUDE.md` | Propose the change as a diff for approval (don't edit directly). |

---

## 4. Decisions

> This section is what gets approved. None of the following is decided until the owner
> confirms it.

### 4.1 How to split the work (recommendation)

| Block | Contents | Why separate | Status |
|---|---|---|---|
| A · `season-progress-reset` | P0-1 | Changes the persisted data model and needs a migration. | **✅ Done 2026-09-22** |
| B · `notification-scheduling-fixes` | P0-2, P0-3, P1-4, P1-5 | All one flow: permission, manual override, WorkManager. | P0-2, P0-3, P1-4 done 2026-09-22; P1-5 and the R1 regression done 2026-09-22 (§9.5). Related follow-ups R5/R6 still open. |
| C · `v3-bugfix-batch` | P0-4, P0-5, P0-6, P0-7, P1-1, P1-6, P1-7, P1-8, P1-9, P1-10, P1-12, P1-14, P2-1…P2-5, P3-2, P3-4 | Well-scoped fixes with no pending product decisions. | P0-4, P1-1, P1-7 (partial), P2-5, P3-4 done 2026-09-22. **Approved 2026-09-24 (Q11).** Everything done 2026-09-24 (§9.10), including P2-4. **Block C complete** (pending on-device testing). |
| D · `serialization-and-layers` | P4-1, P4-2, P4-3, P3-1 | Cross-cutting refactor; R8 depends on the serialization. | Not started — deferred until after the season (see Q11). |
| E · `cleanup-and-tests` | P4-4…P4-11 | Technical debt; the tests protect A and B. | Not started, except P4-5, P4-9 (partial) done 2026-09-22 as drive-by fixes. Deferred (see Q11). |
| — | P1-2, P1-3, P1-11, P1-13, P2-6, P3-3, P4-12 | Depend on the answers in §5. | P1-2, P2-6, P3-3 done 2026-09-22 (Q6/Q9/Q10). P1-3 partially answered (icons stay). **Still open:** P1-11, P1-13, P4-12. |

**Recommendation:** A and B first (they affect the December 2026 season), then C. D and
E can come after the season. **Status as of 2026-09-22:** A and B (except P1-5) are
done; several C and §5-dependent items got fixed alongside them while touching the
same code. See Q11 below for whether the remaining, smaller C list should also go now
or wait with D/E.

### 4.2 Recommended technical fixes

The proposal under each finding in §3 is the default recommendation. Approving a block
approves those proposals unless stated otherwise.

## 5. Open questions

Decisions that belong to the owner. Each one notes which finding it unblocks.

- **Q1 (P0-1) — How should progress reset each season?**
  - (a) Store progress per season (`completed_days_<id>_<startYear>`): past years'
    history is kept and a future "history" view becomes possible. *Recommended.*
  - (b) Clear progress when the window closes, same as the notification override:
    simpler, but the history is lost.
  - (c) Store full dates instead of day numbers.
  - And in any case, what happens to the progress already stored without a year
    (migration: assign it to 2025, or discard it)?
  - **Decision (2026-09-22):** (b) — "no tiene sentido forzar un historial que nadie
    revisará, el reinicio es lo mejor." No one would review a season-history view, so
    it isn't worth the extra storage complexity. The owner added a nuance beyond the
    original options: a future **daily** devotional (e.g. a Rosary, `Evergreen`
    schedule) is different — it should only reset when the owner/user resets it
    manually, never automatically. That's why the fix generalized the mechanism into
    a `cycleKey` shared by progress and notifications, with `Evergreen` mapping to a
    constant key that never changes on its own. No explicit migration step for old
    keyless data was needed — it reads as stale (empty) on first load under the new
    scheme, which is the same end state option (b) asked for.
- **Q2 (P0-2) — Notification choice made before the window opens.** If the user turns
  notifications on or off in November, should that carry into the upcoming window
  (recommended), or should the long press not be available outside the window?
  - **Decision:** resolved as part of Q1's cycle-key design (not asked separately) —
    it carries into the upcoming window, per the recommended option. See P0-2 above.
- **Q3 (P4-6) — Images disabled.** The uncommitted changes switched to
  `buildDevotionalDayPrayersWithoutImages` and the image version is commented out
  (`AppNavHost.kt:123`). Is that final (and the related code and drawables get deleted),
  temporary, or should it be a user option?
  - **Decision (2026-09-22):** "dejemosla en todo para un próximo roadmap" — leave the
    images-off state as-is for now; revisit whether to delete the image-path code and
    drawables, or turn it into a user option, in a future roadmap item. Not touched
    this session.
- **Q4 (P0-5 / language) — Carols only in Spanish.** `songs.json` has no English
  version. Is that intentional (they're Spanish carols), or is English content needed?
  - **Decision (2026-09-22):** "falta contenido en inglés" — English content is
    missing and needed; this is a content gap, not a code bug. No code change; needs
    English lyrics content to be authored/added to `songs.json` before it can close.
- **Q5 (P1-13) — Day screen icons.** Should the fire on the day pill be replaced with a
  neutral icon (calendar, candle, star)? And the "Listen" icon with a speaker or
  headphones?
  - **Status:** still open — "pendiente por definir." Not decided this session.
- **Q6 (P1-2) — December 25.** Should Home show only the celebration animation, or the
  animation plus access to the prayers/cards? With several devotionals, should one's
  celebration hide the others?
  - **Decision (2026-09-22):** "animación + acceso" — the celebration animation plays
    alongside full access to the devotionals feed, not instead of it. Implemented: see
    P1-2 above.
  - **Revised (2026-10-19, after on-device test 2.3):** the cards covered the
    celebration sign. "Ocultar solo las que celebran": on its celebration day, a
    devotional's own card is hidden from Home; any other devotional keeps its card. With
    today's single novena, Dec 25 shows only the animation (no access to the prayers
    that day, accepted). Same session, "dismiss por sesión": the day screen's preview
    banner gets a close button; once closed it stays hidden until the next app launch
    (in-memory flag on `MainViewModel`, nothing persisted).
- **Q7 (P1-3, P1-7) — Top bar and card actions.** Should the Notifications and Profile
  buttons be removed, or given a purpose (e.g. Notifications → reminder settings)?
  Should tapping the journey card open the current day, same as its button?
  - **Decision (2026-09-22, partial):** "no borres los iconos, habíamos quedado que
    debería ser algo que guarde o muestre la lista de notificaciones para las
    activas" — the Notifications icon should eventually open a list of
    devotionals with active notifications (not yet specced or built; corrected an
    earlier wrong recommendation from the assistant to remove it). **Still open:**
    what the Profile icon should do, and whether a plain tap on the card should open
    the current day.
- **Q8 (P1-4) — When to request the notification permission.** On first launch (as now,
  but only once), when a notification is turned on, or behind a screen that explains
  why first?
  - **Decision (2026-09-22):** the assistant's recommendation was accepted — request
    it only at the moment a notification is actually turned on via long-press, not on
    every Home visit. Implemented: see P1-4 above.
  - **Reopened by R1 (§9.2) and re-decided (2026-09-22):** "adelante con Q8 opción (a)
    junto con P1-5" — additionally ask **once, automatically** when Home shows any
    devotional that's effectively on (covers default-on novenas), plus the P1-5
    permission-aware Toast. Implemented: see §9.5.
- **Q9 (P2-6, P4-9) — Visual tweaks.** Should the snow keep looping forever? Should the
  prayer text change from justified to left-aligned?
  - **Decision (2026-09-22):** snow (and other decorative Lottie animations) stop
    after a time budget rather than looping forever — "por tiempo de uso mayor a x
    tiempo" (30s, chosen as a reasonable default; not separately specced). Text:
    left-aligned, not justified. Both implemented: see P2-6 and P4-9 above.
- **Q10 (P3-3) — Backup.** Should progress and notification preferences be backed up to
  the cloud (recommended), or nothing?
  - **Decision (2026-09-22):** "sí" — back up progress and notification preferences,
    exclude the debug forced-date override. Implemented: see P3-3 above.
- **Q11 (§4.1) — Order and split.** Do you agree with blocks A–E and with prioritizing A
  and B before December?
  - **Status:** re-explained to the owner on 2026-09-22 (they asked "explícame mejor
    de qué va") — still awaiting an explicit answer. As of this session, blocks A and
    B are effectively done (only P1-5 remains in B), which narrows the practical
    question to: (1) do the remaining `v3-bugfix-batch` items (P0-5, P0-6, P0-7, P1-6,
    P1-8, P1-9, P1-10, P1-12, P1-14, P2-1…P2-4, P3-2) get picked up now, and (2) does
    the original recommendation to defer blocks D (`serialization-and-layers`) and E
    (`cleanup-and-tests`) until after the December 2026 season still stand. Not yet
    decided.
  - **Decision (2026-09-24), part (1):** "adelante con el bloque C, empieza por P0-5,
    P0-6 y P0-7". Block C is approved now, starting with those three. Part (2),
    deferring D/E until after the season, hasn't been explicitly answered yet.

## 6. Tasks

Reflects what's actually been implemented, not a pre-approved plan — each item below
was authorized either by the "arregla los 7 bugs" instruction or by that finding's Q
answer in §5. Owner has not yet done on-device testing of any of it.

- [x] P0-1 — cycle-key based progress reset (`Util.resolveCycleKey`,
      `DevotionalProgressStore`, `Util.resolveEffectiveCompletedDays`)
- [x] P0-2 — notification override survives into the upcoming window (same cycle-key)
- [x] P0-3 — `NovenaReminderScheduler.enqueue()` advances past already-passed times
- [x] P0-4 — `savedInstanceState` guard + extras cleared + `launchSingleTop`
- [x] P1-1 — `LottieAnimationRender` honors its `modifier` parameter
- [x] P1-2 — celebration animation no longer hides the devotionals feed
- [x] P1-4 — notification permission requested only on long-press activation
- [x] P1-7 — ripple clipped to card shape, `onLongClickLabel` added
- [x] P2-5 — `collectAsStateWithLifecycle` for `homeDevotionals` and day progress
- [x] P2-6 — Lottie animations stop after a 30s time budget, skip when reduce-motion is on
- [x] P3-3 — backup/data-extraction rules include progress + notifications, exclude debug
- [x] P3-4 — `SupervisorJob` + `CoroutineExceptionHandler` on the startup scope
- [x] P4-5 — single `Util.devotionalFeatureOrder` comparator, no duplication
- [x] P4-9 (partial) — `TextAlign.Start` in `ReadingScreen`/`ReadingWithImageScreen`
- [x] P1-5 — Toast is permission-aware (enabled / ask-then-confirm / blocked → Settings)
- [x] R1 — one automatic permission ask when Home shows a devotional that's effectively on
- [x] P0-5 — lyrics resolved synchronously from the nav argument, string empty state
- [x] P0-6 — narration voice follows the content language; Toast when no voice exists
- [x] P0-7 — "Rate this app" falls back to the web listing, never crashes
- [x] Rest of `v3-bugfix-batch`: P1-6, P1-8, P1-9, P1-10, P1-12, P1-14, P2-1, P2-2,
      P2-3, P3-2 (§9.10)
- [x] P2-4 — `CountdownToDate.kt` deleted (explicit OK 2026-09-24)
- [ ] Block D (`serialization-and-layers`) and Block E (`cleanup-and-tests`) — blocked
      on Q11 part (2)
- [ ] Q5 (day-screen icons), Q7 profile-icon purpose, Q11 (order/split) — awaiting
      owner answers

## 7. Follow-ups

- Once P0-1 is resolved, evaluate whether a history of past seasons adds value
  (positive-only framing, `CLAUDE.md` §2.6). That would get its own spec.
- Review the `raw/` and `drawable/` resources left unused after Q3 to reduce APK size.
- Review every screen with TalkBack after block C.
- Spec out the Notifications-icon feature agreed in Q7: a list/view of devotionals with
  active notifications, reachable from `NovenAppBar`'s bell icon.
- Author English carol content for `songs.json` (Q4) — content gap, not a code task.

## 8. Files

| File | Findings |
|---|---|
| `app/build.gradle.kts`, `app/proguard-rules.pro` | P3-1, P4-11 |
| `app/src/main/res/values/strings.xml`, `values-en/strings.xml` | P1-8, P1-9, P1-12, P4-6 |
| `app/src/main/res/xml/backup_rules.xml`, `data_extraction_rules.xml` | P3-3 |
| `MainActivity.kt` | P0-4, P1-3, P1-6, P1-12, P4-10 |
| `NovenAppApplication.kt` | P3-4 |
| `data/progress/DevotionalProgressStore.kt` | P0-1 |
| `data/notifications/DevotionalNotificationStore.kt` | P0-2 |
| `data/devotional/DevotionalRepositoryImpl.kt` | P0-6, P2-2, P4-1 |
| `presentation/MainViewModel.kt` | P0-3, P0-4, P4-3, P4-5 |
| `presentation/NovenaAction.kt` | P4-6 |
| `presentation/AppNavHost.kt` | P0-5, P2-1, P2-2, P2-5, P3-2, P4-6, P4-10 |
| `presentation/mapper/DevotionalDayMapper.kt` | P4-1, P4-6 |
| `presentation/model/DevotionalContent.kt`, `model/devotional/*` | P4-1, P4-2, P4-6 |
| `presentation/util/Util.kt` | P4-4, P4-5, P4-6 |
| `presentation/screens/home/HomeScreen.kt` | P1-1, P1-2, P1-4, P1-5, P1-7, P2-6 |
| `presentation/screens/home/JourneyCard.kt` | P1-7, P1-8, P4-7 |
| `presentation/screens/home/JourneyStatus.kt` | P0-1 |
| `presentation/screens/prayer/PrayerScreen.kt` | P1-13 |
| `presentation/screens/prayer/PrayerNarrator.kt` | P0-6, P1-10 |
| `presentation/screens/prayer/ReadingScreen.kt`, `ReadingWithImageScreen.kt`, `PrayerFooter.kt` | P4-6, P4-9, P4-10 |
| `presentation/screens/lyrics/LyricsViewModel.kt`, `LyricsScreen.kt` | P0-5, P1-14, P2-1 |
| `presentation/screens/more/MoreScreen.kt` | P0-7, P1-12 |
| `presentation/components/NovenAppBar.kt` | P1-3 |
| `presentation/components/GozosScreen.kt` | P1-10, P2-3 |
| `presentation/components/CountdownToDate.kt`, `MainTitle.kt`, `MainTitle.kt~` | P2-4, P4-6 |
| `reminder/NovenaReminderScheduler.kt` | P0-2, P0-3, P4-3 |
| `reminder/EveningCheckInWorker.kt` | P0-1 |
| `widget/NovenaWidget.kt` | P1-8, P1-11 |
| `debug/DebugNotificationTrigger.kt` | P0-1, P4-3 |
| `CLAUDE.md` | P4-12 (as a diff for approval only) |

## 9. Re-validation (2026-09-22, second pass)

A second audit pass over the fixes recorded above: every "✅ Fixed" item was re-read in
code and its boundary scenarios traced by hand (per `CLAUDE.md` §1.4), the still-open
items were re-scanned to confirm they're genuinely still open, and
`./gradlew :app:compileDebugKotlin` was run (passes, no errors). There are still no unit
tests, so every verdict below comes from reading and tracing the code by hand, not from
automated checks. None of this replaces on-device testing.

### 9.1 Verdict on the fixes

| ID | Verdict | Boundary scenarios traced |
|---|---|---|
| P0-1 | ✅ Correct as specified | Legacy keyless data → stale/empty. Dec 16–24 → same key `YYYY-12-16`. Dec 25 → key flips to next year's start → progress resets (see R3 for the UX consequence). Evergreen → constant key, never resets. |
| P0-2 | ✅ Correct | November ON → key = upcoming Dec 16 → honored through Dec 24 → ignored from Dec 25. In-window OFF → honored until Dec 24. Evergreen override persists. |
| P0-3 | ✅ Correct | ON at 21:00 Dec 18 → 9am and 8pm both land on Dec 19. ON at 21:00 Dec 24 → next Dec 16. ON in November → Dec 16. Worker self-reschedule (`from = tomorrow`) resolves on the first pass. The loop always ends: it stops after 400 attempts, and for Evergreen the target date is always found. |
| P0-4 | ✅ Correct | Fresh tap → navigates once, extras stripped. Rotation / theme change / process-death restore → `savedInstanceState != null` → not re-processed. New tap while running → `onNewIntent`. |
| P1-1 | ✅ Correct | `modifier.fillMaxSize()` now honored, so the balloon offset applies. |
| P1-2 | ✅ Works; needs a visual check on device | Feed always renders on Dec 25. The centered "Merry" sign now sits behind the cards and may be partially covered. |
| P1-4 | ⚠️ Fixed as written, but introduces **R1** | See R1: the permission is never requested for notifications that are *on by default*. |
| P1-7 | ⚠️ Label ✅; ripple clip introduces **R2** | See R2. |
| P2-5 | ✅ Correct | Both collectors are lifecycle-aware; the dependency was added through `libs.versions.toml`. |
| P2-6 | ✅ Works; see R4 | Stops after ~30s; restarts every time Home comes back into view. |
| P3-3 | ✅ Correct | The DataStore paths (`files/datastore/*.preferences_pb`) match the store names. The `<include>` makes backup opt-in, so `debug_settings` is excluded either way. |
| P3-4 | ✅ Correct | `SupervisorJob` + handler. See R6 for the same concern inside the workers. |
| P4-5 | ✅ Correct | A single comparator. |
| P4-9 | ✅ Partial, as recorded | `TextAlign.Start`. `Arrangement.Center` and the fixed `18.sp` remain. |

Items recorded as open (P0-5, P0-6, P0-7, P1-5, P1-6, P1-8…P1-14, P2-1…P2-4, P3-1,
P3-2, P4-1…P4-4, P4-6…P4-8, P4-10…P4-12) were re-scanned and are **confirmed still
present** in code (e.g. `"TODO(fix)"` at `AppNavHost.kt:216`, `es-CO` at
`PrayerNarrator.kt:51`, `market://` with no fallback at `MoreScreen.kt:219`, three
`Log.d("Test")`, zero `<plurals>`, `MainTitle.kt~` still present).

### 9.2 New findings introduced or exposed by the fixes

#### R1 · P0 (regression) — Default-on notifications never get the Android 13+ permission

- **Files:** `presentation/screens/home/HomeScreen.kt:69, 113-122, 161-180`,
  `reminder/NovenaNotifier.kt:87-95`
- **What happens:** the permission request now fires only from the long-press handler,
  and only when turning notifications **on** (`!wasNotifying`). But a `FixedRange`
  devotional is **on by default** inside its window (`Util.isNotifyingByDefault`), so no
  long press is ever needed.
- **Scenario:** a fresh install on Android 13+ on December 18. The card shows the
  "Active" badge (default on), no permission dialog ever appears, the 9am and 8pm workers
  run, and `NovenaNotifier` skips the notification because `canPost == false`. The user
  gets nothing all season. The only way to get the prompt is to long-press twice (off,
  then on), which nobody would guess. This is exactly the case `CLAUDE.md` §1.4.3
  describes: Q8's answer assumed notifications only become active via long-press, which
  isn't true for defaults.
- **Fix options (owner decision, reopens Q8):** (a) also request the permission once when
  Home shows any devotional whose effective state is on and the permission isn't granted
  (remember that it was asked); (b) request it once on first launch, as before, but only
  once; (c) show the badge as "Active, but notifications are blocked", merged with P1-5.
  *Recommended: (a) + P1-5 together.*

#### R2 · P1 (regression) — The card's `clip()` cuts off the ElevatedCard shadow

- **Files:** `presentation/screens/home/HomeScreen.kt:109-127`,
  `presentation/screens/home/JourneyCard.kt:113`
- **What happens:** `Modifier.clip(shapes.medium)` is applied *outside* `ElevatedCard`,
  and a clip also clips anything its children draw beyond the bounds, including the
  card's own elevation shadow.
- **Scenario:** the Home card loses its visible elevation or shadow and looks flat.
  Needs a quick check on device to confirm.
- **Proposed fix:** remove the outer `clip` and put `combinedClickable` *inside* the card
  (on the content `Column`), where the card's own shape already clips the ripple.

#### R3 · P1 (UX consequence of Q1) — "Completed" is visible for only a few hours

- **Files:** `presentation/util/Util.kt:141-150`,
  `presentation/screens/home/JourneyStatus.kt:68-72`
- **What happens:** the cycle key flips on the day after `endDay` (Dec 25), so progress
  reads as empty from Christmas Day on.
- **Scenario:** the user marks day 9 on Dec 24 and sees "Completed". On Dec 25, the
  celebration day, the card already shows "356 days to go" instead of their completed
  novena. The `JourneyStatus.Completed` state is now effectively reachable only on
  Dec 24. Days marked while previewing before the window (e.g. in November) do count
  toward the upcoming season, since they share its key.
- **Owner decision:** is that intended? Alternatives: flip the key after the
  *celebration* day (Dec 26), or after N days. Recorded as Q12.

#### R4 · P2 — Reduce-motion freezes on frame 0

- **Files:** `presentation/screens/home/HomeScreen.kt:197-215`
- With "Remove animations" on, every Lottie holds `progress = 0`. For the Dec 25 "Merry"
  sign, frame 0 may be empty, so with reduce-motion the celebration may show nothing.
  Proposed fix: snap to a representative frame (e.g. `progress = 1f`) instead of 0.

#### R5 · P2 (pre-existing, surfaced by the new tracing) — Next season relies on the app being opened

- **Files:** `reminder/NovenaReminderScheduler.kt:47-56`,
  `reminder/DailyNovenaReminderWorker.kt:34-36`
- If notifications were **off** at the end of a season, the chain stops, and next
  season's default-on reminders are only enqueued when `scheduleIfNeeded` runs at app
  start. A user who doesn't open the app before Dec 16 gets no reminders. Option: have
  the daily `WidgetMidnightRefreshWorker` also call `scheduleIfNeeded`.

#### R6 · P3 — Worker failures break their chain until the next app start

- **Files:** `reminder/DailyNovenaReminderWorker.kt`, `reminder/EveningCheckInWorker.kt`,
  `reminder/WidgetMidnightRefreshWorker.kt`
- Nothing in `doWork` handles failures. A DataStore read error throws, WorkManager marks
  the work failed, and the chain never reschedules. `CLAUDE.md` §1.4.4 now explicitly
  covers `doWork`. Proposed fix: wrap the body in `try/catch` and always reschedule (or
  return `Result.retry()`).

#### R7 · P4 — Minor observations

- `MainViewModel.toggleNotification` computes the cycle key with `Util.currentDate()`
  (debug clock) but schedules with `LocalDate.now()`. This only matters in debug builds
  with a forced date (still open from P0-3).
- The old DataStore keys (`notify_override_<id>`) are orphaned by the key rename.
  Harmless, but they stay on disk forever.
- `launchSingleTop` on `GoToDay`: a notification tap while already on *another* day's
  DayScreen now reuses the top entry. The pager page and text size from the previous
  day may carry over. To verify on device.

### 9.3 Spec bookkeeping to reconcile (owner's call)

- §2 Non-goals still says "No code has been touched", which is no longer true.
- **Q2 was never actually answered by the owner.** It's recorded as "resolved as part of
  Q1's cycle-key design (not asked separately)". Under `CLAUDE.md` §1.1.3 it still needs
  an explicit owner confirmation, even though the implemented behavior matches the
  recommended option.
- **Q9's 30s budget** was chosen by the assistant ("x tiempo" in the answer). It needs
  the owner's confirmation.
- The Status header says "blocks A and B … implemented", but B still has P1-5 (and now
  R1) open. §4.1 is accurate.

### 9.4 New open questions

- **Q12 (R3):** Should "Completed" survive through the celebration day (or longer), or
  reset the day after the window closes as it does now?
- ~~**Q12 (R3):**~~ Answered: reset on the 26th — see §9.6.
- ~~**Q8 (reopened, R1):** How should the permission be requested for notifications that
  are on by default?~~ Answered: option (a) together with P1-5 — see §9.5.

### 9.5 R1 + P1-5 implementation (2026-09-22)

**Files:** `data/notifications/NotificationPermissionPromptStore.kt` (new),
`di/AppModule.kt`, `presentation/MainViewModel.kt`, `presentation/AppNavHost.kt`,
`presentation/screens/home/HomeScreen.kt`, `res/values/strings.xml`,
`res/values-en/strings.xml` (new `devotional_notifications_blocked`).

- **Automatic ask (R1):** a `LaunchedEffect(anyNotifying)` in `HomeScreen` launches the
  system dialog when any devotional is effectively on, the Android 13+ permission is
  missing, and it hasn't auto-asked before. The "already asked" flag lives in
  `NotificationPermissionPromptStore`, which uses its own SharedPreferences file. That
  file is deliberately **left out of backup** (the backup rules only include the two
  DataStore files), because Android doesn't restore runtime permissions with a backup: a
  restored device must be asked again.
- **Long-press ON (P1-5):** the Toast depends on whether notifications can actually be
  shown right now: "activated" if they can; if the permission is missing, the system
  dialog first, with the Toast coming from its result; "blocked, enable in Settings"
  otherwise. Asking from the long press also marks the automatic ask as done, so a second
  dialog can't pop up. The devotional's title for the Toast is kept with
  `rememberSaveable`, so it survives a rotation while the dialog is showing.
- **Verification:** `compileDebugKotlin` passes. Scenarios traced by hand:

  | Scenario | Result |
  |---|---|
  | Fresh Android 13+ install on Dec 18 (default on) | Home asks once when the list loads; granted → reminders post; denied → never asked automatically again. |
  | Install in November (default off) | No ask until something is on: a November long-press asks right then; otherwise Home asks when opened on or after Dec 16. |
  | Long-press ON, permission granted | "Activated" Toast, same as before. |
  | Long-press ON, first or second denial | System dialog → Toast from the result. |
  | Long-press ON, denied for good | The dialog closes immediately with "denied" → "blocked" Toast. |
  | Android < 13 with app notifications off in Settings | No dialog (no runtime permission) → "blocked" Toast. |
  | Rotation while the dialog is showing | The result is still delivered, the title survives, the Toast shows. |
  | Returning to Home after the automatic ask | The flag is set → no new dialog. |
  | Backup restored onto a new device | The flag isn't restored → asked again. |

- **Known limitations (not fixed, for the owner to judge):**
  - The permission can only be requested from the UI. A user who installs before the
    season and never opens the app between Dec 16 and the first 9am slot misses those
    first reminders; option (b) would have covered this.
  - `areNotificationsEnabled()` is app-level. If the user blocks only the
    `novena_daily_reminder` channel, the Toast still says "activated". Checking the
    channel is possible if wanted.
  - The "Active" badge still shows while notifications are blocked. That was option
    (c), which wasn't chosen. *(Superseded by §9.6: a blocked bell is now shown next to it.)*
  - Not tested on a device.

### 9.6 Owner answers, round 3 (2026-09-22) and what was implemented

| Item | Owner's answer | Result |
|---|---|---|
| Q2 | "q2 confirmada" | ✅ Confirmed. Behavior unchanged: an override made before the window carries into it. |
| Q9 | "1 min" (30s was too short) | ✅ `LottieAnimationRender`'s `maxLoopDuration` default is now `1.minutes` (`HomeScreen.kt`). |
| Q12 (R3) | "reiniciar el 26" | ✅ New `Util.resolveProgressCycleKey`: same keys as `resolveCycleKey`, but a `FixedRange` cycle stays current **through its celebration day**. Used for completed-day progress only (`resolveEffectiveCompletedDays`, `MainViewModel.toggleDayCompleted`). |
| Q5 | Candle for the day pill, speaker for "Listen" | ◐ "Listen" now uses `Icons.AutoMirrored.Filled.VolumeUp`. **Candle pending:** `material-icons-extended` has no candle icon (only `CandlestickChart`, a finance chart) — needs an owner call (see §9.7). The fire icon is unchanged for now. |
| Q7 | Profile: "no hace nada por ahora. esconder, no remover". Card tap: leave as is, TODO for later | ✅ Profile hidden behind `SHOW_PROFILE_ACTION = false` in `NovenAppBar.kt`, code kept, with a `TODO(owner)`. Card `onClick = {}` kept, with a `TODO(owner)` in `HomeScreen.kt`. Both still open as product decisions (§7 follow-up). |
| R1 limitation 1 | Accepted: first-season reminders can be missed if the app isn't opened | ✅ Accepted, no change. |
| R1 limitation 2 | "sí": check the specific channel | ✅ New `NovenaNotifier.canPostNotifications()`: app-level enabled **and** the `novena_daily_reminder` channel not at `IMPORTANCE_NONE` (a channel that doesn't exist yet counts as allowed). Home's Toasts now use it. |
| R1 limitation 3 | Add a blocked-bell icon next to "Active" | ✅ `JourneyCard`/`ActiveBadge` take `notificationsBlocked`; when the devotional is on and notifications can't be shown, a `NotificationsOff` icon (tinted `colorScheme.error`, with a content description) appears next to "Active". Home re-checks on every resume (`LifecycleResumeEffect`) and after the permission dialog. |
| P1-11 (widget `GlanceTheme`) | "explícame más" | Explained to the owner; not decided. |

**Boundary tracing for Q12** (novena Dec 16–24, celebration Dec 25):

| Date | Progress key | Notification-override key | Effect |
|---|---|---|---|
| Dec 15 | 2026-12-16 | 2026-12-16 | Same as before |
| Dec 16–24 | 2026-12-16 | 2026-12-16 | Same as before |
| **Dec 25** | **2026-12-16** (kept) | 2027-12-16 (flipped) | The card still shows "Completed" if all 9 days were prayed (otherwise the countdown). A notification choice made on Dec 25 already applies to next season, so Q2 is preserved. |
| Dec 26 | 2027-12-16 | 2027-12-16 | Progress resets |
| Wrapping range Dec 28–Jan 5, celebration Jan 6 | Kept through Jan 6, reset Jan 7 | Flips Jan 6 | The celebration date is resolved relative to the window's own start year. |

A day marked on Dec 25 counts toward the season that just ended and disappears on
Dec 26, which is consistent with the answer.

**Verification:** `compileDebugKotlin` passes. The scenarios above were traced by hand.
Not tested on a device.

### 9.7 Still open after round 3

- **Q5 candle icon:** pick one of (a) import a candle vector from Material Symbols as a
  drawable, (b) another existing icon, (c) a custom drawable the owner provides.
- **P1-11 widget theming:** pending the owner's decision after the explanation.
- **Q7:** what Profile does and what a plain card tap does (both hidden or no-op with a
  `TODO(owner)`).
- **R2** (card shadow), **R4**, **R5**, **R6**, **R7**: approval pending.
- **Q11** (order of the rest of block C, and D/E after the season) and the changes that
  need explicit sign-off (P3-1 R8, P4-11 build, P4-6 deleting dead code, P4-12
  `CLAUDE.md` diff).
- Spec bookkeeping (§9.3): the §2 "No code has been touched" line and the Status header.

### 9.8 Owner answers, round 4 (2026-09-22)

| Item | Owner's answer | Result |
|---|---|---|
| Q5 candle | Option (a): Material Symbols | ✅ `res/drawable/candle_24px.xml` is Google's official Material Symbols "candle" (outlined, filled variant), fetched from `google/material-design-icons` in the same format as the existing `music_note_24px.xml`. `PrayerScreen`'s `ProgressPill` uses it in place of `LocalFireDepartment`. **Q5 closed.** |
| P1-11 widget | Option (a): the app's brand palette | ✅ `NovenaWidget` content is wrapped in `GlanceTheme(ColorProviders(light = LightColorScheme, dark = DarkColorScheme))`. Background → `surface`, title → `onSurface`, day/countdown label → `secondary` (amber, the app's "in progress" accent) instead of the old red. Added `androidx.glance:glance-material3` (same Glance version, 1.2.0); `LightColorScheme`/`DarkColorScheme` in `Theme.kt` went from `private` to `internal`. **Still open within P1-11:** the widget picks the devotional with `DebugClock` but computes the day with `LocalDate.now()` (only matters in debug builds). |
| R2 | "haz rollback" | ✅ Removed the outer `clip(MaterialTheme.shapes.medium)` from the Home card, so the `ElevatedCard` shadow is back. `onLongClickLabel` is kept. **Trade-off accepted:** P1-7's long-press ripple draws as a rectangle over the rounded corners again. |

**Verification:** `compileDebugKotlin` passes. Not tested on a device. The widget's
dark-mode look and the candle at 16dp are worth a visual check.

### 9.9 R5 + R6 (2026-09-22, "adelante con R5 y R6, p4-12")

**R5 ✅:** `NovenaReminderScheduler.scheduleDevotionalChainsIfNeeded(context, today)` is
the per-devotional half of `scheduleIfNeeded` (the 9am/8pm chains with `KEEP`), now also
called every midnight by `WidgetMidnightRefreshWorker`. It deliberately leaves out the
widget chain: a `KEEP` on the midnight worker's own unique work would find it still
running and silently skip, so the worker keeps rescheduling itself explicitly.

**R6 ✅:** all three workers catch failures. Coroutine cancellation is always rethrown,
so WorkManager can still stop them. The shared helper is `runCatchingNonCancellation`
(in `WidgetMidnightRefreshWorker.kt`).
- 9am/8pm workers: only an explicit "not notifying" ends the chain. An error while
  reading the state or showing the notification is logged, and the chain still
  reschedules. If the reschedule itself fails, the worker returns `failure()`, not
  `retry()`, because a retry re-runs the whole job and could show the same notification
  twice. The midnight worker restores the chain.
- Midnight worker: widget refresh and chain restore are independent. If its own
  reschedule fails it returns `retry()`, which is safe because everything it does is
  idempotent.

**Scenarios traced by hand:**

| Scenario | Result |
|---|---|
| Normal day in season, chains pending | At midnight `KEEP` finds the 9am/8pm work already pending → no change. |
| Turned off on Dec 18 | The 8pm worker sees "off" and ends its chain; midnight finds "off" → doesn't restore. |
| Season ends with notifications off; user doesn't open the app | Dec 16 00:00: override expired + default on in window → the midnight worker enqueues → **9am on Dec 16 fires** (was the R5 gap). |
| Season ends with notifications on | The Dec 24 worker already enqueued next Dec 16 → midnight `KEEP` leaves it. |
| The 9am worker's reschedule fails on Dec 18 | `failure()` → midnight Dec 19 recreates it → 9am Dec 19 fires. Dec 18's reminder had already been shown. |
| DataStore read fails at 9am | Nothing is shown that day, the chain continues. |
| Midnight worker delayed (Doze) until 10:00 | Today's 9am slot has already passed → `enqueue()` moves it to tomorrow; it doesn't fire immediately (P0-3 preserved). |
| WorkManager stops a worker | `CancellationException` is rethrown, not swallowed. |

**Known limitations:** the midnight chain has to have been created at least once by an
app start. A force-stop cancels everything until the app is opened again (Android
behavior).

**P4-12:** diff proposed to the owner, **not applied** (`CLAUDE.md` requires approving
the text).

### 9.10 Rest of block C (2026-09-24, "procede con el resto del bloque c")

| ID | What changed |
|---|---|
| P1-6 | Titles are tagged with the id of the back-stack entry that reported them: `AppNavHost`'s `onStageTitleUpdated`/`onSongTitleUpdated` now pass `(entryId, title)`, and `MainActivity` only shows a title while that entry is current. A new DayScreen (arrows, notification, Home) shows the static fallback until its own first page reports, never the previous day's page. The same applies to the song title. |
| P1-8 | New `<plurals>` `widget_countdown_label` (widget, `getQuantityString`) and `journey_countdown_label` (`JourneyCard`'s `NotStartedBody`, `pluralStringResource`), split so the two surfaces no longer share a string. "Falta 1 día" / "1 day to go". The old `<string>` was replaced. |
| P1-9 | "Avisos activados para %1$s" / "Avisos desactivados para %1$s" ("Alerts turned on/off for %1$s"): the wording proposed in this spec, with no grammatical gender. |
| P1-10 | Gozos music: pauses on `ON_STOP` (except during rotation, via `isChangingConfigurations`) and stays paused, with the button in sync; it requests `AUDIOFOCUS_GAIN` on play, pauses on a focus loss (call, another music app), and doesn't start if focus is denied. "Can duck" losses are left to the system's automatic ducking. Narration: stops on `ON_STOP` (TTS can't pause). **Not covered:** the narrator doesn't request audio focus itself; the TTS engine handles that. |
| P1-12 | The lyrics app bar falls back to `R.string.app_bar_section_lyrics` instead of the hardcoded "Villancicos". The debug preset label is now built from `DebugSettings.celebrationDate()` ("Celebration · Dec 25"); it stays in English like the other debug texts. |
| P1-14 | `LyricsScreen(lyrics)`: `bodyLarge` from the theme, `onBackground` color, 24dp/16dp padding, and the unused `songTitle`/`onBack` parameters removed. Still centered. **Visual change, worth a look on device.** |
| P2-1 | `LyricsViewModel(context)` loads `songs.json` once in `init` on `Dispatchers.IO` and exposes `StateFlow<List<Song>?>`. An unreadable asset logs and loads as an empty list, never a crash. Both lyrics routes collect it with `collectAsStateWithLifecycle`. The song is still derived from the entry's own nav argument (P0-5 preserved). The title is reported from a `LaunchedEffect`, not during composition. |
| P2-2 | `DevotionalRepositoryImpl` caches parsed content by asset file name (`ConcurrentHashMap`, since it's called from `Dispatchers.IO`) and reuses one `Gson`. Failed loads aren't cached. The cached object is shared, which is safe because the mapper only reads it. |
| P2-3 | The new `SoundtrackPlayer` creates the `MediaPlayer` on the first play, not when the Gozos page first shows. |
| P3-2 | The last `Log.d("Test")` (DayScreen route) removed; none remain. |

**P2-4 still open:** `CountdownToDate` is dead code. Deleting the file needs an explicit OK (`CLAUDE.md` §1.1.5, P4-6); the alternative is to fix its `LaunchedEffect` key and keep it.

**Verification:** `compileDebugKotlin` passes. Adversarial re-read of the diff: no bugs found. Traced by hand: rotation with music playing (the music continues, restarting from the beginning, same as before); app to background with music or narration (both stop; music stays paused on return); a call during the music (pauses); ← / → between days (fallback title, then the new day's; content parsed once); a restore straight onto a lyrics screen (blank until the list loads, then the right song); an id that doesn't exist (message). Pre-existing, not new: with `launchSingleTop` (R7), a notification tap onto an already-open DayScreen reuses the entry, so the page title can carry over. Not tested on a device.

**P2-4 ✅ (2026-09-24, "si no se usa borralo"):** confirmed unused (its only caller was its own preview) and deleted `presentation/components/CountdownToDate.kt`; `compileDebugKotlin` passes. This leaves `Util.calculateTimeRemaining` and the `text_navidad_en*` strings with no callers. They were already listed as dead code under P4-6 (block E) and weren't touched here.
