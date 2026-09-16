# NovenApp — Constitution

This file is the standing contract for anyone (human or AI agent) working on this
codebase. It has two parts: a **workflow** that governs *how* decisions get made, and
a set of **architectural principles** that govern *what* good changes look like here.
Both are binding. If a task seems to require breaking either, stop and raise it
instead of proceeding.

This document itself is under the same rule it defines: changes to `CLAUDE.md` are a
decision, and decisions belong to the project owner (see below). Propose edits here as
a diff for approval, don't just rewrite it.

---

## 1. Workflow: human-in-the-middle

**Every decision passes through the project owner. No exceptions, no assumed consent.**

This is a one-person-owned app. An agent's job is to research, draft options, propose
a concrete plan, and — once explicitly told to proceed — execute it well. It is never
to decide unilaterally what the app should do, look like, or be built with.

### 1.1 The gates

Work on any non-trivial feature or change moves through these stages. **Each arrow is
a stop, not a formality** — do not cross it without an explicit go-ahead.

```
Ask/Task  →  [spec: Draft]  →  APPROVAL GATE  →  [spec: Approved]
                                                        │
                                                        ▼
                                          tasks / implementation plan
                                                        │
                                                  APPROVAL GATE (if the
                                                  plan deviates from the
                                                  spec, or is non-obvious)
                                                        ▼
                                              implementation (code)
                                                        │
                                                  owner reviews the result
                                                        ▼
                                                  [spec: Done]
```

1. **Draft a spec before writing code** for anything that isn't a trivial, obviously-scoped
   fix (typo, one-line bug, a change the owner described in exact, unambiguous terms).
   Use `specs/_template/spec.md` — see `specs/README.md` for the full lifecycle.
2. **Present the Draft spec and stop.** Do not start implementation, do not write
   tasks, do not touch code. Wait for the owner to respond.
3. **Silence is not approval.** No response, a vague "ok", or moving on to another
   topic does not authorize implementation. Approval means the owner clearly says so
   (e.g. "apruebo", "adelante", "sí, impleméntalo").
4. Once **Approved**, the spec's "Decisions" section is the source of truth for *what*
   to build. If implementation surfaces a case the spec didn't cover, or a better
   approach becomes apparent mid-build, **pause and ask** rather than deciding
   silently and continuing — even if the deviation seems small or obviously better.
5. **Destructive or hard-to-reverse actions always need a fresh, explicit ask**, even
   inside already-approved work: `git push`, force-push, deleting files/branches,
   dropping generated code that isn't trivially regenerable, changing signing/release
   config, touching `novenapp.jks`, editing CI, or anything that reaches outside this
   repo (Play Console, external services).
6. When the work is done, say so plainly and let the owner confirm before marking the
   spec `Done`. Don't self-certify a feature as finished.

### 1.2 Scope discipline

Keep changes tied to exactly what the spec (or the owner's explicit instruction)
names. If you notice adjacent problems, dead code, or "while I'm in here" improvements,
**do not fold them into the current change** — note them (in the spec's Follow-ups
section, or just to the owner) and propose them as a separate, separately-approved
piece of work.

### 1.3 What doesn't need a spec

Small, unambiguous, fully-specified asks ("rename this string key", "fix this null
crash on line 42", "bump the version code") can go straight to implementation without
a spec document — but the approval gate still applies before anything is committed or
pushed. When in doubt about whether something is "small," treat it as needing a spec;
the cost of a short spec is much lower than the cost of unwinding an unwanted change.

---

## 2. Architecture principles

These are standing constraints on the codebase, distilled from `V3_REDESIGN_ANALYSIS.md`
(the original architecture audit) and from decisions already made and documented in
`specs/` / `docs/`. They exist so the same mistakes aren't re-made as the app grows
beyond the single Christmas novena it started as.

1. **The content model stays generic.** `Devotional` and its content blocks must
   represent *any* devotional (a novena, a rosary, a daily gospel reading), not fields
   named for Christmas prayers. If a change only fits by adding Christmas-specific
   named fields again, it's the wrong shape — model it as typed, ordered content
   blocks instead.
2. **Date-gating is per-devotional config, not a global constant.** Values like
   start/end day belong on a devotional's own schedule (`DevotionalSchedule.FixedRange`,
   `.Evergreen`, and eventually `.SelfPaced`), never as top-level constants that assume
   there's only one devotional.
3. **Routes and state are parameterized by devotional id.** Don't reintroduce code
   that assumes "the one devotional that exists" — new features (widget, reminders,
   deep links) must be able to point at a specific devotional.
4. **One DI-managed instance per ViewModel/repository.** Don't hand-construct
   `MainViewModel` or repositories in more than one place. Route everything through
   the DI graph (`di/AppModule.kt`) so state isn't silently duplicated.
5. **Content assembly stays out of the UI/navigation layer.** Building the list of
   content blocks for a day belongs in a mapper/use-case, not inline inside a
   composable in the nav graph.
6. **No streak/loss framing on the calendar-locked novena.** Reinforcement for the
   fixed 9-day novena is positive-only (see `docs/home-journey-redesign.md §3.3`); a
   streak model is only appropriate for a future evergreen or self-paced devotional,
   and even then needs its own spec before it's built.
7. **Theme tokens over hardcoded colors.** No `Color.White`/`Color.DarkGray`/etc.
   literals in composables — use `MaterialTheme.colorScheme` (or the seasonal
   `RedChristmas`/`GreenChristmas`/`GoldChristmas` tokens where a seasonal accent is
   intended) so dark mode and dynamic color stay honored everywhere.
8. **Don't build ahead of content.** Speculative structure for a devotional type that
   doesn't exist yet (e.g. `SelfPaced` scheduling with no self-paced content authored)
   is explicitly out of scope until there's real content behind it — see
   `docs/home-journey-redesign.md §4`.

---

## 3. Project orientation (for a cold-start agent)

- **What it is:** a single-purpose Android app (Jetpack Compose + Material 3) guiding
  a user through the Colombian *Novena de Aguinaldos*. No accounts, no backend.
- **Module layout:** `:app` (the application) + `:compose-preview` (multi-device/theme
  Compose previews).
- **Key packages** under `app/src/main/java/com/mirkwood/novenapp/`:
  `data/devotional` (repository + catalog), `data/progress` (completion tracking),
  `di` (DI graph), `presentation/screens/{home,prayer,lyrics,aboutus,more}`,
  `reminder` (WorkManager daily notification), `widget` (Glance home-screen widget),
  `debug` (debug-only forced-date override for previewing seasonal UI off-season).
- **Living docs:**
  - `specs/` — feature specs going forward (this is the SDD workflow; see
    `specs/README.md`).
  - `docs/` — design notes for already-implemented features (e.g.
    `home-journey-redesign.md`).
  - `V3_REDESIGN_ANALYSIS.md` — the original architecture audit. Partially stale (Phase
    0/1 and part of Phase 4 from its plan are already implemented) — treat it as
    historical context, not a live status tracker; current/future work belongs in
    `specs/`.
