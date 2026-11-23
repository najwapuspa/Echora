package com.example.echora.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echora.data.model.Album
import com.example.echora.data.model.Playlist
import com.example.echora.data.model.Song
import com.example.echora.ui.components.SongRow
import com.example.echora.ui.theme.EchoraCard
import com.example.echora.ui.theme.EchoraPrimary
import com.example.echora.ui.theme.EchoraTextSecondary

@Composable
fun MyLibraryScreen(
    songs: List<Song>,
    playlists: List<Playlist>,
    likedSongs: List<Song>,
    albums: List<Album>,
    onLikedSongsClick: () -> Unit,
    onPlaylistsClick: () -> Unit,
    onAlbumCollectionClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onMoreClick: (Song) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onAddLocalSongClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Your Library",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Your music, your collection",
                    color = EchoraTextSecondary,
                    fontSize = 13.sp
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCreatePlaylistClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EchoraCard)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Playlist", tint = EchoraPrimary)
                }
                IconButton(
                    onClick = onAddLocalSongClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EchoraCard)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = "Add Local MP3", tint = EchoraPrimary)
                }
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = EchoraPrimary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            val tabs = listOf(
                "Playlists (${playlists.size})",
                "Favorites (${likedSongs.size})",
                "Albums (${albums.size})",
                "Local (${songs.count { !it.localUri.isNullOrBlank() }})"
            )
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) Color.White else EchoraTextSecondary,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Belum ada playlist", color = EchoraTextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onCreatePlaylistClick,
                                colors = ButtonDefaults.buttonColors(containerColor = EchoraPrimary),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Buat Playlist Pertama", color = Color.White)
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(playlists) { playlist ->
                            PlaylistGridCard(playlist = playlist, onClick = { onPlaylistClick(playlist) })
                        }
                    }
                }
            }
            1 -> {
                if (likedSongs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada lagu favorit", color = EchoraTextSecondary)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        likedSongs.forEach { song ->
                            SongRow(
                                song = song,
                                onClick = { onSongClick(song) },
                                onFavoriteClick = { onFavoriteClick(song.id) },
                                onMoreClick = { onMoreClick(song) }
                            )
                        }
                    }
                }
            }
            2 -> {
                if (albums.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada album tersedia", color = EchoraTextSecondary)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(albums) { album ->
                            AlbumGridCardItem(album = album, onClick = { onAlbumCollectionClick() })
                        }
                    }
                }
            }
            3 -> {
                val localSongs = songs.filter { !it.localUri.isNullOrBlank() }
                if (localSongs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Belum ada lagu lokal", color = EchoraTextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onAddLocalSongClick,
                                colors = ButtonDefaults.buttonColors(containerColor = EchoraPrimary),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Pilih MP3 dari HP", color = Color.White)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        localSongs.forEach { song ->
                            SongRow(
                                song = song,
                                onClick = { onSongClick(song) },
                                onFavoriteClick = { onFavoriteClick(song.id) },
                                onMoreClick = { onMoreClick(song) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistGridCard(playlist: Playlist, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EchoraCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Image(
                painter = painterResource(id = playlist.coverRes),
                contentDescription = playlist.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EchoraPrimary)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = playlist.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${playlist.songCount} songs",
                color = EchoraTextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AlbumGridCardItem(album: Album, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EchoraCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Image(
                painter = painterResource(id = album.coverRes),
                contentDescription = album.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EchoraPrimary)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = album.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = album.artist,
                color = EchoraTextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
