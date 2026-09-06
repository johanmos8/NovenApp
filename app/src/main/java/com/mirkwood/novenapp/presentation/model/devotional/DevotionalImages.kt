package com.mirkwood.novenapp.presentation.model.devotional

import androidx.annotation.DrawableRes

/**
 * The artwork a devotional's day screen is built from. Kept as data on [DevotionalMeta]
 * instead of hardcoded drawable references in the navigation graph, so a new devotional
 * only needs a catalog entry, not a code branch.
 */
data class DevotionalImages(
    @DrawableRes val cover: Int,
    @DrawableRes val dailyReflection: Int,
    @DrawableRes val dailyPrayer: Int,
    @DrawableRes val virgenMaria: Int,
    @DrawableRes val sanJose: Int,
    @DrawableRes val gozos: Int,
    @DrawableRes val ninoJesus: Int
)
