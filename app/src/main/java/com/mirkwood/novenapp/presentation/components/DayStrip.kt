package com.mirkwood.novenapp.presentation.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

/**
 * All of a devotional's days, visible at a glance instead of buried in a drawer.
 * When [currentDay] is non-null (the novena is actually running), days up to it are
 * reachable and later ones are shown "locked" - kept clickable rather than
 * Compose-disabled so a tap still gives feedback instead of being a silent no-op.
 * When [currentDay] is null (outside the Dec 16-24 window, or before/after it),
 * there's no "today" to gate against, so every day is browsable - opening one shows
 * it in preview mode rather than blocking access to the prayers year-round. Days in
 * [completedDays] (marked "prayed" from the day screen) show a checkmark instead of
 * their number.
 */
@Composable
internal fun DayStrip(
    totalDays: Int,
    currentDay: Int?,
    completedDays: Set<Int> = emptySet(),
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lockedMessage = stringResource(R.string.day_strip_locked_message)

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(totalDays) { index ->
            val day = index + 1
            val isUnlocked = currentDay == null || day <= currentDay
            val isCompleted = day in completedDays
            val dayLabel = stringResource(
                if (isUnlocked) R.string.text_dia else R.string.text_dia_locked,
                day
            )
            FilterChip(
                selected = day == currentDay,
                onClick = {
                    if (isUnlocked) {
                        onDaySelected(day)
                    } else {
                        Toast.makeText(context, lockedMessage, Toast.LENGTH_SHORT).show()
                    }
                },
                label = {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    } else {
                        Text(day.toString())
                    }
                },
                colors = if (isUnlocked) {
                    FilterChipDefaults.filterChipColors()
                } else {
                    FilterChipDefaults.filterChipColors(
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                },
                modifier = Modifier.semantics { contentDescription = dayLabel }
            )
        }
    }
}

@PreviewAllPhones
@Composable
fun PreviewDayStrip() {
    NovenAppTheme {
        DayStrip(totalDays = 9, currentDay = 4, completedDays = setOf(1, 2, 3), onDaySelected = {})
    }
}

@PreviewAllPhones
@Composable
fun PreviewDayStripOffSeason() {
    NovenAppTheme {
        DayStrip(totalDays = 9, currentDay = null, onDaySelected = {})
    }
}
