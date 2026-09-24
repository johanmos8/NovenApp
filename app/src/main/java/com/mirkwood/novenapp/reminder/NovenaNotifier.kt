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
    private const val DAILY_REMINDER_ID_BASE = 1_000_000
    private const val EVENING_CHECK_IN_ID_BASE = 2_000_000

    fun showDailyReminder(context: Context, devotional: DevotionalMeta, day: Int) {
        showNotification(
            context = context,
            notificationId = notificationId(DAILY_REMINDER_ID_BASE, devotional.id),
            devotionalId = devotional.id,
            day = day,
            title = context.getString(devotional.titleResId),
            text = context.getString(R.string.notification_daily_reminder_text, day)
        )
    }

    /** Positive, non-loss-framed nudge - see specs/smart-prayer-reminder and CLAUDE.md §2.6. */
    fun showEveningCheckIn(context: Context, devotional: DevotionalMeta, day: Int) {
        showNotification(
            context = context,
            notificationId = notificationId(EVENING_CHECK_IN_ID_BASE, devotional.id),
            devotionalId = devotional.id,
            day = day,
            title = context.getString(devotional.titleResId),
            text = context.getString(R.string.notification_evening_check_in_text, day)
        )
    }

    /**
     * Whether a reminder would actually reach the user right now: app-level
     * notifications on (which on Android 13+ also requires the POST_NOTIFICATIONS
     * permission) *and* this app's reminder channel not blocked on its own in system
     * Settings. A channel that doesn't exist yet counts as allowed - it's created at
     * default importance the first time a reminder is shown. Used by Home to keep the
     * "activated" Toast and the "Active" badge honest (P1-5 / Q6 answer in
     * specs/code-audit-v3).
     */
    fun canPostNotifications(context: Context): Boolean {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false
        val channel = manager.getNotificationChannel(CHANNEL_ID) ?: return true
        return channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    /** Keeps each devotional's notifications distinct in the tray instead of overwriting each other. */
    private fun notificationId(base: Int, devotionalId: String): Int =
        base + (devotionalId.hashCode() and 0x000FFFFF)

    /** Same channel for both notifications, by owner's choice - see specs/smart-prayer-reminder. */
    private fun showNotification(
        context: Context,
        notificationId: Int,
        devotionalId: String,
        day: Int,
        title: String,
        text: String
    ) {
        ensureChannel(context)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_TARGET_DAY, day)
            putExtra(MainActivity.EXTRA_TARGET_DEVOTIONAL_ID, devotionalId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId + day,
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
