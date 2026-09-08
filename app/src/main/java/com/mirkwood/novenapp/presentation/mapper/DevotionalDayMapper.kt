package com.mirkwood.novenapp.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.model.Dia
import com.mirkwood.novenapp.presentation.model.Novena
import com.mirkwood.novenapp.presentation.model.NovenaTab
import com.mirkwood.novenapp.presentation.model.Prayer
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalImages
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta

/**
 * Turns a devotional's raw JSON content + its catalog artwork into the ordered list of
 * [Prayer] pages the day screen pages through. Used to live inline in `AppNavHost`'s
 * `DayScreen` composable, hardcoding one novena's drawables and titles directly in the
 * nav graph; pulling it out here means the nav graph stays generic across devotionals.
 *
 * Takes the already-resolved [dayContent] rather than indexing `content.dias` itself,
 * so an out-of-range day (a stale deep link, or a catalog `totalDays` that drifts from
 * the JSON's actual day count) is the caller's bounds-check to make, not a crash buried
 * in here.
 */
@Composable
internal fun buildDevotionalDayPrayers(
    content: Novena,
    dayContent: Dia,
    devotional: DevotionalMeta,
    currentDay: Int
): List<Prayer> = buildDayPrayers(content, dayContent, currentDay, devotional.images)

/**
 * The same page order as [buildDevotionalDayPrayers] but text-only: every reading is a
 * [Prayer.Simple] and the Gozos page carries no hero image. For devotionals that ship
 * without artwork, or a plain-text reading mode where the imagery would only get in the
 * way of the prayer text.
 */
@Composable
internal fun buildDevotionalDayPrayersWithoutImages(
    content: Novena,
    dayContent: Dia,
    currentDay: Int
): List<Prayer> = buildDayPrayers(content, dayContent, currentDay, images = null)

/**
 * Shared page assembly for both builders. When [images] is null each reading becomes a
 * [Prayer.Simple] instead of a [Prayer.WithImage], and the Gozos page drops its hero.
 */
@Composable
private fun buildDayPrayers(
    content: Novena,
    dayContent: Dia,
    currentDay: Int,
    images: DevotionalImages?
): List<Prayer> {
    fun reading(
        text: String,
        image: Int?,
        title: String,
        finalPrayers: Boolean = false
    ): Prayer = if (image != null) {
        Prayer.WithImage(text, image, title, finalPrayers)
    } else {
        Prayer.Simple(text, title, finalPrayers)
    }

    return buildList {
        add(
            reading(
                dayContent.reflexion,
                images?.dailyReflection,
                stringResource(R.string.text_consideration, currentDay)
            )
        )
        add(
            reading(
                content.general.oracion_todos_los_dias,
                images?.dailyPrayer,
                stringResource(NovenaTab.OracionTodosLosDias.titleResId),
                true
            )
        )
        add(
            reading(
                content.general.oracion_virgen_maria,
                images?.virgenMaria,
                stringResource(NovenaTab.OracionALaVirgen.titleResId),
                true
            )
        )
        add(
            reading(
                content.general.oracion_san_jose,
                images?.sanJose,
                stringResource(NovenaTab.OracionSanJose.titleResId),
                true
            )
        )
        add(Prayer.AllGozos(content.gozos, images?.gozos))
        add(
            reading(
                content.general.oracion_niño_jesus,
                images?.ninoJesus,
                stringResource(NovenaTab.OracionAJesus.titleResId),
                true
            )
        )
    }
}
