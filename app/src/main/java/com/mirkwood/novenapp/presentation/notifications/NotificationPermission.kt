package com.mirkwood.novenapp.presentation.notifications

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.mirkwood.novenapp.reminder.NovenaNotifier

/*
 * Notification-permission helpers shared by Home (long-press toggle, one-time automatic
 * prompt) and the app bar's notifications sheet, so both run exactly the same checks
 * (specs/notifications-panel §4.2). Moved here from HomeScreen.kt unchanged.
 */

/**
 * Whether the POST_NOTIFICATIONS runtime permission still has to be granted before
 * [NovenaNotifier] can post anything. Always false below Android 13, where that
 * permission doesn't exist.
 */
internal fun needsNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED

/** See [NovenaNotifier.canPostNotifications] - app-level *and* reminder-channel check. */
internal fun areNotificationsEnabled(context: Context): Boolean =
    NovenaNotifier.canPostNotifications(context)

/**
 * Opens this app's notification settings, the usual place to undo a permanent denial or
 * a channel turned off. Falls back to the app's details page if a device doesn't
 * resolve the notification-settings screen, and does nothing rather than crash if
 * neither exists.
 */
internal fun openAppNotificationSettings(context: Context) {
    val notificationSettings = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(notificationSettings)
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: ActivityNotFoundException) {
            // Nothing to open on this device.
        }
    }
}
