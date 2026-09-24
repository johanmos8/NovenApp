package com.mirkwood.novenapp.reminder

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mirkwood.novenapp.widget.NovenaWidgetUpdater
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "WidgetMidnightRefresh"

/**
 * Keeps the home-screen widget's "Day N" in sync with the calendar day, independent
 * of [DailyNovenaReminderWorker]'s 9am notification slot - without this, the widget
 * would only pick up a day rollover whenever that 9am worker happens to run, or
 * whenever the OS's own inexact ~24h widget update timer happens to land, leaving it
 * stale for hours after midnight.
 *
 * Also the daily safety net for the reminder chains: it re-runs
 * [NovenaReminderScheduler.scheduleDevotionalChainsIfNeeded] every midnight, so a
 * devotional that's notifying but has no pending chain (next season opening after it
 * was turned off, or a chain broken by a failure) gets one back without the app being
 * opened - see R5/R6 in specs/code-audit-v3.
 */
class WidgetMidnightRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        // Each step is independent: a widget or DataStore failure must not stop the
        // other, and above all must not stop this chain from rescheduling itself.
        runCatchingNonCancellation(TAG, "refresh the widget") {
            NovenaWidgetUpdater.updateAll(applicationContext)
        }
        runCatchingNonCancellation(TAG, "restore reminder chains") {
            NovenaReminderScheduler.scheduleDevotionalChainsIfNeeded(applicationContext, today)
        }
        return try {
            NovenaReminderScheduler.scheduleWidgetRefresh(applicationContext, today.plusDays(1))
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Retrying is safe here - refreshing the widget and KEEP-scheduling chains
            // are idempotent - and it's the only way this chain survives the failure
            // short of the next app start.
            Log.e(TAG, "Failed to reschedule the midnight refresh; retrying", e)
            Result.retry()
        }
    }
}

/**
 * Runs [block], logging instead of propagating any failure - except coroutine
 * cancellation, which must propagate so WorkManager can actually stop the worker.
 * Shared by the reminder workers (R6 in specs/code-audit-v3 / CLAUDE.md §1.4.4).
 */
internal suspend fun runCatchingNonCancellation(tag: String, what: String, block: suspend () -> Unit) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(tag, "Failed to $what", e)
    }
}
