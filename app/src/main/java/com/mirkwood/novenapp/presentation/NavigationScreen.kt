package com.mirkwood.novenapp.presentation

sealed class NavigationScreen(val route: String) {
    object HomeScreen : NavigationScreen("home")
    object DayScreen : NavigationScreen("day/{position}") {
        fun createRoute(position: Int): String = "day/$position"
        const val ARG_POSITION = "position"
    }
}
