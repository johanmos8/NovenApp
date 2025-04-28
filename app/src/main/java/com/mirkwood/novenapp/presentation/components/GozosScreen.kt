package com.mirkwood.novenapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mirkwood.compose_preview.PreviewAllPhones
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.model.Gozo
import com.mirkwood.novenapp.presentation.model.MainModule
import com.mirkwood.novenapp.ui.theme.NovenAppTheme


@Composable
internal fun GozosScreen(
    gozos: List<Gozo>,
    image: MainModule.Hero
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    if (screenWidthDp < 600) {
        PhoneLayout(
            gozos, image
        )
    } else {
        TabletLayout(gozos, image)
    }

}

@Composable
internal fun TabletLayout(
    gozos: List<Gozo>,
    image: MainModule.Hero
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HeroImage(
            header = image,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f, fill = false)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.gozos_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    IconView()
                }
                GozosNavigator(gozos)
            }
        }
    }

}

@Composable
internal fun PhoneLayout(
    gozos: List<Gozo>,
    image: MainModule.Hero
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        HeroImage(
            header = image,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-48).dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = stringResource(R.string.gozos_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    IconView()
                }
                GozosNavigator(gozos)
            }
        }
    }
}

@PreviewAllPhones
@Composable
fun PreviewGozosScreen() {
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
        GozosScreen(
            gozos = gozos,
            image = MainModule.Hero(
                imageResId = R.drawable.virgen_maria,
                height = MainModule.Hero.Height.Medium
            )
        )
    }
}