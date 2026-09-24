package com.mirkwood.novenapp.presentation.screens.lyrics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

/**
 * A carol's lyrics. The title lives in the app bar, and back navigation is the app
 * bar's too, so this only renders the text - with side margins and the theme's
 * typography, so it scales with the system font size.
 */
@Composable
fun LyricsScreen(lyrics: String) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = lyrics,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

@PreviewAllPhones
@Composable
fun PreviewLyricsScreen() {
    NovenAppTheme {

        LyricsScreen(
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
