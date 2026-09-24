package com.mirkwood.novenapp.presentation.util

import com.mirkwood.novenapp.debug.DebugClock
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalSchedule
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Date math for a [DevotionalSchedule], used to be hardcoded Christmas-only constants
 * (TARGET_MONTH/INITIAL_DAY/LAST_DAY/TARGET_DAY). Keeping it schedule-driven means a
 * future devotional with different dates, or no dates at all, doesn't need new code here.
 */
object Util {

    /**
     * The date the whole app treats as "today". Normally [LocalDate.now]; in a debug
     * build it can be overridden from More ▸ Debug (see [DebugClock]) to preview the
     * seasonal novena UI on any date. Always the real date in release builds.
     */
    fun currentDate(): LocalDate = DebugClock.today()

    /**
     * Tries the range anchored to the previous year first, then today's year, so a
     * range that wraps into January (e.g. Dec 28 - Jan 5) resolves correctly on
     * either side of the New Year, and computes the day index by elapsed days
     * (not day-of-month subtraction) so a range spanning two months works too.
     */
    fun resolveCurrentDay(schedule: DevotionalSchedule, today: LocalDate = currentDate()): Int? {
        if (schedule !is DevotionalSchedule.FixedRange) return null
        for (startYear in intArrayOf(today.year - 1, today.year)) {
            val start = LocalDate.of(startYear, schedule.startMonth, schedule.startDay)
            var end = LocalDate.of(startYear, schedule.endMonth, schedule.endDay)
            if (end.isBefore(start)) end = end.plusYears(1)
            if (today in start..end) {
                return (ChronoUnit.DAYS.between(start, today) + 1).toInt()
            }
        }
        return null
    }

    fun isCelebrationDay(schedule: DevotionalSchedule, today: LocalDate = currentDate()): Boolean {
        if (schedule !is DevotionalSchedule.FixedRange) return false
        return today.monthValue == schedule.celebrationMonth && today.dayOfMonth == schedule.celebrationDay
    }

    /**
     * The next date on/after [from] that falls within [schedule]'s active range - `from`
     * itself if it's already in range, otherwise this year's (or, if `from` is already
     * past this year's range, next year's) start date. Shared by the daily reminder
     * scheduler (to find when to fire next) and the home-screen widget (to show a
     * countdown when the devotional isn't active yet).
     */
    fun resolveNextOccurrence(schedule: DevotionalSchedule, from: LocalDate = currentDate()): LocalDate? {
        if (schedule !is DevotionalSchedule.FixedRange) return null
        val startThisYear = LocalDate.of(from.year, schedule.startMonth, schedule.startDay)
        val endThisYear = LocalDate.of(from.year, schedule.endMonth, schedule.endDay)
        return when {
            from.isAfter(endThisYear) -> LocalDate.of(from.year + 1, schedule.startMonth, schedule.startDay)
            from.isBefore(startThisYear) -> startThisYear
            else -> from
        }
    }

    /**
     * Whether a devotional should notify by default, before any manual override (see
     * `DevotionalNotificationStore`) - on for a [DevotionalSchedule.FixedRange]
     * currently in its window (nobody should miss a live novena by default), off for
     * [DevotionalSchedule.Evergreen] (always available, so no calendar-driven urgency
     * unless the user opts in). See specs/devotional-catalog-selection §4.3.
     */
    fun isNotifyingByDefault(schedule: DevotionalSchedule, today: LocalDate = currentDate()): Boolean =
        when (schedule) {
            is DevotionalSchedule.FixedRange -> resolveCurrentDay(schedule, today) != null
            DevotionalSchedule.Evergreen -> false
        }

    /**
     * A stable identifier for "which cycle of this devotional is relevant right now" -
     * the start date of the window that's either currently open or the next upcoming
     * one (for [DevotionalSchedule.FixedRange]), or a fixed constant (for
     * [DevotionalSchedule.Evergreen], which has no cycles - its progress/overrides
     * simply never go stale on their own, matching the owner's call that a future
     * daily devotional's progress should only ever reset by the user's own manual
     * action, not automatically). Both a manual notification override
     * ([isNotifyingEffective]) and completed-day progress
     * ([resolveEffectiveCompletedDays]) are only honored while this key matches the
     * one they were recorded under - this is what makes an override set in November
     * survive through and during December's window and then reset on its own once
     * that same window closes, and what makes last year's completed days stop
     * silently reading as "already done" once a new cycle starts, without any
     * separate "clear" step or stale state to garbage collect. See
     * specs/devotional-catalog-selection §4.3 and the P0-1/P0-2 findings in
     * specs/code-audit-v3.
     */
    fun resolveCycleKey(schedule: DevotionalSchedule, today: LocalDate = currentDate()): String =
        when (schedule) {
            is DevotionalSchedule.FixedRange -> resolveRelevantWindowStart(schedule, today).toString()
            DevotionalSchedule.Evergreen -> "evergreen"
        }

    /**
     * The effective on/off state for a devotional's notifications: the manual override
     * if one is on record *for the currently relevant cycle* ([resolveCycleKey]),
     * otherwise [isNotifyingByDefault]. Takes the override's raw fields rather than a
     * shared type so this stays a pure function of primitives - the caller (which
     * already talks to both `Util` and `DevotionalNotificationStore`) is what bridges
     * the two, keeping the data-layer store from needing to know about schedules at all.
     */
    fun isNotifyingEffective(
        schedule: DevotionalSchedule,
        overrideEnabled: Boolean?,
        overrideCycleKey: String?,
        today: LocalDate = currentDate()
    ): Boolean {
        val appliesToCurrentCycle = overrideCycleKey != null && overrideCycleKey == resolveCycleKey(schedule, today)
        return (overrideEnabled.takeIf { appliesToCurrentCycle }) ?: isNotifyingByDefault(schedule, today)
    }

