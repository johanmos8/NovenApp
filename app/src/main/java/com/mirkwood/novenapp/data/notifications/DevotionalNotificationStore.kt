package com.mirkwood.novenapp.data.notifications

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationSettingsDataStore by preferencesDataStore(name = "devotional_notifications")

/**
 * A manual notification on/off choice, stamped with the "cycle" it was made for (see
 * `Util.resolveCycleKey`). Deliberately dumb: this store only knows how to
 * read/write a (Boolean, String) pair per devotional - it has no notion of schedules,
 * windows, or "is this still valid," which lives in `Util.isNotifyingEffective`
 * instead, so this class (and the rest of `data/`) doesn't need to depend on
 * `presentation.model.devotional` just to answer "is the override stale."
 */
data class DevotionalNotificationOverride(val enabled: Boolean, val cycleKey: String)

/**
 * Per-devotional manual notification on/off override. There is no explicit "clear" -
 * an override past its cycle is simply never returned as valid by whichever caller
 * compares [DevotionalNotificationOverride.cycleKey] against the current cycle, so
 * there's no separate cleanup step to time correctly (see the P0-2 finding in
 * specs/code-audit-v3, which is exactly what a premature clear step caused).
 */
class DevotionalNotificationStore(private val context: Context) {

    fun observeOverride(devotionalId: String): Flow<DevotionalNotificationOverride?> {
        val enabledKey = enabledKey(devotionalId)
        val cycleKey = cycleKey(devotionalId)
        return context.notificationSettingsDataStore.data.map { prefs ->
            val enabled = prefs[enabledKey] ?: return@map null
            val cycle = prefs[cycleKey] ?: return@map null
            DevotionalNotificationOverride(enabled, cycle)
        }
    }

    suspend fun setOverride(devotionalId: String, enabled: Boolean, cycleKey: String) {
        context.notificationSettingsDataStore.edit { prefs ->
            prefs[enabledKey(devotionalId)] = enabled
            prefs[cycleKey(devotionalId)] = cycleKey
        }
    }

    private fun enabledKey(devotionalId: String) = booleanPreferencesKey("notify_override_enabled_$devotionalId")
    private fun cycleKey(devotionalId: String) = stringPreferencesKey("notify_override_cycle_$devotionalId")
}
