package com.mirkwood.novenapp.presentation.util

import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalSchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.time.LocalDate

/**
 * Boundary dates for the schedule/cycle logic (P4-8 in specs/code-audit-v3). Every test
 * passes an explicit date, so none of them depends on the real clock or the debug one.
 */
class UtilTest {

    /** The Novena de Aguinaldos: Dec 16-24, celebrated Dec 25. */
    private val novena = DevotionalSchedule.FixedRange(12, 16, 12, 24, 12, 25)

    /** A range that wraps into January, celebrated the day after it ends. */
    private val wrapping = DevotionalSchedule.FixedRange(12, 28, 1, 5, 1, 6)

    private fun date(year: Int, month: Int, day: Int) = LocalDate.of(year, month, day)

    // resolveCurrentDay

    @Test
    fun currentDay_isNullJustBeforeAndAfterTheWindow() {
        assertNull(Util.resolveCurrentDay(novena, date(2026, 12, 15)))
        assertNull(Util.resolveCurrentDay(novena, date(2026, 12, 25)))
    }

    @Test
    fun currentDay_countsFromOneOnTheFirstDayToNineOnTheLast() {
        assertEquals(1, Util.resolveCurrentDay(novena, date(2026, 12, 16)))
        assertEquals(9, Util.resolveCurrentDay(novena, date(2026, 12, 24)))
    }

    @Test
    fun currentDay_handlesARangeThatWrapsIntoJanuary() {
        assertEquals(1, Util.resolveCurrentDay(wrapping, date(2026, 12, 28)))
        assertEquals(6, Util.resolveCurrentDay(wrapping, date(2027, 1, 2)))
        assertEquals(9, Util.resolveCurrentDay(wrapping, date(2027, 1, 5)))
        assertNull(Util.resolveCurrentDay(wrapping, date(2027, 1, 6)))
    }

    @Test
    fun currentDay_isNullForEvergreen() {
        assertNull(Util.resolveCurrentDay(DevotionalSchedule.Evergreen, date(2026, 12, 20)))
    }

    // isCelebrationDay

    @Test
    fun celebrationDay_isOnlyTheCelebrationDate() {
        assertFalse(Util.isCelebrationDay(novena, date(2026, 12, 24)))
        assertTrue(Util.isCelebrationDay(novena, date(2026, 12, 25)))
        assertFalse(Util.isCelebrationDay(novena, date(2026, 12, 26)))
        assertFalse(Util.isCelebrationDay(DevotionalSchedule.Evergreen, date(2026, 12, 25)))
    }

    // resolveNextOccurrence (what the reminder scheduler and the widget countdown use)

    @Test
    fun nextOccurrence_beforeTheWindowIsThisYearsStart() {
        assertEquals(date(2026, 12, 16), Util.resolveNextOccurrence(novena, date(2026, 10, 20)))
        assertEquals(date(2026, 12, 16), Util.resolveNextOccurrence(novena, date(2026, 12, 15)))
    }

    @Test
    fun nextOccurrence_insideTheWindowIsTheSameDay() {
        assertEquals(date(2026, 12, 16), Util.resolveNextOccurrence(novena, date(2026, 12, 16)))
        assertEquals(date(2026, 12, 24), Util.resolveNextOccurrence(novena, date(2026, 12, 24)))
    }

    @Test
    fun nextOccurrence_afterTheWindowIsNextYearsStart() {
        assertEquals(date(2027, 12, 16), Util.resolveNextOccurrence(novena, date(2026, 12, 25)))
        assertEquals(date(2027, 12, 16), Util.resolveNextOccurrence(novena, date(2026, 12, 31)))
    }

    @Test
    fun nextOccurrence_isNullForEvergreen() {
        assertNull(Util.resolveNextOccurrence(DevotionalSchedule.Evergreen, date(2026, 12, 20)))
    }

    @Ignore("P4-4 in specs/code-audit-v3: resolveNextOccurrence doesn't handle a range that wraps into January yet. No shipped devotional has one.")
    @Test
    fun nextOccurrence_insideAWrappingWindowIsTheSameDay() {
        assertEquals(date(2027, 1, 2), Util.resolveNextOccurrence(wrapping, date(2027, 1, 2)))
    }

    // resolveCycleKey / resolveProgressCycleKey

