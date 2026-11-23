package com.example.echora.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echora.data.model.Playlist
import com.example.echora.data.model.Song
import com.example.echora.ui.components.SongRow
import com.example.echora.ui.theme.EchoraPrimary
import com.example.echora.ui.theme.EchoraSurfaceVariant
import com.example.echora.ui.theme.EchoraTextSecondary

@Composable
fun LibraryScreen(
    songs: List<Song>,
    playlists: List<Playlist>,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onAddLocalSongClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    val favoriteSongs = songs.filter { it.isFavorite }
    val localSongs = songs.filter { !it.localUri.isNullOrBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Library",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAddLocalSongClick) {
                    Icon(Icons.Default.FolderOpen, contentDescription = "Tambah dari HP", tint = EchoraPrimary)
                }
                if (selectedTab == 1) {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Playlist", tint = EchoraPrimary)
                    }
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = EchoraPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Favorites (${favoriteSongs.size})", color = if (selectedTab == 0) EchoraPrimary else EchoraTextSecondary) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Playlists (${playlists.size})", color = if (selectedTab == 1) EchoraPrimary else EchoraTextSecondary) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Local (${localSongs.size})", color = if (selectedTab == 2) EchoraPrimary else EchoraTextSecondary) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                if (favoriteSongs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No favorite songs yet", color = EchoraTextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onAddLocalSongClick,
                                colors = ButtonDefaults.buttonColors(containerColor = EchoraPrimary)
                            ) {
                                Text("Pilih Lagu dari HP", color = Color.White)
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        favoriteSongs.forEach { song ->
                            SongRow(
                                song = song,
                                onClick = { onSongClick(song) },
                                onFavoriteClick = { onFavoriteClick(song.id) }
                            )
                        }
                    }
                }
            }
            1 -> {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    playlists.forEach { playlist ->
                        PlaylistCard(playlist = playlist, onClick = { onPlaylistClick(playlist) })
                    }
                }
            }
            2 -> {
                if (localSongs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Belum ada lagu lokal", color = EchoraTextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onAddLocalSongClick,
                                colors = ButtonDefaults.buttonColors(containerColor = EchoraPrimary)
                            ) {
                                Text("Tambah MP3 dari HP", color = Color.White)
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        localSongs.forEach { song ->
                            SongRow(
                                song = song,
                                onClick = { onSongClick(song) },
                                onFavoriteClick = { onFavoriteClick(song.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Create", color = EchoraPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = EchoraTextSecondary)
                }
            },
            containerColor = EchoraSurfaceVariant
        )
    }
}

@Composable
fun PlaylistCard(playlist: Playlist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EchoraSurfaceVariant)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = playlist.coverRes),
            contentDescription = playlist.name,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(EchoraPrimary)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = playlist.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "${playlist.songCount} songs", color = EchoraTextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = EchoraPrimary)
    }
}
