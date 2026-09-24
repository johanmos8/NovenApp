# Generic devotional content-block model (foundation for multi-devotional support)

**Status:** Done
**Owner approval:** Approved by Johan Mosquera (chat, "apruebo content model spec"), 2026-09-22, after resolving the one-vs-two-object-types question in discussion (see correction note in §4); confirmed working in both English and Spanish on-device 2026-09-22 ("parece ser que funciona bien en español también, aprueba ese spec")
**Scope:** `presentation/model/Novena.kt`, `presentation/model/NovenaTab.kt`, `presentation/model/devotional/DevotionalImages.kt`, `presentation/mapper/DevotionalDayMapper.kt`, `data/devotional/DevotionalRepositoryImpl.kt`, `app/src/main/assets/content_es.json`, `app/src/main/assets/content_en.json`
**Related:** `CLAUDE.md` §2.1 ("The content model stays generic"); `V3_REDESIGN_ANALYSIS.md` §0 Phase 0.0 (claimed ✅ Done — **this spec corrects that**: only the metadata/scheduling layer was generalized, not the content itself); this is the technical foundation "Dirección 1" (multi-festividad) depends on

---

## 1. Intent

The owner's stated direction: grow NovenApp beyond a single Christmas novena to other religious festivities (Semana Santa, a daily Rosary, novenas to specific saints, etc.). Checking the code directly to validate feasibility surfaced a blocking gap the original audit's reconciliation table missed: **the actual devotional *content* model is still entirely Christmas-novena-shaped**, even though `DevotionalMeta`/`DevotionalSchedule` (the *metadata* layer) genuinely were generalized in `591f27c`. Concretely:

- `Novena`/`Dia`/`General`/`Gozo` (`presentation/model/Novena.kt`) have hardcoded fields like `oracion_virgen_maria`, `oracion_san_jose`, `oracion_niño_jesus` — this is precisely the pattern CLAUDE.md §2.1 forbids.
- `DevotionalImages` (used by `DevotionalMeta.images`) has hardcoded fields `virgenMaria`, `sanJose`, `gozos`, `ninoJesus` for the same reason.
- `DevotionalDayMapper.buildDayPrayers()` hardcodes an exact 6-page sequence (reflection → daily prayer → prayer to Mary → prayer to Joseph → Gozos → prayer to the Child Jesus) reading those exact fields by name, with titles sourced from `NovenaTab`, a fixed 6-entry `sealed class`.

**None of this can represent a devotional with a structurally different day** — a Vía Crucis (14 stations, each with its own short meditation), a daily Rosary (5 decades, a different mystery set per day of the week), or even a differently-shaped novena to another saint, would each require new Kotlin data classes and a new hardcoded mapper function, exactly the "branch the code per devotional" outcome CLAUDE.md §2.1 rules out. This spec is the one genuinely blocking prerequisite for adding any second devotional with different content shape — everything else in the "Dirección 1" roadmap (catalog UI, multi-devotional notifications) can be built on top of today's model for exactly one devotional, but not for a second, differently-shaped one.

## 2. Non-goals

- Does not add a second devotional's actual content — this spec only makes the *shape* generic; authoring a Vía Crucis or Rosary's real text is separate work (content/business, not engineering), covered as a Follow-up.
- Does not touch `DevotionalMeta`/`DevotionalSchedule` — those are already appropriately generic (confirmed by reading them directly).
- Does not change the catalog/selection UI, widget, or reminder generalization — that's `specs/devotional-catalog-selection`, which depends on this spec but is reviewed separately.
- Does not change `Prayer` (`presentation/model/Prayer.kt`), the *rendering*-layer model `PrayerScreen`'s pager consumes — that model is already reasonably generic (`Simple`, `WithImage`, and a litany-shaped `AllGozos`). The fix here is upstream of it: making the JSON-backed content model produce an ordered block list directly, instead of `DevotionalDayMapper` hardcoding a fixed 6-step assembly from named fields.

## 3. Proposed behavior / UX

