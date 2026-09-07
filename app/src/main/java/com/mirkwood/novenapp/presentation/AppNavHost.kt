package com.mirkwood.novenapp.presentation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.mapper.buildDevotionalDayPrayers
import com.mirkwood.novenapp.presentation.model.Novena
import com.mirkwood.novenapp.presentation.navigation.NavigationScreen
import com.mirkwood.novenapp.presentation.screens.aboutus.AboutUsScreen
import com.mirkwood.novenapp.presentation.screens.home.HomeScreen
import com.mirkwood.novenapp.presentation.screens.lyrics.LyricsScreen
import com.mirkwood.novenapp.presentation.screens.lyrics.LyricsViewModel
import com.mirkwood.novenapp.presentation.screens.lyrics.SongListScreen
import com.mirkwood.novenapp.presentation.screens.more.MoreScreen
import com.mirkwood.novenapp.presentation.screens.prayer.PrayerScreen
import com.mirkwood.novenapp.presentation.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = koinViewModel(),
    onSongTitleUpdated: (String) -> Unit
) {
    val state by mainViewModel.state.collectAsState()
    val completedDays by mainViewModel.completedDays.collectAsState()
    val lyricsViewModel: LyricsViewModel = koinViewModel()
    NavHost(
        navController = navController,
        startDestination = NavigationScreen.HomeScreen.route,
    ) {

        composable(NavigationScreen.HomeScreen.route) {
            HomeScreen(
                viewState = state,
                devotional = mainViewModel.activeDevotional,
                completedDays = completedDays,
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

            val devotionalId = backStackEntry.arguments?.getString(NavigationScreen.DayScreen.ARG_DEVOTIONAL_ID)
                ?: mainViewModel.activeDevotional.id
            val devotionalMeta = mainViewModel.getDevotional(devotionalId)

            val requestedDay = backStackEntry.arguments?.getInt(NavigationScreen.DayScreen.ARG_POSITION) ?: -1
            // Bounds-checked against the catalog before it's trusted anywhere: this is
            // the only thing standing between an out-of-range day (a stale deep link,
            // or - since MainActivity is exported - a deliberately crafted intent extra)
            // and an index-out-of-bounds crash further down.
            val novenaDay = if (requestedDay in 1..devotionalMeta.totalDays) {
                requestedDay
            } else {
                mainViewModel.refreshCurrentDay()
            }
            Log.d("Test", "currentDay: $novenaDay")
            novenaDay?.let { currentDay ->

                val context = LocalContext.current
                val language = remember { context.resources.configuration.locales[0].language }

                // Keyed on (devotionalId, language) so rotation/theme-change recompositions
                // don't re-read the asset and re-parse the JSON on the main thread.
                val content by produceState<Novena?>(initialValue = null, devotionalId, language) {
                    value = withContext(Dispatchers.IO) {
                        mainViewModel.getContentFor(devotionalId, language)
                    }
                }

                val loadedContent = content
                val dayContent = loadedContent?.dias?.getOrNull(currentDay - 1)
                if (loadedContent != null && dayContent != null) {
                    val list = buildDevotionalDayPrayers(loadedContent, dayContent, devotionalMeta, currentDay)
                    // Not derived from `novenaDay` above: that already falls back to
                    // today's day when the requested one is out of bounds, so it can
                    // never itself be "ahead". This is the actual current day, used
                    // only to decide whether the day being viewed is a preview.
                    val actualCurrentDay = Util.resolveCurrentDay(devotionalMeta.schedule)
                    val isPreview = actualCurrentDay == null || currentDay > actualCurrentDay
                    PrayerScreen(
                        prayers = list,
                        dayNumber = currentDay,
                        isPreview = isPreview,
                        isDayCompleted = currentDay in completedDays,
                        onToggleDayCompleted = { mainViewModel.toggleDayCompleted(currentDay) }
                    )
                } else if (loadedContent != null) {
                    // Content loaded, but this day index doesn't exist in it (catalog's
                    // totalDays drifted from the JSON's actual day count) - show a
                    // message instead of nothing, rather than silently rendering blank.
                    MissingContentMessage()
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
        composable(NavigationScreen.MoreScreen.route) {
            MoreScreen(
                onAboutUsClick = { navController.navigate(NavigationScreen.AboutUsScreen.route) },
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(NavigationScreen.AboutUsScreen.route) {
            AboutUsScreen(
                modifier = Modifier.fillMaxSize(),
            )
        }

    }

}

@Composable
private fun MissingContentMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.error_devotional_content_unavailable),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
