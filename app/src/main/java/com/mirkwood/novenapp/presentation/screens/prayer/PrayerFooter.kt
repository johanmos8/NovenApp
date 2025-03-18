package com.mirkwood.novenapp.presentation.screens.prayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.presentation.model.MainPrayer
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
internal fun PrayerFooter() {
    val list = listOf(
        MainPrayer.OurFather,
        MainPrayer.HailMary,
        MainPrayer.GloryBe,
        MainPrayer.DivineChild
    )
    var openDialog by remember { mutableStateOf(false) }
    var selectedPrayer by remember { mutableStateOf<MainPrayer?>(null) }

    val ctx = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        list.forEach { prayer ->
            TextButton(
                onClick = {
                    selectedPrayer = prayer  // Guardar la oración seleccionada
                    openDialog = true       // Mostrar el diálogo
                }
            ) {
                Text(
                    text = stringResource(prayer.titleRes),
                )
            }

        }
        if (openDialog && selectedPrayer != null) {
            PrayerDialog(prayer = selectedPrayer!!, onDismiss = { openDialog = false })
        }
    }
}

@PreviewAllPhones
@Composable
internal fun PreviewPrayerFooter() {
    NovenAppTheme {
        PrayerFooter()
    }
}