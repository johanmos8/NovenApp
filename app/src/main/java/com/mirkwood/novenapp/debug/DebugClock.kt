package com.mirkwood.novenapp.debug

import com.mirkwood.novenapp.BuildConfig
import java.time.LocalDate

/**
 * A test-only override for "today", so the seasonal (Dec 16–24) novena UI can be
 * exercised on any calendar date without waiting for December.
 *
 * Inert in release builds: [set] refuses to store anything unless [BuildConfig.DEBUG],
 * the only caller ([DebugSettings]) is itself debug-gated, and no release UI exposes a
 * way in. [Util.currentDate()][com.mirkwood.novenapp.presentation.util.Util.currentDate]
 * routes every date calculation in the app through [today].
 */
object DebugClock {

    @Volatile
    var overrideDate: LocalDate? = null
        private set

    fun today(): LocalDate = overrideDate ?: LocalDate.now()

    internal fun set(date: LocalDate?) {
        overrideDate = if (BuildConfig.DEBUG) date else null
    }
}
