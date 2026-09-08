package com.mirkwood.novenapp.presentation.screens.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.components.GozosScreen
import com.mirkwood.novenapp.presentation.model.MainModule
import com.mirkwood.novenapp.presentation.model.Prayer
import com.mirkwood.novenapp.ui.theme.NovenAppTheme
import kotlinx.coroutines.launch
import kotlin.math.ceil

/** Bounds and step for the in-screen reading-text size control (A- / A+). */
private const val MIN_TEXT_SCALE = 0.85f
private const val MAX_TEXT_SCALE = 1.5f
private const val TEXT_SCALE_STEP = 0.15f

/** Average adult reading speed, used for the per-stage "~N min" estimate. */
private const val WORDS_PER_MINUTE = 200.0

@Composable
internal fun PrayerScreen(
    prayers: List<Prayer>,
    dayNumber: Int,
    totalDays: Int,
    isPreview: Boolean = false,
    isDayCompleted: Boolean = false,
    onToggleDayCompleted: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = {
        prayers.size
    })

    // Reading-text size, shared by every page of this day. rememberSaveable keeps it
    // across rotation; it is intentionally not persisted between visits to the screen.
    var textScale by rememberSaveable { mutableStateOf(1f) }

    val scope = rememberCoroutineScope()

    val narrator = rememberPrayerNarrator()
    // Narration is tied to the page it was started from - swiping away stops it rather
    // than leaving a previous prayer being read aloud over a different one on screen.
    LaunchedEffect(pagerState.currentPage) {
        narrator.stop()
    }

    val gozosLabel = stringResource(R.string.gozos_title)
    val currentStage = prayers.getOrNull(pagerState.currentPage)

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (isPreview) {
            PreviewBanner()
        }

        PrayerToolbar(
            dayNumber = dayNumber,
            totalDays = totalDays,
            canDecreaseText = textScale > MIN_TEXT_SCALE,
            canIncreaseText = textScale < MAX_TEXT_SCALE,
            onDecreaseText = {
                textScale = (textScale - TEXT_SCALE_STEP).coerceAtLeast(MIN_TEXT_SCALE)
            },
            onIncreaseText = {
                textScale = (textScale + TEXT_SCALE_STEP).coerceAtMost(MAX_TEXT_SCALE)
            },
            isSpeaking = narrator.isSpeaking,
            onToggleListen = {
                if (narrator.isSpeaking) {
                    narrator.stop()
                } else {
                    narrator.speak(prayers[pagerState.currentPage].narrationText())
                }
            }
        )

        if (currentStage != null) {
            StageCard(
                stageNumber = pagerState.currentPage + 1,
                stageCount = prayers.size,
                stageTitle = currentStage.stageTitle(gozosLabel),
                estimatedMinutes = currentStage.estimatedMinutes(),
                onStageSelected = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) { page ->

            when (val selectedTab = prayers[page]) {
                is Prayer.Simple -> {
                    ReadingScreen(
                        textContent = selectedTab.text,
                        finalPrayers = selectedTab.finalPrayers,
                        textScale = textScale
                    )
                }

                is Prayer.WithImage -> {
                    ReadingWithImageScreen(
                        title = selectedTab.title,
                        selectedTab.text, MainModule.Hero(
                            selectedTab.imageRes,
                            height = MainModule.Hero.DefaultHeight
                        ),
                        finalPrayers = selectedTab.finalPrayers,
                        textScale = textScale
                    )
                }

                is Prayer.AllGozos -> {
                    GozosScreen(
                        gozos = selectedTab.gozos,
                        image = selectedTab.imageRes?.let { imageRes ->
                            MainModule.Hero(
                                imageRes,
                                height = MainModule.Hero.DefaultHeight
                            )
                        }
                    )
                }
            }
        }
        DayCompletionToggle(
            dayNumber = dayNumber,
            isCompleted = isDayCompleted,
            onToggle = onToggleDayCompleted,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

/** The text read aloud for a page when "Listen" is tapped. */
private fun Prayer.narrationText(): String = when (this) {
    is Prayer.Simple -> text
    is Prayer.WithImage -> "$title. $text"
    is Prayer.AllGozos -> gozos.joinToString(separator = "\n\n") { it.texto }
}

/** Name of this stage for the stage card. Gozos has no title of its own, so [gozosLabel] stands in. */
private fun Prayer.stageTitle(gozosLabel: String): String = when (this) {
    is Prayer.Simple -> title
    is Prayer.WithImage -> title
    is Prayer.AllGozos -> gozosLabel
}

/** Rough minutes to read this stage aloud/at a contemplative pace, floored at 1. */
private fun Prayer.estimatedMinutes(): Int {
    val body = when (this) {
        is Prayer.Simple -> text
        is Prayer.WithImage -> text
        is Prayer.AllGozos -> gozos.joinToString(" ") { it.texto }
    }
    val words = body.split(Regex("\\s+")).count { it.isNotBlank() }
    return ceil(words / WORDS_PER_MINUTE).toInt().coerceAtLeast(1)
}

/**
 * The "where am I in today's prayer" card, pinned between the toolbar and the pager so
 * it stays put while the prayer text scrolls. Row 1 names the current stage and its
 * rough length; row 2 is a tappable segmented bar - one segment per pager page, filled
 * up to the current one, which is outlined. This replaces the animated dot row that
 * used to sit under the pager.
 */
@Composable
private fun StageCard(
    stageNumber: Int,
    stageCount: Int,
    stageTitle: String,
    estimatedMinutes: Int,
    onStageSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.prayer_stage_label, stageNumber, stageTitle),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.prayer_stage_minutes, estimatedMinutes),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StageSegments(
                total = stageCount,
                currentIndex = stageNumber - 1,
                onSelect = onStageSelected
            )
        }
    }
}

