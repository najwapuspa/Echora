package com.example.echora.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.echora.R
import com.example.echora.data.model.Album
import com.example.echora.data.model.Playlist
import com.example.echora.data.model.Song
import com.example.echora.data.storage.MusicStorageManager
import com.example.echora.service.EchoraMediaService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
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

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF) // 0: OFF, 1: ONE, 2: ALL
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _themeMode = MutableStateFlow(storageManager.loadThemeMode())
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

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    val likedSongs: StateFlow<List<Song>> = MutableStateFlow(emptyList())

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

    private fun updateLikedSongs() {
        (likedSongs as MutableStateFlow).value = _songs.value.filter { it.isFavorite }
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

        initMediaController(application)
        startProgressTracker()
    }

    private fun initMediaController(context: Context) {
        val sessionToken = SessionToken(context, ComponentName(context, EchoraMediaService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                val controller = controllerFuture?.get()
                mediaController = controller
                controller?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingState: Boolean) {
                        _isPlaying.value = isPlayingState
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        mediaItem?.let { item ->
                            val foundSong = _playbackQueue.value.find { it.id == item.mediaId }
                            if (foundSong != null) {
                                _currentSong.value = foundSong
                                _playbackPosition.value = 0f
                            }
                        }
                    }

                    override fun onRepeatModeChanged(repeatModeState: Int) {
                        _repeatMode.value = repeatModeState
                    }

                    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                        _isShuffle.value = shuffleModeEnabled
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        _errorMessage.value = "File audio tidak dapat diputar: ${error.message}"
                        _isPlaying.value = false
                    }
                })
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghubungkan media controller: ${e.localizedMessage}"
            }
        }, MoreExecutors.directExecutor())
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                val controller = mediaController
                if (controller != null && controller.isPlaying) {
                    val duration = controller.duration
                    val position = controller.currentPosition
                    if (duration > 0) {
                        _playbackPosition.value = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    }
                }
                delay(500L)
            }
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false) {
        val controller = mediaController
        if (controller == null) {
            _errorMessage.value = "Media Controller belum terhubung."
            return
        }

        val playables = songs.filter { !it.localUri.isNullOrBlank() }
        if (playables.isEmpty()) {
            _errorMessage.value = "Pilih file MP3 lokal dari HP untuk memutar lagu ini."
            return
        }

        _playbackQueue.value = playables
        _isShuffle.value = shuffle
        controller.shuffleModeEnabled = shuffle

        val mediaItems = playables.map { song ->
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .build()

            MediaItem.Builder()
                .setMediaId(song.id)
                .setUri(Uri.parse(song.localUri))
                .setMediaMetadata(metadata)
                .build()
        }

        val index = startIndex.coerceIn(0, playables.size - 1)
        _currentSong.value = playables[index]
        _playbackPosition.value = 0f

        try {
            controller.setMediaItems(mediaItems, index, 0L)
            controller.prepare()
            controller.play()
        } catch (_: SecurityException) {
            _errorMessage.value = "File audio tidak dapat diakses (izin dicabut)."
            _isPlaying.value = false
        } catch (e: Exception) {
            _errorMessage.value = "Gagal memutar antrean: ${e.localizedMessage}"
            _isPlaying.value = false
        }
    }

    fun playSong(song: Song, queue: List<Song>? = null) {
        val targetQueue = queue ?: _playbackQueue.value.ifEmpty { _songs.value }
        val index = targetQueue.indexOfFirst { it.id == song.id }
        if (index != -1) {
            playQueue(targetQueue, index, _isShuffle.value)
        } else {
            playQueue(listOf(song) + targetQueue, 0, _isShuffle.value)
        }
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        val current = _currentSong.value
        if (current == null || current.localUri.isNullOrBlank()) {
            _errorMessage.value = "Silakan tambahkan file MP3 lokal terlebih dahulu."
            return
        }
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_IDLE || controller.playbackState == Player.STATE_ENDED) {
                val queue = _playbackQueue.value.ifEmpty { _songs.value }
                val index = queue.indexOfFirst { it.id == current.id }.coerceAtLeast(0)
                playQueue(queue, index, _isShuffle.value)
            } else {
                controller.play()
            }
        }
    }

    fun nextSong() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        } else if (_repeatMode.value == Player.REPEAT_MODE_ALL) {
            controller.seekTo(0, 0L)
            controller.play()
        } else {
            controller.pause()
            controller.seekTo(0, 0L)
            _playbackPosition.value = 0f
        }
    }

    fun previousSong() {
        val controller = mediaController ?: return
        if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else if (_repeatMode.value == Player.REPEAT_MODE_ALL) {
            val count = controller.mediaItemCount
            if (count > 0) {
                controller.seekTo(count - 1, 0L)
                controller.play()
            }
        } else {
            controller.seekTo(0L)
            _playbackPosition.value = 0f
        }
    }

    fun seekTo(positionFraction: Float) {
        val controller = mediaController ?: return
        val duration = controller.duration
        if (duration > 0) {
            val targetPosition = (duration * positionFraction).toLong()
            controller.seekTo(targetPosition)
            _playbackPosition.value = positionFraction.coerceIn(0f, 1f)
        }
    }

    fun addLocalSongFromUri(uri: Uri, context: Context) {
        try {
            val uriString = uri.toString()
            val existing = _songs.value.find { it.localUri == uriString }
            if (existing != null) {
                _errorMessage.value = "Lagu '${existing.title}' sudah ada di Library."
                playSong(existing)
                return
            }

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
            var durationStr = "Durasi tidak diketahui"
            var durationSec = 0

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
                    if (ms > 0) {
                        durationSec = (ms / 1000).toInt()
                        durationStr = String.format("%d:%02d", durationSec / 60, durationSec % 60)
                    }
                }
            } catch (_: Exception) {
                // fallback
            } finally {
                retriever.release()
            }

            val stableId = "local_${uriString.hashCode()}"

            val newSong = Song(
                id = stableId,
                title = title,
                artist = artist,
                album = album,
                duration = durationStr,
                durationSeconds = durationSec,
                genre = "Local",
                coverRes = R.drawable.echora_placeholder,
                isFavorite = false,
                localUri = uriString
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
        mediaController?.shuffleModeEnabled = nextShuffle
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
        val controller = mediaController ?: return
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = nextMode
        controller.repeatMode = nextMode
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
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
    }
}
