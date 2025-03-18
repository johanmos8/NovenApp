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

    data class AllGozos(val list: List<Gozo>, @DrawableRes val imageRes: Int, val title: String) :
        Prayer()
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
