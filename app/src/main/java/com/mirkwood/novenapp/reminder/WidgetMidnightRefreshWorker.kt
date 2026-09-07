package com.mirkwood.novenapp.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mirkwood.novenapp.widget.NovenaWidgetUpdater
import java.time.LocalDate

/**
 * Keeps the home-screen widget's "Day N" in sync with the calendar day, independent
 * of [DailyNovenaReminderWorker]'s 9am notification slot - without this, the widget
 * would only pick up a day rollover whenever that 9am worker happens to run, or
 * whenever the OS's own inexact ~24h widget update timer happens to land, leaving it
 * stale for hours after midnight.
 */
class WidgetMidnightRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        NovenaWidgetUpdater.updateAll(applicationContext)
        NovenaReminderScheduler.scheduleWidgetRefresh(applicationContext, LocalDate.now().plusDays(1))
        return Result.success()
    }
}
