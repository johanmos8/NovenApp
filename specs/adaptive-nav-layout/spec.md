# Adaptive navigation layout (NavigationSuiteScaffold) + tablet/foldable validation

**Status:** Done
**Owner approval:** Approved by Johan Mosquera (chat, "apruebo 4, 5, 6, 7"), 2026-09-17; confirmed working on-device 2026-09-20 ("aprobar todos excepto el splash art")
**Scope:** `MainActivity.kt` (currently a plain Material 3 `NavigationBar`), `compose-preview` module (`PreviewAllPhones` / large-screen previews)

> **2026-09-17 note — surfaced while starting implementation, not covered by the Draft:** the current bottom bar isn't a plain edge-to-edge `NavigationBar` as this spec's Decisions section assumed. `MainActivity.kt` wraps it in a `Surface` with `MaterialTheme.shapes.large`, side margins, and elevation, specifically so it reads as a rounded bar "floating" over the background rather than a slab pinned to the screen edge (see the comment at `MainActivity.kt:193-197`) — a deliberate, already-shipped visual design. `NavigationSuiteScaffold`'s compact-width layout renders a plain `NavigationBar` by default; getting the same floating-pill look out of it means either customizing its compact slot (non-trivial, `NavigationSuiteScaffold`'s API isn't designed around swapping in an arbitrary container for just one window-size class) or accepting a visual regression on phones — the only screen size real users are on today — in exchange for a tablet/foldable rail layout for a form factor this app has no evidence of being used on. Per `CLAUDE.md` §1.1.4 ("if implementation surfaces a case the spec didn't cover... pause and ask"), I stopped here rather than picking one silently.
**Related:** `V3_REDESIGN_ANALYSIS.md` §5 A.1, §5 E.18 (E.18 is blocked on A.1 per the audit's own reconciliation, so this spec bundles both)

---

## 1. Intent

The audit's original recommendation was `NavigationSuiteScaffold` (Material 3 Adaptive) specifically because it renders as a bottom bar on phones, a rail on tablets, and a drawer on large/unfolded screens from one API — the current implementation is a plain `NavigationBar` hardcoded for phone-width layouts (see `MainActivity.kt:98-114`), which is a reasonable simplification for a phone-only launch but doesn't adapt if the app is ever opened on a tablet or a foldable. E.18 (tablet/foldable preview validation) can't meaningfully happen until this exists, since there's currently no adaptive layout to validate.

## 2. Non-goals

- Not a redesign of the 3 destinations themselves (Novena/Villancicos/More stay as-is) — this is purely the container/shell that hosts them.
- Does not add tablet-specific content layouts (e.g. two-pane day list + reading view) — that would be a separate, larger spec if there's ever a real tablet-usage signal for this app.

## 3. Proposed behavior / UX

- Phone (compact width): visually unchanged — still a bottom `NavigationBar` with the same 3 destinations.
- Tablet/foldable (medium/expanded width): the same 3 destinations render as a `NavigationRail` (medium width) or an equivalent large-screen layout, per `NavigationSuiteScaffold`'s built-in adaptation — no new design work required, since this is exactly what the component is for.

## 4. Decisions

**Option A confirmed by owner (chat, 2026-09-17): "mantengamos la píldora en teléfono."**

- Compute `NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())` once per composition of `MyApp()` to get the window's `NavigationSuiteType`.
- **Compact width** (`NavigationSuiteType.NavigationBar`): unchanged — same `Scaffold` + `Surface`-wrapped floating-pill `NavigationBar` as before, byte-for-byte the same visual result.
- **Medium/expanded width, on a top-level route**: wrap the same `topBar`/`content` in `NavigationSuiteScaffold` (rail on medium, drawer on expanded), driven by the same `bottomNavDestinations` list via `NavigationSuiteScope.item(...)`.
- **Medium/expanded width, on a stack route** (Day/LyricsView/AboutUs): plain `Scaffold`, no nav container — mirrors the existing phone-width rule that stack screens don't show bottom nav either.
- Extracted `topBar`, `content`, and a `navigateToTopLevel()` helper as shared lambdas/function so the three branches don't duplicate the `when (currentRoute)` app-bar logic or the nav-graph `popUpTo`/`launchSingleTop` logic.
- **Validation:** no new preview infrastructure needed — `compose-preview`'s existing `PreviewDevices` (used by `@PreviewAllPhones`, already on `MainActivity.kt`'s `PreviewApp()`) already includes a `"Tablet"` entry at `1280x800dp`, which is wide enough to exercise the new rail/drawer branch automatically.

## 5. Open questions

None — resolved by the owner's Option A choice.

## 6. Tasks

- [x] Added `androidx.compose.material3:material3-adaptive-navigation-suite` (via BOM, no explicit version) to `libs.versions.toml`/`app/build.gradle.kts`
- [x] Refactored `MyApp()` in `MainActivity.kt`: extracted `topBar`/`content`/`navigateToTopLevel`, branched on `isCompactWidth` vs. `isTopLevelRoute` for medium/expanded windows
- [x] Verified `:app:assembleDebug` builds successfully
- [ ] Owner to visually confirm the phone layout is pixel-identical to before, and ideally check the `"Tablet"` preview renders the rail/drawer correctly (Android Studio preview, or an actual tablet/emulator)

## 7. Follow-ups

- Tablet-specific two-pane content layouts, if ever justified by real usage data.

## 8. Files

| File | Change |
|---|---|
| `MainActivity.kt` | Added adaptive branch (`NavigationSuiteScaffold` for medium/expanded); compact-width path unchanged |
| `gradle/libs.versions.toml` / `app/build.gradle.kts` | Added `androidx-material3-adaptive-navigation-suite` |
