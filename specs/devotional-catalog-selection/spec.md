# Devotional catalog & selection (Home stops assuming "the one devotional")

**Status:** In Progress — implemented, awaiting owner confirmation on-device (per `CLAUDE.md` §1.1.6) - this touches notification scheduling, which can't be fully verified without a real device/emulator
**Owner approval:** Approved by Johan Mosquera (chat, "Sí, apruebo"), 2026-09-22
**Scope:** `presentation/MainViewModel.kt`, `presentation/screens/home/HomeScreen.kt`, `presentation/screens/home/JourneyCard.kt`, `presentation/screens/home/JourneyStatus.kt`, `reminder/*`, `widget/NovenaWidget.kt`, `debug/DebugSettings.kt`, `presentation/components/CountdownToDate.kt`
**Related:** Depends on `specs/generic-devotional-content-model` (a second devotional needs a generic content shape before it can exist at all); `V3_REDESIGN_ANALYSIS.md` §0 Phase 0.6 ("Devotionals home listing" - explicitly deferred as "not needed yet"); CLAUDE.md §2.1/§2.3

---

## 1. Intent

Every place that reads "which devotional" today hardcodes the first (and only) catalog entry — confirmed directly in code, 7 call sites: `MainViewModel.activeDevotionalId`, `DailyNovenaReminderWorker`, `EveningCheckInWorker`, `NovenaReminderScheduler`, `NovenaWidget`, `DebugNotificationTrigger` (×2), plus `DevotionalCatalog.default` used in `CountdownToDate`'s preview, `HomeScreen`'s previews, and `DebugSettings`' novena-day-to-date helper. None of this is wrong *today* (there's only one devotional), but every one of these is exactly the code CLAUDE.md §2.3 ("Routes and state are parameterized by devotional id... don't reintroduce code that assumes 'the one devotional that exists'") warns will need rework the moment a second devotional exists. This spec does that rework, plus the actual UI surface (Phase 0.6, "Devotionals home listing", explicitly deferred in the original audit until it was needed - it's needed now) for a user to see/reach more than one devotional.

## 2. Non-goals

- Does not add a second devotional's content - depends on `specs/generic-devotional-content-model` for the *shape*, and separately on content being authored. This spec's job is to make the app behave correctly *once* a second catalog entry exists, provable with a placeholder/test entry if needed before real content is ready.
- Does not build `DevotionalSchedule.SelfPaced` (user-chosen start date) - a distinct, larger, not-yet-approved idea from earlier in this conversation. Out of scope here.
- Does not build the community-feed direction discussed alongside this ("Dirección 3") - explicitly the most expensive, most different path; not started.
- Does not build a full in-app notification history/center. The notification bell icon (previously slated for removal - see the icon-viability discussion in this conversation) now has a concrete future justification under Decision 4.4 (an aggregate summary of which devotionals are notifying), but actually building that is bigger than this spec and belongs to its own follow-up once there are enough active devotionals to justify it.

## 3. Proposed behavior / UX (revised 2026-09-22 after discussion)

**Home *is* the devotional list, from the start** - not one featured card plus a secondary screen (the original Decision 4.2 recommendation, superseded). 
Every devotional the catalog considers relevant today (an in-window `FixedRange`, or any `Evergreen`) renders as its own card in a single scrollable feed, 
reusing the existing `JourneyCard` per entry rather than introducing a second, different list-item design. 
This is also where "Dirección 2" content (a bundled daily contemplation, once built) would naturally slot in later - one more card in the same feed, not a separate surface.

**Activating/deactivating a devotional:** long-pressing a card directly toggles it active/inactive (owner's explicit choice, 2026-09-22 - no context menu). To keep this discoverable despite being a "hidden" gesture with no visible affordance beforehand, the toggle fires haptic feedback plus a brief Snackbar confirmation ("Rosario diario activado" / "desactivado") the moment it happens, and the card keeps a persistent badge/check afterward so its state reads at a glance without needing to repeat the gesture to check (see Decision 4.2).

**Notifications, with multiple devotionals:** no longer "featured vs. not" - see Decision 4.3. Each devotional independently can be notifying or not; a currently-open `FixedRange` novena defaults to on, an `Evergreen` devotional defaults to off until the user opts in.

## 4. Decisions

**4.1 — Home layout & sort order.** Home is a single scrollable feed of `JourneyCard`s, one per relevant devotional (an in-window `FixedRange`, or any `Evergreen`) - not a single featured card. Sort order still needs the old "which one matters most right now" rule, just repurposed for ordering instead of exclusive selection: an in-window `FixedRange` sorts before any `Evergreen`; if more than one `FixedRange` is somehow open at once, the one whose window started first sorts first. This directly fulfils Phase 0.6. **Open sub-question:** should the top item additionally get a visually distinct "featured" treatment (bigger card, different styling) versus every card in the list looking the same? Recommend deciding this once there are 2 real devotionals to look at side by side, rather than guessing at layout weight with only placeholder data - default to "all cards equal weight, sorted" until then.

**4.2 — Activation mechanism (owner's explicit choice, 2026-09-22): long-press directly toggles active/inactive - no context menu.** The tradeoff this accepts: a long-press gesture has no visible affordance before the user commits to it, unlike a menu that shows the available actions first. Mitigated two ways instead of a menu: (1) haptic feedback + a brief Snackbar confirmation fire the instant the toggle happens, so the user gets immediate feedback even without a menu to read first; (2) the card keeps a persistent badge/check afterward, so the current state is always visible at a glance without needing to repeat the gesture just to check. The exact visual treatment of the toggle moment (e.g. a brief highlight/scale-in on the badge) is an implementation-time UI detail, not designed further here - captured in Tasks once approved.

**4.3 — Notifications: a per-devotional toggle, not a single "active" selection.** Resolved through discussion: rather than one exclusive "active" devotional gating all reminders (which risks silently missing a time-bound novena just because something else was chosen earlier), each devotional independently has notifications on or off:
- An in-window `FixedRange` devotional defaults to **on** the moment its window opens - nobody misses a live novena's reminder by default.
- An `Evergreen` devotional defaults to **off** - matches the original reasoning (it's always available, so it doesn't need calendar-driven urgency), but the user can manually turn it on via the same activation mechanism (4.2) if they do want a daily nudge for it (e.g. a daily Rosary reminder).
- **Auto-revert rule:** when a `FixedRange` devotional's window closes, its notification toggle resets to the default (off, until its next occurrence reopens it) - no stale manual state lingers past the devotional's own relevant period. `Evergreen` toggles have no natural end point, so a manual choice there persists until the user changes it. This only clears *that devotional's own* override when *its own* window closes - it does not affect any other devotional's toggle.
- **More than one devotional can notify at the same time** - this was the resolving insight: since Home already shows every relevant devotional as its own visible card (4.1), a second devotional's notification while another is also "on" doesn't read as random noise - the card explaining it is right there. This sidesteps needing a single-exclusive-active model entirely.
- Requires `NovenaReminderScheduler`/the two workers to iterate `repository.getCatalog()` filtered to devotionals with notifications currently on, instead of assuming index 0. The home-screen widget shows one glanceable summary - the same 4.1 sort order picks which one, if several are eligible.

**4.4 — Notification bell, revisited.** The earlier recommendation (remove the bell/profile icons - no code built anything for them) still holds for *this* spec, but the bell now has a concrete future justification this spec's model directly enables: an aggregate summary of which devotionals currently have notifications on (4.3). Building that summary/history UI is out of scope here (see Non-goals) - flagged as the natural follow-up spec once there's more than one real devotional to summarize.

**4.5 — Evergreen `JourneyStatus` completeness.** Unchanged from the original draft: today's `resolveJourneyStatus` has a fallback path for Evergreen, written for a single always-shown devotional. Needs its `InProgress`/`Completed` framing re-checked once a real Evergreen devotional exists - deferred, not decided abstractly here (see Open questions).

## 5. Open questions

- **Home layout weight** (4.1): equal-weight cards for all, or a visually distinct top/featured card? Recommend deciding once a second real devotional exists to actually look at.
- Evergreen's "Completed" framing (4.5) - deferred to a follow-up once a concrete Evergreen devotional exists.
- None of the above block `Approved` - both are explicitly deferred with a recommended default, not left ambiguous.

## 6. Tasks

- [x] `MainViewModel` reworked: `homeDevotionals: StateFlow<List<DevotionalHomeItem>>` replaces the single `activeDevotionalId`/`activeDevotional`/`completedDays`/`NovenaViewState` - every catalog devotional gets its own resolved `currentDay`/`completedDays`/`isNotifying`, combined reactively from `DevotionalProgressStore` + the new `DevotionalNotificationStore` + a manual refresh trigger (date rollover / debug forced-date switch)
- [x] **Bug caught during implementation and fixed:** an early version filtered Home down to only "relevant" (in-window or Evergreen) devotionals - which would have made Home show *nothing* for the ~356 days/year the one real novena isn't active, instead of today's countdown card. Fixed: Home always shows every catalog devotional (`JourneyStatus.NotStarted`'s countdown included); the "relevant" filter only ever applied to the *notification* default, not which cards render.
- [x] New `data/notifications/DevotionalNotificationStore.kt` (DataStore, mirrors `DevotionalProgressStore`'s shape) - per-devotional manual on/off override, `null` = no override (use the schedule-based default)
- [x] `Util.isNotifyingByDefault()` and `Util.pickFeaturedDevotional()` added - shared by `MainViewModel`, `NovenaReminderScheduler`, and `NovenaWidget`
- [x] `HomeScreen` rewritten as a feed (`devotionals.forEach { JourneyCard(...) }`), long-press directly toggles notifications (haptic feedback + Snackbar confirmation, no menu - per owner's explicit choice); `JourneyCard` gained an `isNotifying` badge
- [x] `NovenaAction.GoToDay` now carries `devotionalId`; threaded through `MainActivity`'s `PendingTarget` (replacing the old bare `pendingTargetDay: Int?`), `NovenaNotifier`'s `PendingIntent` extras, and `NovenaWidget`'s launch intent
- [x] `NovenaNotifier` notification IDs now derived per-devotional (`base + devotionalId.hashCode()`), so two devotionals' notifications no longer overwrite each other in the tray
- [x] `NovenaReminderScheduler`/`DailyNovenaReminderWorker`/`EveningCheckInWorker` rewritten: work names and `WorkerParameters.inputData` are now per-devotional; each worker re-checks `isNotifying` before showing *and* before rescheduling itself, so turning notifications off takes effect the same day rather than waiting for the next app start; `scheduleIfNeeded` also clears a `FixedRange` devotional's stale override once its window has closed (4.3's auto-revert rule)
- [x] Turning notifications **on** via long-press also schedules immediately (`MainViewModel.toggleNotification`), rather than only taking effect on next app start
- [x] **Simplification:** `WidgetMidnightRefreshWorker`'s own reschedule no longer ties its cadence to one devotional's schedule (the old code accidentally made it stop firing outside that devotional's window, and `Util.resolveNextOccurrence` doesn't handle `Evergreen` at all) - it now refreshes every midnight unconditionally, which is simpler and avoids that bug, at the cost of a few extra daily wake-ups outside any active season
- [x] Deleted confirmed-dead code found while wiring this up: `GoToDayButton.kt` (unused since the `JourneyCard` redesign) and `NovenaViewState.kt` (fully replaced)
- [x] Verified `:app:assembleDebug` builds successfully after each change
- [ ] Owner to confirm on-device: Home shows the existing single novena correctly in both in-season and off-season states; long-press toggles notifications with haptic + Snackbar feedback; the 9am/8pm notifications still fire correctly; tapping a notification/widget still opens the right day

## 7a. Deferred from this pass (not started)

- The Home layout-weight question (4.1: equal cards vs. a featured one) and Evergreen `JourneyStatus` completeness (4.5) remain open per their recommended defaults - nothing to implement for either until a second real devotional exists.

## 7. Follow-ups

- A future "notification center" spec building the aggregate summary/history the bell icon needs (4.4), once there's more than one real devotional to summarize.
- The activation badge's exact micro-animation (4.2) - a small implementation-time UI choice, not designed further here.
- Business/content: the strongest candidate for the actual *second* devotional to prove all of this against is a **daily Rosary as `Evergreen`** - simpler structurally than Semana Santa (5 decades + a rotating mystery set by weekday, all public-domain/well-established text, no licensing question) and it's also the natural first step toward the "daily contemplation" year-round-engagement idea from the roadmap discussion, since an Evergreen devotional is inherently something to open any day of the year. Semana Santa (a `FixedRange` devotional, different dates each year since Easter moves) is a reasonable second addition after that, once the catalog/selection machinery has been proven with two devotionals of different schedule types.

## 8. Files

| File | Change |
|---|---|
| `presentation/MainViewModel.kt` | No longer resolves one `activeDevotionalId` - Home now needs the full relevant list |
| `presentation/screens/home/HomeScreen.kt` | Becomes a scrollable feed of `JourneyCard`s (4.1), not one card |
| `presentation/screens/home/JourneyCard.kt` | Reused per list item; add the activation badge + long-press context menu (4.2) |
| New: per-devotional notification-toggle store | Persists which devotionals currently notify (`DataStore`, mirrors `DevotionalProgressStore`'s shape) |
| `reminder/NovenaReminderScheduler.kt`, `DailyNovenaReminderWorker.kt`, `EveningCheckInWorker.kt` | Iterate devotionals with notifications on (4.3), not index 0 |
| `widget/NovenaWidget.kt` | Featured devotional via the 4.1 sort rule |
| `presentation/screens/home/JourneyStatus.kt` | Evergreen framing revisited (4.5, per Open Questions) |
| `debug/DebugSettings.kt`, `presentation/components/CountdownToDate.kt` | `.default` usages reconciled with the 4.1 sort rule |
