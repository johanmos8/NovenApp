package com.mirkwood.novenapp.presentation.screens.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.components.GozosScreen
import com.mirkwood.novenapp.presentation.model.MainModule
import com.mirkwood.novenapp.presentation.model.Prayer
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
internal fun PrayerScreen(
    prayers: List<Prayer>
) {
    val pagerState = rememberPagerState(pageCount = {
        prayers.size
    })

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
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
                        selectedTab.list, MainModule.Hero(
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
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color =
                    if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
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
        PrayerScreen(prayers = prayers)
    }
}
