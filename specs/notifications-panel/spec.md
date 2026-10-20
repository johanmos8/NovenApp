# Notifications panel (the app bar bell)

**Status:** Done
**Owner approval:** Approved by Johan Mosquera (chat, "apruebo con las recomendaciones, implementa"), 2026-10-19 — Q1 (a), Q2 yes, Q3 before December; confirmed 2026-10-20 ("aprueba los specs anteriores")
**Scope:** `presentation/components/NovenAppBar.kt` (bell action and icon state), `MainActivity.kt` (hosting the sheet), new `presentation/components/NotificationsSheet.kt`, `presentation/screens/home/HomeScreen.kt` (permission helpers moved out to be shared), `presentation/MainViewModel.kt` (state for the sheet), `reminder/NovenaReminderScheduler.kt` (expose the reminder hours read-only), `presentation/util/Util.kt` (only if Q1 = (a)), `res/values*/strings.xml`
**Related:** `specs/code-audit-v3` Q7 (decision: the bell shows the devotionals with active notifications) and P1-3; `specs/devotional-catalog-selection` §4.2/§4.3 (long-press toggle, cycle-scoped override); `specs/code-audit-v3` P0-2, P0-3, P1-5, R1 (override, scheduling and permission behavior this reuses); `CLAUDE.md` §1.4, §2.3, §2.6

---

## 1. Intent

The bell in the app bar (Home, Carols, More) does nothing today (`onNotificationsClick = {}`),
so v3.0.0 would ship a dead button. In Q7 the owner decided it should show the devotionals
whose notifications are active. On 2026-10-19 a history of sent notifications was
evaluated and rejected: it would repeat what the Home card already shows, need a new
store written from background workers, and invite unread-count pressure (§2.6).

The panel also fixes the two weakest points of the current notification UX:

1. **Discoverability.** The only way to turn a devotional's notifications on or off is a
   long press on its Home card, which has no visible affordance
   (`devotional-catalog-selection` §4.2 says so itself).
2. **Recovery.** When notifications are blocked, the card shows a crossed-out bell next to
   "Active", but there's no path to fix it from inside the app.

## 2. Non-goals

- **No notification history or inbox, and no unread badge.** Rejected (§1).
- **No custom reminder times.** 9:00 and 20:00 are shown read-only; making them
  editable needs its own spec (new persisted store, rescheduling).
- **No change to scheduling, cycle keys or override semantics** (P0-2/P0-3 stay as
  they are). The panel calls the existing `MainViewModel.toggleNotification`.
- **The long press stays.** It remains a shortcut on the Home card.
- **The Profile icon stays hidden** (`SHOW_PROFILE_ACTION = false`). Separate question.
  *(Closed 2026-10-20, `code-audit-v3` Q7: there is no Profile action; the code stays hidden.)*
- **The widget is unchanged.**

## 3. Proposed behavior / UX

### 3.1 Entry point
Tapping the bell opens a `ModalBottomSheet` over the current tab. The bell icon reflects
the app's notification state:

| State | Icon | Content description |
|---|---|---|
| Notifications can be posted | `Notifications` | "Notificaciones" (as today) |
| Blocked (permission denied, app or reminder channel off; same check as `NovenaNotifier.canPostNotifications`) | `NotificationsOff` | "Notificaciones bloqueadas" |

No dot or count badge on the bell.

### 3.2 Sheet content

```
Notificaciones
────────────────────────────────────────────
⚠ Las notificaciones de NovenApp están        ← only when blocked
  bloqueadas en este dispositivo.
  [ Abrir Ajustes ]
────────────────────────────────────────────
Novena de Aguinaldos                  [ ● ]
Recordatorio 9:00 · Cierre del día 20:00
Empieza el 16 de diciembre                    ← only outside the window
```

*(As built, the subtitle reuses the existing `journey_starts_on` string, "Comienza el …" /
"Starts on …", not "Empieza el …". Same meaning, one string shared with the Home card.)*

- **One row per catalog devotional**, in the Home feed's order (`homeDevotionals`). Today
  that's one row. A future evergreen devotional is simply another row (§2.3).
- **Switch.** Toggling calls the existing `toggleNotification(devotionalId, isCurrentlyNotifying)`.
  Turning on while the Android 13+ permission is missing first shows the system dialog,
  using the same requester and flow as the long press (P1-5). Turning on while blocked
  some other way keeps the switch on and shows the blocked banner (no Toast; the sheet
  shows it inline).
- **Subtitle line 1:** the reminder times, formatted with the device's 12/24-hour
  setting, read from the scheduler's constants (no duplicated literals).
- **Subtitle line 2:** for a `FixedRange` devotional outside its window, "Empieza el
  {date}". Hidden inside the window. Never shown for `Evergreen`.
- **Blocked banner:** neutral copy (no blame, §2.6). "Abrir Ajustes" opens
  `Settings.ACTION_APP_NOTIFICATION_SETTINGS` for this package (available on minSdk 28).
  The sheet re-checks on resume, so returning from Settings updates the banner, the bell
  and the switches immediately.
- **Dark mode / palette:** standard `ModalBottomSheet` surface. The switch uses M3
  defaults (`primary` when on). The banner uses `errorContainer`/`onErrorContainer`, the
  same family as the card's blocked bell. Consistent with `palette-tonal-containers` §3.2:
  the switch is the sheet's only primary element.

### 3.3 Boundary scenarios (CLAUDE.md §1.4)

Novena window Dec 16–24, celebration Dec 25.

