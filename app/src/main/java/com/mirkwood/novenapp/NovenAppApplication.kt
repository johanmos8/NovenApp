package com.mirkwood.novenapp

import android.app.Application
import com.mirkwood.novenapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NovenAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NovenAppApplication)
            modules(appModule)
        }
    }
}
