package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.model.Gozo
import com.mirkwood.novenapp.presentation.model.MainModule
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
fun GozosNavigator(gozos: List<Gozo>, modifier: Modifier = Modifier) {
    var currentIndex by remember { mutableStateOf(0) }
    val currentGozo: Gozo = gozos.get(currentIndex)
    val gozosSize = gozos.size
    if (gozos.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = currentGozo.texto,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,


            )

        Text(
            text = "${currentIndex + 1}/$gozosSize",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(
                onClick = { if (currentIndex > 0) currentIndex-- },
                enabled = currentIndex > 0
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.text_anterior)
                )
            }

            IconButton(
                onClick = { if (currentIndex < gozos.size - 1) currentIndex++ },
                enabled = currentIndex < gozos.size - 1
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.text_siguiente)
                )
            }
        }

        IconButton(
            onClick = { currentIndex = 0 },
            enabled = currentIndex != 0
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.text_ir_al_principio)
            )
        }
    }
}
@PreviewAllPhones
@Composable
fun PreviewGozosNavigator() {
    NovenAppTheme {
        val gozos =
            listOf(

                Gozo(
                    id = 1,
                    texto = "¡Oh, Sapiencia suma del Dios soberano, que a infantil alcance te rebajas sacro! ¡Oh, Divino Niño, ven para enseñarnos la prudencia que hace verdaderos sabios! (Villancico 'Ven a nuestras almas')"
                ),
                Gozo(
                    id = 2,
                    texto = "¡Oh, Adonai potente que Moisés hablando, de Israel al pueblo diste los mandatos! ¡Ah, ven prontamente para rescatarnos, y que un niño débil muestre fuerte el brazo! (Villancico 'Ven a nuestras almas')"
                ),
            )
        GozosNavigator(
            gozos = gozos,
        )
    }
}