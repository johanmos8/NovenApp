package com.mirkwood.novenapp.presentation.model

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mirkwood.novenapp.R

@Immutable
internal sealed interface MainModule {

    @Immutable
    data class Hero(
        @DrawableRes val imageResId: Int,
        val height: Height,
    ) {

        sealed class Height(val valueDp: Dp) {
            data object Small : Height(180.dp)
            data object Medium : Height(240.dp)
            data object Large : Height(320.dp)
            data class Custom(val customValue: Dp) : Height(customValue)
        }

        companion object {
            internal val DefaultHeight = Height.Small
        }
    }

    sealed class Instrument(@RawRes val soundRes: Int) {
        object Tambourine : Instrument(R.raw.sonido_pandereta)
        object Maraca : Instrument(R.raw.sonido_maraca)
        object Music : Instrument(R.raw.sonido_maraca)

    }
}