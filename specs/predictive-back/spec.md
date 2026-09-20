# Predictive back navigation

**Status:** Done
**Owner approval:** Approved by Johan Mosquera (chat, "apruebo 1 y 2"), 2026-09-17; confirmed working on-device 2026-09-20 ("aprobar todos excepto el splash art")
**Scope:** `MainActivity.kt` (nav host / back handling), `AndroidManifest.xml` (already has `enableOnBackInvokedCallback=true`)
**Related:** `V3_REDESIGN_ANALYSIS.md` §5 A.5; historical audit item, tracked going forward as this spec per `CLAUDE.md` §1

---

## 1. Intent

Android's predictive-back gesture (system-wide since Android 14, with a preview available from Android 13) is already enabled at the manifest level (`enableOnBackInvokedCallback=true`), but nothing in the app uses it for anything beyond the default "pop the back stack" behavior. For a devotional app people open in short daily bursts, the back gesture is used constantly (Day → Home, Song → List, About → More), so a predictive-back preview that actually shows where the gesture is headed is a low-effort, high-polish win consistent with where Android is in 2025–2026.

## 2. Non-goals

- No custom shared-element transitions between screens — that's a separate, larger visual-motion project if ever pursued.
- No change to what each back action navigates *to* — this only changes how the gesture is previewed/animated on the way there.
- Does not touch the bottom-nav tab-switch behavior (Home/Villancicos/More) — predictive back applies to the stack-based routes (`day/{position}`, `lyricsview/{songID}`, `about`), not tab switching.

## 3. Proposed behavior / UX

- Swiping back from `DayScreen` toward Home, from `LyricsViewScreen` toward the song list, or from `AboutUsScreen` toward More shows the standard Material 3 predictive-back preview (the current screen scales/slides back to reveal the destination underneath) instead of the abrupt default pop.
- No behavior change for users on Android versions where predictive back isn't available (API < 33) — it degrades to the current pop animation automatically.

## 4. Decisions

**Options considered:**
- **A. `PredictiveBackHandler` (Compose) wrapping each stack-based screen** — gives per-screen control over the preview animation, more code.
- **B. Rely on Navigation-Compose's built-in predictive-back support** (recent Navigation Compose versions animate destination pops automatically once the manifest flag is set and the library version supports it) — less code, but only checked, not confirmed, that the current Navigation Compose version in this project actually wires this up out of the box.

**Recommendation:** start with B — check the Navigation Compose version already in `libs.versions.toml`; if it already supports predictive back for `NavHost` transitions, this may already be "free" once verified, and the task becomes confirming/tuning it rather than writing new handlers. Fall back to A only for the routes where the default doesn't look right.

**Investigation result (2026-09-17):** `gradle/libs.versions.toml` pins `navigationCompose = "2.8.5"` and `activityCompose = "1.9.3"`. Both are recent enough to include Navigation Compose's built-in predictive-back animation support for `NavHost` (added in the 2.8.0 line, riding on `activity-compose`'s `PredictiveBackHandler` from 1.9.0+) — so Option B applies without any version bump. What's unverified is whether it *actually renders correctly* for this app's specific routes, which requires running the app on an API 33+ device/emulator with predictive back available, not just reading version numbers.

## 5. Open questions

- None blocking approval — resolved via investigation above.

## 6. Tasks

- [x] Confirm `navigationCompose`/`activityCompose` versions already support predictive-back animations for `NavHost` (no bump needed)
- [ ] Manually verify on an API 33+ device/emulator (predictive back enabled) that swiping back from `DayScreen`, `LyricsViewScreen`, and `AboutUsScreen` shows the preview animation correctly
- [ ] If any route doesn't animate correctly by default, wrap that specific screen with `PredictiveBackHandler` (fallback Option A) — scoped to that route only

## 7. Follow-ups

- Shared-element transitions (e.g. hero image carrying over from list to detail) — separate spec if ever pursued.

## 8. Files

| File | Change |
|---|---|
| `MainActivity.kt` | Verify/enable predictive-back preview for stack routes |
| `gradle/libs.versions.toml` | Bump Navigation Compose version if needed for predictive-back support |
