package com.mirkwood.novenapp.data.devotional

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.mirkwood.novenapp.presentation.model.DevotionalContent
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalCatalog
import com.mirkwood.novenapp.presentation.model.devotional.DevotionalMeta
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "DevotionalRepository"

/**
 * The language devotional content is actually served in for a given device language:
 * English content for English devices, Spanish for everything else. Shared with the
 * prayer narrator so it reads the text aloud in the same language it's written in.
 */
internal fun contentLanguageFor(deviceLanguage: String): String =
    if (deviceLanguage == "en") "en" else "es"

class DevotionalRepositoryImpl(context: Context) : DevotionalRepository {

    private val appContext = context.applicationContext
    private val gson = Gson()

    /**
     * Parsed content by asset file name. The assets never change while the app runs, so
     * each file is parsed at most once - stepping between days with the ← / → arrows no
     * longer re-reads the JSON every time (P2-2 in specs/code-audit-v3). Failed loads
     * aren't cached, so they're retried on the next request. Concurrent: `loadContent`
     * is called from `Dispatchers.IO`.
     */
    private val contentCache = ConcurrentHashMap<String, DevotionalContent>()

    override fun getCatalog(): List<DevotionalMeta> = DevotionalCatalog.all

    override fun getDevotional(id: String): DevotionalMeta = DevotionalCatalog.findById(id)

    override fun loadContent(devotionalId: String, language: String): DevotionalContent? {
        val meta = getDevotional(devotionalId)
        val fileName = if (contentLanguageFor(language) == "en") meta.contentFileEn else meta.contentFileEs
        contentCache[fileName]?.let { return it }
        val json = readAsset(fileName) ?: return null
        return try {
            gson.fromJson(json, DevotionalContent::class.java)?.also { contentCache[fileName] = it }
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
