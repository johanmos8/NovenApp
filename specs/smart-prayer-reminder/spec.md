# Smart reminder: notify only when today's prayer hasn't been done

**Status:** Done
**Owner approval:** Approved by Johan Mosquera (chat: positive framing confirmed, "8pm y un solo canal, el mismo del recordatorio de las 9am"), 2026-09-17; confirmed working on-device 2026-09-20 ("aprobar todos excepto el splash art")
**Scope:** `reminder/DailyNovenaReminderWorker.kt`, `reminder/NovenaReminderScheduler.kt`, `reminder/NovenaNotifier.kt`, `data/progress/DevotionalProgressStore.kt` (read-only usage)
**Related:** `V3_REDESIGN_ANALYSIS.md` §7 Phase 4 (notifications) — extends the already-shipped C.11 daily reminder; new item added to the plan at the owner's request (2026-09-17); **directly implicated: `CLAUDE.md` §2.6 ("No streak/loss framing on the calendar-locked novena")**

---

## 1. Intent

Today, `DailyNovenaReminderWorker` fires unconditionally at 9am every active day with a generic "Today is Day N" message — it never checks whether the user has already prayed that day (`DevotionalProgressStore` already tracks exactly this, per-devotional, and is unused by the reminder system today). The ask is a second, smarter check-in later in the day that only notifies if the day is still unprayed — closing the loop between the existing progress-tracking feature and the existing reminder feature, which currently don't talk to each other at all.

## 2. Non-goals

- Does not remove or change the existing 9am "Day N is here" notification — this spec is additive.
- Does not add any per-day streak counter, "days missed" tally, or loss-framed copy/badge anywhere in the UI — see the Decisions section below, this is a hard constraint from `CLAUDE.md` §2.6, not a style preference.
- Does not touch the widget's own refresh logic (`WidgetMidnightRefreshWorker`).

## 3. Proposed behavior / UX

A second `WorkManager` chain, structured exactly like the existing `NovenaReminderScheduler` pattern (self-rescheduling `OneTimeWorkRequest` via `resolveNextOccurrence`), fires later in the day (time TBD — see Open questions) and:
1. Resolves today's day via `Util.resolveCurrentDay` (same as the existing worker) — if `null` (outside the active window), does nothing, exactly like the existing worker.
2. Reads `DevotionalProgressStore.observeCompletedDays(devotionalId)` for the active devotional.
3. If today's day is **already** in the completed set → shows nothing (silently skips).
4. If today's day is **not** completed → shows a gentle check-in notification.

## 4. Decisions

**⚠️ Framing — this needs explicit owner sign-off, it isn't a free technical choice:**

You asked for something "similar to Duolingo" — but Duolingo's reminder pattern is built on loss-aversion/streak framing ("you'll lose your streak!"), and `CLAUDE.md` §2.6 explicitly rules that out for this calendar-locked novena: *"Reinforcement for the fixed 9-day novena is positive-only... a streak model is only appropriate for a future evergreen or self-paced devotional, and even then needs its own spec before it's built."*

**Recommendation:** keep the *mechanism* you're asking for (a check-in that only fires when unprayed) but make the *copy* positive/inviting rather than loss-framed — e.g. "Today's prayer is still waiting for you 🕯️" rather than anything implying a broken streak, missed obligation, or guilt. This satisfies the actual behavior you asked for (a nudge when nothing's been done yet) without violating the existing architecture principle.

**Other decisions:**
- **Time of day:** recommend early evening (e.g. 7–8pm) as a reasonable "you probably haven't gotten to it yet today" checkpoint, distinct enough from the 9am worker to not feel redundant. Exact hour is an open question below.
- **Implementation shape:** a new `EveningCheckInWorker` (or similarly named) mirroring `DailyNovenaReminderWorker`'s structure, scheduled by extending `NovenaReminderScheduler`'s existing `enqueue<W>` helper (already generic over worker type) rather than duplicating the scheduling logic.
- **Notification channel:** reuse the existing notification channel from `NovenaNotifier` unless the owner wants these visually/behaviorally distinguished as a separate channel (which would let a user mute one but not the other) — open question below.

## 5. Open questions

None — all resolved 2026-09-17: positive framing confirmed; time set to 8pm; single shared channel with the 9am reminder.

## 6. Tasks

- [x] Added `EveningCheckInWorker` mirroring `DailyNovenaReminderWorker`'s shape - resolves today's day, checks `DevotionalProgressStore`, skips silently if already prayed or outside the active window, self-reschedules for tomorrow
- [x] Extended `NovenaReminderScheduler` with `scheduleEveningCheckIn()` (8pm) using the existing generic `enqueue<W>` helper; wired into `scheduleIfNeeded()`
- [x] Added `NovenaNotifier.showEveningCheckIn()`, refactored to share a private `showNotification()` helper with `showDailyReminder()` - both use the same `CHANNEL_ID`, distinct notification IDs
- [x] Added positively-framed `notification_evening_check_in_text` string (both locales) - "still waiting for you," no streak/loss language
- [x] Verified `:app:assembleDebug` builds successfully
- [ ] Owner to confirm the 8pm check-in fires correctly and only when the day is unprayed (real-device/emulator test, since WorkManager timing isn't practically unit-testable here)

## 7. Follow-ups

- If a future evergreen/self-paced devotional is ever built, a real streak model becomes appropriate there per CLAUDE.md §2.6/2.8 — out of scope here, this spec is scoped to the existing fixed-range novena only.

## 8. Files

| File | Change |
|---|---|
| `reminder/DailyNovenaReminderWorker.kt` | Reference only — new worker mirrors this shape |
| `reminder/NovenaReminderScheduler.kt` | Add scheduling for the new evening worker via the existing generic `enqueue<W>` helper |
| `reminder/NovenaNotifier.kt` | Add a (positively-framed) check-in notification method |
| `data/progress/DevotionalProgressStore.kt` | Read-only: query today's completion status |
