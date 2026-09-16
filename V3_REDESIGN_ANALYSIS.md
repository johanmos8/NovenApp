# NovenApp — Architecture Audit & v3.0.0 Redesign Proposal

**Scope:** analysis of the current codebase (branch `3.0.0`, last shipped version 2.0.1 / versionCode 11) plus a concrete plan to modernize the UI/UX for a 3.0.0 release.

---

## 0. Reconciliation status (2026-09-16)

This audit was written before Phase 0 of its own plan (§ "Additions needed for real
multi-content scalability") existed. Since then, commits `591f27c` ("generalized
Devotional model, repository + content catalog, per-devotional date-gating"),
`de7c740` ("ui adjustments"), and `f2331c2` ("prayer screen redesing") have landed
most of Phases 0, 1, and part of 4. This section reconciles the plan against the
actual `3.0.0` codebase so the document stays trustworthy as a historical record
instead of silently going stale. **Going forward, new work is tracked as specs under
`specs/` per `CLAUDE.md` §1, not by editing this audit further.**

Status legend: ✅ Done · 🟡 Done differently / partial · ⬜ Not done.

| Ref | Item | Status |
|---|---|---|
| Phase 0.0 | Generalize `Devotional` model | ✅ `data/devotional`, generic content blocks, not Christmas-named fields |
| Phase 0.1 | Repository + content catalog | ✅ `DevotionalRepository` / `DevotionalRepositoryImpl` |
| Phase 0.2 | Per-devotional date-gating | ✅ `DevotionalSchedule.FixedRange` / `.Evergreen` |
| Phase 0.3 | Parameterized routes | ✅ `devotional/{devotionalId}/day/{position}` in `NavigationScreen.kt` |
| Phase 0.4 | Content assembly out of the NavHost composable | ✅ `presentation/mapper` |
| Phase 0.5 | DI container | ✅ Koin (`di/AppModule.kt`) — Hilt was suggested, Koin is an equally valid substitute |
| Phase 0.6 | "Devotionals" home listing multiple active devotionals | ⬜ Not needed yet — still exactly one devotional; home is the single "Camino" card |
| A.1 | Drawer → `NavigationSuiteScaffold` | 🟡 Drawer replaced with a plain Material 3 `NavigationBar`; no adaptive tablet/foldable layout yet (see E.18) |
| A.2 | 9-day list → `DayStrip` on the Novena tab | ✅ `DayStrip.kt`, kept under the Journey card per `docs/home-journey-redesign.md` |
| A.3 | Fold About us / Rate into "More" tab | ✅ `MoreScreen.kt`; `AppDrawer.kt` and its item composables (`NovenaItem`, `VillancicoItem`, `FeedbackItem`) are gone entirely |
| A.4 | Fix nested-pager problem in Gozos | ✅ differently — `GozosScreen` was redesigned as one scrollable page with no inner pager/prev-next-reset controls at all, rather than flattened into the outer pager |
| A.5 | Predictive back polish | ⬜ Not done |
| B.6 | Bump to Material 3 Expressive | 🟡 Expressive-style shapes exist (`JourneyCard`, `SegmentedProgressIndicator`) but not confirmed as built on the formal M3 Expressive dependency |
| B.7 | Wire `RedChristmas`/`GreenChristmas`/`GoldChristmas` into a seasonal scheme | 🟡 Those specific unused tokens are gone; replaced with a full bespoke "Novena Contemplative MD3" palette (`ui/theme/Color.kt`, indigo/amber/violet) — same complaint resolved a different way |
| B.8 | Replace hardcoded `Color.White`/`Black`/`DarkGray`/`LightGray` with theme tokens | ✅ none of those remain outside `Color.kt`'s own palette definitions |
| B.9 | Restyle pager-dot indicator as Material 3 expressive | ✅ bundled into B.8/A.4's cleanup — the old plain gray dots no longer exist |
| B.10 | Intentional empty/locked state for future days | 🟡 `JourneyStatus.NotStarted` + debug preview mode exist at the home-card level; not verified per-day beyond that |
| C.11 | Daily local notification (WorkManager) | ✅ `reminder/DailyNovenaReminderWorker`, `NovenaReminderScheduler`, `NovenaNotifier` |
| C.12 | Home-screen widget (Glance) | ✅ `widget/NovenaWidget`, `NovenaWidgetReceiver`, `NovenaWidgetUpdater` |
| C.13 | Android 12+ `SplashScreen` API | ⬜ Not done |
| D.14 | Fix inverted locale (default resources = Spanish) | ✅ `values/strings.xml` is Spanish; `values-en/` is now the override |
| D.15 | Clean up `AboutUsScreen` ("Shuffle Friends" leftovers, commented TODO) | ✅ no leftover copy or commented `SocialMediaList()` TODO found; "Outrageous Cat" branding remains, but that is this app's own dev-studio credit, not leftover copy |
| D.16 | Fill in placeholder `contentDescription`s | 🟡 the `"hola"` placeholder is gone; not exhaustively re-audited image by image |
| D.17 | Bump `versionCode`/`versionName`, fix in-app version display | 🟡 `versionCode` is now `12` (was 11); no `versionName` found in `app/build.gradle.kts` — confirm this is intentional before a release build |
| E.18 | Tablet/foldable preview validation | ⬜ Not done — blocked on A.1's `NavigationSuiteScaffold` gap |
| E.19 | "Mark day as done" progress | ✅ `DevotionalProgressStore` + the Journey card / segmented indicator — see `docs/home-journey-redesign.md`, which also documents a known per-season-key bug in this feature |

**Net effect:** Phases 0, 1 (except predictive back), 2, and half of 4 are already
done — well ahead of the `§7` phasing table below, which predates this work. What
actually remains from this plan: predictive back (A.5), a formal Expressive
shapes/tokens pass (B.6, partly B.10), the splash screen (C.13), adaptive/tablet
layout (A.1's `NavigationSuiteScaffold`, E.18), and the small polish items (D.16,
D.17). Each of these — and anything new — should get its own spec under `specs/`
rather than being bolted onto this document.

---

## 1. What the app actually is

NovenApp is a single-purpose, **seasonal ritual app**: it guides a user through the Colombian *Novena de Aguinaldos*, 
a 9-day Christmas prayer tradition (Dec 16–24). Every session has the same shape: open the app, jump to **today's day**, 
read/say prayers, optionally sing along to villancicos. There is no account, no persistence of user data, no social feed — it's closer to a **daily devotional / missal app** than a general content app.

That single fact should drive every design decision below. A generic "modernize the UI" 
pass would miss it — the biggest win here isn't visual polish, it's shortening the distance between *open app* and *today's prayer*.

## 2. Current architecture (as built)

- **Stack:** Jetpack Compose + Material 3, `compileSdk/targetSdk 36`, `minSdk 28`, single `:app` module + a `:compose-preview` module for multi-device/theme previews. `enableOnBackInvokedCallback=true` is set (predictive-back plumbing exists at the manifest level but isn't used for anything custom).
- **Navigation:** one `NavHost` (`AppNavHost.kt`) with 5 routes — `home`, `day/{position}`, `lyrics`, `lyricsview/{songID}`, `about`. All non-home routes are reached through a single `ModalNavigationDrawer` (`AppDrawer.kt`) opened via a hamburger icon in `MainActivity.kt`.
- **Drawer contents:** a scrollable list of "Day 1"–"Day 9" items, a "Villancicos" link, "About us" and "Rate this app". This is the *only* way to jump to a specific day — there's no in-content day switcher.
- **Home screen:** Lottie snow animation background, title, Christmas countdown, and a single "Go to day X" button that resolves the day from `LocalDate.now()`.
- **Day screen:** a `HorizontalPager` swiping across 6 fixed "pages" (reflection → daily prayer → prayer to Mary → prayer to Joseph → Gozos → prayer to the Child Jesus), with plain dot indicators drawn in raw `Color.DarkGray`/`Color.LightGray` (not theme-aware). Inside the Gozos page there's a **second, independent pager-like widget** (`GozosNavigator`) with its own prev/next/reset icon buttons — a pager nested inside a pager, which is a real UX trap (horizontal swipe gestures conflict, and "reset" duplicates what swipe-back already does).
- **Visual treatment:** `ReadingWithImageScreen` has a nice collapsing parallax hero image (a genuinely modern touch), but the rest of the app is fairly bare Material 3 defaults — default `TopAppBar`, default `Button`, no custom shapes, no motion beyond the collapse effect.
- **Theming:** Dynamic color (Material You) is wired up for API 31+, with a manual light/dark fallback. But several components bypass the theme with hardcoded colors (`Color.White`, `Color.Black.copy(alpha = 0.3f)`, `Color.DarkGray`/`Color.LightGray` for pager dots, unused `RedChristmas`/`GreenChristmas`/`GoldChristmas` tokens that were clearly intended for a festive palette but never wired in). Result: dynamic color and dark mode are only partially honored.
- **Engagement/retention features:** none. No notifications, no widget, no onboarding, no splash screen API usage. For an app people are only supposed to open on 9 specific days of the year, there is nothing that *reminds* them to open it.
- **Accessibility:** several `contentDescription = null` or placeholder strings (`"hola"`, `""`) on meaningful images; icon-only buttons in `GozosNavigator` rely only on `contentDescription`, fine, but the hamburger-hidden day list has no "current day" affordance in the app bar itself.
- **Branding split:** `AboutUsScreen` mixes "Outrageous Cat" (dev studio) branding with leftover copy from a different app ("Shuffle Friends" string keys, a commented-out `SocialMediaList()` "TODO"). This reads as unfinished/inconsistent to a user who taps "About us".
- **Locale setup is inverted:** `values/strings.xml` (the default, no-qualifier resource set) holds *English* text, while `values-es/strings.xml` is the override. For an app about a Colombian Catholic tradition, Spanish should be the default locale and English the qualified override — right now a device with no locale match (or Play Store's "default" fallback) shows the app in English first.

## 3. Where this sits against current app-design trends (2025–2026)

| Trend | Where the industry is | Where NovenApp is |
|---|---|---|
| **Primary navigation** | Bottom nav bar / nav rail for 3–5 top-level destinations; drawers reserved for large, rarely-used option lists (Gmail-style) or large-screen layouts | Everything (day picker, songs, about, rate) funneled through one hamburger drawer |
| **Adaptive layouts** | `NavigationSuiteScaffold` / Material 3 Adaptive — one nav model that renders as bottom bar on phones, rail on tablets, drawer on large/unfolded screens | Fixed `ModalNavigationDrawer` regardless of window size; no tablet/foldable treatment |
| **Motion & shape** | Material 3 Expressive (2025 M3 update): bouncier spring transitions, expressive shape morphing, larger/blockier type scales | Default M3 transitions, mostly default shapes; only one bespoke motion (image collapse) |
| **Predictive back** | Android 14+ predictive-back preview expected on every screen | Enabled in the manifest but not used for anything custom (no shared-element/back-preview polish) |
| **Glanceable content** | Home-screen widgets (Glance), lock-screen/notification surfaces for daily-use apps (habit trackers, devotionals, prayer apps like Hallow/Laudate) | None — the countdown and "today's day" only exist inside the app |
| **Re-engagement** | Local notifications, WorkManager-scheduled daily reminders | None |
| **Onboarding/splash** | Android 12+ `SplashScreen` API, first-run explainer for seasonal apps | Standard launcher icon splash only, no first-run context |
| **Content-forward home** | Devotional/reading apps lead with "continue where you left off" or "today's reading" as a card, not a menu | Home leads with countdown + one CTA button, which is close but still requires the drawer for anything else |
| **Accessibility** | WCAG-conscious content descriptions, dynamic type support | Several missing/placeholder `contentDescription`s |

## 4. Is the hamburger menu obsolete?

**Short answer: for this app, yes — not because hamburgers are "outdated" in general, but because it's the wrong pattern for what this app does.**

The hamburger/drawer pattern isn't dead everywhere — Gmail, YouTube Music, and most large-screen (tablet/foldable) layouts still use it for long, secondary option lists. But the pattern's known weakness has always been the same: **it hides navigation behind an extra tap and an icon that doesn't communicate what's inside it** ("hamburger" tests since the mid-2010s consistently show lower engagement with items buried in a drawer vs. a visible tab bar). That cost is worst exactly when:

- the destinations are few (NovenApp has 3 real top-level destinations: *today's prayer*, *villancicos*, *about/settings*),
- one destination is used on every single visit (today's day — currently still requires opening the drawer or the one Home button, never a persistent control), and
- the app is used in short, frequent bursts (a daily devotional, opened once a day for ~9 days) rather than deep, exploratory sessions.

NovenApp fits all three. Concretely, right now a returning user has exactly one fast path ("Go to day X" on Home) and one slow path (drawer → pick a day) to reach the same content — the drawer's day list is largely redundant with the Home button, just slower.

**Recommendation:** replace the drawer with a persistent **bottom navigation bar** (or `NavigationSuiteScaffold`, so it automatically becomes a rail/drawer on tablets and foldables) with 3 destinations:

1. **Novena** (home/today) — the default tab, content-forward: today's day front and center.
2. **Villancicos** (songs)
3. **More** (about us, rate app, share, settings) — this is where a drawer-like list is actually still appropriate, because it's genuinely secondary content, not a place users go daily.

And critically: **the 9-day picker shouldn't be a drawer list at all.** It should be a horizontal day-strip/chip row (a small `LazyRow` of "Day 1"…"Day 9" chips, current day highlighted, future days optionally locked/dimmed) surfaced directly on the Novena tab and pinned above/near the prayer content — the same pattern calendar and fitness-streak apps use for "day N of a program." That turns "find today's prayer" from a 2-tap drawer traversal into something visible without any tap at all.

## 5. Proposed changes for v3.0.0

Grouped by effort/impact so they can be tackled incrementally.

### A. Navigation overhaul (highest impact, moderate effort)
1. Replace `ModalNavigationDrawer` in `MainActivity.kt` with `NavigationSuiteScaffold` (Material 3 Adaptive) driving 3 destinations: Novena, Villancicos, More.
2. Move the 9-day list out of `AppDrawer.kt` into a `DayStrip` composable shown on the Novena tab (chips or a segmented row), replacing the current single "Go to day X" button — keep that button too as a large primary CTA for "today," but make all 9 days reachable inline.
3. Fold "About us" and "Rate this app" into the "More" tab as a simple list screen (this is the one place a drawer-style list still makes sense) — delete the now-redundant `AppDrawer.kt` drawer-item composables (`NovenaItem`, `VillancicoItem`, `FeedbackItem`) or repurpose them there.
4. Fix the nested-pager problem: make `GozosNavigator` consume swipe gestures from the *same* `HorizontalPager` used in `PrayerScreen` (i.e., flatten Gozos verses into the outer pager's page list, or use a `PagerState` sub-controller) instead of stacking an independent prev/next control inside a swipeable page.
5. Use predictive-back properly: since `enableOnBackInvokedCallback` is already on, add `PredictiveBackHandler`/shared-element-style transitions for Day → Home and Song → List, since the plumbing is already half-done.

### B. Visual refresh (Material 3 Expressive)
6. Bump `androidx.compose.material3` to the Material 3 Expressive release; adopt expressive shapes/typography scale for `MainTitle`, headline text in `ReadingWithImageScreen`, and buttons.
7. Wire the already-defined but unused `RedChristmas` / `GreenChristmas` / `GoldChristmas` tokens into an actual seasonal color scheme (or remove them if abandoned) — right now the app is themed with generic purple Material defaults for a *Christmas* app, which undersells the seasonal identity.
8. Replace every hardcoded `Color.White` / `Color.Black` / `Color.DarkGray` / `Color.LightGray` (pager dots in `PrayerScreen.kt`, `BackButton` in `MainActivity.kt`) with theme tokens so dark mode and dynamic color are honored everywhere, not just in screens that happen to use `MaterialTheme.colorScheme`.
9. Restyle the page-indicator dots in `PrayerScreen.kt` as a Material 3 `expressive` indicator (pill-shaped active dot, themed colors) instead of plain gray circles.
10. Add a proper first-run/empty state for days that haven't arrived yet (currently unclear what happens if a user opens day 5 before December 20 — decide and design intentionally: preview mode vs. locked state).

### C. Re-engagement & "glanceable" features (biggest gap vs. trend, high value for a 9-day seasonal app)
11. **Daily local notification** (WorkManager-scheduled) reminding the user "Today is Day N of the Novena" during Dec 16–24 — this single feature does more for retention than any visual change, since the app currently has zero mechanism to bring users back.
12. **Home-screen widget** (Glance API) showing the Christmas countdown and/or "today's day" with a tap-through into `day/{position}` — this reuses `CountdownToDate`'s logic and turns the app from "something you must remember to open" into something glanceable.
13. Add Android 12+ `SplashScreen` API usage with a simple branded splash instead of the default cold-start white flash.

### D. Content & polish cleanup
14. Fix the inverted locale setup: make `values/strings.xml` (default) Spanish and move the current English text into `values-en/strings.xml`, so devices without an exact `es` match don't get an English-first experience for a Colombian tradition app.
15. Clean up `AboutUsScreen.kt`: remove leftover "Shuffle Friends" string keys/copy, either ship the commented-out `SocialMediaList()` or delete it, and make sure "Outrageous Cat" branding and NovenApp branding aren't visually competing on the same screen.
16. Fill in missing/placeholder `contentDescription`s (`HeroImage`, `ic_logo` image currently described as `"hola"`, decorative images that should be `null` intentionally vs. meaningful images that need real descriptions) as a straightforward accessibility pass.
17. Bump `versionCode`/`versionName` to reflect 3.0.0 and update the in-app "About" version display once `BuildConfig.VERSION_NAME` reads correctly.

### E. Nice-to-have / stretch
18. Adaptive layout validation on tablet/foldable via the existing `:compose-preview` module (`PreviewAllPhones`) — add large-screen previews now that `NavigationSuiteScaffold` makes this cheap.
19. Consider a lightweight "mark day as done" checkmark per day (local `DataStore`, no account needed) so the day-strip can show progress — small addition, fits the existing "streak" mental model of the redesigned day picker.

## 6. Scalability check — does this plan support adding more religious content later?

The nav/UI proposal in Section 5 is a **presentation-layer** redesign. It does not, by itself, make the app scalable to more content (other novenas, a rosary, a saints/liturgical-calendar feature, daily gospel readings, etc.) — that bottleneck lives one layer down, in the domain model and data loading, which the plan above doesn't touch. Checked the model/view-model code directly to confirm:

- **`Novena`/`General` (`model/Novena.kt`) is shaped for this one novena, not a generic devotional.** `General` has fixed, named fields (`oracion_todos_los_dias`, `oracion_virgen_maria`, `oracion_san_jose`, `oracion_niño_jesus`) instead of an ordered list of typed content blocks. A different novena or a rosary simply doesn't fit these field names — you'd need a parallel data class per content type, not new data in the existing one.
- **There is no concept of "which devotional" to load.** `MainViewModel.getContent()` always opens one hardcoded asset (`content_es.json` / `content_en.json`, selected only by locale). Nothing in the code lists or chooses between devotionals, because only one has ever existed.
- **Date-gating is global, not per-content.** `TARGET_MONTH`, `INITIAL_DAY = 16`, `LAST_DAY = 24`, `TARGET_DAY = 25` in `util/Util.kt` are top-level constants hardcoded to this Christmas tradition. A future evergreen devotional (e.g., a daily rosary with no date range) or a novena on different dates can't reuse this logic without rewriting it.
- **Day content is assembled inline, in the UI layer.** The `DayScreen` composable in `AppNavHost.kt` manually builds the 6-item `Prayer` list with hardcoded drawables (`R.drawable.novena`, `pesebre`, `san_jose`, …) and hardcoded `NovenaTab` strings, directly inside the navigation graph. Adding a second devotional means branching this block, not just adding a JSON file.
- **No dependency injection.** `MainViewModel()` is constructed manually in two separate places (`MyApp()` in `MainActivity.kt`, and again inside `AppNavHost.kt`) — meaning two independent instances/states already exist today. Every additional content-type ViewModel would repeat this pattern and compound the duplication.

**Verdict: the navigation overhaul (Section 5, part A) is neutral-to-helpful for scalability — a bottom-nav shell is actually a better home for "browse other devotionals" than a hamburger — but it needs to be designed against a generalized model, not the current Christmas-specific one, or it will need rework the moment a second devotional is added.** The other parts of the plan (visual refresh, notifications/widget, locale/a11y cleanup) are orthogonal to scalability — they neither help nor hurt it, though the widget/notification work in particular (C.11–C.13) will need to know *which* devotional it's pointing at once there's more than one, so it's cleaner to build that after the model is generalized.

### Additions needed for real multi-content scalability (new Phase 0)

0. **Generalize the content model:** replace `Novena`/`Dia`/`General` with a generic `Devotional` shape — metadata (id, title, cover image, optional date range vs. "evergreen") plus an ordered list of "days/sections," each holding an ordered list of typed content blocks (reading, prayer, image, song reference) instead of fixed named fields. The current Christmas novena becomes the first instance of this shape, not a special case.
1. **Introduce a repository + content catalog:** a `DevotionalRepository` that reads a small index (a JSON manifest listing available devotionals and their asset files) and can load any of them by id, replacing `MainViewModel.getContent()`'s single hardcoded lookup. Adding a new novena/rosary then means "drop in a JSON file + assets + one catalog entry," not new branches in view code — this also keeps content genuinely data-driven, matching the existing `content_es.json`/`content_en.json` pattern instead of abandoning it.
2. **Move date-gating into per-devotional config:** `INITIAL_DAY`/`LAST_DAY`/`TARGET_MONTH`/`TARGET_DAY` become fields on a devotional's metadata (nullable/absent for evergreen content) instead of global constants in `Util.kt`.
3. **Parameterize routes by content id:** `devotional/{devotionalId}/day/{position}` instead of the current bare `day/{position}`, so the nav graph and any deep links (including the future home-screen widget) can point at a specific devotional, not "the one that exists."
4. **Pull day-content assembly out of the NavHost composable** into a mapper/use-case that turns a `Devotional` + day index into the list of content blocks to render — so `AppNavHost.kt` stays generic across content types instead of hardcoding one novena's drawables and titles inline.
5. **Adopt a DI container (Hilt or Koin)** for `MainViewModel` and the future `DevotionalRepository`, fixing the existing duplicate-instance issue and avoiding the same mistake being copy-pasted for each new content-type ViewModel.
6. **Reconsider the bottom-nav destinations from Section 5 in light of this:** instead of a fixed "Novena / Villancicos / More" bar tied to one novena's name, make that first tab a "Devotionals" home that lists whichever devotionals are active (even if v3.0.0 ships with exactly one) — cosmetically similar today, but it means the nav shell doesn't have to be rebuilt when a second novena or a rosary feature is added later.

## 7. Suggested phasing

| Phase | Contents | Why first/last |
|---|---|---|
| 0 | New: generalized `Devotional` model, repository + content catalog, per-devotional date-gating, parameterized routes, DI | Foundation everything else should be built on — doing the nav/day-strip work (Phase 1) before this risks rebuilding it once a second devotional is added |
| 1 | A.1–A.4 (nav overhaul + day strip + fix nested pager), designed against the Phase 0 model | Structural change everything else builds on; also the direct answer to "hamburger menu" |
| 2 | D.14–D.16 (locale fix, About cleanup, a11y) | Cheap, high-embarrassment-reduction, no architecture risk |
| 3 | B.6–B.9 (Expressive visuals, theme-token cleanup, seasonal palette) | Depends on nav shell from Phase 1 being in place |
| 4 | C.11–C.13 (notifications, widget, splash), pointing at a devotional id from Phase 0 | New capability, best done once navigation/routes and the content model are stable |
| 5 | B.10, E.18–E.19 | Polish/stretch once the core redesign ships |

---

*This document originally reflected the codebase as of the `3.0.0` branch (post
`c5306c3`, version 2.0.1 / versionCode 11), before Phase 0 of its own plan existed.
See §0 above for the 2026-09-16 reconciliation against the current codebase
(versionCode 12). File references are relative to
`app/src/main/java/com/mirkwood/novenapp/`. This document is now historical context,
not a live status tracker — current and future work is specced under `specs/` per
`CLAUDE.md`.*
