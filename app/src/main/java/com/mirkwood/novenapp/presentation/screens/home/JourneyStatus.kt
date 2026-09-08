package com.mirkwood.novenapp.presentation.screens.home

import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta
import com.mirkwood.novenapp.presentation.util.Util
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The state the home "Camino" card renders. Derived entirely from data [HomeScreen]
 * already receives (current day, total days, completed days, the devotional's schedule),
 * so the card's label is a function of *where the journey is*, not a hardcoded
 * "Active novena" - see `docs/home-journey-redesign.md` for the reasoning.
 *
 * Only the calendar-locked ([com.mirkwood.novenapp.presentation.model.devotional.DevotionalSchedule.FixedRange])
 * paths exist today because that's the only devotional the app ships. A future
 * self-paced novena (one the user starts themselves and advances at their own pace)
 * would add a `NotBegun` case here plus a matching `DevotionalSchedule.SelfPaced` and
 * a resolver branch below - deliberately left out until there's content that needs it.
 */
internal sealed interface JourneyStatus {

    val totalDays: Int

    /** Calendar-locked devotional, today falls inside its active window. */
    data class InProgress(
        val currentDay: Int,
        override val totalDays: Int,
        val prayedDays: Set<Int>,
    ) : JourneyStatus {
        /** Days actually marked prayed, clamped to this devotional's real range. */
        val prayedInRange: Set<Int> = prayedDays.filterTo(mutableSetOf()) { it in 1..totalDays }

        val prayedCount: Int = prayedInRange.size

        val percent: Int = if (totalDays <= 0) 0 else (prayedCount * 100) / totalDays

        /**
         * Positive-only signal: the user's prayers have kept pace with the calendar.
         * Never inverted into a "you fell behind" nag - missing a day of a prayer
         * tradition should not be punished by the UI.
         */
        val keepingUp: Boolean = prayedCount > 0 && prayedCount >= currentDay
    }

    /** Calendar-locked devotional whose window has not opened yet. */
    data class NotStarted(
        override val totalDays: Int,
        val startsOn: LocalDate,
        val daysUntilStart: Long,
    ) : JourneyStatus

    /** Every day of the devotional has been marked prayed. */
    data class Completed(
        override val totalDays: Int,
    ) : JourneyStatus
}

/**
 * @param currentDay the schedule-resolved day (null outside the active window), as
 *   already computed by [com.mirkwood.novenapp.presentation.MainViewModel].
 */
internal fun resolveJourneyStatus(
    devotional: DevotionalMeta,
    currentDay: Int?,
    completedDays: Set<Int>,
    today: LocalDate = Util.currentDate(),
): JourneyStatus {
    val total = devotional.totalDays
    val prayedAll = total > 0 && (1..total).all { it in completedDays }

    return when {
        prayedAll -> JourneyStatus.Completed(total)

        currentDay != null -> JourneyStatus.InProgress(currentDay, total, completedDays)

        else -> {
            val nextStart = Util.resolveNextOccurrence(devotional.schedule, today)
            if (nextStart != null && nextStart.isAfter(today)) {
                JourneyStatus.NotStarted(
                    totalDays = total,
                    startsOn = nextStart,
                    daysUntilStart = ChronoUnit.DAYS.between(today, nextStart),
                )
            } else {
                // Evergreen devotional, or an edge date with no resolvable next window:
                // fall back to a browsable "in progress from day 1" rather than a blank card.
                JourneyStatus.InProgress(1, total, completedDays)
            }
        }
    }
}
