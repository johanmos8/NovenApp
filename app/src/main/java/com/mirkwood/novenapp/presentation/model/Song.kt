package com.mirkwood.novenapp.presentation.model

data class Song(
    val id: Int,
    val title: String,
    val artist: String? = null,
    val lyrics: String
)
