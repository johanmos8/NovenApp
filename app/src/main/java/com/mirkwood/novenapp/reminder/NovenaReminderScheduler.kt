package com.mirkwood.novenapp.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mirkwood.novenapp.data.devotional.DevotionalRepository
import com.mirkwood.novenapp.presentation.util.Util
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Schedules two independent self-perpetuating work chains: [DailyNovenaReminderWorker]
 * at 9am (shows the reminder notification) and [WidgetMidnightRefreshWorker] at
 * midnight (keeps the home-screen widget's day number accurate from the moment the
 * calendar rolls over, instead of waiting for the 9am worker or the OS's own inexact
 * ~24h widget update timer). Each run reschedules its own chain for the next
 * occurrence. [scheduleIfNeeded] is meant to be called on every app start - it's a
 * no-op (via [ExistingWorkPolicy.KEEP]) whenever a chain is already running, so it
 * only actually re-arms things the first time the app is opened near/during a new
 * season.
 */
object NovenaReminderScheduler : KoinComponent {

    private val repository: DevotionalRepository by inject()

    private const val REMINDER_WORK_NAME = "novena_daily_reminder"
    private const val WIDGET_REFRESH_WORK_NAME = "novena_widget_midnight_refresh"
    private const val REMINDER_HOUR = 9
    private const val WIDGET_REFRESH_HOUR = 0

    fun scheduleIfNeeded(context: Context) {
        scheduleReminder(context, LocalDate.now(), ExistingWorkPolicy.KEEP)
        scheduleWidgetRefresh(context, LocalDate.now(), ExistingWorkPolicy.KEEP)
    }

    fun scheduleReminder(
        context: Context,
        from: LocalDate,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE
    ) {
        enqueue<DailyNovenaReminderWorker>(context, REMINDER_WORK_NAME, REMINDER_HOUR, from, policy)
    }

    fun scheduleWidgetRefresh(
        context: Context,
        from: LocalDate,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE
    ) {
        enqueue<WidgetMidnightRefreshWorker>(context, WIDGET_REFRESH_WORK_NAME, WIDGET_REFRESH_HOUR, from, policy)
    }

    private inline fun <reified W : ListenableWorker> enqueue(
        context: Context,
        workName: String,
        hour: Int,
        from: LocalDate,
        policy: ExistingWorkPolicy
    ) {
        val schedule = repository.getCatalog().first().schedule
        val targetDate = Util.resolveNextOccurrence(schedule, from) ?: return
        val delay = delayUntil(targetDate, hour)

        val request = OneTimeWorkRequestBuilder<W>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(workName, policy, request)
    }

    private fun delayUntil(targetDate: LocalDate, hour: Int, now: ZonedDateTime = ZonedDateTime.now()): Duration {
        val target = targetDate.atTime(hour, 0).atZone(ZoneId.systemDefault())
        return if (target.isBefore(now)) Duration.ZERO else Duration.between(now, target)
    }
}
