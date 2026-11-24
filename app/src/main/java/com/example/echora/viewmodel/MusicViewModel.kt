package com.example.echora.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.echora.R
import com.example.echora.data.model.Album
import com.example.echora.data.model.Playlist
import com.example.echora.data.model.Song
import com.example.echora.data.storage.MusicStorageManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val storageManager = MusicStorageManager(application)

    private val _songs = MutableStateFlow(storageManager.loadSongs())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _playlists = MutableStateFlow(storageManager.loadPlaylists())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _savedAlbumTitles = MutableStateFlow(storageManager.loadSavedAlbums())
    val savedAlbumTitles: StateFlow<List<String>> = _savedAlbumTitles.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    private val _librarySubScreen = MutableStateFlow("main")
    val librarySubScreen: StateFlow<String> = _librarySubScreen.asStateFlow()

    private val _playlistSongMap = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val playlistSongMap: StateFlow<Map<String, List<String>>> = _playlistSongMap.asStateFlow()

    private val _playbackQueue = MutableStateFlow<List<Song>>(_songs.value)
    val playbackQueue: StateFlow<List<Song>> = _playbackQueue.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(_songs.value.firstOrNull())
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0f)
    val playbackPosition: StateFlow<Float> = _playbackPosition.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _themeMode = MutableStateFlow(storageManager.loadThemeMode()) // "system", "light", "dark"
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _username = MutableStateFlow(storageManager.loadUsername("Alex Rivera"))
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("alex.rivera@echora.com")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _selectedSongForDetail = MutableStateFlow<Song?>(null)
    val selectedSongForDetail: StateFlow<Song?> = _selectedSongForDetail.asStateFlow()

    private val _songForPlaylist = MutableStateFlow<Song?>(null)
    val songForPlaylist: StateFlow<Song?> = _songForPlaylist.asStateFlow()

    private val _isAddSongToPlaylistOpen = MutableStateFlow(false)
    val isAddSongToPlaylistOpen: StateFlow<Boolean> = _isAddSongToPlaylistOpen.asStateFlow()

    private val _isCreatePlaylistOpen = MutableStateFlow(false)
    val isCreatePlaylistOpen: StateFlow<Boolean> = _isCreatePlaylistOpen.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private fun updateAlbums() {
        val allSongs = _songs.value
        val saved = _savedAlbumTitles.value
        val grouped = allSongs.groupBy { it.album.ifBlank { "Unknown Album" } }
        val albumList = grouped.map { (title, songList) ->
            Album(
                title = title,
                artist = songList.firstOrNull()?.artist ?: "Unknown Artist",
                coverRes = songList.firstOrNull()?.coverRes ?: R.drawable.echora_placeholder,
                songCount = songList.size,
                songs = songList,
                isSaved = saved.contains(title)
            )
        }
        _albums.value = albumList
    }

    val likedSongs: StateFlow<List<Song>> = MutableStateFlow(emptyList())

    private fun updateLikedSongs() {
        (likedSongs as MutableStateFlow).value = _songs.value.filter { it.isFavorite }
    }

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build().apply {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingState: Boolean) {
                _isPlaying.value = isPlayingState
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    if (_isRepeat.value) {
                        exoPlayer.seekTo(0L)
                        exoPlayer.play()
                    } else {
                        nextSong()
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _errorMessage.value = "File audio tidak dapat diputar: ${error.message}"
                _isPlaying.value = false
            }
        })
    }

    private var progressJob: Job? = null

    init {
        val map = mutableMapOf<String, List<String>>()
        val updatedPlaylists = _playlists.value.map { p ->
            val loadedSongIds = storageManager.loadPlaylistSongs(p.id)
            map[p.id] = loadedSongIds
            p.copy(songCount = loadedSongIds.size)
        }
        _playlists.value = updatedPlaylists
        _playlistSongMap.value = map
        updateAlbums()
        updateLikedSongs()
        startProgressTracker()
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    val duration = exoPlayer.duration
                    val position = exoPlayer.currentPosition
                    if (duration > 0) {
                        _playbackPosition.value = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    }
                }
                delay(500L)
            }
        }
    }

    fun playSong(song: Song, queue: List<Song>? = null) {
        if (queue != null) {
            _playbackQueue.value = queue
        }
        _currentSong.value = song
        _playbackPosition.value = 0f
        _errorMessage.value = null
        try {
            if (!song.localUri.isNullOrBlank()) {
                val mediaItem = MediaItem.fromUri(Uri.parse(song.localUri))
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            } else {
                exoPlayer.stop()
                _isPlaying.value = false
                _errorMessage.value = "Pilih file MP3 lokal dari HP untuk memutar lagu ini."
            }
        } catch (_: SecurityException) {
            _errorMessage.value = "File audio tidak dapat diakses (izin dicabut)."
            _isPlaying.value = false
        } catch (e: Exception) {
            _errorMessage.value = "Error memutar lagu: ${e.localizedMessage}"
            _isPlaying.value = false
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false) {
        if (songs.isEmpty()) return
        val listToPlay = if (shuffle) songs.shuffled() else songs
        _isShuffle.value = shuffle
        exoPlayer.shuffleModeEnabled = shuffle
        val index = startIndex.coerceIn(0, listToPlay.size - 1)
        playSong(listToPlay[index], listToPlay)
    }

    fun togglePlayPause() {
        val current = _currentSong.value
        if (current == null || current.localUri.isNullOrBlank()) {
            _errorMessage.value = "Silakan tambahkan file MP3 lokal terlebih dahulu."
            return
        }
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_IDLE || exoPlayer.playbackState == Player.STATE_ENDED) {
                playSong(current)
            } else {
                exoPlayer.play()
            }
        }
    }

    fun nextSong() {
        val current = _currentSong.value ?: return
        val queue = _playbackQueue.value.ifEmpty { _songs.value }
        if (queue.isEmpty()) return
        if (queue.size == 1) {
            playSong(queue.first(), queue)
            return
        }
        val currentIndex = queue.indexOfFirst { it.id == current.id }
        val nextIndex = if (_isShuffle.value) {
            val otherIndices = queue.indices.filter { it != currentIndex }
            if (otherIndices.isNotEmpty()) otherIndices.random() else 0
        } else {
            if (currentIndex != -1) (currentIndex + 1) % queue.size else 0
        }
        playSong(queue[nextIndex], queue)
    }

    fun previousSong() {
        val current = _currentSong.value ?: return
        val queue = _playbackQueue.value.ifEmpty { _songs.value }
        if (queue.isEmpty()) return
        if (queue.size == 1) {
            playSong(queue.first(), queue)
            return
        }
        val currentIndex = queue.indexOfFirst { it.id == current.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else queue.size - 1
        playSong(queue[prevIndex], queue)
    }

    fun seekTo(positionFraction: Float) {
        val duration = exoPlayer.duration
        if (duration > 0) {
            val targetPosition = (duration * positionFraction).toLong()
            exoPlayer.seekTo(targetPosition)
            _playbackPosition.value = positionFraction.coerceIn(0f, 1f)
        }
    }

    fun addLocalSongFromUri(uri: Uri, context: Context) {
        try {
            var fileName = "Unknown Song"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                }
            }

            var title = fileName.substringBeforeLast(".")
            var artist = "Local Artist"
            var album = "Local Files"
            var durationStr = "3:30"
            var durationSec = 210

            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val metaTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                val metaArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                val metaAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                val metaDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

                if (!metaTitle.isNullOrBlank()) title = metaTitle
                if (!metaArtist.isNullOrBlank()) artist = metaArtist
                if (!metaAlbum.isNullOrBlank()) album = metaAlbum
                if (!metaDuration.isNullOrBlank()) {
                    val ms = metaDuration.toLong()
                    durationSec = (ms / 1000).toInt()
                    durationStr = String.format("%d:%02d", durationSec / 60, durationSec % 60)
                }
            } catch (_: Exception) {
                // fallback
            } finally {
                retriever.release()
            }

            val newSong = Song(
                id = "local_${System.currentTimeMillis()}",
                title = title,
                artist = artist,
                album = album,
                duration = durationStr,
                durationSeconds = durationSec,
                genre = "Local",
                coverRes = R.drawable.echora_placeholder,
                isFavorite = false,
                localUri = uri.toString()
            )

            _songs.update {
                val updated = listOf(newSong) + it
                storageManager.saveSongs(updated)
                updated
            }
            updateAlbums()
            updateLikedSongs()
            playSong(newSong)
            _errorMessage.value = null
        } catch (e: Exception) {
            _errorMessage.value = "Gagal memuat file audio: ${e.localizedMessage}"
        }
    }

    fun toggleFavorite(songId: String) {
        _songs.update { list ->
            val updated = list.map { song ->
                if (song.id == songId) song.copy(isFavorite = !song.isFavorite) else song
            }
            storageManager.saveSongs(updated)
            updated
        }
        updateAlbums()
        updateLikedSongs()
        val current = _currentSong.value
        if (current != null && current.id == songId) {
            _currentSong.value = current.copy(isFavorite = !current.isFavorite)
        }
        val detailSong = _selectedSongForDetail.value
        if (detailSong != null && detailSong.id == songId) {
            _selectedSongForDetail.value = detailSong.copy(isFavorite = !detailSong.isFavorite)
        }
    }

    fun createPlaylist(name: String, coverRes: Int = R.drawable.album_cover_1) {
        if (name.isBlank()) return
        val newPlaylist = Playlist(
            id = "p_${System.currentTimeMillis()}",
            name = name.trim(),
            songCount = 0,
            coverRes = coverRes
        )
        _playlists.update { list ->
            val updated = list + newPlaylist
            storageManager.savePlaylists(updated)
            updated
        }
    }

    fun createPlaylistWithSongs(
        name: String,
        songIds: List<String>,
        description: String,
        isPrivate: Boolean,
        coverRes: Int
    ) {
        if (name.isBlank()) return
        val newPlaylist = Playlist(
            id = "p_${System.currentTimeMillis()}",
            name = name.trim(),
            songCount = songIds.size,
            coverRes = coverRes,
            description = description.trim(),
            isPrivate = isPrivate
        )
        _playlists.update { list ->
            val updated = list + newPlaylist
            storageManager.savePlaylists(updated)
            updated
        }
        if (songIds.isNotEmpty()) {
            val currentMap = _playlistSongMap.value.toMutableMap()
            currentMap[newPlaylist.id] = songIds
            _playlistSongMap.value = currentMap
            storageManager.savePlaylistSongs(newPlaylist.id, songIds)
        }
        closeCreatePlaylist()
        selectPlaylist(newPlaylist)
    }

    fun editPlaylist(playlistId: String, newName: String, newCoverRes: Int) {
        if (newName.isBlank()) return
        _playlists.update { list ->
            val updated = list.map {
                if (it.id == playlistId) it.copy(name = newName.trim(), coverRes = newCoverRes) else it
            }
            storageManager.savePlaylists(updated)
            updated
        }
        if (_selectedPlaylist.value?.id == playlistId) {
            _selectedPlaylist.value = _selectedPlaylist.value?.copy(name = newName.trim(), coverRes = newCoverRes)
        }
    }

    fun deletePlaylist(playlistId: String) {
        _playlists.update { list ->
            val updated = list.filter { it.id != playlistId }
            storageManager.savePlaylists(updated)
            updated
        }
        _playlistSongMap.update { map ->
            val updated = map.toMutableMap()
            updated.remove(playlistId)
            updated
        }
        storageManager.removePlaylistSongs(playlistId)
        if (_selectedPlaylist.value?.id == playlistId) {
            _selectedPlaylist.value = null
            _librarySubScreen.value = "main"
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        val currentMap = _playlistSongMap.value.toMutableMap()
        val currentList = currentMap[playlistId] ?: emptyList()
        if (!currentList.contains(songId)) {
            val newList = currentList + songId
            currentMap[playlistId] = newList
            _playlistSongMap.value = currentMap
            storageManager.savePlaylistSongs(playlistId, newList)

            _playlists.update { list ->
                val updated = list.map { if (it.id == playlistId) it.copy(songCount = newList.size) else it }
                storageManager.savePlaylists(updated)
                updated
            }
            _errorMessage.value = "Lagu berhasil ditambahkan ke playlist!"
        } else {
            _errorMessage.value = "Lagu sudah ada di dalam playlist ini."
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        val currentMap = _playlistSongMap.value.toMutableMap()
        val currentList = currentMap[playlistId] ?: emptyList()
        if (currentList.contains(songId)) {
            val newList = currentList.filter { it != songId }
            currentMap[playlistId] = newList
            _playlistSongMap.value = currentMap
            storageManager.savePlaylistSongs(playlistId, newList)

            _playlists.update { list ->
                val updated = list.map { if (it.id == playlistId) it.copy(songCount = newList.size) else it }
                storageManager.savePlaylists(updated)
                updated
            }
        }
    }

    fun toggleSaveAlbum(albumTitle: String) {
        val current = _savedAlbumTitles.value.toMutableList()
        if (current.contains(albumTitle)) {
            current.remove(albumTitle)
        } else {
            current.add(albumTitle)
        }
        _savedAlbumTitles.value = current
        storageManager.saveSavedAlbums(current)
        updateAlbums()
        if (_selectedAlbum.value?.title == albumTitle) {
            _selectedAlbum.value = _selectedAlbum.value?.copy(isSaved = current.contains(albumTitle))
        }
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
        if (playlist != null) {
            _librarySubScreen.value = "playlist_detail"
        } else {
            _librarySubScreen.value = "main"
        }
    }

    fun selectAlbum(album: Album?) {
        _selectedAlbum.value = album
        if (album != null) {
            _librarySubScreen.value = "album_detail"
        } else {
            _librarySubScreen.value = "main"
        }
    }

    fun setLibrarySubScreen(screen: String) {
        _librarySubScreen.value = screen
        if (screen == "main") {
            _selectedPlaylist.value = null
            _selectedAlbum.value = null
        }
    }

    fun openSongDetail(song: Song) {
        _selectedSongForDetail.value = song
    }

    fun closeSongDetail() {
        _selectedSongForDetail.value = null
    }

    fun openAddSongToPlaylist(song: Song) {
        _songForPlaylist.value = song
        _isAddSongToPlaylistOpen.value = true
    }

    fun closeAddSongToPlaylist() {
        _isAddSongToPlaylistOpen.value = false
        _songForPlaylist.value = null
    }

    fun openCreatePlaylist() {
        _isCreatePlaylistOpen.value = true
    }

    fun closeCreatePlaylist() {
        _isCreatePlaylistOpen.value = false
    }

    fun updateUsername(newName: String) {
        if (newName.isBlank()) return
        _username.value = newName.trim()
        storageManager.saveUsername(newName.trim())
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        storageManager.saveThemeMode(mode)
    }

    fun getSongsForPlaylist(playlistId: String): List<Song> {
        val songIds = _playlistSongMap.value[playlistId] ?: emptyList()
        val allSongs = _songs.value
        return songIds.mapNotNull { id -> allSongs.find { it.id == id } }
    }

    fun playPlaylist(playlistId: String, shuffle: Boolean = false) {
        val songs = getSongsForPlaylist(playlistId)
        if (songs.isNotEmpty()) {
            playQueue(songs, 0, shuffle)
        } else {
            _errorMessage.value = "Playlist ini masih kosong."
        }
    }

    fun playAlbum(album: Album, shuffle: Boolean = false) {
        if (album.songs.isNotEmpty()) {
            playQueue(album.songs, 0, shuffle)
        } else {
            _errorMessage.value = "Album ini kosong."
        }
    }

    fun playLikedSongs(shuffle: Boolean = false) {
        val liked = likedSongs.value
        if (liked.isNotEmpty()) {
            playQueue(liked, 0, shuffle)
        } else {
            _errorMessage.value = "Belum ada lagu yang disukai."
        }
    }

    fun shuffleQueue() {
        val nextShuffle = !_isShuffle.value
        _isShuffle.value = nextShuffle
        exoPlayer.shuffleModeEnabled = nextShuffle
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
        if (screen != "library") {
            _librarySubScreen.value = "main"
            _selectedPlaylist.value = null
            _selectedAlbum.value = null
        }
    }

    fun toggleRepeat() {
        val nextRepeat = !_isRepeat.value
        _isRepeat.value = nextRepeat
        exoPlayer.repeatMode = if (nextRepeat) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    fun toggleDarkMode() {
        val nextMode = when (_themeMode.value) {
            "light" -> "dark"
            else -> "light"
        }
        setThemeMode(nextMode)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        exoPlayer.release()
    }
}
