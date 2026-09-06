package com.mirkwood.novenapp.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mirkwood.novenapp.R
import com.mirkwood.novenapp.presentation.model.Novena
import com.mirkwood.novenapp.presentation.model.NovenaTab
import com.mirkwood.novenapp.presentation.model.Prayer
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta

/**
 * Turns a devotional's raw JSON content + its catalog artwork into the ordered list of
 * [Prayer] pages the day screen pages through. Used to live inline in `AppNavHost`'s
 * `DayScreen` composable, hardcoding one novena's drawables and titles directly in the
 * nav graph; pulling it out here means the nav graph stays generic across devotionals.
 */
@Composable
internal fun buildDevotionalDayPrayers(
    content: Novena,
    devotional: DevotionalMeta,
    currentDay: Int
): List<Prayer> {
    val images = devotional.images
    return listOf(
        Prayer.WithImage(
            content.dias[currentDay - 1].reflexion,
            images.dailyReflection,
            stringResource(R.string.text_consideration, currentDay)
        ),
        Prayer.WithImage(
            content.general.oracion_todos_los_dias,
            images.dailyPrayer,
            stringResource(NovenaTab.OracionTodosLosDias.titleResId),
            true
        ),
        Prayer.WithImage(
            content.general.oracion_virgen_maria,
            images.virgenMaria,
            stringResource(NovenaTab.OracionALaVirgen.titleResId),
            true
        ),
        Prayer.WithImage(
            content.general.oracion_san_jose,
            images.sanJose,
            stringResource(NovenaTab.OracionSanJose.titleResId),
            true
        ),
        Prayer.AllGozos(
            content.gozos,
            images.gozos,
            stringResource(NovenaTab.Gozos.titleResId)
        ),
        Prayer.WithImage(
            content.general.oracion_niño_jesus,
            images.ninoJesus,
            stringResource(NovenaTab.OracionAJesus.titleResId),
            true
        )
    )
}
