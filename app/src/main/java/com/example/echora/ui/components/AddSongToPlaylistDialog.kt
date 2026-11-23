package com.example.echora.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.echora.data.model.Playlist
import com.example.echora.ui.theme.EchoraPrimary
import com.example.echora.ui.theme.EchoraSurfaceVariant
import com.example.echora.ui.theme.EchoraTextSecondary

@Composable
fun AddSongToPlaylistDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Playlist) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Playlist", color = Color.White) },
        text = {
            if (playlists.isEmpty()) {
                Text("Belum ada playlist. Buat playlist terlebih dahulu di Library.", color = EchoraTextSecondary)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(playlists) { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.name, color = Color.White) },
                            supportingContent = { Text("${playlist.songCount} songs", color = EchoraTextSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlaylistSelected(playlist) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = EchoraPrimary)
            }
        },
        containerColor = EchoraSurfaceVariant
    )
}
