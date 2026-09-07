package com.mirkwood.novenapp.presentation.screens.prayer

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.components.GozosScreen
import com.mirkwood.novenapp.presentation.model.MainModule
import com.mirkwood.novenapp.presentation.model.Prayer
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
internal fun PrayerScreen(
    prayers: List<Prayer>,
    dayNumber: Int,
    isPreview: Boolean = false,
    isDayCompleted: Boolean = false,
    onToggleDayCompleted: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = {
        prayers.size
    })

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (isPreview) {
            PreviewBanner()
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) { page ->

            when (val selectedTab = prayers[page]) {
                is Prayer.Simple -> {
                    ReadingScreen(selectedTab.text)
                }

                is Prayer.WithImage -> {
                    ReadingWithImageScreen(
                        title = selectedTab.title,
                        selectedTab.text, MainModule.Hero(
                            selectedTab.imageRes,
                            height = MainModule.Hero.DefaultHeight
                        ),
                        finalPrayers = selectedTab.finalPrayers
                    )
                }

                is Prayer.AllGozos -> {
                    GozosScreen(
                        gozos = selectedTab.gozos,
                        image = MainModule.Hero(
                            selectedTab.imageRes,
                            height = MainModule.Hero.DefaultHeight
                        )
                    )
                }
            }
        }
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                val width by animateDpAsState(
                    targetValue = if (isSelected) 20.dp else 8.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "pagerIndicatorWidth"
                )
                val color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                }
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                )
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
        PrayerScreen(prayers = prayers, dayNumber = 4)
    }
}
