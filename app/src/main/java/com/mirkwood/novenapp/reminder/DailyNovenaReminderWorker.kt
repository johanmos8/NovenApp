package com.mirkwood.novenapp.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mirkwood.novenapp.data.devotional.DevotionalRepository
import com.mirkwood.novenapp.presentation.util.Util
import com.mirkwood.novenapp.widget.NovenaWidgetUpdater
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate

/**
 * Fires once a day at 9am: shows "today is Day N" (skipped silently outside the
 * active range), refreshes the home-screen widget, then enqueues tomorrow's
 * occurrence. [Util.resolveNextOccurrence] means that "tomorrow" automatically jumps
 * a whole year ahead once the season ends, instead of this worker firing daily all
 * year.
 */
class DailyNovenaReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: DevotionalRepository by inject()

    override suspend fun doWork(): Result {
        val devotional = repository.getCatalog().first()
        val today = LocalDate.now()
        val day = Util.resolveCurrentDay(devotional.schedule, today)

        if (day != null) {
            NovenaNotifier.showDailyReminder(applicationContext, devotional, day)
        }
        NovenaWidgetUpdater.updateAll(applicationContext)

        NovenaReminderScheduler.scheduleReminder(applicationContext, today.plusDays(1))
        return Result.success()
    }
}
