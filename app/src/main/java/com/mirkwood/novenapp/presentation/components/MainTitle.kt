package com.mirkwood.novenapp.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MainTitle() {
    Text(
        text = "Novena de Aguinaldos",
        style = MaterialTheme.typography.titleLarge,
        color = Color.White
    )
}