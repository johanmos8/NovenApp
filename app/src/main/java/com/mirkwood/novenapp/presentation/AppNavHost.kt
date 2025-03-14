package com.mirkwood.novenapp.presentation

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.components.TextTabs
import com.mirkwood.novenapp.presentation.model.NovenaTab
import com.mirkwood.novenapp.presentation.model.Prayer
import com.mirkwood.novenapp.presentation.screens.home.HomeScreen
import com.mirkwood.novenapp.presentation.screens.prayer.PrayerScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val mainViewModel = MainViewModel()
    val state by mainViewModel.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NavigationScreen.HomeScreen.route,
    ) {

        composable(NavigationScreen.HomeScreen.route) {
            HomeScreen(
                viewState = state,
                onEvent = { event -> mainViewModel.onAction(event, navController) }
            )
        }
        composable(
            NavigationScreen.DayScreen.route,
            arguments = listOf(navArgument("position") {
                type = NavType.IntType
            })
        ) { it ->

            val day = it.arguments?.getInt("position") ?: -1

            val novenaDay = if (day != -1) {
                day
            } else {
                mainViewModel.getNovenaDay()
            }
            Log.d("Test", "currentDay: $novenaDay")
            novenaDay?.let { currentDay ->

                val content = mainViewModel.getContent(LocalContext.current)
                val list = listOf(
                    Prayer.WithImage(content.dias[currentDay - 1].reflexion, R.drawable.novena, NovenaTab.Consideracion.title),
                    Prayer.WithImage(content.general.oracion_todos_los_dias, R.drawable.pesebre, NovenaTab.OracionTodosLosDias.title),
                    Prayer.WithImage(content.general.oracion_virgen_maria, R.drawable.virgen_maria, NovenaTab.OracionALaVirgen.title),
                    Prayer.WithImage(content.general.oracion_san_jose, R.drawable.san_jose, NovenaTab.OracionSanJose.title),
                    Prayer.AllGozos(content.gozos, R.drawable.novena_music, NovenaTab.Gozos.title),
                    Prayer.WithImage(content.general.oracion_niño_jesus, R.drawable.baby_jesus, NovenaTab.OracionAJesus.title),

                    )
                PrayerScreen(list)
            }
        }

    }
}