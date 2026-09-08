package com.mirkwood.novenapp

import android.app.Application
import com.mirkwood.novenapp.debug.DebugSettings
import com.mirkwood.novenapp.di.appModule
import com.mirkwood.novenapp.reminder.NovenaReminderScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NovenAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Restore any debug "forced date" before anything reads the clock. No-op in release.
        DebugSettings(this).hydrate()
        startKoin {
            androidContext(this@NovenAppApplication)
            modules(appModule)
        }
        NovenaReminderScheduler.scheduleIfNeeded(this)
    }
}
