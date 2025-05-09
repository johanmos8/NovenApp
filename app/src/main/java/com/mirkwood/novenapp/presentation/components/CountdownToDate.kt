package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.util.Util.calculateTimeRemaining
import com.mirkwood.novenapp.ui.theme.NovenAppTheme
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@Composable
fun CountdownToDate() {

    var remainingTime by remember { mutableStateOf(calculateTimeRemaining()) }

    LaunchedEffect(key1 = remainingTime) {
        while (remainingTime > 0) {
            delay(1000L) // Espera un segundo
            remainingTime = calculateTimeRemaining()
        }
    }

    if (remainingTime > 0) {

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
                text = stringResource(R.string.text_navidad_en),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.text_navidad_en_yy_mm_dd,
                    days,
                    hours,
                    minutes,
                    seconds
                ),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}


@PreviewAllPhones
@Composable
fun CountdownPreview() {
    NovenAppTheme {
        Surface(
            color = MaterialTheme.colorScheme.background // Usa color del theme

        ) {
            CountdownToDate()
        }
    }
}
