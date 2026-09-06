package com.mirkwood.novenapp.data.devotional

import android.content.Context
import com.google.gson.Gson
import com.mirkwood.novenapp.presentation.model.Novena
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalCatalog
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta

class DevotionalRepositoryImpl(context: Context) : DevotionalRepository {

    private val appContext = context.applicationContext

    override fun getCatalog(): List<DevotionalMeta> = DevotionalCatalog.all

    override fun getDevotional(id: String): DevotionalMeta = DevotionalCatalog.findById(id)

    override fun loadContent(devotionalId: String, language: String): Novena? {
        val meta = getDevotional(devotionalId)
        val fileName = if (language == "en") meta.contentFileEn else meta.contentFileEs
        val json = readAsset(fileName) ?: return null
        return Gson().fromJson(json, Novena::class.java)
    }

    private fun readAsset(fileName: String): String? = try {
        appContext.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ex: Exception) {
        null
    }
}
