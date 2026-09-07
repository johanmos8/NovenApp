package com.mirkwood.novenapp.presentation.navigation

sealed class NavigationScreen(val route: String) {
    object HomeScreen : NavigationScreen("home")
    object DayScreen : NavigationScreen("devotional/{devotionalId}/day/{position}") {
        fun createRoute(devotionalId: String, position: Int): String =
            "devotional/$devotionalId/day/$position"

        const val ARG_DEVOTIONAL_ID = "devotionalId"
        const val ARG_POSITION = "position"
    }

    object LyricsScreen : NavigationScreen("lyrics")
    object LyricsViewScreen : NavigationScreen("lyricsview/{songID}"){
        fun createRoute(songID: Int): String = "lyricsview/$songID"
        const val ARG_POSITION = "songID"
    }
    object MoreScreen : NavigationScreen("more")
    object AboutUsScreen : NavigationScreen("about")

}
