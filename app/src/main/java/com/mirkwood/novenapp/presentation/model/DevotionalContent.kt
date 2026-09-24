package com.mirkwood.novenapp.presentation.model

/**
 * A devotional's content, generic across any devotional shape - a bounded novena
 * (Christmas, or any other saint's) or a rotating/evergreen daily devotional alike.
 * [everyDayBlocks] repeat on every day (e.g. a prayer said every day of a novena),
 * [days] holds each day's own content in order, and [closingBlocks] repeat after every
 * day's own content (e.g. a final prayer). See specs/generic-devotional-content-model
 * for why this replaced the old Christmas-specific `Novena`/`Dia`/`General` shape.
 */
data class DevotionalContent(
    val everyDayBlocks: List<ContentBlockData> = emptyList(),
    val days: List<DevotionalDayContent>,
    val closingBlocks: List<ContentBlockData> = emptyList()
)

data class DevotionalDayContent(
    val day: Int,
    val title: String,
    val blocks: List<ContentBlockData>
)

/**
 * One typed, ordered piece of content. [type] is a plain string discriminator (not a
 * sealed class) so Gson can deserialize it directly with no custom type adapter - see
 * [com.mirkwood.novenapp.presentation.mapper.buildDevotionalDayPrayers] for how each
 * type maps to a renderable [Prayer]. [imageKey] is looked up in the devotional's own
 * `DevotionalMeta.images` map rather than a hardcoded field name.
 */
data class ContentBlockData(
    val type: String,
    val title: String,
    val text: String? = null,
    val imageKey: String? = null,
    val items: List<String>? = null,
    val finalPrayers: Boolean = false
) {
    companion object {
        const val TYPE_READING = "reading"
        const val TYPE_LITANY = "litany"
    }
}
