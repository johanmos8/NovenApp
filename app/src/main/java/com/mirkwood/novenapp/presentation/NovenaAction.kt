package com.mirkwood.novenapp.presentation

internal sealed interface NovenaAction {
    data class GoToDay(val selectedDay: Int) : NovenaAction
    data object GoHome : NovenaAction
}
