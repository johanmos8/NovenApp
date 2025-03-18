package com.mirkwood.novenapp.presentation.navigation

sealed class NavigationScreen(val route: String) {
    object HomeScreen : NavigationScreen("home")
    object DayScreen : NavigationScreen("day/{position}") {
        fun createRoute(position: Int): String = "day/$position"
        const val ARG_POSITION = "position"
    }

    object LyricsScreen : NavigationScreen("lyrics")
    object LyricsViewScreen : NavigationScreen("lyricsview/{songID}"){
        fun createRoute(songID: Int): String = "lyricsview/$songID"
        const val ARG_POSITION = "songID"
    }

}
