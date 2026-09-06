package com.mirkwood.novenapp.presentation.model.devotional

/**
 * How a devotional relates to the calendar. Most Catholic novenas run for a fixed
 * number of days leading up to a celebration (e.g. the 9 days before Christmas), but
 * a future devotional (a daily rosary, a saint-of-the-day feature) may have no date
 * range at all, hence the [Evergreen] case.
 */
sealed interface DevotionalSchedule {

    data object Evergreen : DevotionalSchedule

    data class FixedRange(
        val startMonth: Int,
        val startDay: Int,
        val endMonth: Int,
        val endDay: Int,
        val celebrationMonth: Int,
        val celebrationDay: Int
    ) : DevotionalSchedule
}
