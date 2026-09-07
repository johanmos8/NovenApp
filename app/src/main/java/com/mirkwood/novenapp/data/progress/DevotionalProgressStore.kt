package com.mirkwood.novenapp.data.progress

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.progressDataStore by preferencesDataStore(name = "devotional_progress")

/**
 * Which days of a devotional the user has marked as prayed - purely local, no
 * account needed, matching the app's existing no-login design. One preference key
 * per devotional id, so progress for different devotionals (once more than one
 * exists) doesn't collide.
 */
class DevotionalProgressStore(private val context: Context) {

    fun observeCompletedDays(devotionalId: String): Flow<Set<Int>> {
        val key = completedDaysKey(devotionalId)
        return context.progressDataStore.data.map { prefs ->
            prefs[key].orEmpty().mapNotNull { it.toIntOrNull() }.toSet()
        }
    }

    suspend fun setDayCompleted(devotionalId: String, day: Int, completed: Boolean) {
        val key = completedDaysKey(devotionalId)
        context.progressDataStore.edit { prefs ->
            val current = prefs[key].orEmpty().toMutableSet()
            if (completed) current.add(day.toString()) else current.remove(day.toString())
            prefs[key] = current
        }
    }

    private fun completedDaysKey(devotionalId: String) =
        stringSetPreferencesKey("completed_days_$devotionalId")
}
