package com.mirkwood.novenapp.presentation.screens.prayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

/**
 * A plain text page of the day's pager (reflection / prayer without a hero image). The
 * stage name and length now live in [PrayerScreen]'s pinned stage card, so this screen
 * only renders the body text and, when asked, the shared [finalPrayers] footer.
 *
 * @param textScale multiplier applied to the body text size, driven by the A- / A+
 * control in [PrayerScreen]'s toolbar. Narration lives in that toolbar too, so this
 * screen no longer carries its own "Listen" buttons.
 */
@Composable
fun ReadingScreen(
    textContent: String,
    finalPrayers: Boolean = false,
    textScale: Float = 1f,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = textContent,
                style = TextStyle(
                    fontSize = 18.sp * textScale,
                    lineHeight = 28.sp * textScale,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Justify
                ),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (finalPrayers) {
                Spacer(modifier = Modifier.height(8.dp))
                PrayerFooter()
            }
            Spacer(modifier = Modifier.height(24.dp))
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
