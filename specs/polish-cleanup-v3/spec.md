# v3.0.0 polish cleanup: accessibility audit + version display

**Status:** Done
**Owner approval:** Approved by Johan Mosquera (chat, "apruebo 4, 5, 6, 7"), 2026-09-17; confirmed 2026-09-20 ("aprobar todos excepto el splash art")
**Scope:** `contentDescription` usage across `presentation/`, `app/build.gradle.kts` (`versionName`)
**Related:** `V3_REDESIGN_ANALYSIS.md` §5 D.16, D.17

---

## 1. Intent

Two small, independent leftover items from the original audit, grouped into one spec because both are quick, low-risk polish passes with no architectural weight of their own:

- **D.16:** the original `"hola"` placeholder `contentDescription` is confirmed gone, but there's been no systematic pass confirming every image is *correctly* either `null` (decorative) or a real localized string (meaningful).
- **D.17:** `app/build.gradle.kts` currently sets `versionCode = 12` but had no `versionName` in `defaultConfig`.

**Correction (2026-09-17, during implementation):** the Draft's claim that "`AboutUsScreen.kt` doesn't display any version string today" was wrong — it was checked by grepping `AboutUsScreen.kt` directly, which missed that the screen renders `AppInfoFooter()` (`presentation/components/AppInfoFooter.kt`), a separate composable that already shows `stringResource(R.string.about_us_version_label, BuildConfig.VERSION_NAME)`. That `BuildConfig.VERSION_NAME` field already resolves correctly today via a custom `buildConfigField` in `app/build.gradle.kts` reading `project.properties["VERSION_NAME"]` (`gradle.properties` already has `VERSION_NAME=3.0.0`). So the in-app display was already working — the one real gap was that `android.defaultConfig.versionName` (the actual APK/AAB metadata field Play Console reads, distinct from the custom `BuildConfig` field) was never set.

## 2. Non-goals

- Not a full accessibility audit (TalkBack flow testing, touch target sizing, contrast) — scoped specifically to `contentDescription` correctness, matching what the original audit item named.
- Does not add a changelog/release-notes screen — just a version string.

## 3. Proposed behavior / UX

- No visible change for D.16 unless a currently-`null` image turns out to need a real description (TalkBack users would notice the improvement).
- `AboutUsScreen` gains a small "Version 3.0.0" (or similar) line, sourced from `BuildConfig.VERSION_NAME` so it never needs manual updating again after this lands.

## 4. Decisions

- **D.16 approach:** walk every `contentDescription` call site found in this session (`DayStrip.kt`, `GozosScreen.kt`, `HeroImage.kt`, `NovenAppBar.kt`, `SegmentedProgressIndicator.kt`, `PrayerScreen.kt`, `MoreScreen.kt`, `AboutUsScreen.kt`, `JourneyCard.kt`, `SongListScreen.kt`, and the 3 bottom-nav icons in `MainActivity.kt`) and confirm each `null` is genuinely decorative vs. one that needs a real string.
- **D.17 approach:** set `versionName = project.properties["VERSION_NAME"].toString()` in `app/build.gradle.kts`'s `defaultConfig`, reusing the same `gradle.properties` value the existing custom `BuildConfig.VERSION_NAME` field already reads, so there's one source of truth instead of two independently-set version strings.

## 5. Open questions

None — resolved during implementation (see Correction above).

## 6. Tasks

- [x] Audited every `contentDescription` call site listed in Decisions — every `null` is genuinely decorative (icon paired with adjacent visible text: bottom-nav items, `A+`/listen buttons, progress pill, preview banner, day-chip checkmarks whose parent `FilterChip` already carries a real `semantics { contentDescription }`, `SongListScreen`/`JourneyCard` chevrons next to title text) or a purely ornamental hero image. **No changes needed.**
- [x] Added `versionName = project.properties["VERSION_NAME"].toString()` to `app/build.gradle.kts` `defaultConfig` (was previously only read into a custom `BuildConfig` field, not the real AGP `versionName`). Verified `:app:assembleDebug` builds successfully.
- [x] Confirmed `AboutUsScreen` already displays the version correctly via `AppInfoFooter` → `BuildConfig.VERSION_NAME` — no UI change needed.

## 7. Follow-ups

- None currently identified.

## 8. Files

| File | Change |
|---|---|
| `app/build.gradle.kts` | Added `versionName` to `defaultConfig`, sourced from `gradle.properties` |
| Various (see Decisions) | Audited only — no changes needed |
