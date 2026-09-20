package com.mirkwood.novenapp.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mirkwood.novenapp.MainActivity
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta

/**
 * Shows the "today is Day N" reminder. Scheduling lives in [NovenaReminderScheduler];
 * this only knows how to put one notification on screen for a given day.
 */
object NovenaNotifier {

    private const val CHANNEL_ID = "novena_daily_reminder"
    private const val NOTIFICATION_ID = 1001
    private const val EVENING_CHECK_IN_NOTIFICATION_ID = 1002

    fun showDailyReminder(context: Context, devotional: DevotionalMeta, day: Int) {
        showNotification(
            context = context,
            notificationId = NOTIFICATION_ID,
            day = day,
            title = context.getString(devotional.titleResId),
            text = context.getString(R.string.notification_daily_reminder_text, day)
        )
    }

    /** Positive, non-loss-framed nudge - see specs/smart-prayer-reminder and CLAUDE.md §2.6. */
    fun showEveningCheckIn(context: Context, devotional: DevotionalMeta, day: Int) {
        showNotification(
            context = context,
            notificationId = EVENING_CHECK_IN_NOTIFICATION_ID,
            day = day,
            title = context.getString(devotional.titleResId),
            text = context.getString(R.string.notification_evening_check_in_text, day)
        )
    }

    /** Same channel for both notifications, by owner's choice - see specs/smart-prayer-reminder. */
    private fun showNotification(
        context: Context,
        notificationId: Int,
        day: Int,
        title: String,
        text: String
    ) {
        ensureChannel(context)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_TARGET_DAY, day)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId * 1000 + day,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val canPost = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (canPost) {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
