package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mirkwood.novenapp.presentation.MainViewModel
import com.mirkwood.novenapp.presentation.model.NovenaTab

@Composable
fun TextTabs(innerPadding: PaddingValues,
    mainViewModel: MainViewModel,
    position: Int
) {
    var state by remember { mutableStateOf(0) }
    val ctx= LocalContext.current
    // Define los tabs usando sealed classes
    val tabs = listOf(
        NovenaTab.Consideracion,
        NovenaTab.OracionTodosLosDias,
        NovenaTab.OracionALaVirgen,
        NovenaTab.OracionSanJose
    )

    Column(modifier = Modifier.padding(innerPadding)) {
        // Mostrar los títulos en el TabRow
        TabRow(selectedTabIndex = state) {
            tabs.forEachIndexed { index, tab ->
                if(tab == NovenaTab.Consideracion){
                    tab.title = "Consideración día $position"
                }
                Tab(
                    text = { Text(tab.title) },
                    selected = state == index,
                    onClick = { state = index }
                )
            }
        }

        when (val selectedTab = tabs[state]) {
            is NovenaTab.Consideracion -> ReadingScreen(textContent = mainViewModel.getContent(ctx).dias[position-1].reflexion)
            is NovenaTab.OracionTodosLosDias -> ReadingScreen(textContent = mainViewModel.getContent(ctx).general.oracion_todos_los_dias)
            is NovenaTab.OracionALaVirgen -> ReadingScreen(textContent = mainViewModel.getContent(ctx).general.oracion_virgen_maria)
            is NovenaTab.OracionSanJose -> ReadingScreen(textContent = mainViewModel.getContent(ctx).general.oracion_san_jose)
            else -> Unit
        }
    }
}


