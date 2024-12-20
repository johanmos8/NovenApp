package com.mirkwood.novenapp.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.mirkwood.novenapp.presentation.MainViewModel
import com.mirkwood.novenapp.presentation.model.Gozo


@Composable
fun GozosScreen(
    gozos: List<Gozo>
) {

    GozosNavigator(gozos)
}