package com.mirkwood.novenapp.presentation.screens.lyrics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(songTitle: String, lyrics: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(songTitle) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Guardar en favoritos */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favoritos")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = lyrics,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

@PreviewAllPhones
@Composable
fun PreviewLyricsScreen() {
    NovenAppTheme {

        LyricsScreen(
            onBack = {},
            songTitle = "Noche de paz",
            lyrics = "Noche de paz, noche de amor\n" +
                    "Todo duerme alrededor\n" +
                    "Entre los astros que esparcen su luz\n" +
                    "Bella, anunciando al niño Jesús\n" +
                    "Brilla la estrella de paz\n" +
                    "Brilla la estrella de amor\n" +
                    "\n" +
                    "Noche de paz, noche de luz\n" +
                    "Ha nacido Jesús\n" +
                    "Pastorcillos que oíd anunciar\n" +
                    "No temáis cuando entréis a adorar\n" +
                    "Que ha nacido el amor\n" +
                    "Que ha nacido el amor\n" +
                    "\n" +
                    "Desde el pesebre del niño Jesús\n" +
                    "La Tierra entera se llena de luz\n" +
                    "Porque ha nacido Jesús\n" +
                    "Entre canciones de amor"
        )
    }
}
