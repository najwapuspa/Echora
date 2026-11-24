package com.example.echora

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.echora.ui.components.AddSongToPlaylistDialog
import com.example.echora.ui.components.BottomNavigationBar
import com.example.echora.ui.components.MiniPlayer
import com.example.echora.ui.components.SongDetailBottomSheet
import com.example.echora.ui.screens.*
import com.example.echora.ui.theme.EchoraTheme
import com.example.echora.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            EchoraTheme(themeMode = themeMode) {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val librarySubScreen by viewModel.librarySubScreen.collectAsState()
                val songs by viewModel.songs.collectAsState()
                val playlists by viewModel.playlists.collectAsState()
                val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
                val selectedAlbum by viewModel.selectedAlbum.collectAsState()
                val likedSongs by viewModel.likedSongs.collectAsState()
                val albums by viewModel.albums.collectAsState()
                val currentSong by viewModel.currentSong.collectAsState()
                val isPlaying by viewModel.isPlaying.collectAsState()
                val playbackPosition by viewModel.playbackPosition.collectAsState()
                val isShuffle by viewModel.isShuffle.collectAsState()
                val repeatMode by viewModel.repeatMode.collectAsState()
                val searchQuery by viewModel.searchQuery.collectAsState()
                val selectedCategory by viewModel.selectedCategory.collectAsState()
                val username by viewModel.username.collectAsState()
                val email by viewModel.email.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                val selectedSongForDetail by viewModel.selectedSongForDetail.collectAsState()
                val isAddSongToPlaylistOpen by viewModel.isAddSongToPlaylistOpen.collectAsState()
                val songForPlaylist by viewModel.songForPlaylist.collectAsState()
                val isCreatePlaylistOpen by viewModel.isCreatePlaylistOpen.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearError()
                    }
                }

                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri: Uri? ->
                    uri?.let {
                        try {
                            contentResolver.takePersistableUriPermission(
                                it,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: Exception) {
                            // ignore
                        }
                        viewModel.addLocalSongFromUri(it, applicationContext)
                    }
                }

                if (isCreatePlaylistOpen) {
                    CreatePlaylistScreen(
                        allSongs = songs,
                        onBackClick = { viewModel.closeCreatePlaylist() },
                        onSavePlaylist = { name, songIds, desc, isPrivate, cover ->
                            viewModel.createPlaylistWithSongs(name, songIds, desc, isPrivate, cover)
                        }
                    )
                } else if (currentScreen == "player") {
                    MusicPlayerScreen(
                        song = currentSong,
                        isPlaying = isPlaying,
                        playbackPosition = playbackPosition,
                        isShuffle = isShuffle,
                        repeatMode = repeatMode,
                        onBackClick = { viewModel.navigateTo("home") },
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onNextClick = { viewModel.nextSong() },
                        onPreviousClick = { viewModel.previousSong() },
                        onSeek = { viewModel.seekTo(it) },
                        onFavoriteClick = { viewModel.toggleFavorite(it) },
                        onShuffleClick = { viewModel.shuffleQueue() },
                        onRepeatClick = { viewModel.toggleRepeat() }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            androidx.compose.foundation.layout.Column {
                                MiniPlayer(
                                    song = currentSong,
                                    isPlaying = isPlaying,
                                    onPlayPauseClick = { viewModel.togglePlayPause() },
                                    onNextClick = { viewModel.nextSong() },
                                    onClick = { viewModel.navigateTo("player") }
                                )
                                BottomNavigationBar(
                                    currentScreen = currentScreen,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentScreen) {
                                "home" -> HomeScreen(
                                    username = username,
                                    songs = songs,
                                    onSongClick = { song -> viewModel.playSong(song, songs) },
                                    onFavoriteClick = { id -> viewModel.toggleFavorite(id) },
                                    onMoreClick = { song -> viewModel.openSongDetail(song) },
                                    onPlayQueue = { queue, idx, shuffle -> viewModel.playQueue(queue, idx, shuffle) }
                                )
                                "search" -> SearchScreen(
                                    searchQuery = searchQuery,
                                    selectedCategory = selectedCategory,
                                    songs = songs,
                                    onQueryChange = { viewModel.setSearchQuery(it) },
                                    onCategorySelect = { viewModel.setSelectedCategory(it) },
                                    onSongClick = { song -> viewModel.playSong(song, songs) },
                                    onFavoriteClick = { id -> viewModel.toggleFavorite(id) },
                                    onMoreClick = { song -> viewModel.openSongDetail(song) }
                                )
                                "library" -> {
                                    when (librarySubScreen) {
                                        "liked" -> LikedSongsScreen(
                                            likedSongs = likedSongs,
                                            onBackClick = { viewModel.setLibrarySubScreen("main") },
                                            onPlayAllClick = { viewModel.playLikedSongs(false) },
                                            onShuffleClick = { viewModel.playLikedSongs(true) },
                                            onSongClick = { song -> viewModel.playSong(song, likedSongs) },
                                            onFavoriteClick = { id -> viewModel.toggleFavorite(id) },
                                            onMoreClick = { song -> viewModel.openSongDetail(song) }
                                        )
                                        "albums" -> AlbumCollectionScreen(
                                            albums = albums,
                                            onBackClick = { viewModel.setLibrarySubScreen("main") },
                                            onAlbumClick = { album -> viewModel.selectAlbum(album) }
                                        )
                                        "playlist_detail" -> {
                                            selectedPlaylist?.let { playlist ->
                                                val playlistSongs = viewModel.getSongsForPlaylist(playlist.id)
                                                PlaylistDetailScreen(
                                                    playlist = playlist,
                                                    playlistSongs = playlistSongs,
                                                    onBackClick = { viewModel.selectPlaylist(null) },
                                                    onPlayAllClick = { viewModel.playPlaylist(playlist.id, false) },
                                                    onSongClick = { song -> viewModel.playSong(song, playlistSongs) },
                                                    onFavoriteClick = { id -> viewModel.toggleFavorite(id) },
                                                    onMoreClick = { song -> viewModel.openSongDetail(song) },
                                                    onDeletePlaylist = { viewModel.deletePlaylist(playlist.id) },
                                                    onRenamePlaylist = { newName -> viewModel.editPlaylist(playlist.id, newName, playlist.coverRes) }
                                                )
                                            } ?: run {
                                                MyLibraryScreen(
                                                    songs = songs,
                                                    playlists = playlists,
                                                    likedSongs = likedSongs,
                                                    albums = albums,
                                                    onLikedSongsClick = { viewModel.setLibrarySubScreen("liked") },
                                                    onPlaylistsClick = { viewModel.setLibrarySubScreen("albums") },
                                                    onAlbumCollectionClick = { viewModel.setLibrarySubScreen("albums") },
                                                    onPlaylistClick = { playlist -> viewModel.selectPlaylist(playlist) },
                                                    onSongClick = { song -> viewModel.playSong(song, songs) },
                                                    onFavoriteClick = { id -> viewModel.toggleFavorite(id) },
                                                    onMoreClick = { song -> viewModel.openSongDetail(song) },
                                                    onCreatePlaylistClick = { viewModel.openCreatePlaylist() },
                                                    onAddLocalSongClick = { filePickerLauncher.launch(arrayOf("audio/*")) }
                                                )
                                            }
                                        }
                                        "album_detail" -> {
                                            selectedAlbum?.let { album ->
                                                AlbumDetailScreen(
                                                    album = album,
                                                    onBackClick = { viewModel.selectAlbum(null) },
                                                    onPlayAllClick = { viewModel.playAlbum(album, false) },
                                                    onShuffleClick = { viewModel.playAlbum(album, true) },
                                                    onToggleSaveAlbum = { viewModel.toggleSaveAlbum(album.title) },
                                                    onSongClick = { song -> viewModel.playSong(song, album.songs) },
                                                    onFavoriteClick = { id -> viewModel.toggleFavorite(id) },
                                                    onMoreClick = { song -> viewModel.openSongDetail(song) }
                                                )
                                            } ?: run {
                                                MyLibraryScreen(
                                                    songs = songs,
                                                    playlists = playlists,
                                                    likedSongs = likedSongs,
                                                    albums = albums,
                                                    onLikedSongsClick = { viewModel.setLibrarySubScreen("liked") },
                                                    onPlaylistsClick = { viewModel.setLibrarySubScreen("albums") },
                                                    onAlbumCollectionClick = { viewModel.setLibrarySubScreen("albums") },
                                                    onPlaylistClick = { playlist -> viewModel.selectPlaylist(playlist) },
                                                    onSongClick = { song -> viewModel.playSong(song, songs) },
                                                    onFavoriteClick = { id -> viewModel.toggleFavorite(id) },
                                                    onMoreClick = { song -> viewModel.openSongDetail(song) },
                                                    onCreatePlaylistClick = { viewModel.openCreatePlaylist() },
                                                    onAddLocalSongClick = { filePickerLauncher.launch(arrayOf("audio/*")) }
                                                )
                                            }
                                        }
                                        else -> {
                                            MyLibraryScreen(
                                                songs = songs,
                                                playlists = playlists,
                                                likedSongs = likedSongs,
                                                albums = albums,
                                                onLikedSongsClick = { viewModel.setLibrarySubScreen("liked") },
                                                onPlaylistsClick = { viewModel.setLibrarySubScreen("albums") },
                                                onAlbumCollectionClick = { viewModel.setLibrarySubScreen("albums") },
                                                onPlaylistClick = { playlist -> viewModel.selectPlaylist(playlist) },
                                                onSongClick = { song -> viewModel.playSong(song, songs) },
                                                onFavoriteClick = { id -> viewModel.toggleFavorite(id) },
                                                onMoreClick = { song -> viewModel.openSongDetail(song) },
                                                onCreatePlaylistClick = { viewModel.openCreatePlaylist() },
                                                onAddLocalSongClick = { filePickerLauncher.launch(arrayOf("audio/*")) }
                                            )
                                        }
                                    }
                                }
                                "profile" -> ProfileScreen(
                                    username = username,
                                    email = email,
                                    songCount = songs.size,
                                    favoriteCount = likedSongs.size,
                                    playlistCount = playlists.size,
                                    themeMode = themeMode,
                                    onSetThemeMode = { viewModel.setThemeMode(it) },
                                    onUpdateUsername = { viewModel.updateUsername(it) }
                                )
                            }
                        }
                    }
                }

                // Song Detail Bottom Sheet
                selectedSongForDetail?.let { song ->
                    SongDetailBottomSheet(
                        song = song,
                        onDismiss = { viewModel.closeSongDetail() },
                        onPlayClick = { viewModel.playSong(song, songs) },
                        onFavoriteClick = { viewModel.toggleFavorite(song.id) },
                        onAddToPlaylistClick = {
                            viewModel.closeSongDetail()
                            viewModel.openAddSongToPlaylist(song)
                        }
                    )
                }

                // Add to Playlist Dialog
                if (isAddSongToPlaylistOpen && songForPlaylist != null) {
                    AddSongToPlaylistDialog(
                        playlists = playlists,
                        onDismiss = { viewModel.closeAddSongToPlaylist() },
                        onPlaylistSelected = { playlist ->
                            viewModel.addSongToPlaylist(playlist.id, songForPlaylist!!.id)
                            viewModel.closeAddSongToPlaylist()
                        }
                    )
                }
            }
        }
    }
}
