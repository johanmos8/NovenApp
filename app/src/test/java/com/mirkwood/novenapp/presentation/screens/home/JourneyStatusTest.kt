package com.mirkwood.novenapp.presentation.screens.home

import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalSchedule
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** What the Home card shows around the season's edges (P4-8 in specs/code-audit-v3). */
class JourneyStatusTest {

    private val novena = DevotionalMeta(
        id = "novena",
        titleResId = 0,
        contentFileEs = "",
        contentFileEn = "",
        images = emptyMap(),
        schedule = DevotionalSchedule.FixedRange(12, 16, 12, 24, 12, 25),
        totalDays = 9
    )

    @Test
    fun beforeTheWindow_countsDownToTheFirstDay() {
        val status = resolveJourneyStatus(novena, null, emptySet(), LocalDate.of(2026, 12, 15))
        assertEquals(JourneyStatus.NotStarted(9, LocalDate.of(2026, 12, 16), 1), status)
    }

    @Test
    fun insideTheWindow_isInProgressOnToday() {
        val status = resolveJourneyStatus(novena, 4, setOf(1, 2, 3), LocalDate.of(2026, 12, 19))
        assertEquals(JourneyStatus.InProgress(4, 9, setOf(1, 2, 3)), status)
    }

    @Test
    fun allNineDaysPrayed_isCompleted() {
        val status = resolveJourneyStatus(novena, 9, (1..9).toSet(), LocalDate.of(2026, 12, 24))
        assertEquals(JourneyStatus.Completed(9), status)
    }

    @Test
    fun celebrationDay_staysCompletedWhenEveryDayWasPrayed() {
        val status = resolveJourneyStatus(novena, null, (1..9).toSet(), LocalDate.of(2026, 12, 25))
        assertEquals(JourneyStatus.Completed(9), status)
    }

    @Test
    fun celebrationDay_countsDownToNextSeasonOtherwise() {
        val status = resolveJourneyStatus(novena, null, setOf(1, 2), LocalDate.of(2026, 12, 25))
        assertEquals(JourneyStatus.NotStarted(9, LocalDate.of(2027, 12, 16), 356), status)
    }
}
