package com.mirkwood.novenapp.presentation

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mirkwood.novenapp.presentation.mapper.buildDevotionalDayPrayers
import com.mirkwood.novenapp.presentation.navigation.NavigationScreen
import com.mirkwood.novenapp.presentation.screens.aboutus.AboutUsScreen
import com.mirkwood.novenapp.presentation.screens.home.HomeScreen
import com.mirkwood.novenapp.presentation.screens.lyrics.LyricsScreen
import com.mirkwood.novenapp.presentation.screens.lyrics.LyricsViewModel
import com.mirkwood.novenapp.presentation.screens.lyrics.SongListScreen
import com.mirkwood.novenapp.presentation.screens.prayer.PrayerScreen
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = koinViewModel(),
    onSongTitleUpdated: (String) -> Unit
) {
    val state by mainViewModel.state.collectAsState()
    val lyricsViewModel: LyricsViewModel = koinViewModel()
    NavHost(
        navController = navController,
        startDestination = NavigationScreen.HomeScreen.route,
    ) {

        composable(NavigationScreen.HomeScreen.route) {
            HomeScreen(
                viewState = state,
                devotional = mainViewModel.activeDevotional,
                onEvent = { event -> mainViewModel.onAction(event, navController) }
            )
        }
        composable(
            NavigationScreen.DayScreen.route,
            arguments = listOf(
                navArgument(NavigationScreen.DayScreen.ARG_DEVOTIONAL_ID) {
                    type = NavType.StringType
                },
                navArgument(NavigationScreen.DayScreen.ARG_POSITION) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val day = backStackEntry.arguments?.getInt(NavigationScreen.DayScreen.ARG_POSITION) ?: -1

            val novenaDay = if (day != -1) {
                day
            } else {
                mainViewModel.refreshCurrentDay()
            }
            Log.d("Test", "currentDay: $novenaDay")
            novenaDay?.let { currentDay ->

                val language = LocalContext.current.resources.configuration.locales[0].language
                val content = mainViewModel.getContent(language)
                content?.let {
                    val list = buildDevotionalDayPrayers(it, mainViewModel.activeDevotional, currentDay)
                    PrayerScreen(list)
                }
            }
        }
        composable(NavigationScreen.LyricsScreen.route) {
            lyricsViewModel.loadSongsFromJson(context = LocalContext.current)
            lyricsViewModel.songs.value?.let { it1 ->
                SongListScreen(
                    onAction = { songID ->
                        navController.navigate(
                            NavigationScreen.LyricsViewScreen.createRoute(
                                songID
                            )
                        )
                    },
                    songs = it1,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(
            NavigationScreen.LyricsViewScreen.route,
            arguments = listOf(navArgument("songID") { type = NavType.IntType })
        ) { backStackEntry ->

            val songID = backStackEntry.arguments?.getInt("songID")
            Log.d("Test", "songID: $songID")
            val context = LocalContext.current
            val selectedSong by lyricsViewModel.selectedSong.collectAsState()

            LaunchedEffect(songID) {
                // Si las canciones aún no se han cargado, cargarlas y luego obtener la canción
                if (lyricsViewModel.songs.value == null) {
                    lyricsViewModel.loadSongsFromJson(context, songID)
                } else {
                    songID?.let { lyricsViewModel.getSongById(it) }
                }
            }
            Log.d("Test", "selectedSong: $selectedSong")

            if (selectedSong != null) {
                onSongTitleUpdated(selectedSong!!.title)
                LyricsScreen(
                    songTitle = selectedSong!!.title,
                    lyrics = selectedSong!!.lyrics,
                    onBack = { navController.popBackStack() }
                )
            } else {
                Text(text = "TODO(fix)")
            }
        }
        composable(NavigationScreen.AboutUsScreen.route) {
            AboutUsScreen(
                modifier = Modifier.fillMaxSize(),
            )
        }

    }

}
