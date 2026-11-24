package com.example.echora.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.echora.data.model.Playlist
import com.example.echora.data.model.Song
import com.example.echora.data.repository.MusicRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MusicStorageManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("echora_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveSongs(songs: List<Song>) {
        val json = gson.toJson(songs)
        prefs.edit().putString("saved_songs", json).apply()
    }

    fun loadSongs(): List<Song> {
        if (!prefs.contains("saved_songs")) {
            return MusicRepository.sampleSongs
        }
        val json = prefs.getString("saved_songs", null) ?: return emptyList()
        val type = object : TypeToken<List<Song>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun savePlaylists(playlists: List<Playlist>) {
        val json = gson.toJson(playlists)
        prefs.edit().putString("saved_playlists", json).apply()
    }

    fun loadPlaylists(): List<Playlist> {
        if (!prefs.contains("saved_playlists")) {
            return MusicRepository.samplePlaylists
        }
        val json = prefs.getString("saved_playlists", null) ?: return emptyList()
        val type = object : TypeToken<List<Playlist>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun savePlaylistSongs(playlistId: String, songIds: List<String>) {
        val json = gson.toJson(songIds)
        prefs.edit().putString("playlist_songs_$playlistId", json).apply()
    }

    fun loadPlaylistSongs(playlistId: String): List<String> {
        val json = prefs.getString("playlist_songs_$playlistId", null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun removePlaylistSongs(playlistId: String) {
        prefs.edit().remove("playlist_songs_$playlistId").apply()
    }

    fun saveSavedAlbums(albumTitles: List<String>) {
        val json = gson.toJson(albumTitles)
        prefs.edit().putString("saved_albums", json).apply()
    }

    fun loadSavedAlbums(): List<String> {
        val json = prefs.getString("saved_albums", null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveUsername(username: String) {
        prefs.edit().putString("saved_username", username).apply()
    }

    fun loadUsername(defaultName: String): String {
        return prefs.getString("saved_username", defaultName) ?: defaultName
    }

    fun saveThemeMode(mode: String) {
        prefs.edit().putString("saved_theme_mode", mode).apply()
    }

    fun loadThemeMode(): String {
        return prefs.getString("saved_theme_mode", "system") ?: "system"
    }
}
