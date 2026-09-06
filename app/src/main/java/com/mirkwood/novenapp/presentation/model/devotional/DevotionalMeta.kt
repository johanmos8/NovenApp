package com.mirkwood.novenapp.presentation.model.devotional

import androidx.annotation.StringRes

/**
 * Catalog entry for one devotional (a novena, and in the future a rosary, a
 * saint-of-the-day feature, etc). This is the unit [com.mirkwood.novenapp.data.devotional.DevotionalRepository]
 * loads content for and the unit navigation routes are keyed by.
 */
data class DevotionalMeta(
    val id: String,
    @StringRes val titleResId: Int,
    val contentFileEs: String,
    val contentFileEn: String,
    val images: DevotionalImages,
    val schedule: DevotionalSchedule,
    val totalDays: Int
)
