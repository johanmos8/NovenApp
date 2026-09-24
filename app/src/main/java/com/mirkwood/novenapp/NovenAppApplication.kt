package com.mirkwood.novenapp

import android.app.Application
import android.util.Log
import com.mirkwood.novenapp.debug.DebugSettings
import com.mirkwood.novenapp.di.appModule
import com.mirkwood.novenapp.reminder.NovenaReminderScheduler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NovenAppApplication : Application() {

    // A SupervisorJob so a failure here can't cascade into cancelling anything else
    // sharing the scope, plus a handler so a DataStore/WorkManager failure on startup
    // is logged instead of crashing the app on every single launch (an unstructured
    // CoroutineScope with no handler propagates an uncaught exception to the default
    // handler - see the P3-4 finding in specs/code-audit-v3).
    private val startupExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("NovenAppApplication", "Failed to schedule reminders on startup", throwable)
    }
    private val startupScope = CoroutineScope(Dispatchers.Default + SupervisorJob() + startupExceptionHandler)

    override fun onCreate() {
        super.onCreate()
        // Restore any debug "forced date" before anything reads the clock. No-op in release.
        DebugSettings(this).hydrate()
        startKoin {
            androidContext(this@NovenAppApplication)
            modules(appModule)
        }
        // Reads DataStore (notification overrides), so it can't run synchronously here.
        startupScope.launch {
            NovenaReminderScheduler.scheduleIfNeeded(this@NovenAppApplication)
        }
    }
}
