package com.mirkwood.novenapp.presentation.model.devotional

import android.util.Log
import com.mirkwood.novenapp.R

/**
 * The list of devotionals the app can present. Ships with a single entry today (the
 * Novena de Aguinaldos), but adding another devotional in the future is meant to be
 * "add a catalog entry + JSON content file", not "branch the navigation graph" —
 * see [com.mirkwood.novenapp.data.devotional.DevotionalRepository] and
 * [com.mirkwood.novenapp.presentation.mapper.buildDevotionalDayPrayers].
 */
object DevotionalCatalog {

    val novenaDeAguinaldos = DevotionalMeta(
        id = "novena_aguinaldos",
        titleResId = R.string.novena_aguinaldos_title,
        contentFileEs = "content_es.json",
        contentFileEn = "content_en.json",
        images = DevotionalImages(
            cover = R.drawable.novena,
            dailyReflection = R.drawable.novena,
            dailyPrayer = R.drawable.pesebre,
            virgenMaria = R.drawable.virgen_maria,
            sanJose = R.drawable.san_jose,
            gozos = R.drawable.novena_music,
            ninoJesus = R.drawable.baby_jesus
        ),
        schedule = DevotionalSchedule.FixedRange(
            startMonth = 12,
            startDay = 16,
            endMonth = 12,
            endDay = 24,
            celebrationMonth = 12,
            celebrationDay = 25
        ),
        totalDays = 9
    )

    val all: List<DevotionalMeta> = listOf(novenaDeAguinaldos)

    val default: DevotionalMeta get() = all.first()

    fun findById(id: String): DevotionalMeta {
        val match = all.firstOrNull { it.id == id }
        if (match == null) {
            Log.w("DevotionalCatalog", "No devotional found for id=\"$id\"; falling back to \"${default.id}\"")
        }
        return match ?: default
    }
}
