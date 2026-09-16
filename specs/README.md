# specs/

This is NovenApp's spec-driven development (SDD) workspace. Every non-trivial feature
or redesign gets a spec **before** code, following the human-in-the-middle workflow
defined in `../CLAUDE.md` §1 — the short version: **nothing gets built without the
project owner explicitly approving the spec first.**

## Layout

```
specs/
  README.md              this file
  _template/spec.md       copy this to start a new spec
  <feature-slug>/spec.md  one folder per feature/change
```

Name feature folders with a short kebab-case slug describing the change, e.g.
`specs/self-paced-schedule/spec.md`, `specs/season-progress-key-fix/spec.md`.

## Lifecycle

A spec's `Status` field moves through exactly these states:

| Status | Meaning |
|---|---|
| `Draft` | Written, presented to the owner, **not yet approved**. No code changes yet. |
| `Approved` | Owner explicitly signed off on the Decisions section. Implementation may begin. |
| `In Progress` | Implementation underway against an Approved spec. |
| `Done` | Owner confirmed the result matches the spec. |
| `Superseded` | Replaced by a later spec — link to it. |

A spec never skips `Draft → Approved`. If scope changes materially during
implementation, the spec goes back to the owner for approval of the delta before
continuing — don't silently expand or reinterpret an already-approved spec.

## Writing a spec

1. Copy `_template/spec.md` into a new `specs/<slug>/spec.md`.
2. Fill in **Intent**, **Non-goals**, **Proposed behavior**, and **Decisions**. Where a
   decision has more than one reasonable option, present the options and a
   recommendation — don't just pick one silently.
3. Leave **Open questions** for anything that genuinely needs the owner's input before
   the spec can be approved.
4. Present the Draft to the owner and stop. Do not fill in **Tasks** or touch code
   until the status is updated to `Approved`.
5. Once approved, break the work into **Tasks** and implement against them.
6. Log anything discovered-but-out-of-scope in **Follow-ups** rather than folding it
   into the current change.

## Relationship to `docs/`

`docs/` holds write-ups for features that already shipped (e.g.
`docs/home-journey-redesign.md`) — historical/reference material. `specs/` is where
work starts, going forward. When a spec's implementation is `Done`, it can stay in
`specs/` as the historical record for that change (no need to move it to `docs/`).
