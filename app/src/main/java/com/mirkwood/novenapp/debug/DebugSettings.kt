package com.mirkwood.novenapp.debug

import android.content.Context
import com.mirkwood.novenapp.BuildConfig
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalCatalog
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalSchedule
import java.time.LocalDate

/**
 * Persists the developer's [DebugClock] choice across restarts (plain SharedPreferences
 * so it can be read synchronously at startup). Every entry point is guarded on
 * [BuildConfig.DEBUG]; in a release build this class stores and applies nothing.
 */
class DebugSettings(context: Context) {

    private val prefs = context.getSharedPreferences("debug_settings", Context.MODE_PRIVATE)

    /** The forced date, or null for "use the real date". */
    var forcedDate: LocalDate?
        get() = if (BuildConfig.DEBUG) prefs.getString(KEY_FORCED_DATE, null)?.let(LocalDate::parse) else null
        set(value) {
            if (!BuildConfig.DEBUG) return
            prefs.edit().apply {
                if (value == null) remove(KEY_FORCED_DATE) else putString(KEY_FORCED_DATE, value.toString())
            }.apply()
            DebugClock.set(value)
        }

    /** Push the persisted choice into [DebugClock]. Call once, early, from Application. */
    fun hydrate() {
        if (!BuildConfig.DEBUG) return
        DebugClock.set(forcedDate)
    }

    companion object {
        private const val KEY_FORCED_DATE = "forced_date"

        /**
         * The date a given novena day falls on this year, derived from the active
         * devotional's schedule so the debug presets stay correct if the dates change.
         * Day 1 = start date … plus celebration day.
         */
        fun dateForNovenaDay(day: Int, year: Int = LocalDate.now().year): LocalDate {
            val schedule = DevotionalCatalog.default.schedule as? DevotionalSchedule.FixedRange
                ?: return LocalDate.of(year, 12, 16).plusDays((day - 1).toLong())
            val start = LocalDate.of(year, schedule.startMonth, schedule.startDay)
            return start.plusDays((day - 1).toLong())
        }

        fun celebrationDate(year: Int = LocalDate.now().year): LocalDate {
            val schedule = DevotionalCatalog.default.schedule as? DevotionalSchedule.FixedRange
                ?: return LocalDate.of(year, 12, 25)
            return LocalDate.of(year, schedule.celebrationMonth, schedule.celebrationDay)
        }
    }
}