No user-visible change if done correctly — this is a data-model/content-schema refactor. The existing Novena de Aguinaldos must render identically before and after, verified by a direct before/after comparison of the rendered day screens (all 9 days, both languages).

## 4. Decisions

**One content-block model, not two (correction after owner discussion, 2026-09-22):** the owner asked whether a bounded novena (Christmas, or any other saint's novena) and an unbounded "daily" devotional should be two separate object types, one "default" and one "custom." Answer worked out in discussion: **no** - the block *types* (reading, litany) are identical needs regardless of whether the devotional is a 9-day novena or a rotating daily one; splitting into two type hierarchies would reintroduce the "two content types, two code paths" problem CLAUDE.md §2.1 already warns against. The axis that actually varies isn't subject matter (Christmas vs. a saint vs. daily) - any *novena*, regardless of which figure it's about, shares the same bounded/culminating shape, already captured by `DevotionalSchedule.FixedRange`. What's genuinely different is **bounded-with-culmination (any novena) vs. unbounded/rotating (`Evergreen`)** - and that axis already exists in `DevotionalSchedule`, so it doesn't need a second content-model type. What the original Decisions draft below got wrong: it implicitly assumed `days[currentDay - 1]`, a simple 1..N index into a fixed window - correct for any `FixedRange` novena, but meaningless for `Evergreen` (a daily Rosary doesn't have "day 47 of the devotion," it has "which of a ~7-entry weekly cycle applies today"). Fixed by making the day-index resolution schedule-aware (see the new `resolveContentIndex` decision below) instead of hardcoding a linear count.

**Target shape (recommendation):** replace `Novena`/`Dia`/`General`/`Gozo` with something like:

```kotlin
data class DevotionalContent(
    val everyDayBlocks: List<ContentBlockData>,   // repeated on every day (e.g. the daily prayer)
    val days: List<DevotionalDayContent>,          // one entry per day, in order
    val closingBlocks: List<ContentBlockData> = emptyList()  // e.g. final prayers shown on every day
)

data class DevotionalDayContent(
    val day: Int,
    val title: String,
    val blocks: List<ContentBlockData>   // day-specific blocks (e.g. that day's reflection)
)

data class ContentBlockData(
    val type: String,          // "reading" | "litany" | ... - discriminator for JSON parsing
    val title: String,
    val text: String? = null,
    val imageKey: String? = null,       // looked up in DevotionalMeta.images, not a hardcoded field name
    val items: List<String>? = null     // for litany-shaped blocks (Gozos, a Rosary decade's Hail Marys, a Via Crucis station's responses)
)
```

- **`DevotionalImages` becomes a `Map<String, Int>`** (image key → drawable resource id) instead of named fields, resolved by `ContentBlockData.imageKey`. A devotional with no imagery just ships an empty map.
- **New: `Util.resolveContentIndex(schedule, days.size, today)` replaces the implicit `days[currentDay - 1]` indexing.** For `FixedRange`, behaves exactly like today's `resolveCurrentDay` (1..totalDays, null outside the window - `days.size` must equal the schedule's day count). For `Evergreen`, indexes by a repeating cycle over `days.size` entries (e.g. ISO day-of-week for a 7-entry weekly cycle, which is exactly what the recommended pilot Evergreen devotional - a daily Rosary - needs); the precise cycle rule for `Evergreen` is deliberately not nailed down further here (see Open questions) since designing it abstractly, before a real Evergreen devotional's actual cycle shape is known, risks guessing wrong.
- **`DevotionalDayMapper` becomes generic:** concatenate `everyDayBlocks` + the day's own `blocks` + `closingBlocks`, mapping each `ContentBlockData` to the existing `Prayer.Simple`/`Prayer.WithImage`/`Prayer.AllGozos` shape by `type`. No hardcoded field-by-field assembly, no `NovenaTab`.
- **JSON migration:** `content_es.json`/`content_en.json` get restructured once, by hand, into the new schema — this is a one-time content edit, not a runtime migration (no other client reads these files). Recommend doing this migration as part of this spec's Tasks so the refactor is proven against real content immediately, not left half-verified.
- **Alternative considered:** keep `Novena`/`Dia`/`General` as-is and add a *parallel* generic model only for future devotionals, branching on which shape a given devotional uses. Rejected — this is exactly the "two content types, two code paths" outcome CLAUDE.md §2.1 warns about; better to pay the migration cost once now than carry two systems indefinitely.

