package com.mirkwood.novenapp.presentation.util

import com.mirkwood.novenapp.presentation.model.devotional.DevotionalSchedule
import java.time.LocalDate
import java.time.ZoneId

/**
 * Date math for a [DevotionalSchedule], used to be hardcoded Christmas-only constants
 * (TARGET_MONTH/INITIAL_DAY/LAST_DAY/TARGET_DAY). Keeping it schedule-driven means a
 * future devotional with different dates, or no dates at all, doesn't need new code here.
 */
object Util {

    fun resolveCurrentDay(schedule: DevotionalSchedule, today: LocalDate = LocalDate.now()): Int? {
        if (schedule !is DevotionalSchedule.FixedRange) return null
        val start = LocalDate.of(today.year, schedule.startMonth, schedule.startDay)
        val end = LocalDate.of(today.year, schedule.endMonth, schedule.endDay)
        return if (today in start..end) today.dayOfMonth - start.dayOfMonth + 1 else null
    }

    fun isCelebrationDay(schedule: DevotionalSchedule, today: LocalDate = LocalDate.now()): Boolean {
        if (schedule !is DevotionalSchedule.FixedRange) return false
        return today.monthValue == schedule.celebrationMonth && today.dayOfMonth == schedule.celebrationDay
    }

    fun calculateTimeRemaining(schedule: DevotionalSchedule, now: LocalDate = LocalDate.now()): Long {
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
