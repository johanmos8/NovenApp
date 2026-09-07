package com.mirkwood.novenapp.data.devotional

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.mirkwood.novenapp.presentation.model.Novena
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalCatalog
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta

private const val TAG = "DevotionalRepository"

class DevotionalRepositoryImpl(context: Context) : DevotionalRepository {

    private val appContext = context.applicationContext

    override fun getCatalog(): List<DevotionalMeta> = DevotionalCatalog.all

    override fun getDevotional(id: String): DevotionalMeta = DevotionalCatalog.findById(id)

    override fun loadContent(devotionalId: String, language: String): Novena? {
        val meta = getDevotional(devotionalId)
        val fileName = if (language == "en") meta.contentFileEn else meta.contentFileEs
        val json = readAsset(fileName) ?: return null
        return try {
            Gson().fromJson(json, Novena::class.java)
        } catch (ex: JsonSyntaxException) {
            Log.e(TAG, "Malformed content asset \"$fileName\" for devotional \"$devotionalId\"", ex)
            null
        }
    }

    private fun readAsset(fileName: String): String? = try {
        appContext.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ex: Exception) {
        Log.e(TAG, "Failed to read content asset \"$fileName\"", ex)
        null
    }
}
