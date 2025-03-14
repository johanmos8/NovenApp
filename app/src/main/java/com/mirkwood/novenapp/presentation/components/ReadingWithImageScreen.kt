package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.model.MainModule
import com.mirkwood.novenapp.ui.theme.NovenAppTheme

/*@Composable
internal fun ReadingWithImageScreen(
    textContent: String,
    image: MainModule.Hero
) {

    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var expanded by remember { mutableStateOf(false) }
    val height by animateDpAsState(
        targetValue = if (expanded) 600.dp else 250.dp,
        label = "ExpandOnScroll"
    )
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y > 0) { // Scroll UP
                    expanded = true
                } else if (available.y < 0) { // Scroll DOWN
                    expanded = false
                }
                return Offset.Zero
            }
        }
    }
    /*
        // Inicializar Text-to-Speech cuando se carga el Composable
        LaunchedEffect(Unit) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale("es", "CO") // Español de Colombia
                }
            }
        }

        // Función para detener narración
        fun stopSpeech() {
            tts?.stop()
        }

        fun playSpeech() {
            tts?.speak(textContent, TextToSpeech.QUEUE_FLUSH, null, null)
        }*/
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            HeroImage(
                header = image,
                modifier = Modifier.graphicsLayer {
                    translationY =
                        -scrollState.value.toFloat() * 0.5f
                    alpha = (1f - (scrollState.value / 500f)).coerceIn(0.3f, 1f)
                }
            )


            // UI en Jetpack Compose
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .nestedScroll(nestedScrollConnection)
                    .offset(y = (-8).dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .height(height)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(4.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    /* Row(
                         horizontalArrangement = Arrangement.SpaceBetween
                     ) {
                         Button(
                             onClick = { playSpeech() },
                             colors = ButtonDefaults.buttonColors(
                                 containerColor = MaterialTheme.colorScheme.primary,
                                 contentColor = MaterialTheme.colorScheme.onPrimary
                             )
                         ) {
                             Text(
                                 "Escuchar Novena",

                                 )
                         }
                         Button(
                             onClick = { stopSpeech() },
                             colors = ButtonDefaults.buttonColors(
                                 containerColor = MaterialTheme.colorScheme.primary,
                                 contentColor = MaterialTheme.colorScheme.onPrimary
                             )
                         ) {
                             Text(
                                 "Detener"
                             )
                         }

                     }*/
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
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            tts?.shutdown()
        }
    }
}
*/
@Composable
internal fun ReadingWithImageScreen(title: String, textContent: String, image: MainModule.Hero) {

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        HeroImage(
            header = image,


            )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-32).dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(scrollState)
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