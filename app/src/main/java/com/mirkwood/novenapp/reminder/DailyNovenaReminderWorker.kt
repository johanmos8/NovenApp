package com.mirkwood.novenapp.reminder

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mirkwood.novenapp.data.devotional.DevotionalRepository
import com.mirkwood.novenapp.presentation.util.Util
import com.mirkwood.novenapp.widget.NovenaWidgetUpdater
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "DailyNovenaReminder"

/**
 * Fires once a day at 9am for one specific devotional (its id passed via
 * [androidx.work.WorkerParameters.getInputData] - see [NovenaReminderScheduler]):
 * shows "today is Day N" (skipped silently outside the active range), refreshes the
 * home-screen widget, then enqueues tomorrow's occurrence - unless this devotional's
 * notifications were turned off since it was last scheduled, in which case the chain
 * stops here rather than rescheduling.
 *
 * Failures don't break the chain (R6 in specs/code-audit-v3, CLAUDE.md §1.4.4): if
 * reading the on/off state or showing the notification throws, it's logged and the
 * chain still reschedules (tomorrow re-checks everything). If the reschedule itself
 * throws, the result is `failure()` rather than `retry()` - a retry would re-run the
 * whole job and could show today's reminder twice - and
 * [WidgetMidnightRefreshWorker] restores the chain at the next midnight (R5).
 */
class DailyNovenaReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: DevotionalRepository by inject()

    override suspend fun doWork(): Result {
        val devotionalId = inputData.getString(NovenaReminderScheduler.INPUT_DEVOTIONAL_ID)
            ?: return Result.failure()
        val today = LocalDate.now()

        // Only an explicit "not notifying" answer ends the chain; an error keeps it going.
        var turnedOff = false
        runCatchingNonCancellation(TAG, "show today's reminder") {
            val devotional = repository.getDevotional(devotionalId)
            if (!NovenaReminderScheduler.isNotifying(devotional, today)) {
                turnedOff = true
                return@runCatchingNonCancellation
            }
            val day = Util.resolveCurrentDay(devotional.schedule, today)
            if (day != null) {
                NovenaNotifier.showDailyReminder(applicationContext, devotional, day)
            }
        }
        if (turnedOff) return Result.success()

        runCatchingNonCancellation(TAG, "refresh the widget") {
            NovenaWidgetUpdater.updateAll(applicationContext)
        }

        return try {
            NovenaReminderScheduler.scheduleReminder(applicationContext, devotionalId, today.plusDays(1))
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reschedule; the midnight refresh will restore the chain", e)
            Result.failure()
        }
    }
}
