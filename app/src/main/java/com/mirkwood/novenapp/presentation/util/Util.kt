package com.mirkwood.novenapp.presentation.util

import com.mirkwood.novenapp.debug.DebugClock
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
