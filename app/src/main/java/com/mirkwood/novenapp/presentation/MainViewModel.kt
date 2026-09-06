package com.mirkwood.novenapp.presentation

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.mirkwood.novenapp.data.devotional.DevotionalRepository
import com.mirkwood.novenapp.presentation.model.Novena
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta
import com.mirkwood.novenapp.presentation.navigation.NavigationScreen
import com.mirkwood.novenapp.presentation.state.NovenaViewState
import com.mirkwood.novenapp.presentation.util.Util
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Drives whichever devotional is active. Today that's always [DevotionalRepository]'s
 * first catalog entry (the Novena de Aguinaldos) — [activeDevotionalId] exists so a
 * future "pick a devotional" screen has somewhere to plug in without touching this
 * class's public shape again.
 */
internal class MainViewModel(
    private val repository: DevotionalRepository,
    private val activeDevotionalId: String = repository.getCatalog().first().id
) : ViewModel() {

    private val _state = MutableStateFlow(NovenaViewState())
    val state: StateFlow<NovenaViewState> = _state.asStateFlow()

    val activeDevotional: DevotionalMeta
        get() = repository.getDevotional(activeDevotionalId)

    init {
        refreshCurrentDay()
    }

    fun getContent(language: String): Novena? = repository.loadContent(activeDevotionalId, language)

    fun refreshCurrentDay(): Int? {
        val value = Util.resolveCurrentDay(activeDevotional.schedule)
        _state.value = _state.value.copy(currentDay = value)
        return value
    }

    fun onAction(action: NovenaAction, navController: NavController) {
        when (action) {
            NovenaAction.GoHome -> {
                navController.navigate(NavigationScreen.HomeScreen.route)
            }

            is NovenaAction.GoToDay -> {
                navController.navigate(
                    NavigationScreen.DayScreen.createRoute(activeDevotionalId, action.selectedDay)
                )
            }
        }
    }
}
