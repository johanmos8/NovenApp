# PrayerDialog: Material 3 typography consistency

**Status:** Done
**Owner approval:** Approved by Johan Mosquera (chat, "aprueba las 1,2"), 2026-09-17; confirmed 2026-09-20 ("aprobar todos excepto el splash art")
**Scope:** `presentation/screens/prayer/PrayerDialog.kt`
**Related:** `V3_REDESIGN_ANALYSIS.md` §7 Phase 3 (visual refresh) — new item, added to the plan at the owner's request (2026-09-17); same theme-consistency spirit as `specs/material3-expressive-pass`

---

## 1. Intent

Confirmed by reading the component directly: `PrayerDialog` already uses Material 3's `AlertDialog` (`androidx.compose.material3.AlertDialog`), so it is not off the M3 component system. The gap is narrower than "does it use M3" — it's that its text styling bypasses the M3 **type scale**: the title uses a raw `FontWeight.Bold` instead of a typography style, and the body text uses a raw `fontSize = 18.sp` instead of `MaterialTheme.typography.*`. This is the same class of issue CLAUDE.md §2.7 calls out for hardcoded colors, applied to typography instead — worth fixing for the same reason (consistency, and it stops tracking automatically if the type scale ever changes, e.g. under `specs/material3-expressive-pass`).

## 2. Non-goals

- Does not change the dialog's layout, scroll behavior, or the `Column`/`Box` structure.
- Does not change *which* prayers use this dialog or when it's shown.

## 3. Proposed behavior / UX

Visually near-identical (M3's default `AlertDialog` title/body styles are close to the current bold-18sp look), but now driven by theme typography tokens so it automatically stays consistent with the rest of the app's type scale, including if `specs/material3-expressive-pass` later changes that scale.

## 4. Decisions

- Title: replace `Text(text = ..., fontWeight = FontWeight.Bold)` with `Text(text = ..., style = MaterialTheme.typography.titleLarge)` (or `headlineSmall`, to be confirmed visually against the current bold look during implementation — not a decision that needs owner sign-off, just a styling detail).
- Body: replace `Text(text = ..., fontSize = 18.sp)` with `Text(text = ..., style = MaterialTheme.typography.bodyLarge)`.
- No change to `AlertDialog`'s own default title/text slots otherwise — this is a minimal, scoped fix, not a redesign of the dialog.

## 5. Open questions

- None blocking.

## 6. Tasks

- [x] Title now uses `MaterialTheme.typography.titleLarge` instead of raw `FontWeight.Bold`
- [x] Body now uses `MaterialTheme.typography.bodyLarge` instead of raw `fontSize = 18.sp`
- [x] Removed now-unused `FontWeight`/`sp` imports
- [x] Verified `:app:assembleDebug` builds successfully
- [x] Owner to confirm the dialog still looks right (should be near-identical to before) *(confirmed 2026-09-20)*

## 7. Follow-ups

- None currently identified.

## 8. Files

| File | Change |
|---|---|
| `presentation/screens/prayer/PrayerDialog.kt` | Replace raw `fontSize`/`FontWeight` with `MaterialTheme.typography` styles |
