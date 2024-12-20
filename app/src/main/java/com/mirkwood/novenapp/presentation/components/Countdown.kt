package com.mirkwood.novenapp.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@Composable
fun CountdownToDate(targetDate: Long) {
    var remainingTime by remember { mutableStateOf(calculateTimeRemaining(targetDate)) }

    // LaunchedEffect para actualizar el temporizador cada segundo
    LaunchedEffect(key1 = targetDate) {
        while (remainingTime > 0) {
            delay(1000L) // Espera un segundo
            remainingTime = calculateTimeRemaining(targetDate)
        }
    }

    if (remainingTime <= 0) {
        Text(
            text = "¡El tiempo ha llegado!",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    } else {
        val days = TimeUnit.MILLISECONDS.toDays(remainingTime)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingTime) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTime) % 60

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = "Navidad en:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White

            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$days días, $hours horas, $minutes minutos, $seconds segundos",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White

            )
        }
    }
}

// Calcula el tiempo restante en milisegundos
fun calculateTimeRemaining(targetDate: Long): Long {
    return targetDate - System.currentTimeMillis()
}

@Composable
fun CountdownExample() {
    val targetDate = LocalDate.of(2024, 12, 25) // Año, mes, día
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    CountdownToDate(targetDate = targetDate)
}

@Preview
@Composable
fun countdownPreview() {
    CountdownExample()
}