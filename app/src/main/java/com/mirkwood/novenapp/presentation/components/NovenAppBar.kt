package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

/**
 * The branded top bar shown on every screen: logo + a two-line title (small app name,
 * bigger per-screen section name) on the leading side. Top-level tabs (Home, Villancicos,
 * More) show notifications/profile on the trailing side; screens reached by drilling in
 * (the day reading) pass [onBackClick] instead, which swaps those actions for a back arrow.
 */
/** The Profile action has no purpose yet in an account-less app - hidden until the owner defines one. */
private const val SHOW_PROFILE_ACTION = false

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovenAppBar(
    sectionTitle: String,
    onBackClick: (() -> Unit)? = null,
    onNotificationsClick: () -> Unit = {},
    notificationsBlocked: Boolean = false,
    onProfileClick: () -> Unit = {}
) {
    TopAppBar(
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.btn_back)
                    )
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = stringResource(R.string.content_description_app_logo),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sectionTitle,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        },
        actions = {
            if (onBackClick == null) {
                // Opens the notifications sheet (specs/notifications-panel). Crossed out
                // while the app can't post notifications; deliberately no unread badge.
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = if (notificationsBlocked) Icons.Filled.NotificationsOff else Icons.Filled.Notifications,
                        contentDescription = stringResource(
                            if (notificationsBlocked) {
                                R.string.content_description_notifications_blocked
                            } else {
                                R.string.content_description_notifications
                            }
                        )
                    )
                }
                // TODO(owner): define what Profile does (Q7 in specs/code-audit-v3). Hidden,
                // not removed, until then - flip SHOW_PROFILE_ACTION back on once it has a purpose.
                if (SHOW_PROFILE_ACTION) {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = stringResource(R.string.content_description_profile),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    )
}

@PreviewAllPhones
@Composable
private fun PreviewNovenAppBar() {
    NovenAppTheme {
        NovenAppBar(sectionTitle = "Journey")
    }
}

@PreviewAllPhones
@Composable
private fun PreviewNovenAppBarWithBack() {
    NovenAppTheme {
        NovenAppBar(sectionTitle = "Daily prayer", onBackClick = {})
    }
}
