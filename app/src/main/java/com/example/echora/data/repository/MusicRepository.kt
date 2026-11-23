package com.example.echora.data.repository

import com.example.echora.R
import com.example.echora.data.model.Playlist
import com.example.echora.data.model.Song

object MusicRepository {
    val sampleSongs = listOf(
        Song(
            id = "1",
            title = "Midnight Echoes",
            artist = "Luna Vesper",
            album = "Starlight Odyssey",
            duration = "3:45",
            durationSeconds = 225,
            genre = "Pop",
            coverRes = R.drawable.album_cover_1,
            isFavorite = true
        ),
        Song(
            id = "2",
            title = "Electric Pulse",
            artist = "Neon Syndicate",
            album = "Cyber Horizon",
            duration = "4:12",
            durationSeconds = 252,
            genre = "Electronic",
            coverRes = R.drawable.album_cover_2,
            isFavorite = false
        ),
        Song(
            id = "3",
            title = "Velvet Rain",
            artist = "Aria Monroe",
            album = "Coffee & Jazz",
            duration = "3:20",
            durationSeconds = 200,
            genre = "Jazz",
            coverRes = R.drawable.album_cover_3,
            isFavorite = true
        ),
        Song(
            id = "4",
            title = "Shadow & Bone",
            artist = "The Rogue Vibe",
            album = "Rebellion",
            duration = "5:01",
            durationSeconds = 301,
            genre = "Rock",
            coverRes = R.drawable.album_cover_4,
            isFavorite = false
        ),
        Song(
            id = "5",
            title = "Golden Hour Grooves",
            artist = "Kaelen Soul",
            album = "Sunset Avenue",
            duration = "3:55",
            durationSeconds = 235,
            genre = "R&B",
            coverRes = R.drawable.album_cover_5,
            isFavorite = true
        ),
        Song(
            id = "6",
            title = "Cosmic Drift",
            artist = "Astral Projection",
            album = "Deep Space",
            duration = "6:15",
            durationSeconds = 375,
            genre = "Electronic",
            coverRes = R.drawable.album_cover_6,
            isFavorite = false
        ),
        Song(
            id = "7",
            title = "Urban Symphony",
            artist = "Metro Beats",
            album = "City Lights",
            duration = "3:10",
            durationSeconds = 190,
            genre = "Hip Hop",
            coverRes = R.drawable.album_cover_7,
            isFavorite = false
        ),
        Song(
            id = "8",
            title = "Acoustic Breeze",
            artist = "Sienna Woods",
            album = "Unplugged",
            duration = "2:50",
            durationSeconds = 170,
            genre = "Pop",
            coverRes = R.drawable.album_cover_8,
            isFavorite = true
        )
    )

    val samplePlaylists = listOf(
        Playlist(id = "p1", name = "Chill Vibes", songCount = 12, coverRes = R.drawable.album_cover_1),
        Playlist(id = "p2", name = "Workout Energy", songCount = 20, coverRes = R.drawable.album_cover_2),
        Playlist(id = "p3", name = "Late Night Coding", songCount = 15, coverRes = R.drawable.album_cover_6),
        Playlist(id = "p4", name = "Top Hits 2026", songCount = 50, coverRes = R.drawable.album_cover_5)
    )

    val categories = listOf("All", "Pop", "Rock", "Jazz", "R&B", "Electronic", "Hip Hop")
}
