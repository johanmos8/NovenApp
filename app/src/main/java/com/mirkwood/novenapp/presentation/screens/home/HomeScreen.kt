package com.mirkwood.novenapp.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.NovenaAction
import com.mirkwood.novenapp.presentation.components.CountdownToDate
import com.mirkwood.novenapp.presentation.components.GoToDayButton
import com.mirkwood.novenapp.presentation.components.MainTitle
import com.mirkwood.novenapp.presentation.state.NovenaViewState

@Composable
internal fun HomeScreen(
    viewState: NovenaViewState,
    onEvent: (NovenaAction) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SnowfallAnimation()
        /*Image(
            painter = painterResource(id = R.drawable.novena3),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            contentDescription = "novena"
        )*/
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            MainTitle()
            CountdownToDate()
            viewState.currentDay?.let {
                GoToDayButton(
                    currentDay = 1,
                    onAction = onEvent
                )
            }
        }
    }
}

@Composable
fun SnowfallAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_snow_falling))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever, // Para que sea infinita
        modifier = Modifier.fillMaxSize() // Ajusta el tamaño según necesites
    )
}