package com.mirkwood.novenapp.reminder

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mirkwood.novenapp.data.devotional.DevotionalRepository
import com.mirkwood.novenapp.data.progress.DevotionalProgressStore
import com.mirkwood.novenapp.presentation.util.Util
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "EveningCheckIn"

/**
 * Fires once a day at 8pm for one specific devotional (its id passed via
 * [androidx.work.WorkerParameters.getInputData] - see [NovenaReminderScheduler]): if
 * today's day hasn't been marked prayed yet ([DevotionalProgressStore]), shows a
 * gentle check-in notification; skipped silently otherwise, outside the active range,
 * or if this devotional's notifications were turned off since it was last scheduled.
 * Additive to [DailyNovenaReminderWorker]'s 9am reminder, not a replacement - see
 * specs/smart-prayer-reminder for why the copy stays positive rather than
 * streak/loss-framed (CLAUDE.md §2.6).
 *
 * Same failure handling as [DailyNovenaReminderWorker] (R6 in specs/code-audit-v3): an
 * error is logged and the chain still reschedules; a failed reschedule returns
 * `failure()` and is restored by [WidgetMidnightRefreshWorker] at the next midnight.
 */
class EveningCheckInWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: DevotionalRepository by inject()
    private val progressStore: DevotionalProgressStore by inject()

    override suspend fun doWork(): Result {
        val devotionalId = inputData.getString(NovenaReminderScheduler.INPUT_DEVOTIONAL_ID)
            ?: return Result.failure()
        val today = LocalDate.now()

        // Only an explicit "not notifying" answer ends the chain; an error keeps it going.
        var turnedOff = false
        runCatchingNonCancellation(TAG, "show today's check-in") {
            val devotional = repository.getDevotional(devotionalId)
            if (!NovenaReminderScheduler.isNotifying(devotional, today)) {
                turnedOff = true
                return@runCatchingNonCancellation
            }
            val day = Util.resolveCurrentDay(devotional.schedule, today)
            if (day != null) {
                val progress = progressStore.observeProgress(devotionalId).first()
                val completedDays = Util.resolveEffectiveCompletedDays(
                    devotional.schedule, progress.completedDays, progress.cycleKey, today
                )
                if (day !in completedDays) {
                    NovenaNotifier.showEveningCheckIn(applicationContext, devotional, day)
                }
            }
        }
        if (turnedOff) return Result.success()

        return try {
            NovenaReminderScheduler.scheduleEveningCheckIn(applicationContext, devotionalId, today.plusDays(1))
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reschedule; the midnight refresh will restore the chain", e)
            Result.failure()
        }
    }
}
