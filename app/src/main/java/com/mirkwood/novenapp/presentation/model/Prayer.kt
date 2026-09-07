package com.mirkwood.novenapp.presentation.model

import androidx.annotation.DrawableRes
import com.mirkwood.novenapp.R


sealed class Prayer {
    data class Simple(val text: String, val title: String, val finalPrayers: Boolean = false) :
        Prayer()

    data class WithImage(
        val text: String,
        @DrawableRes val imageRes: Int,
        val title: String,
        val finalPrayers: Boolean = false
    ) :
        Prayer()

    /**
     * All of the Gozos verses, as one scrollable page of the day's [outer pager][
     * com.mirkwood.novenapp.presentation.screens.prayer.PrayerScreen] - a Gozos card
     * is a short call-and-response litany meant to be read straight through, not
     * paged one verse per full-screen swipe.
     */
    data class AllGozos(
        val gozos: List<Gozo>,
        @DrawableRes val imageRes: Int
    ) : Prayer()
}

sealed interface MainPrayer {
    val titleRes: Int
    val contentRes: Int

    data object OurFather : MainPrayer {
        override val titleRes = R.string.our_father_title
        override val contentRes = R.string.our_father
    }

    data object HailMary : MainPrayer {
        override val titleRes = R.string.hail_mary_title
        override val contentRes = R.string.hail_mary
    }

    data object GloryBe : MainPrayer {
        override val titleRes = R.string.glory_be_title
        override val contentRes = R.string.glory_be
    }

    data object DivineChild : MainPrayer {
        override val titleRes = R.string.divine_child_title
        override val contentRes = R.string.divine_child
    }
}
