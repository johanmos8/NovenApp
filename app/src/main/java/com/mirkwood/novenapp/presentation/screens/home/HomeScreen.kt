package com.mirkwood.novenapp.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.mirkwood.novenapp.presentation.util.TARGET_DAY
import com.mirkwood.novenapp.presentation.util.TARGET_MONTH
import java.time.LocalDate

@Composable
internal fun HomeScreen(
    viewState: NovenaViewState,
    onEvent: (NovenaAction) -> Unit
) {
    val currentDate = remember { LocalDate.now() }
    val isChristmas = currentDate.monthValue == TARGET_MONTH && currentDate.dayOfMonth == TARGET_DAY

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isChristmas) {
            CelebrationAnimation()
        } else {
            LottieAnimationRender(R.raw.animation_snow_falling)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            if (!isChristmas) {
                MainTitle()
                CountdownToDate()
            }
            viewState.currentDay?.let {
                GoToDayButton(
                    currentDay = viewState.currentDay,
                    onAction = onEvent
                )
            }
        }
    }
}

@Composable
internal fun CelebrationAnimation() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Centra el letrero
    ) {
        // Animación de globos en diferentes posiciones
        LottieAnimationRender(R.raw.animation_celebration, Modifier.offset(x = 50.dp, y = 100.dp))

        // Animación del letrero en el centro
        LottieAnimationRender(R.raw.animation_merry)
    }
}


@Composable
internal fun LottieAnimationRender(animationRes: Int, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever, // Para que sea infinita
        modifier = Modifier.fillMaxSize() // Ajusta el tamaño según necesites
    )
}
