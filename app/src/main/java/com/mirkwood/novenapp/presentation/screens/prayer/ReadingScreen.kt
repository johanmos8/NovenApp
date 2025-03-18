package com.mirkwood.novenapp.presentation.screens.prayer

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.ui.theme.NovenAppTheme
import java.util.Locale

@Composable
fun ReadingScreen(
    textContent: String,
) {

    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    val context = LocalContext.current

    fun playNarration() {
        tts?.speak(textContent, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Inicializar Text-to-Speech cuando se carga el Composable
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "CO") // Español de Colombia
            }
        }
    }

    // Función para detener narración
    fun detenerNarracion() {
        tts?.stop()
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            // UI en Jetpack Compose
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(
                        onClick = { playNarration() },
                        colors = ButtonColors(
                            contentColor = MaterialTheme.colorScheme.background,
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = MaterialTheme.colorScheme.onSecondary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSecondaryContainer

                        )
                    ) {
                        Text(
                            "Escuchar Novena",

                            )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { detenerNarracion() },
                        colors = ButtonColors(
                            contentColor = MaterialTheme.colorScheme.background,
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = MaterialTheme.colorScheme.onSecondary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSecondaryContainer

                        )
                    ) {
                        Text(
                            "Detener"
                        )
                    }

                }
            }
            Text(
                text = textContent,
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 28.sp, // Espaciado entre líneas
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Justify // Texto justificado
                ),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground
            )

        }
    }
    DisposableEffect(Unit) {
        onDispose {
            tts?.shutdown()
        }
    }
}

@PreviewAllPhones
@Composable
fun PreviewReadingScreen() {
    NovenAppTheme {
        ReadingScreen("hola")
    }
}