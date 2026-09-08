package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

/**
 * A row of [totalSegments] equal bars - the compact, non-interactive counterpart to
 * [DayStrip]. Segments whose 1-based index is in [filledSegments] read as "done" (so a
 * gap shows as a gap, matching the checkmarks on the day strip); [highlightedSegment]
 * gets an outline to mark "today" while it isn't filled yet. Used by the home Camino
 * card to show novena progress at a glance without the full tappable day strip.
 */
@Composable
internal fun SegmentedProgressIndicator(
    totalSegments: Int,
    filledSegments: Set<Int>,
    modifier: Modifier = Modifier,
    highlightedSegment: Int? = null,
    contentDescription: String? = null,
) {
    val shape = RoundedCornerShape(4.dp)
    val description = contentDescription
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (description != null) {
                    Modifier.semantics { this.contentDescription = description }
                } else {
                    Modifier
                }
            ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (segment in 1..totalSegments) {
            val filled = segment in filledSegments
            val outlined = !filled && segment == highlightedSegment
            Box(
                Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(shape)
                    .background(
                        if (filled) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .then(
                        if (outlined) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.secondary, shape)
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

@PreviewAllPhones
@Composable
private fun PreviewSegmentedProgressIndicator() {
    NovenAppTheme {
        SegmentedProgressIndicator(
            totalSegments = 9,
            filledSegments = setOf(1, 2, 3),
            highlightedSegment = 4,
        )
    }
}

@PreviewAllPhones
@Composable
private fun PreviewSegmentedProgressIndicatorComplete() {
    NovenAppTheme {
        SegmentedProgressIndicator(totalSegments = 9, filledSegments = (1..9).toSet())
    }
}
