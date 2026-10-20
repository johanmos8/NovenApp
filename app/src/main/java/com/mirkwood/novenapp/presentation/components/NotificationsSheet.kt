package com.mirkwood.novenapp.presentation.components

import android.Manifest
import android.content.Context
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.DevotionalHomeItem
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalSchedule
import com.mirkwood.novenapp.presentation.notifications.needsNotificationPermission
import com.mirkwood.novenapp.presentation.notifications.openAppNotificationSettings
import com.mirkwood.novenapp.presentation.util.Util
import com.mirkwood.novenapp.reminder.NovenaReminderScheduler
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Locale

/**
 * Opened from the app bar bell (specs/notifications-panel): one row per devotional with a
 * switch for its reminders, plus a banner with a way into system Settings while the app
 * can't post notifications. No history and no unread count, by design.
 *
 * @param devotionals the Home feed's items, in its order; each row's switch reflects
 *   [DevotionalHomeItem.isNotifying] (the relevant cycle's state, not just today's).
 * @param onToggle same contract as Home's long press: `(devotionalId, isCurrentlyNotifying)`.
 * @param onPermissionResult called after the system permission dialog closes, so the
 *   caller can re-check whether notifications are blocked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationsSheet(
    devotionals: List<DevotionalHomeItem>,
    notificationsBlocked: Boolean,
    onToggle: (devotionalId: String, isCurrentlyNotifying: Boolean) -> Unit,
    onPermissionResult: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { onPermissionResult() }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.notifications_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (notificationsBlocked) {
                BlockedBanner(onOpenSettings = { openAppNotificationSettings(context) })
            }

            devotionals.forEach { item ->
                DevotionalNotificationRow(
                    item = item,
                    onToggle = {
                        val enabling = !item.isNotifying
                        onToggle(item.devotional.id, item.isNotifying)
                        // Same flow as Home's long press (P1-5): turning on asks for the
                        // Android 13+ permission if it's still missing. When it's blocked
                        // some other way, the banner above already says so.
                        if (enabling && needsNotificationPermission(context)) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BlockedBanner(onOpenSettings: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.notifications_sheet_blocked),
                style = MaterialTheme.typography.bodyMedium
            )
            FilledTonalButton(
                onClick = onOpenSettings,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(R.string.notifications_sheet_open_settings))
            }
        }
    }
}

@Composable
private fun DevotionalNotificationRow(item: DevotionalHomeItem, onToggle: () -> Unit) {
    val context = LocalContext.current
    val title = stringResource(item.devotional.titleResId)
    val times = remember(context) {
        context.getString(
            R.string.notifications_sheet_times,
            formatHour(context, NovenaReminderScheduler.REMINDER_HOUR),
            formatHour(context, NovenaReminderScheduler.EVENING_CHECK_IN_HOUR)
        )
    }
    // Only a FixedRange devotional outside its window has a start date worth showing.
    val startsOn = remember(item.devotional, item.currentDay) {
        val schedule = item.devotional.schedule
        if (schedule is DevotionalSchedule.FixedRange && item.currentDay == null) {
            Util.resolveNextOccurrence(schedule)?.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault())
            )
        } else {
            null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = item.isNotifying, role = Role.Switch, onValueChange = { onToggle() }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = times,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (startsOn != null) {
                Text(
                    text = stringResource(R.string.journey_starts_on, startsOn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        // The whole row is the toggle target (and its accessibility node); the switch is
        // visual only, so it doesn't take a second, separate tap target.
        Switch(checked = item.isNotifying, onCheckedChange = null)
    }
}

/** An hour of the day in the device's own 12/24-hour format. */
private fun formatHour(context: Context, hour: Int): String {
    val time = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, 0)
    }
    return DateFormat.getTimeFormat(context).format(time.time)
}
