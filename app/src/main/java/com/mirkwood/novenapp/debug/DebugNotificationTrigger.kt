package com.mirkwood.novenapp.debug

import android.content.Context
import com.mirkwood.novenapp.data.devotional.DevotionalRepository
import com.mirkwood.novenapp.data.progress.DevotionalProgressStore
import com.mirkwood.novenapp.presentation.util.Util
import com.mirkwood.novenapp.reminder.NovenaNotifier
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Debug-only "fire it now" path for the two reminder notifications. Unlike
 * [com.mirkwood.novenapp.reminder.DailyNovenaReminderWorker] and
 * [com.mirkwood.novenapp.reminder.EveningCheckInWorker], which use the real device
 * clock so real scheduling is never fooled by a debug override, this reads
 * [Util.currentDate] - the same forced date [DebugDateSection] sets - so picking a
 * "Day N" preset there and tapping a button here previews that day's notification
 * immediately instead of waiting for the real date/hour to arrive. Only reachable from
 * a `BuildConfig.DEBUG`-gated composable; does not touch WorkManager scheduling.
 */
internal object DebugNotificationTrigger : KoinComponent {

    private val repository: DevotionalRepository by inject()
    private val progressStore: DevotionalProgressStore by inject()

    /** Returns a short debug-only status string, meant for a Toast - not user-facing copy. */
    suspend fun triggerDailyReminder(context: Context): String {
        val devotional = repository.getCatalog().first()
        val day = Util.resolveCurrentDay(devotional.schedule)
            ?: return "Outside the active window - nothing to show"
        NovenaNotifier.showDailyReminder(context, devotional, day)
        return "Shown for day $day"
    }

    /** Returns a short debug-only status string, meant for a Toast - not user-facing copy. */
    suspend fun triggerEveningCheckIn(context: Context): String {
        val devotional = repository.getCatalog().first()
        val day = Util.resolveCurrentDay(devotional.schedule)
            ?: return "Outside the active window - nothing to show"
        val progress = progressStore.observeProgress(devotional.id).first()
        val completedDays = Util.resolveEffectiveCompletedDays(devotional.schedule, progress.completedDays, progress.cycleKey)
        if (day in completedDays) {
            return "Day $day already marked prayed - check-in skipped by design"
        }
        NovenaNotifier.showEveningCheckIn(context, devotional, day)
        return "Shown for day $day"
    }
}
