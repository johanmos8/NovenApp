package com.mirkwood.novenapp.presentation.screens.prayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.model.MainPrayer
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
fun PrayerDialog(
    prayer: MainPrayer,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(prayer.titleRes), fontWeight = FontWeight.Bold) },
        text = {
            Box(modifier = Modifier.wrapContentHeight()) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(text = stringResource(prayer.contentRes), fontSize = 18.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@PreviewAllPhones
@Composable
fun PreviewPrayerDialog() {
    NovenAppTheme {
        PrayerDialog(
            prayer = MainPrayer.HailMary,
            onDismiss = { }
        )
    }
}
