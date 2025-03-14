package com.mirkwood.novenapp.presentation.model

import androidx.annotation.DrawableRes


sealed class Prayer {
    data class Simple(val text: String, val title: String) : Prayer()
    data class WithImage(val text: String, @DrawableRes val imageRes: Int, val title: String) : Prayer()
    data class AllGozos(val list: List<Gozo>, @DrawableRes val imageRes: Int, val title: String) : Prayer()
}
