package com.mirkwood.novenapp.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mirkwood.novenapp.MainActivity
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.data.devotional.DevotionalRepository
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalSchedule
import com.mirkwood.novenapp.presentation.util.Util
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Glanceable home-screen counterpart to the in-app countdown/day-picker - the whole
 * point of a daily devotional is remembering to open it, so today's day (or the
 * countdown to day 1) should be visible without opening the app at all. Tapping it
 * deep-links into today's day screen the same way the daily notification does.
 */
class NovenaWidget : GlanceAppWidget(), KoinComponent {

    private val repository: DevotionalRepository by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val devotional = repository.getCatalog().first()
        provideContent {
            Content(context, devotional)
        }
    }

    @Composable
    private fun Content(context: Context, devotional: DevotionalMeta) {
        val today = LocalDate.now()
        val day = Util.resolveCurrentDay(devotional.schedule, today)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            // CLEAR_TOP (not CLEAR_TASK) so tapping the widget while the app is
            // already open reuses the existing task via MainActivity's singleTop
            // launch mode/onNewIntent, the same as the daily notification's tap
            // intent, instead of tearing the whole task down and restarting cold.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (day != null) {
                putExtra(MainActivity.EXTRA_TARGET_DAY, day)
            }
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .padding(12.dp)
                .clickable(actionStartActivity(launchIntent)),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = context.getString(devotional.titleResId),
                style = TextStyle(
                    color = ColorProvider(Color(0xFF212121)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = GlanceModifier.height(6.dp))
            Text(
                text = widgetLabel(context, devotional.schedule, day, today),
                style = TextStyle(
                    color = ColorProvider(Color(0xFFD32F2F)),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
        }
    }

    private fun widgetLabel(
        context: Context,
        schedule: DevotionalSchedule,
        day: Int?,
        today: LocalDate
    ): String {
        if (day != null) {
            return context.getString(R.string.widget_today_label, day)
        }
        val nextStart = Util.resolveNextOccurrence(schedule, today)
        val daysUntil = nextStart?.let { ChronoUnit.DAYS.between(today, it) } ?: 0L
        return context.getString(R.string.widget_countdown_label, daysUntil)
    }
}
