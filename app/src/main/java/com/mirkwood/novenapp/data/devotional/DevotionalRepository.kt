package com.mirkwood.novenapp.data.devotional

import com.mirkwood.novenapp.presentation.model.Novena
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta

/**
 * Single point of access to "which devotionals exist" and "what's their content".
 * Replaces [com.mirkwood.novenapp.presentation.MainViewModel] previously reaching
 * directly into a single hardcoded asset file - adding a devotional means adding a
 * catalog entry (see [com.mirkwood.novenapp.presentation.model.devotional.DevotionalCatalog])
 * and a content JSON file, not changing this contract.
 */
interface DevotionalRepository {
    fun getCatalog(): List<DevotionalMeta>
    fun getDevotional(id: String): DevotionalMeta
    fun loadContent(devotionalId: String, language: String): Novena?
}
