package com.example.echora.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
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
fun PlaylistDetailScreen(
    playlist: Playlist,
    playlistSongs: List<Song>,
    onBackClick: () -> Unit,
    onPlayAllClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onMoreClick: (Song) -> Unit,
    onDeletePlaylist: () -> Unit,
    onRenamePlaylist: (String) -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(playlist.name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Row {
                IconButton(onClick = { showRenameDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = Color.White)
                }
                IconButton(onClick = onDeletePlaylist) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = Color.Red)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = playlist.coverRes),
                contentDescription = playlist.name,
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EchoraPrimary)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = playlist.name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${playlistSongs.size} songs",
                color = EchoraTextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPlayAllClick,
                colors = ButtonDefaults.buttonColors(containerColor = EchoraPrimary),
                shape = RoundedCornerShape(50),
                modifier = Modifier.width(180.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play All", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (playlistSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Playlist ini masih kosong", color = EchoraTextSecondary)
            }
        } else {
            Column {
                playlistSongs.forEach { song ->
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

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Playlist") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onRenamePlaylist(newName)
                            showRenameDialog = false
                        }
                    }
                ) {
                    Text("Save", color = EchoraPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = EchoraTextSecondary)
                }
            },
            containerColor = EchoraSurfaceVariant
        )
    }
}