## 5. Open questions

- **Litany block generality:** is one `items: List<String>` shape enough to cover both Gozos (short call-and-response verses) and a future Rosary decade (10 Hail Marys + an Our Father + a Glory Be, i.e. items with *different prayer texts*, not just repeated short lines)? Recommend confirming this only once a second devotional's real content is in hand — designing further than that now risks over-fitting to a guess. Flagging as a known area the model may need a second, smaller iteration on.
- **`Evergreen`'s exact cycle rule for `resolveContentIndex`** (day-of-week? day-of-year modulo `days.size`? something else?) — recommend deciding this concretely once the pilot Evergreen devotional (daily Rosary, per `specs/devotional-catalog-selection` §7) is being built, since a 7-entry weekly cycle is the one real case in hand; a more general "any cycle length" rule can wait until a second Evergreen devotional actually needs a different one.
- None else blocking — the rest is a mechanical (if careful) refactor.

## 6. Tasks

- [x] New `presentation/model/DevotionalContent.kt` (`DevotionalContent`/`DevotionalDayContent`/`ContentBlockData`), replacing `Novena`/`Dia`/`General`/`Gozo`'s content-carrying role
- [x] `Gozo` (still needed by the unchanged `Prayer.AllGozos`) moved into `Prayer.kt`, its actual home now
- [x] Deleted `presentation/model/Novena.kt`, `presentation/model/NovenaTab.kt` (and a stray `NovenaTab.kt~` backup file found alongside it), `presentation/model/devotional/DevotionalImages.kt`
- [x] `DevotionalMeta.images` is now `Map<String, Int>`; `DevotionalCatalog`'s single entry updated accordingly (dropped the unused `cover` key - confirmed nothing read it)
- [x] `DevotionalDayMapper` rewritten generic: no hardcoded field names, no `NovenaTab`, no fixed page count - assembles `dayContent.blocks + everyDayBlocks + closingBlocks` and maps each block's `type` to a `Prayer`. No longer needs `@Composable`/`stringResource` since titles now come from content data, not Android string resources.
- [x] `DevotionalRepository`/`DevotionalRepositoryImpl`/`MainViewModel`/`AppNavHost` updated for the `DevotionalContent` type and `days` (was `dias`) field name
- [x] Migrated both `content_es.json` and `content_en.json` to the new schema by hand, preserving every existing string exactly - validated both parse correctly and produce the expected block counts (4 everyDayBlocks, 9 days, 1 closingBlock, 12 litany items) via PowerShell `ConvertFrom-Json`
- [x] Verified `:app:assembleDebug` builds successfully
- [x] Owner confirmed all 9 days render correctly in both Spanish and English on-device

## 7. Follow-ups

- Authoring a second devotional's actual content (Vía Crucis or a daily Rosary are the two strongest candidates - see `specs/devotional-catalog-selection` for the recommended pilot) — content/business work, separate from this engineering spec.
- If the litany block shape turns out too narrow once real second-devotional content exists, a small follow-up spec to extend `ContentBlockData`.

## 8. Files

| File | Change |
|---|---|
| `presentation/model/Novena.kt` | Replaced with generic `DevotionalContent`/`DevotionalDayContent`/`ContentBlockData` |
| `presentation/model/NovenaTab.kt` | Deleted - titles now come from content data, not a fixed enum |
| `presentation/model/devotional/DevotionalImages.kt` | Named fields → `Map<String, Int>` |
| `presentation/mapper/DevotionalDayMapper.kt` | Rewritten generic (no hardcoded 6-step assembly) |
| `data/devotional/DevotionalRepositoryImpl.kt` | `loadContent()` return type updated |
| `app/src/main/assets/content_es.json`, `content_en.json` | Migrated to the new schema |