/**
 * Tappable sibling of [com.mirkwood.novenapp.presentation.components.SegmentedProgressIndicator]:
 * each bar sits in a taller transparent hit target so it's comfortably tappable despite
 * the 8dp visual height. Segments before [currentIndex] read as done; [currentIndex] is
 * outlined.
 */
@Composable
private fun StageSegments(
    total: Int,
    currentIndex: Int,
    onSelect: (index: Int) -> Unit
) {
    val shape = RoundedCornerShape(4.dp)
    val description = stringResource(
        R.string.prayer_stage_progress, currentIndex + 1, total
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in 0 until total) {
            val done = index < currentIndex
            val current = index == currentIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(shape)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(shape)
                        .background(
                            if (done) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .then(
                            if (current) {
                                Modifier.border(2.dp, MaterialTheme.colorScheme.secondary, shape)
                            } else {
                                Modifier
                            }
                        )
                )
            }
        }
    }
}

/**
 * The control strip directly below the app bar, present on every page of the pager:
 * a read-only progress pill on the leading side, and the reading aids (text size,
 * listen) on the trailing side. Kept out of the pager itself so it does not scroll
 * away with the prayer text and stays put while swiping between pages.
 */
@Composable
private fun PrayerToolbar(
    dayNumber: Int,
    totalDays: Int,
    canDecreaseText: Boolean,
    canIncreaseText: Boolean,
    onDecreaseText: () -> Unit,
    onIncreaseText: () -> Unit,
    isSpeaking: Boolean,
    onToggleListen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProgressPill(dayNumber = dayNumber, totalDays = totalDays)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val decreaseLabel = stringResource(R.string.content_description_decrease_text_size)
            val increaseLabel = stringResource(R.string.content_description_increase_text_size)
            TextButton(
                onClick = onDecreaseText,
                enabled = canDecreaseText,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.semantics { contentDescription = decreaseLabel }
            ) {
                Text(text = "A−", style = MaterialTheme.typography.titleMedium)
            }
            TextButton(
                onClick = onIncreaseText,
                enabled = canIncreaseText,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.semantics { contentDescription = increaseLabel }
            ) {
                Text(text = "A+", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(4.dp))
            FilledTonalButton(
                onClick = onToggleListen,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.Filled.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(
                        if (isSpeaking) R.string.prayer_stop else R.string.prayer_listen
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ProgressPill(dayNumber: Int, totalDays: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = stringResource(R.string.prayer_progress_pill, dayNumber, totalDays),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * Shown when the viewed day is ahead of where the schedule actually is - either the
 * whole devotional hasn't started yet, or the user swiped/linked past today's day.
 * Landing there should say so explicitly rather than silently rendering full content
 * with no indication it's early.
 */
@Composable
private fun PreviewBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.day_preview_banner),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

/**
 * Tracks the whole day, not the individual prayer page currently on screen - it stays
 * checked as you swipe through this day's reflection, prayers, and Gozos, matching the
 * single checkmark shown for this day on the home tab's day strip. Naming the day
 * number explicitly in the label is what makes that scope clear, since the chip sits
 * right below the pager where it could otherwise read as belonging to just one page.
 */
@Composable
private fun DayCompletionToggle(
    dayNumber: Int,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        modifier = modifier,
        selected = isCompleted,
        onClick = onToggle,
        shape = RoundedCornerShape(percent = 50),
        leadingIcon = if (isCompleted) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
        label = {
            Text(
                stringResource(
                    if (isCompleted) R.string.day_marked_as_prayed else R.string.mark_day_as_prayed,
                    dayNumber
                )
            )
        }
    )
}

@Composable
@PreviewAllPhones
fun PreviewPrayerScreen() {
    NovenAppTheme {
        val prayers = listOf(
            Prayer.WithImage(
                "Oración a San José...",
                R.drawable.san_jose,
                "Oracion"
            ), //Oracion a san Jose
            Prayer.Simple("Oración a la Virgen María...", "Oracion"), //Consideracion
            Prayer.Simple("Oraciónpara todos los dias...", "Oracion"), //Oracion para todos los dias
            Prayer.WithImage(
                "Oración a la Virgen María...",
                R.drawable.virgen_maria, "Oracion"
            ), //Virgen Maria
            Prayer.Simple("Oración de los Pastores...", "Oracion"),// Gozos
            Prayer.WithImage(
                "Oración al Niño Jesús...",
                R.drawable.baby_jesus, "Oracion"
            ), //ORacion al niño Jesus
        )
        PrayerScreen(prayers = prayers, dayNumber = 4, totalDays = 9)
    }
}
