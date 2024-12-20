package com.mirkwood.novenapp.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.mirkwood.novenapp.presentation.NavigationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovenaTopAppBar(
    navController: NavController,
    showBackButton: Boolean = false
) {
    TopAppBar(
        title = { Text(text = "Novena de Aguinaldos") },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = {
                    // Acción para regresar a la pantalla principal (Home)
                    navController.navigate(NavigationScreen.HomeScreen.route) // Regresar a Home
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            }
        }
    )
}
