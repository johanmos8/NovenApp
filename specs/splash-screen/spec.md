# Android 12+ SplashScreen API

**Status:** Done
**Owner approval:** Approved by Johan Mosquera (chat, "apruebo 4, 5, 6, 7"), 2026-09-17; confirmed working on-device 2026-09-20 ("se ve bien, márcalo Done")
**Scope:** `MainActivity.kt`, `AndroidManifest.xml`, `themes.xml`, `values/colors.xml`, `values-night/colors.xml` (new), `drawable/ic_logo_splash.png` (new), `drawable-night/ic_logo_splash.png` (new)

> **Correction (2026-09-17, caught before implementing):** this Draft was written from `V3_REDESIGN_ANALYSIS.md`'s claim that C.13 was "⬜ Not done," without re-verifying it against the live code. Checking directly: `MainActivity.kt:9,58` already calls `installSplashScreen()`, the manifest (`AndroidManifest.xml:24`) already points `MainActivity`'s theme at `Theme.NovenApp.Splash`, and `themes.xml` already defines that theme with `windowSplashScreenBackground`, `windowSplashScreenAnimatedIcon` (`ic_logo`), and `postSplashScreenTheme`. **The feature is already fully implemented.** The audit's reconciliation table (and this spec's own Draft) were stale on this specific item — there is nothing to build.
**Related:** `V3_REDESIGN_ANALYSIS.md` §5 C.13

---

## 1. Intent

The app currently uses the default cold-start white flash instead of the Android 12+ `SplashScreen` API, which every modern app is expected to use for a branded, non-jarring launch. This is a small, mostly-mechanical addition with no architectural risk.

## 2. Non-goals

- No first-run explainer/onboarding flow — the audit's trend table (§3) mentions this as a *separate* industry pattern ("first-run explainer for seasonal apps"); this spec is scoped to the splash *transition* only, not an onboarding screens project.
- No custom splash animation beyond what the `SplashScreen` API supports natively (icon + optional branded background color) — a bespoke Lottie/animated splash would be a separate, larger ask.

## 3. Proposed behavior / UX

On cold start (API 31+), the system shows the app icon centered on a themed background for a brief moment before `MainActivity` content renders, using the official `androidx.core:core-splashscreen` library so pre-31 devices get a reasonable equivalent instead of nothing.

## 4. Decisions

- **Library:** `androidx.core:core-splashscreen` (the standard compat path), called via `installSplashScreen()` in `MainActivity.onCreate()` before `super.onCreate()`/`setContent`.
- **Background color:** recommend reusing the existing theme's primary/surface container color already defined in `ui/theme/Color.kt` rather than introducing a new one-off splash color — keeps CLAUDE.md §2.7 (theme tokens over hardcoded colors) intact.
- **Icon:** reuse the existing launcher icon; no new asset needed unless the owner wants a distinct splash-only mark (open question below).

## 5. Open questions

None — moot, see correction above. (For the record: the existing splash already reuses `ic_logo` as-is, no separate simplified variant.)

## 6. Tasks

- [x] Verified `installSplashScreen()`, the manifest theme wiring, and `Theme.NovenApp.Splash` are already in place — no code changes made initially.
- [x] **Bug found (2026-09-20, owner testing):** `splash_background` (`#FAFAFA`) was hardcoded with no `values-night` override, so it stayed light-colored in dark mode and didn't match the app's real background on handoff. Fixed: `values/colors.xml` now matches `LightBackground` (`#FAF8FF`) exactly; new `values-night/colors.xml` matches `DarkBackground` (`#2F3036`) exactly.
- [x] **Bug found:** `ic_logo.png` (used directly as the splash icon) is a fully opaque 1024×1024 PNG with a baked-in cream fill (`~#EDE8DB`, sampled directly) instead of a transparent background, so a visible box/edge showed around the mark. Fixed: chroma-keyed the cream out into a new `drawable/ic_logo_splash.png` (real alpha channel, splash-only - `AboutUsScreen`/`NovenAppBar` still use the original opaque `ic_logo.png`, untouched, out of this spec's scope).
- [x] **Bug found:** the mark's dark navy silhouette (`~#16283A`) had poor contrast against the new dark splash background. Fixed: added `drawable-night/ic_logo_splash.png`, same alpha mask recolored to `#F1F0F8` (the app's own `DarkOnBackground`/`DarkOnSurface` token) - resolves automatically via Android's night-mode drawable qualifier, no `themes.xml` change needed (confirmed this qualifier pattern already exists in the project for the social icons in `drawable-night/`).
- [x] Verified `:app:assembleDebug` builds successfully after each fix

## 7. Follow-ups

- First-run onboarding/explainer screen, if ever wanted — separate spec.
- `ic_logo.png` (the original, opaque-cream version) is still used as-is in `AboutUsScreen` and `NovenAppBar` - may have the same washed-out/low-contrast issue in dark mode there. Not fixed here per scope discipline; flagged for the owner to decide as a separate change.

## 8. Files

| File | Change |
|---|---|
| `values/colors.xml` | `splash_background` corrected to match `LightBackground` exactly |
| `values-night/colors.xml` | New - `splash_background` matches `DarkBackground` exactly |
| `themes.xml` | `windowSplashScreenAnimatedIcon` points at `@drawable/ic_logo_splash` instead of `@drawable/ic_logo` |
| `drawable/ic_logo_splash.png` | New - `ic_logo.png` with its cream background chroma-keyed to real transparency |
| `drawable-night/ic_logo_splash.png` | New - same alpha mask, recolored to `DarkOnBackground`/`DarkOnSurface` (`#F1F0F8`) for contrast against the dark splash |
