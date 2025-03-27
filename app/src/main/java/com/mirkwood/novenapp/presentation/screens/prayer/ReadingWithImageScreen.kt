package com.mirkwood.novenapp.presentation.screens.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.components.HeroImage
import com.mirkwood.novenapp.presentation.model.MainModule
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

@Composable
internal fun ReadingWithImageScreen(
    title: String,
    textContent: String,
    image: MainModule.Hero,
    finalPrayers: Boolean = false
) {

    //val scrollState = rememberScrollState()
    val scrollState = rememberLazyListState()

    val screenHeight =
        LocalConfiguration.current.screenHeightDp.dp // Obtiene el alto de la pantalla
    val imageHeight = screenHeight * 0.5f // La imagen ocupa el 40% de la pantalla
    val minImageHeight = imageHeight * 0.3f // Se colapsará hasta el 30% de su altura inicial

    Box(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        LazyColumn(
            state = scrollState,
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                HeroImage(
                    header = image,
                    modifier = Modifier
                        .graphicsLayer {
                            val collapseFraction =
                                (scrollState.firstVisibleItemScrollOffset.toFloat() / imageHeight.toPx())
                                    .coerceIn(0f, 1f)
                            val newHeight =
                                imageHeight.toPx() - (collapseFraction * (imageHeight.toPx() - minImageHeight.toPx()))
                            this.alpha =
                                1f - (collapseFraction * 0.3f) // Opcional: cambia la opacidad
                            this.translationY =
                                -collapseFraction * (imageHeight.toPx() - minImageHeight.toPx()) / 2
                            this.scaleX =
                                1f - (collapseFraction * 0.1f) // Opcional: ajusta el tamaño al colapsar
                            this.scaleY = 1f - (collapseFraction * 0.1f)
                        }
                        .height(imageHeight)

                )
            }
            item {


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-32).dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(Modifier.height(16.dp))
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
                        if (finalPrayers) {
                            PrayerFooter()
                        }
                    }
                }
            }
        }
    }

}

@PreviewAllPhones
@Composable
internal fun PreviewReadingImageScreen() {
    NovenAppTheme {
        val content =
            "Soberana Maria, que por vuestras grandes virtudes principalmente por tu humildad, mereciste que todo un Dios se hiciese hombre en tu sagrado vientre; infinitas gracias doi al mismo Señor, porque te escojió por madre y para tan alta dignidad te adornó con infinitos dones de naturaleza y gracia; suplícote, Señora, nos alcanceis ardientes deseos de ver al hermoso niño, y heroicas virtudes para recibirlo en nuestras almas, por aquellos actos de amor con que diste á luz el fruto de tu vientre, y por la dulce ternura que sintió tu alma, al tenerlo en tus brazos, aplicarlo á tus pechos, besarle su divino rostro, y adorarlo como á vuestro Dios y vuestro Hijo. Amen.\n\nNueve Ave Marias en memoria de los nueve meses que la Divina Reyna tuvo al Hijo de Dios en su purísimo Vientre. "

        ReadingWithImageScreen(
            "Oracion",
            content, MainModule.Hero(
                R.drawable.virgen_maria,
                height = MainModule.Hero.DefaultHeight
            )
        )
    }
}

