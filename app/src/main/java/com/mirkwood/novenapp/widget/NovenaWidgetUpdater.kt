package com.mirkwood.novenapp.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object NovenaWidgetUpdater {
    suspend fun updateAll(context: Context) {
        NovenaWidget().updateAll(context)
    }
}
