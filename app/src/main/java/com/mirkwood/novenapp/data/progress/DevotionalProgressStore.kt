package com.mirkwood.novenapp.data.progress

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.progressDataStore by preferencesDataStore(name = "devotional_progress")

/**
 * Raw stored progress for a devotional: which days are marked prayed, and which
 * "cycle" ([com.mirkwood.novenapp.presentation.util.Util.resolveProgressCycleKey]) that set
 * belongs to. `cycleKey` is null only if progress was recorded before cycle-tagging
 * existed. Deliberately dumb, mirroring `DevotionalNotificationStore` - this class has
 * no notion of schedules or "is this stale," which lives in
 * `Util.resolveEffectiveCompletedDays` instead.
 */
data class DevotionalProgressState(val completedDays: Set<Int>, val cycleKey: String?)

/**
 * Which days of a devotional the user has marked as prayed - purely local, no
 * account needed, matching the app's existing no-login design. Progress is tagged
 * with the cycle it was recorded in specifically so a fixed-range novena's completed
 * days from last year don't silently read as "already done" this year (see the P0-1
 * finding in specs/code-audit-v3) - a devotional with no cycles (`Evergreen`) is
 * tagged with a constant key that never goes stale, so its progress simply
 * accumulates until a future manual "reset" feature exists for it.
 */
class DevotionalProgressStore(private val context: Context) {

    fun observeProgress(devotionalId: String): Flow<DevotionalProgressState> {
        val daysKey = completedDaysKey(devotionalId)
        val cycleKeyPref = cycleKey(devotionalId)
        return context.progressDataStore.data.map { prefs ->
            DevotionalProgressState(
                completedDays = prefs[daysKey].orEmpty().mapNotNull { it.toIntOrNull() }.toSet(),
                cycleKey = prefs[cycleKeyPref]
            )
        }
    }

    /**
     * [currentCycleKey] is the caller's current answer to "which cycle is this
     * devotional in right now" - if it doesn't match what's already stored, the
     * previous cycle's days are dropped before applying this change, rather than
     * carrying them into the new cycle's count.
     */
    suspend fun setDayCompleted(devotionalId: String, day: Int, completed: Boolean, currentCycleKey: String) {
        val daysKeyPref = completedDaysKey(devotionalId)
        val cycleKeyPref = cycleKey(devotionalId)
        context.progressDataStore.edit { prefs ->
            val current = if (prefs[cycleKeyPref] == currentCycleKey) {
                prefs[daysKeyPref].orEmpty().mapNotNull { it.toIntOrNull() }.toMutableSet()
            } else {
                mutableSetOf()
            }
            if (completed) current.add(day) else current.remove(day)
            prefs[daysKeyPref] = current.map { it.toString() }.toSet()
            prefs[cycleKeyPref] = currentCycleKey
        }
    }

    private fun completedDaysKey(devotionalId: String) = stringSetPreferencesKey("completed_days_$devotionalId")
    private fun cycleKey(devotionalId: String) = stringPreferencesKey("completed_days_cycle_$devotionalId")
}
