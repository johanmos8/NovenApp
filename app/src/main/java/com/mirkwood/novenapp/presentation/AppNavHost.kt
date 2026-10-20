package com.mirkwood.novenapp.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.mirkwood.novenapp.presentation.mapper.buildDevotionalDayPrayersWithoutImages
import com.mirkwood.novenapp.presentation.model.DevotionalContent
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

/**
 * Steps to an adjacent day of the same devotional, replacing the current DayScreen
 * back-stack entry instead of pushing a new one - so tapping the prev/next arrows
 * repeatedly doesn't pile up entries, and the top-bar back button always returns
 * directly to Home regardless of how many days were browsed.
 */
private fun NavHostController.navigateToDevotionalDay(devotionalId: String, day: Int) {
    navigate(NavigationScreen.DayScreen.createRoute(devotionalId, day)) {
        popUpTo(NavigationScreen.DayScreen.route) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
internal fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = koinViewModel(),
    // Both title callbacks carry the reporting back-stack entry's id, so the app bar only
    // shows a title while that same entry is current - a new DayScreen or lyrics entry
    // never inherits the previous one's title (P1-6 in specs/code-audit-v3).
    onSongTitleUpdated: (entryId: String, title: String) -> Unit,
    onStageTitleUpdated: (entryId: String, title: String) -> Unit = { _, _ -> }
) {
    // Lifecycle-aware: a plain collectAsState() keeps collecting (and, through it, the
    // per-devotional DataStore flows underneath) while the app is backgrounded - see
    // the P2-5 finding in specs/code-audit-v3.
    val homeDevotionals by mainViewModel.homeDevotionals.collectAsStateWithLifecycle()
    val previewBannerDismissed by mainViewModel.previewBannerDismissed.collectAsStateWithLifecycle()
    val lyricsViewModel: LyricsViewModel = koinViewModel()
    NavHost(
        navController = navController,
        startDestination = NavigationScreen.HomeScreen.route,
    ) {

        composable(NavigationScreen.HomeScreen.route) {
            val context = LocalContext.current
            HomeScreen(
                devotionals = homeDevotionals,
                onEvent = { event -> mainViewModel.onAction(event, navController) },
                onToggleNotification = { devotionalId, isCurrentlyNotifying ->
                    mainViewModel.toggleNotification(context, devotionalId, isCurrentlyNotifying)
                },
                hasAutoPromptedPermission = mainViewModel::hasAutoPromptedNotificationPermission,
                onAutoPromptedPermission = mainViewModel::markNotificationPermissionAutoPrompted
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
                ?: mainViewModel.defaultDevotionalId()
            val devotionalMeta = mainViewModel.getDevotional(devotionalId)
            val completedDays by remember(devotionalId) { mainViewModel.observeCompletedDays(devotionalId) }
                .collectAsStateWithLifecycle(initialValue = emptySet())

            val requestedDay = backStackEntry.arguments?.getInt(NavigationScreen.DayScreen.ARG_POSITION) ?: -1
            // Bounds-checked against the catalog before it's trusted anywhere: this is
            // the only thing standing between an out-of-range day (a stale deep link,
            // or - since MainActivity is exported - a deliberately crafted intent extra)
            // and an index-out-of-bounds crash further down.
            val novenaDay = if (requestedDay in 1..devotionalMeta.totalDays) {
                requestedDay
            } else {
                Util.resolveCurrentDay(devotionalMeta.schedule)
            }
            novenaDay?.let { currentDay ->

                val context = LocalContext.current
                val language = remember { context.resources.configuration.locales[0].language }

                // Keyed on (devotionalId, language) so rotation/theme-change recompositions
                // don't re-read the asset and re-parse the JSON on the main thread.
                val content by produceState<DevotionalContent?>(initialValue = null, devotionalId, language) {
                    value = withContext(Dispatchers.IO) {
                        mainViewModel.getContentFor(devotionalId, language)
                    }
                }

                val loadedContent = content
                val dayContent = loadedContent?.days?.getOrNull(currentDay - 1)
                if (loadedContent != null && dayContent != null) {
                    //val list = buildDevotionalDayPrayers(loadedContent, dayContent, devotionalMeta.images)
                    val list = buildDevotionalDayPrayersWithoutImages(loadedContent, dayContent)

                    // Not derived from `novenaDay` above: that already falls back to
                    // today's day when the requested one is out of bounds, so it can
                    // never itself be "ahead". This is the actual current day, used
                    // only to decide whether the day being viewed is a preview.
                    val actualCurrentDay = Util.resolveCurrentDay(devotionalMeta.schedule)
                    val isPreview = actualCurrentDay == null || currentDay > actualCurrentDay

                    // Day-to-day navigation is purely ordinal (day ± 1, clamped to the
                    // devotional's own range) - it's deliberately NOT capped at
                    // `actualCurrentDay`, so stepping past "today" lands on the preview
                    // banner above instead of being blocked. If this ever needs to change
                    // back to a locked/no-preview-ahead model, cap `nextDay`'s bound at
                    // `actualCurrentDay ?: devotionalMeta.totalDays` instead of
                    // `devotionalMeta.totalDays`, and surface that block explicitly
                    // (e.g. a Toast, matching the old DayStrip's `isUnlocked` behavior)
                    // rather than silently disabling the arrow.
                    val onPreviousDay = (currentDay - 1).takeIf { it >= 1 }
                        ?.let { previousDay -> { navController.navigateToDevotionalDay(devotionalId, previousDay) } }
                    val onNextDay = (currentDay + 1).takeIf { it <= devotionalMeta.totalDays }
                        ?.let { nextDay -> { navController.navigateToDevotionalDay(devotionalId, nextDay) } }

                    PrayerScreen(
                        prayers = list,
                        deviceLanguage = language,
                        dayNumber = currentDay,
                        totalDays = devotionalMeta.totalDays,
                        isPreview = isPreview && !previewBannerDismissed,
                        onDismissPreview = mainViewModel::dismissPreviewBanner,
                        isDayCompleted = currentDay in completedDays,
                        onToggleDayCompleted = {
                            mainViewModel.toggleDayCompleted(devotionalId, currentDay, currentDay in completedDays)
                        },
                        onPreviousDay = onPreviousDay,
                        onNextDay = onNextDay,
                        onStageTitleChange = { title -> onStageTitleUpdated(backStackEntry.id, title) }
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
            val songs by lyricsViewModel.songs.collectAsStateWithLifecycle()
            songs?.let { loadedSongs ->
                SongListScreen(
                    onAction = { songID ->
                        navController.navigate(
                            NavigationScreen.LyricsViewScreen.createRoute(
                                songID
                            )
                        )
                    },
                    songs = loadedSongs,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(
            NavigationScreen.LyricsViewScreen.route,
            arguments = listOf(navArgument("songID") { type = NavType.IntType })
        ) { backStackEntry ->

            val songID = backStackEntry.arguments?.getInt("songID")
            val songs by lyricsViewModel.songs.collectAsStateWithLifecycle()
            // Nothing renders until the list has loaded (only possible when the app is
            // restored straight onto this screen). Once it has, the song is derived from
            // this entry's own nav argument - never from shared "selected song" state -
            // so the previously opened song can't flash on screen (P0-5).
            songs?.let { loadedSongs ->
                val song = remember(loadedSongs, songID) { loadedSongs.find { it.id == songID } }

                if (song != null) {
                    LaunchedEffect(backStackEntry.id, song.title) {
                        onSongTitleUpdated(backStackEntry.id, song.title)
                    }
                    LyricsScreen(lyrics = song.lyrics, artist = song.artist)
                } else {
                    MissingContentMessage(R.string.error_song_unavailable)
                }
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
private fun MissingContentMessage(
    @StringRes message: Int = R.string.error_devotional_content_unavailable
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(message),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
