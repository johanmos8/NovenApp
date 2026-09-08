package com.mirkwood.novenapp.presentation.screens.more

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.BuildConfig
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.debug.DebugClock
import com.mirkwood.novenapp.debug.DebugSettings
import com.mirkwood.novenapp.presentation.components.CopyRightFooter
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalCatalog
import com.mirkwood.novenapp.ui.theme.NovenAppTheme
import java.time.LocalDate

/**
 * The tab "everything that isn't the daily prayer or the carols" lives on - the one
 * place a short, static list is still the right pattern (see the hamburger-menu
 * removal in [com.mirkwood.novenapp.MainActivity]).
 */
@Composable
internal fun MoreScreen(
    onAboutUsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        MoreItem(
            text = stringResource(R.string.text_about_us),
            icon = Icons.Default.Info,
            onClick = onAboutUsClick
        )
        MoreItem(
            text = stringResource(R.string.text_rate_this_app),
            icon = Icons.Default.Star,
            onClick = { openPlayStore(context) }
        )
        if (BuildConfig.DEBUG) {
            DebugDateSection()
        }
        CopyRightFooter()
    }
}

/**
 * Debug builds only. Switches the app's idea of "today" between the real date and any
 * novena day, so the seasonal (Dec 16–24) home UI can be checked year-round. The choice
 * persists across restarts; picking "Real date" turns it off. Not shown in release
 * builds and stores nothing there (see [DebugSettings]).
 */
@Composable
private fun DebugDateSection() {
    val context = LocalContext.current
    val settings = remember { DebugSettings(context) }
    var forced by remember { mutableStateOf(settings.forcedDate) }

    val presets: List<Pair<String, LocalDate?>> = remember {
        buildList {
            add("Real date" to null)
            for (day in 1..DevotionalCatalog.default.totalDays) {
                add("Day $day" to DebugSettings.dateForNovenaDay(day))
            }
            add("Dec 25" to DebugSettings.celebrationDate())
        }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(
            text = "🐞  Debug · forced date",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "App is acting as ${DebugClock.today()}" +
                if (forced == null) " (real date)" else " (forced)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(presets) { (label, date) ->
                FilterChip(
                    selected = forced == date,
                    onClick = {
                        settings.forcedDate = date
                        forced = date
                    },
                    label = { Text(label) }
                )
            }
        }
        Text(
            text = "Return to the Novena tab to see the change.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun MoreItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = CenterVertically
    ) {
        Icon(
            imageVector = icon,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = null
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 20.dp)
        )
    }
}

fun openPlayStore(context: Context) {
    val uri = Uri.parse("market://details?id=${context.packageName}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    context.startActivity(intent)
}

@PreviewAllPhones
@Composable
fun PreviewMoreScreen() {
    NovenAppTheme {
        MoreScreen(onAboutUsClick = {})
    }
}
