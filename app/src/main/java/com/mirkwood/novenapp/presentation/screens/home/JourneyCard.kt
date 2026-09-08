package com.mirkwood.novenapp.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.components.SegmentedProgressIndicator
import com.mirkwood.novenapp.ui.theme.NovenAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * A soft, pastel horizontal wash for the [JourneyCard] background: a very light tint of the
 * design-system Primary (#3F51B5) on the left easing into a very light tint of Secondary
 * (#D97706) on the right. The tints are near-white so the gradient stays low-contrast and
 * never competes with the card's content or typography.
 *
 * In dark theme the same two hues are used at a very low alpha over the card surface so the
 * wash stays just as subtle without washing out light-on-dark text.
 */
private val JourneyCardGradient: Brush
    @Composable get() = if (isSystemInDarkTheme()) {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF3F51B5).copy(alpha = 0.16f), // Primary
                Color(0xFFD97706).copy(alpha = 0.16f), // Secondary
            ),
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Color(0xFFE8EAF6), // ~12% #3F51B5 over white
                Color(0xFFFAEFE1), // ~12% #D97706 over white
            ),
        )
    }

/**
 * The first card in the home "Camino" feed: a state-driven entry point into the daily
 * prayer. Its pill label, body and call-to-action all follow [JourneyStatus] rather
 * than assuming the novena is currently running - see `docs/home-journey-redesign.md`.
 *
 * @param onContinue navigate to a specific day of the active devotional.
 * @param onPreview  navigate to day 1 in preview mode (used before the season opens).
 */
@Composable
internal fun JourneyCard(
    devotionalTitle: String,
    status: JourneyStatus,
    onContinue: (day: Int) -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(JourneyCardGradient)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            JourneyPill(status)

            Text(
                text = devotionalTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            when (status) {
                is JourneyStatus.InProgress -> InProgressBody(status, onContinue)
                is JourneyStatus.NotStarted -> NotStartedBody(status, onPreview)
                is JourneyStatus.Completed -> CompletedBody(status, onContinue)
            }
        }
    }
}

/**
 * "**Day 4** of 9" - the position through the number is bold, the "of N" tail is normal
 * weight. Located by the day number's substring so it works regardless of where the
 * locale puts it ("**Día 4** de 9").
 */
@Composable
private fun dayOfLabel(currentDay: Int, totalDays: Int): AnnotatedString {
    val full = stringResource(R.string.journey_day_of, currentDay, totalDays)
    val marker = currentDay.toString()
    val boldEnd = full.indexOf(marker).let { if (it >= 0) it + marker.length else full.length }
    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(full.substring(0, boldEnd)) }
        append(full.substring(boldEnd))
    }
}

@Composable
private fun JourneyPill(status: JourneyStatus) {
    val labelRes = when (status) {
        is JourneyStatus.InProgress -> R.string.journey_pill_in_progress
        is JourneyStatus.NotStarted -> R.string.journey_pill_upcoming
        is JourneyStatus.Completed -> R.string.journey_pill_completed
    }
    val accentColor = when (status) {
        is JourneyStatus.InProgress -> MaterialTheme.colorScheme.secondary
        is JourneyStatus.NotStarted -> MaterialTheme.colorScheme.onSurfaceVariant
        is JourneyStatus.Completed -> MaterialTheme.colorScheme.primary
    }
    val labelColor = if (status is JourneyStatus.InProgress) {
        accentColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
        )
    }
}

@Composable
private fun InProgressBody(
    status: JourneyStatus.InProgress,
    onContinue: (day: Int) -> Unit,
) {
    TonalPanel {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = dayOfLabel(status.currentDay, status.totalDays),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.journey_fraction_of_path, status.percent),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        SegmentedProgressIndicator(
            totalSegments = status.totalDays,
            filledSegments = status.prayedInRange,
            highlightedSegment = status.currentDay,
            contentDescription = stringResource(
                R.string.journey_progress_content_description,
                status.prayedCount,
                status.totalDays,
            ),
        )
    }

    if (status.keepingUp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(R.string.journey_keeping_up),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    Button(
        onClick = { onContinue(status.currentDay) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 50),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Text(
            text = stringResource(R.string.journey_continue_day, status.currentDay),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.width(8.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
    }
}

@Composable
private fun NotStartedBody(
    status: JourneyStatus.NotStarted,
    onPreview: () -> Unit,
) {
    val formattedDate = remember(status.startsOn) {
        status.startsOn.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault())
        )
    }

    TonalPanel {
        Text(
            text = stringResource(R.string.widget_countdown_label, status.daysUntilStart.toInt()),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.journey_starts_on, formattedDate),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    FilledTonalButton(
        onClick = onPreview,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 50),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Text(
            text = stringResource(R.string.journey_preview_novena),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.width(8.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
    }
}

@Composable
private fun CompletedBody(
    status: JourneyStatus.Completed,
    onContinue: (day: Int) -> Unit,
) {
    TonalPanel {
        Text(
            text = stringResource(
                R.string.journey_completed_summary,
                status.totalDays,
                status.totalDays,
            ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        SegmentedProgressIndicator(
            totalSegments = status.totalDays,
            filledSegments = (1..status.totalDays).toSet(),
        )
    }

    FilledTonalButton(
        onClick = { onContinue(1) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 50),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Text(
            text = stringResource(R.string.journey_read_again),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.width(8.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
    }
}

/** The inner card the numbers/progress sit inside, shared by all three states. */
@Composable
private fun TonalPanel(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@PreviewAllPhones
@Composable
private fun PreviewJourneyCardInProgress() {
    NovenAppTheme {
        JourneyCard(
            devotionalTitle = "Novena de Aguinaldos",
            status = JourneyStatus.InProgress(
                currentDay = 4,
                totalDays = 9,
                prayedDays = setOf(1, 2, 3, 4),
            ),
            onContinue = {},
            onPreview = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewAllPhones
@Composable
private fun PreviewJourneyCardBehind() {
    NovenAppTheme {
        JourneyCard(
            devotionalTitle = "Novena de Aguinaldos",
            status = JourneyStatus.InProgress(
                currentDay = 5,
                totalDays = 9,
                prayedDays = setOf(1, 2),
            ),
            onContinue = {},
            onPreview = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewAllPhones
@Composable
private fun PreviewJourneyCardNotStarted() {
    NovenAppTheme {
        JourneyCard(
            devotionalTitle = "Novena de Aguinaldos",
            status = JourneyStatus.NotStarted(
                totalDays = 9,
                startsOn = LocalDate.of(2026, 12, 16),
                daysUntilStart = 100,
            ),
            onContinue = {},
            onPreview = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewAllPhones
@Composable
private fun PreviewJourneyCardCompleted() {
    NovenAppTheme {
        JourneyCard(
            devotionalTitle = "Novena de Aguinaldos",
            status = JourneyStatus.Completed(totalDays = 9),
            onContinue = {},
            onPreview = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
