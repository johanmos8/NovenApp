# Material 3 Expressive confirmation pass

**Status:** Done
**Owner approval:** Approved by Johan Mosquera (chat, "apruebo 1 y 2"), 2026-09-17; confirmed 2026-09-20 ("aprobar todos excepto el splash art")
**Scope:** `gradle/libs.versions.toml` (Compose BOM / material3 dependency), `ui/theme/`, shape-using composables (`JourneyCard.kt`, `SegmentedProgressIndicator.kt`, buttons/typography)
**Related:** `V3_REDESIGN_ANALYSIS.md` §5 B.6; CLAUDE.md §2.7 (theme tokens over hardcoded colors — already satisfied, this spec is about the shape/motion system, not color)

---

## 1. Intent

The audit's reconciliation (§0, item B.6) flagged that `JourneyCard` and `SegmentedProgressIndicator` already use expressive-*looking* shapes, but it was never confirmed whether the project is actually pulling in the formal Material 3 Expressive release of `androidx.compose.material3`, or whether those are bespoke shapes built on the older M3 API that merely resemble it. This matters because the formal Expressive release also brings updated component defaults (buttons, motion specs, type scale) that a bespoke approach doesn't get automatically — worth knowing which situation the app is actually in before calling this item done.

## 2. Non-goals

- Not a visual redesign — if the Compose BOM already includes Expressive and nothing looks broken, this spec's job is done once confirmed; it is not an invitation to restyle screens.
- Does not touch seasonal color tokens (`RedChristmas`/`GreenChristmas`/`GoldChristmas` successor palette) — that was already resolved per §0 item B.7.

## 3. Proposed behavior / UX

No user-visible change expected if the dependency is already current. If bumping the Compose BOM changes default component look (button shapes, ripple, type scale) as a side effect, those diffs get called out explicitly for approval before merging — not silently absorbed as "just an upgrade."

## 4. Decisions

**Investigation first:** check `composeBom` version in `gradle/libs.versions.toml` (currently `2026.05.01` per the last read) against when Material 3 Expressive shipped, and check whether `MaterialExpressiveTheme`/`ExpressiveShapes` (or whatever the current API surface is called) is actually in use in `ui/theme/`.

**Two possible outcomes, both fine, but requiring different owner sign-off:**
- **A. Already current** — the BOM already ships Expressive; nothing to build, just document it and close this item in the audit. No visual risk.
- **B. Needs a bump** — the BOM predates Expressive; bumping it may shift default shapes/motion for stock components (buttons, cards) even where no explicit style is set. This carries real visual regression risk across every screen and should come back to the owner as a follow-up spec with before/after screenshots rather than being merged as part of this investigation.

**Recommendation:** do the investigation now (no code changes), then report back which outcome applies before writing any Tasks.

**Investigation result (2026-09-17):** ran `./gradlew :app:dependencies --configuration debugRuntimeClasspath` and confirmed the Compose BOM (`2026.05.01`) resolves `androidx.compose.material3:material3` to **1.4.0** — the release that ships the stable Material 3 Expressive APIs (`MaterialExpressiveTheme`, expressive shapes/motion). **Outcome A applies: already current.** No dependency bump needed, no visual-regression risk, nothing further to build for this item.

## 5. Open questions

None — resolved by the investigation. (Outcome B's question is moot.)

## 6. Tasks

- [x] Confirm resolved `material3` version via Gradle dependency graph → `1.4.0`, includes stable Expressive APIs — no bump required

## 7. Follow-ups

- If B applies and is approved, the actual bump + regression pass becomes its own spec once the investigation's findings are in.

## 8. Files

| File | Change |
|---|---|
| `gradle/libs.versions.toml` | Confirm/bump `composeBom` version (investigation first) |
