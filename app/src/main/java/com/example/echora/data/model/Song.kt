package com.example.echora.data.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val durationSeconds: Int,
    val genre: String,
    val coverRes: Int,
    var isFavorite: Boolean = false,
    val localUri: String? = null
)
