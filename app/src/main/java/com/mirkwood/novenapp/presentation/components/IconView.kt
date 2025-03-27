package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.model.MainModule
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
internal fun IconView() {
    var isAEnabled by remember { mutableStateOf(false) }
    var isBEnabled by remember { mutableStateOf(false) }
    var isMusicEnabled by remember { mutableStateOf(false) }
    fun toggleA() {
        isAEnabled = !isAEnabled
        if (isAEnabled) {
            isBEnabled = false // 🔴 Apagar B si A se enciende
            isMusicEnabled = false
        }
    }

    fun toggleB() {
        isBEnabled = !isBEnabled
        if (isBEnabled) {
            isAEnabled = false // 🔴 Apagar A si B se enciende
            isMusicEnabled = false
        }
    }

    fun toggleC() {
        isMusicEnabled = !isMusicEnabled
        if (isMusicEnabled) {
            isAEnabled = false // 🔴 Apagar A si B se enciende
            isBEnabled = false
        }
    }
    if (isAEnabled) {
        InstrumentSound(MainModule.Instrument.Maraca)
    } else if (isBEnabled) {
        InstrumentSound(MainModule.Instrument.Tambourine)
    } else if (isMusicEnabled) {
        InstrumentSound(MainModule.Instrument.Music, isMusicEnabled)
    }
    Row(
        modifier = Modifier
            .padding(end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,

        ) {

        /*
        TODO("definir bien el sonido de los intrumentos")
        CircularIcon(
            R.drawable.icon_maraca,
            isAEnabled,
            onClick = { toggleA() })
        Spacer(modifier = Modifier.width(8.dp))
        CircularIcon(
            R.drawable.icon_tambourine,
            isBEnabled,
            onClick = { toggleB() })
        Spacer(modifier = Modifier.width(8.dp))*/
        CircularIcon(R.drawable.icon_music, isMusicEnabled, { toggleC() })
    }
}

@Composable
fun CircularIcon(iconRes: Int, enabled: Boolean, onClick: () -> Unit) {
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        modifier = Modifier
            .size(48.dp) // Tamaño del icono
            .alpha(if (!enabled) 0.5f else 1f)
            .clip(CircleShape)
            .border(2.dp, Color.White, CircleShape)
            .clickable { onClick() }
    )
}

@PreviewAllPhones
@Composable
fun PreviewIconView() {
    NovenAppTheme {
        IconView()
    }
}
