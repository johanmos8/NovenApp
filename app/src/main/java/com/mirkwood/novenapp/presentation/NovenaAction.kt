package com.mirkwood.novenapp.presentation

internal sealed interface NovenaAction {
    data class GoToDay(val devotionalId: String, val selectedDay: Int) : NovenaAction
    data object GoHome : NovenaAction
}
