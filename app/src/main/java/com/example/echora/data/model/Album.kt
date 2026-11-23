package com.example.echora.data.model

data class Album(
    val title: String,
    val artist: String,
    val coverRes: Int,
    val songCount: Int,
    val songs: List<Song>,
    var isSaved: Boolean = false
)