    /**
     * The completed-days set to actually show/count: [state]'s stored days if they
     * belong to the currently relevant cycle ([resolveProgressCycleKey]), otherwise empty -
     * this is what makes a `FixedRange` novena's progress read as freshly reset once a
     * new cycle starts, without a separate migration/clear step. Takes the store's raw
     * fields rather than its type directly for the same reason as [isNotifyingEffective].
     */
    fun resolveEffectiveCompletedDays(
        schedule: DevotionalSchedule,
        storedCompletedDays: Set<Int>,
        storedCycleKey: String?,
        today: LocalDate = currentDate()
    ): Set<Int> =
        if (storedCycleKey != null && storedCycleKey == resolveProgressCycleKey(schedule, today)) storedCompletedDays else emptySet()

    /**
     * Same keys as [resolveCycleKey], but a [DevotionalSchedule.FixedRange] cycle keeps
     * its key through its *celebration day*, not just its last prayer day - so the
     * novena still reads as completed on Christmas Day itself and only resets the day
     * after (Q12 in specs/code-audit-v3: "reiniciar el 26"). Deliberately used for
     * completed-day progress only: notification overrides keep flipping right after
     * the window closes via [resolveCycleKey], so a choice made on the celebration day
     * already applies to the *next* season (Q2's behavior) instead of expiring overnight.
     */
    fun resolveProgressCycleKey(schedule: DevotionalSchedule, today: LocalDate = currentDate()): String =
        when (schedule) {
            is DevotionalSchedule.FixedRange ->
                resolveRelevantWindowStart(schedule, today, extendThroughCelebration = true).toString()
            DevotionalSchedule.Evergreen -> "evergreen"
        }

    /**
     * The start of the [DevotionalSchedule.FixedRange] window that's either currently
     * open (checked against both last year's and this year's occurrence, like
     * [resolveCurrentDay]) or, if none is open, the next upcoming one. With
     * [extendThroughCelebration], a window counts as still open through its celebration
     * day when that falls after the last prayer day (resolved relative to the window's
     * own start, so a range wrapping into January still finds the right year).
     */
    private fun resolveRelevantWindowStart(
        schedule: DevotionalSchedule.FixedRange,
        today: LocalDate,
        extendThroughCelebration: Boolean = false
    ): LocalDate {
        for (startYear in intArrayOf(today.year - 1, today.year)) {
            val start = LocalDate.of(startYear, schedule.startMonth, schedule.startDay)
            var end = LocalDate.of(startYear, schedule.endMonth, schedule.endDay)
            if (end.isBefore(start)) end = end.plusYears(1)
            if (extendThroughCelebration) {
                var celebration = LocalDate.of(startYear, schedule.celebrationMonth, schedule.celebrationDay)
                if (celebration.isBefore(start)) celebration = celebration.plusYears(1)
                if (celebration.isAfter(end)) end = celebration
            }
            if (today in start..end) return start
        }
        val startThisYear = LocalDate.of(today.year, schedule.startMonth, schedule.startDay)
        return if (today.isBefore(startThisYear)) startThisYear else LocalDate.of(today.year + 1, schedule.startMonth, schedule.startDay)
    }

    /**
     * Shared by [pickFeaturedDevotional] and Home's full list sort
     * (`MainViewModel.HOME_SORT_ORDER`) so the "what matters most right now" rule is
     * defined exactly once - see the P4-5 finding in specs/code-audit-v3 for what
     * happened when it was written twice.
     */
    val devotionalFeatureOrder: Comparator<DevotionalMeta> = compareBy(
        { if (it.schedule is DevotionalSchedule.Evergreen) 1 else 0 },
        { (it.schedule as? DevotionalSchedule.FixedRange)?.let { s -> s.startMonth * 100 + s.startDay } ?: 0 }
    )

    /**
     * Which devotional Home features first / the widget summarizes, when more than one
     * is relevant at once - an in-window [DevotionalSchedule.FixedRange] before any
     * [DevotionalSchedule.Evergreen], and among several simultaneously-open FixedRange
     * devotionals (unlikely but possible), the one whose window started first this
     * year. Returns null if nothing in [catalog] is currently relevant. See
     * specs/devotional-catalog-selection §4.1.
     */
    fun pickFeaturedDevotional(catalog: List<DevotionalMeta>, today: LocalDate = currentDate()): DevotionalMeta? =
        catalog
            .filter { resolveCurrentDay(it.schedule, today) != null || it.schedule is DevotionalSchedule.Evergreen }
            .minWithOrNull(devotionalFeatureOrder)

    fun calculateTimeRemaining(schedule: DevotionalSchedule, now: LocalDate = currentDate()): Long {
        if (schedule !is DevotionalSchedule.FixedRange) return 0L

        val isAfterCelebration =
            now.monthValue == schedule.celebrationMonth && now.dayOfMonth > schedule.celebrationDay
        val targetYear = if (isAfterCelebration) now.year + 1 else now.year

        val targetDate = LocalDate.of(targetYear, schedule.celebrationMonth, schedule.celebrationDay)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return targetDate - System.currentTimeMillis()
    }
}
