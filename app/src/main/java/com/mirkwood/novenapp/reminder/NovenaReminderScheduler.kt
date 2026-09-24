package com.mirkwood.novenapp.reminder

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mirkwood.novenapp.data.devotional.DevotionalRepository
import com.mirkwood.novenapp.data.notifications.DevotionalNotificationStore
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalSchedule
import com.mirkwood.novenapp.presentation.util.Util
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Schedules, per devotional, two independent self-perpetuating work chains
 * ([DailyNovenaReminderWorker] at 9am, [EveningCheckInWorker] at 8pm) gated on that
 * devotional currently having notifications on (see specs/devotional-catalog-selection
 * §4.3 - a per-devotional toggle, not one exclusive "active" devotional), plus one
 * shared [WidgetMidnightRefreshWorker] chain for the home-screen widget. Each worker
 * reschedules its own chain for the next occurrence and re-checks [isNotifying] before
 * doing so, so turning notifications off for a devotional stops its chain the same day
 * rather than waiting for the next app start. [scheduleIfNeeded] is meant to be called
 * on every app start.
 */
object NovenaReminderScheduler : KoinComponent {

    private val repository: DevotionalRepository by inject()
    private val notificationStore: DevotionalNotificationStore by inject()

    private const val REMINDER_WORK_PREFIX = "novena_daily_reminder_"
    private const val EVENING_CHECK_IN_WORK_PREFIX = "novena_evening_check_in_"
    private const val WIDGET_REFRESH_WORK_NAME = "novena_widget_midnight_refresh"
    private const val REMINDER_HOUR = 9
    private const val EVENING_CHECK_IN_HOUR = 20
    private const val WIDGET_REFRESH_HOUR = 0
    internal const val INPUT_DEVOTIONAL_ID = "devotionalId"

    suspend fun scheduleIfNeeded(context: Context) {
        val today = LocalDate.now()
        scheduleDevotionalChainsIfNeeded(context, today)
        scheduleWidgetRefresh(context, today, ExistingWorkPolicy.KEEP)
    }

    /**
     * (Re)starts the 9am/8pm chains of every devotional that's currently notifying and
     * doesn't already have one pending - `KEEP` leaves a pending chain alone, but
     * enqueues fresh if the previous one finished, failed or was never created.
     *
     * Called at app start *and* every midnight by [WidgetMidnightRefreshWorker] (R5 in
     * specs/code-audit-v3): a chain stops for good once a devotional is turned off, so
     * without the midnight call next season's default-on reminders only came back if the
     * user happened to open the app before its first 9am slot. It's also the safety net
     * for a chain broken by a worker failure (R6). Deliberately doesn't touch the widget
     * chain, so the midnight worker can call it without a `KEEP` on its own, still-running
     * unique work silently skipping its reschedule.
     */
    suspend fun scheduleDevotionalChainsIfNeeded(context: Context, today: LocalDate = LocalDate.now()) {
        repository.getCatalog().forEach { devotional ->
            if (isNotifying(devotional, today)) {
                scheduleReminder(context, devotional.id, today, ExistingWorkPolicy.KEEP)
                scheduleEveningCheckIn(context, devotional.id, today, ExistingWorkPolicy.KEEP)
            }
        }
    }

    suspend fun isNotifying(devotional: DevotionalMeta, today: LocalDate = LocalDate.now()): Boolean {
        val override = notificationStore.observeOverride(devotional.id).first()
        return Util.isNotifyingEffective(devotional.schedule, override?.enabled, override?.cycleKey, today)
    }

    fun scheduleReminder(
        context: Context,
        devotionalId: String,
        from: LocalDate,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE
    ) {
        enqueue<DailyNovenaReminderWorker>(context, REMINDER_WORK_PREFIX + devotionalId, devotionalId, REMINDER_HOUR, from, policy)
    }

    fun scheduleEveningCheckIn(
        context: Context,
        devotionalId: String,
        from: LocalDate,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE
    ) {
        enqueue<EveningCheckInWorker>(context, EVENING_CHECK_IN_WORK_PREFIX + devotionalId, devotionalId, EVENING_CHECK_IN_HOUR, from, policy)
    }

    /** Refreshes the widget every midnight unconditionally - simpler than tying it to
     *  one devotional's schedule, and avoids that devotional's chain silently pausing
     *  the widget refresh outside its own window. */
    fun scheduleWidgetRefresh(
        context: Context,
        from: LocalDate,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE
    ) {
        enqueue<WidgetMidnightRefreshWorker>(context, WIDGET_REFRESH_WORK_NAME, devotionalId = null, WIDGET_REFRESH_HOUR, from, policy)
    }

    /**
     * Resolves a target date/time no earlier than now, then enqueues.
     *
     * Two callers pass `from = today`: [scheduleIfNeeded] (app start) and
     * `MainViewModel.toggleNotification` (turning notifications on by hand). Both used
     * to hand [delayUntil] a target hour that may have *already happened today*, which
     * resolved to `Duration.ZERO` - firing (or re-firing) that slot immediately instead
     * of waiting for its next real occurrence (P0-3 in specs/code-audit-v3: turning
     * notifications on at 9pm fired both the day's reminder and check-in on the spot,
     * and repeating the toggle repeated the firing). The loop below instead keeps
     * advancing the candidate date - re-resolved through [resolveTargetDate] each time,
     * so a bump that lands outside a `FixedRange` window correctly jumps to the next
     * occurrence rather than just adding a day - until the target hour is genuinely in
     * the future. A worker's own self-reschedule (`from = tomorrow`) already resolves
     * on the first pass in practice, since "tomorrow" is never in the past.
     */
    private inline fun <reified W : ListenableWorker> enqueue(
        context: Context,
        workName: String,
        devotionalId: String?,
        hour: Int,
        from: LocalDate,
        policy: ExistingWorkPolicy
    ) {
        val now = ZonedDateTime.now()
        var candidate = from
        var resolvedTarget: LocalDate? = null
        var attempts = 0
        while (attempts < 400) { // generous safety bound (~a year of days) against a misconfigured schedule looping forever
            val resolved = resolveTargetDate(devotionalId, candidate) ?: return
            if (!resolved.atTime(hour, 0).atZone(ZoneId.systemDefault()).isBefore(now)) {
                resolvedTarget = resolved
                break
            }
            candidate = resolved.plusDays(1)
            attempts++
        }
        val targetDate = resolvedTarget ?: return
        val delay = Duration.between(now, targetDate.atTime(hour, 0).atZone(ZoneId.systemDefault()))

        val requestBuilder = OneTimeWorkRequestBuilder<W>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
        if (devotionalId != null) {
            requestBuilder.setInputData(Data.Builder().putString(INPUT_DEVOTIONAL_ID, devotionalId).build())
        }

        WorkManager.getInstance(context).enqueueUniqueWork(workName, policy, requestBuilder.build())
    }

    /** `null` devotionalId (the widget refresh) always resolves to `from` itself - every midnight, unconditionally. */
    private fun resolveTargetDate(devotionalId: String?, from: LocalDate): LocalDate? {
        if (devotionalId == null) return from
        return when (val schedule = repository.getDevotional(devotionalId).schedule) {
            is DevotionalSchedule.FixedRange -> Util.resolveNextOccurrence(schedule, from)
            DevotionalSchedule.Evergreen -> from
        }
    }
}
