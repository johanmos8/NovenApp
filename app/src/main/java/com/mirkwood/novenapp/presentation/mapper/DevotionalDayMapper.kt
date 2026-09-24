package com.mirkwood.novenapp.presentation.mapper

import com.mirkwood.novenapp.presentation.model.ContentBlockData
import com.mirkwood.novenapp.presentation.model.DevotionalContent
import com.mirkwood.novenapp.presentation.model.DevotionalDayContent
import com.mirkwood.novenapp.presentation.model.Gozo
import com.mirkwood.novenapp.presentation.model.Prayer

/**
 * Turns a devotional's generic content blocks + its catalog artwork into the ordered
 * list of [Prayer] pages the day screen pages through. Generic across any devotional
 * shape - no hardcoded field names or fixed page count, unlike the Christmas-specific
 * assembly this replaced - see specs/generic-devotional-content-model.
 *
 * Page order: the day's own blocks first, then [DevotionalContent.everyDayBlocks],
 * then [DevotionalContent.closingBlocks] - this reproduces the existing Novena de
 * Aguinaldos's page order (reflection, then the 3 daily prayers + Gozos, then the
 * closing prayer to the Child Jesus) without hardcoding that any devotional has to
 * follow the same shape.
 */
internal fun buildDevotionalDayPrayers(
    content: DevotionalContent,
    dayContent: DevotionalDayContent,
    images: Map<String, Int>
): List<Prayer> = buildDayPrayers(dayContent, content.everyDayBlocks, content.closingBlocks, images)

/**
 * The same page order as [buildDevotionalDayPrayers] but text-only: every reading is a
 * [Prayer.Simple] and the litany page carries no hero image. For devotionals that ship
 * without artwork, or a plain-text reading mode where the imagery would only get in the
 * way of the prayer text.
 */
internal fun buildDevotionalDayPrayersWithoutImages(
    content: DevotionalContent,
    dayContent: DevotionalDayContent
): List<Prayer> = buildDayPrayers(dayContent, content.everyDayBlocks, content.closingBlocks, images = null)

private fun buildDayPrayers(
    dayContent: DevotionalDayContent,
    everyDayBlocks: List<ContentBlockData>,
    closingBlocks: List<ContentBlockData>,
    images: Map<String, Int>?
): List<Prayer> {
    val orderedBlocks = dayContent.blocks + everyDayBlocks + closingBlocks
    return orderedBlocks.map { it.toPrayer(images) }
}

private fun ContentBlockData.toPrayer(images: Map<String, Int>?): Prayer {
    val image = imageKey?.let { images?.get(it) }
    return when (type) {
        ContentBlockData.TYPE_LITANY -> Prayer.AllGozos(
            gozos = items.orEmpty().mapIndexed { index, text -> Gozo(id = index + 1, texto = text) },
            imageRes = image
        )
        else -> if (image != null) {
            Prayer.WithImage(text.orEmpty(), image, title, finalPrayers)
        } else {
            Prayer.Simple(text.orEmpty(), title, finalPrayers)
        }
    }
}
