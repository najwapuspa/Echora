package com.example.echora

import com.example.echora.data.model.Album
import com.example.echora.data.model.Playlist
import com.example.echora.data.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun playlist_songCount_isCorrect() {
        val playlist = Playlist(
            id = "p1",
            name = "My Favorites",
            songCount = 3,
            coverRes = 1
        )
        assertEquals(3, playlist.songCount)
        assertEquals("My Favorites", playlist.name)
    }

    @Test
    fun song_favoriteToggle_isCorrect() {
        val song = Song(
            id = "s1",
            title = "Midnight Echoes",
            artist = "Luna Vesper",
            album = "Starlight Odyssey",
            duration = "3:45",
            durationSeconds = 225,
            genre = "Pop",
            coverRes = 1,
            isFavorite = false
        )
        assertFalse(song.isFavorite)

        val toggledSong = song.copy(isFavorite = !song.isFavorite)
        assertTrue(toggledSong.isFavorite)
    }

    @Test
    fun album_grouping_isCorrect() {
        val song1 = Song("s1", "Title 1", "Artist A", "Album 1", "3:00", 180, "Pop", 1)
        val song2 = Song("s2", "Title 2", "Artist A", "Album 1", "3:30", 210, "Pop", 1)
        val songs = listOf(song1, song2)

        val grouped = songs.groupBy { it.album }
        assertEquals(1, grouped.size)

        val albumSongs = grouped["Album 1"] ?: emptyList()
        assertEquals(2, albumSongs.size)

        val album = Album(
            title = "Album 1",
            artist = "Artist A",
            coverRes = 1,
            songCount = albumSongs.size,
            songs = albumSongs
        )
        assertEquals(2, album.songCount)
    }
}
