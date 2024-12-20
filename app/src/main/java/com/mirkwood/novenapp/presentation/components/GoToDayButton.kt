package com.mirkwood.novenapp.presentation.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.navigation.NavHostController
import com.mirkwood.novenapp.presentation.MainViewModel
import com.mirkwood.novenapp.presentation.NavigationScreen

@Composable
fun GoToDayButton(
   navHostController: NavHostController,
    mainViewModel: MainViewModel
) {
    val currentDay = mainViewModel.getNovenaDay()
    val onClick = {navHostController.navigate(NavigationScreen.DayScreen.createRoute(currentDay!!))}
    Button(onClick = onClick) {
        Text(
            text = "Ir al día $currentDay",
        )
    }
}