| When | Situation | Expected |
|---|---|---|
| Oct 19 | Never touched | See Q1. (a): switch **on**, "Empieza el 16 de diciembre". (b): switch **off**, same subtitle |
| Oct 19 | User turns it off | Override (false, cycle Dec 16) → switch off; no reminders in December (P0-2) |
| Dec 15, 23:59 | Default | (a): on, subtitle shown. (b): off, then on at 00:00 Dec 16 without user action |
| Dec 16, 10:00 | Turned on now | Today's 9:00 already passed → first reminder 20:00 today; nothing fires immediately (P0-3) |
| Dec 24, 21:00 | Turned on now | Next slot is the next window (Dec 16 next year); subtitle not shown yet (still in window) |
| Dec 25 | Celebration day | Card hidden on Home, but the bell and sheet still work. Override from this cycle expired → row shows the next cycle's state with "Empieza el 16 de diciembre" |
| Any time | Permission denied for good | Turning on → no dialog (Android won't show it) → switch on + blocked banner → "Abrir Ajustes" |
| Any time | Returns from Settings with notifications enabled | Banner gone, bell icon back to normal, without reopening the sheet |
| Rotation with the sheet open | — | Sheet stays open (saveable state) |

## 4. Decisions

### 4.1 Bottom sheet (recommended) vs. a new screen
A sheet keeps the user in place, fits one to a few rows, and needs no new route or back
stack entry. A full screen only pays off with per-devotional settings (custom times),
which are a non-goal.

### 4.2 Reuse, don't duplicate
- `toggleNotification` and `NovenaNotifier.canPostNotifications` are reused as they are.
- The permission helpers that are private to `HomeScreen.kt` today
  (`needsNotificationPermission`, the requester, `areNotificationsEnabled`) move to a
  shared file so Home and the sheet run the exact same flow. The auto-prompt-once rule
  (R1) stays where it is. It's a move, not a behavior change.
- The reminder hours become readable constants on `NovenaReminderScheduler` (still
  `const`).

### 4.3 State
The sheet reads `homeDevotionals` (already has `isNotifying` per devotional). Whether
it's open is local `rememberSaveable` in `MainActivity`'s app bar host. No new
ViewModel or store (§2.4).

## 5. Open questions

- **Q1 → (a):** the switch shows the relevant cycle's state (`Util.isNotifyingForRelevantCycle`).
- **Q2 → yes:** the Home "Active" badge uses the same value.
- **Q3 → before December.**
- **Implementation note (not a behavior change):** `DevotionalHomeItem` keeps today's effective
  state as `isNotifyingNow`, used only by Home's one-time automatic permission prompt (R1).
  Without it, Q2 would have made that prompt fire out of season (e.g. October) for a default
  novena. R1's behavior stays exactly as approved.

## 6. Tasks

- [x] `presentation/notifications/NotificationPermission.kt`: permission helpers moved from `HomeScreen.kt` unchanged, plus `openAppNotificationSettings` (falls back to app details, never crashes)
- [x] `Util.isNotifyingForRelevantCycle`; `DevotionalHomeItem.isNotifying` uses it, and `isNotifyingNow` keeps the R1 prompt on today's state
- [x] `NovenaReminderScheduler.REMINDER_HOUR` / `EVENING_CHECK_IN_HOUR` made `internal`
- [x] `NotificationsSheet`: title, blocked banner (`errorContainer`) with "Abrir Ajustes", one toggleable row per devotional (times in the device's 12/24h format, "Comienza el …" outside the window)
- [x] `NovenAppBar`: bell opens the sheet; `NotificationsOff` icon + description when blocked
- [x] `MainActivity`: hosts the sheet (saveable open state), re-checks blocked on resume and after the permission dialog
- [x] Strings es/en
- [x] `compileDebugKotlin` passes; adversarial re-read of the diff: workers and the scheduler still decide firing with today's effective state (untouched)
- [x] Owner on-device review (scenarios in §3.3) *(owner confirmation 2026-10-20, "aprueba los specs anteriores")*

## 7. Follow-ups

- When the permission was simply never asked (fresh Android 13+ install, out of season), the banner's
  button could show the system dialog instead of opening Settings. Settings works in every case, and
  the switch already triggers the dialog, so this is polish only.

- Editable reminder times (per devotional or global). Needs a store and rescheduling, so it
  gets its own spec.
- ~~Profile icon purpose (Q7, still open).~~ Closed 2026-10-20: no Profile action.
- If more devotionals exist later, consider a "turn all off" action at the top of the
  sheet.

## 8. Files

| File | Change |
|---|---|
| `presentation/components/NovenAppBar.kt` | Bell icon switches to `NotificationsOff` when blocked; new `notificationsBlocked` parameter |
| `presentation/components/NotificationsSheet.kt` | New: sheet with banner + one row per devotional |
| `MainActivity.kt` | Hosts the sheet; passes the click and blocked state to `NovenAppBar` |
| `presentation/notifications/NotificationPermission.kt` (new) | Permission helpers moved from `HomeScreen.kt`, unchanged |
| `presentation/screens/home/HomeScreen.kt` | Uses the shared helpers |
| `presentation/MainViewModel.kt` | Q1/Q2 = (a), applied: `isNotifying` uses the upcoming-window value; `isNotifyingNow` keeps today's |
| `presentation/util/Util.kt` | Q1 = (a), applied: `isNotifyingForRelevantCycle` |
| `reminder/NovenaReminderScheduler.kt` | Reminder hour constants readable (`internal`) |
| `res/values/strings.xml`, `res/values-en/strings.xml` | Sheet title, subtitle templates, blocked banner, "Abrir Ajustes", blocked bell description |
