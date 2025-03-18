package com.mirkwood.novenapp.presentation.screens.lyrics

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mirkwood.novenapp.presentation.model.Song

class LyricsViewModel(

) : ViewModel() {


    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    private val _selectedSong = MutableLiveData<Song?>()
    val selectedSong: LiveData<Song?> = _selectedSong

    fun loadSongsFromJson(context: Context) {
        val jsonString = context.assets.open("songs.json").bufferedReader().use { it.readText() }
        _songs.value = Gson().fromJson(jsonString, object : TypeToken<List<Song>>() {}.type)

    }

    fun getSongById(id: Int) {
        _selectedSong.value = _songs.value?.find { it.id == id }
    }
}