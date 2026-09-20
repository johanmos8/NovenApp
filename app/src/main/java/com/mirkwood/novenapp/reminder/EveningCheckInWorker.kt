package com.mirkwood.novenapp.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mirkwood.novenapp.data.devotional.DevotionalRepository
import com.mirkwood.novenapp.data.progress.DevotionalProgressStore
import com.mirkwood.novenapp.presentation.util.Util
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate

/**
 * Fires once a day at 8pm: if today's day hasn't been marked prayed yet
 * ([DevotionalProgressStore]), shows a gentle check-in notification; skipped silently
 * otherwise, or outside the active range. Additive to [DailyNovenaReminderWorker]'s 9am
 * reminder, not a replacement - see specs/smart-prayer-reminder for why the copy stays
 * positive rather than streak/loss-framed (CLAUDE.md §2.6).
 */
class EveningCheckInWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: DevotionalRepository by inject()
    private val progressStore: DevotionalProgressStore by inject()

    override suspend fun doWork(): Result {
        val devotional = repository.getCatalog().first()
        val today = LocalDate.now()
        val day = Util.resolveCurrentDay(devotional.schedule, today)

        if (day != null) {
            val completedDays = progressStore.observeCompletedDays(devotional.id).first()
            if (day !in completedDays) {
                NovenaNotifier.showEveningCheckIn(applicationContext, devotional, day)
            }
        }

        NovenaReminderScheduler.scheduleEveningCheckIn(applicationContext, today.plusDays(1))
        return Result.success()
    }
}
