package com.mirkwood.novenapp.presentation.screens.lyrics

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mirkwood.novenapp.presentation.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LyricsViewModel(

) : ViewModel() {


    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    private val _selectedSong = MutableStateFlow<Song?>(null)
    val selectedSong: StateFlow<Song?> = _selectedSong.asStateFlow()

    fun loadSongsFromJson(context: Context, songID: Int? = null) {
        val jsonString = context.assets.open("songs.json").bufferedReader().use { it.readText() }
        _songs.value = Gson().fromJson(jsonString, object : TypeToken<List<Song>>() {}.type)

        // Si hay una canción seleccionada, búscala después de cargar los datos
        songID?.let { getSongById(it) }
    }


    fun getSongById(id: Int) {
        _selectedSong.value = _songs.value?.find { it.id == id }
    }
}
