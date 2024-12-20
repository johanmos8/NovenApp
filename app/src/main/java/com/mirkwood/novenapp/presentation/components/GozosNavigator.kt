package com.mirkwood.novenapp.presentation.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mirkwood.novenapp.presentation.model.Gozo
import androidx.compose.material3.Text

@Composable
fun GozosNavigator(gozos: List<Gozo>) {
    var currentIndex by remember { mutableStateOf(0) }
    val currentGozo: Gozo = gozos.get(currentIndex)
    if (gozos.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = currentGozo.texto,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if (currentIndex > 0) currentIndex--
                },
                enabled = currentIndex > 0
            ) {
                Text(text = "Anterior")
            }

            Button(
                onClick = {
                    if (currentIndex < gozos.size - 1) currentIndex++
                },
                enabled = currentIndex < gozos.size - 1
            ) {
                Text(text = "Siguiente")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                currentIndex = 0
            },
            enabled = currentIndex != 0

            ) {
            Text(text = "Ir al principio")
        }
    }
}