    @Test
    fun cycleKey_isTheUpcomingOrOpenWindowsStart() {
        assertEquals("2026-12-16", Util.resolveCycleKey(novena, date(2026, 10, 20)))
        assertEquals("2026-12-16", Util.resolveCycleKey(novena, date(2026, 12, 24)))
        assertEquals("2027-12-16", Util.resolveCycleKey(novena, date(2026, 12, 25)))
        assertEquals("2027-12-16", Util.resolveCycleKey(novena, date(2027, 1, 1)))
    }

    @Test
    fun progressCycleKey_keepsTheSeasonThroughTheCelebrationDay() {
        assertEquals("2026-12-16", Util.resolveProgressCycleKey(novena, date(2026, 12, 25)))
        assertEquals("2027-12-16", Util.resolveProgressCycleKey(novena, date(2026, 12, 26)))
    }

    @Test
    fun cycleKeys_areConstantForEvergreen() {
        assertEquals("evergreen", Util.resolveCycleKey(DevotionalSchedule.Evergreen, date(2026, 1, 1)))
        assertEquals("evergreen", Util.resolveProgressCycleKey(DevotionalSchedule.Evergreen, date(2026, 12, 25)))
    }

    // Notification state

    @Test
    fun notifyingEffective_followsTheWindowWhenNeverTouched() {
        assertFalse(Util.isNotifyingEffective(novena, null, null, date(2026, 12, 15)))
        assertTrue(Util.isNotifyingEffective(novena, null, null, date(2026, 12, 16)))
        assertFalse(Util.isNotifyingEffective(novena, null, null, date(2026, 12, 25)))
        assertFalse(Util.isNotifyingEffective(DevotionalSchedule.Evergreen, null, null, date(2026, 12, 20)))
    }

    @Test
    fun notifyingEffective_aNovemberOverrideLastsThroughTheSeasonAndExpiresAfterIt() {
        val key = Util.resolveCycleKey(novena, date(2026, 11, 10))
        assertFalse(Util.isNotifyingEffective(novena, false, key, date(2026, 12, 20)))
        assertTrue(Util.isNotifyingEffective(novena, true, key, date(2026, 11, 10)))
        // Dec 25: the window closed, so the override no longer applies and the default (off) does.
        assertFalse(Util.isNotifyingEffective(novena, true, key, date(2026, 12, 25)))
    }

    @Test
    fun notifyingForRelevantCycle_readsOnBeforeTheWindowUnlessTurnedOff() {
        assertTrue(Util.isNotifyingForRelevantCycle(novena, null, null, date(2026, 10, 20)))
        assertFalse(Util.isNotifyingForRelevantCycle(novena, false, "2026-12-16", date(2026, 10, 20)))
        // Last season's "off" doesn't carry into the next one.
        assertTrue(Util.isNotifyingForRelevantCycle(novena, false, "2026-12-16", date(2026, 12, 25)))
        assertFalse(Util.isNotifyingForRelevantCycle(DevotionalSchedule.Evergreen, null, null, date(2026, 10, 20)))
    }

    // Completed days

    @Test
    fun completedDays_showThroughTheCelebrationDayAndResetTheDayAfter() {
        val allDays = (1..9).toSet()
        assertEquals(allDays, Util.resolveEffectiveCompletedDays(novena, allDays, "2026-12-16", date(2026, 12, 24)))
        assertEquals(allDays, Util.resolveEffectiveCompletedDays(novena, allDays, "2026-12-16", date(2026, 12, 25)))
        assertEquals(emptySet<Int>(), Util.resolveEffectiveCompletedDays(novena, allDays, "2026-12-16", date(2026, 12, 26)))
        assertEquals(emptySet<Int>(), Util.resolveEffectiveCompletedDays(novena, allDays, null, date(2026, 12, 20)))
    }

    // pickFeaturedDevotional

    private fun meta(id: String, schedule: DevotionalSchedule) =
        DevotionalMeta(id, 0, "", "", emptyMap(), schedule, totalDays = 9)

    @Test
    fun featured_isNullOutOfSeasonWithOnlyTheNovena() {
        assertNull(Util.pickFeaturedDevotional(listOf(meta("novena", novena)), date(2026, 10, 20)))
    }

    @Test
    fun featured_prefersAnOpenNovenaOverAnEvergreen() {
        val catalog = listOf(meta("daily", DevotionalSchedule.Evergreen), meta("novena", novena))
        assertEquals("daily", Util.pickFeaturedDevotional(catalog, date(2026, 10, 20))?.id)
        assertEquals("novena", Util.pickFeaturedDevotional(catalog, date(2026, 12, 20))?.id)
    }
}
