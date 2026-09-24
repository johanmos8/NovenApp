package com.mirkwood.novenapp.presentation.screens.lyrics

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mirkwood.novenapp.presentation.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "LyricsViewModel"

/**
 * Loads the carols once, off the main thread, when the ViewModel is created - rather
 * than re-reading and re-parsing `songs.json` during composition, on every
 * recomposition of the carols tab (P2-1 in specs/code-audit-v3).
 */
class LyricsViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext

    /** `null` until loaded. An unreadable asset loads as an empty list, never a crash. */
    private val _songs = MutableStateFlow<List<Song>?>(null)
    val songs: StateFlow<List<Song>?> = _songs.asStateFlow()

    init {
        viewModelScope.launch {
            _songs.value = withContext(Dispatchers.IO) { readSongs() }
        }
    }

    private fun readSongs(): List<Song> = try {
        val json = appContext.assets.open("songs.json").bufferedReader().use { it.readText() }
        Gson().fromJson<List<Song>>(json, object : TypeToken<List<Song>>() {}.type).orEmpty()
    } catch (ex: Exception) {
        Log.e(TAG, "Failed to load songs.json", ex)
        emptyList()
    }
}
