package com.example.echora.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echora.data.model.Song
import com.example.echora.ui.theme.EchoraPrimary
import com.example.echora.ui.theme.EchoraSurfaceVariant
import com.example.echora.ui.theme.EchoraTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailBottomSheet(
    song: Song,
    onDismiss: () -> Unit,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = EchoraSurfaceVariant,
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = song.coverRes),
                contentDescription = song.title,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EchoraPrimary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = song.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${song.artist} • ${song.album}",
                color = EchoraTextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Duration: ${song.duration} | Genre: ${song.genre}",
                color = EchoraTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = Color.DarkGray)

            Spacer(modifier = Modifier.height(8.dp))

            BottomSheetActionItem(
                icon = Icons.Default.PlayArrow,
                label = "Putar Lagu",
                onClick = {
                    onPlayClick()
                    onDismiss()
                }
            )

            BottomSheetActionItem(
                icon = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = if (song.isFavorite) "Hapus dari Favorites" else "Tambah ke Favorites",
                tint = if (song.isFavorite) EchoraPrimary else Color.White,
                onClick = {
                    onFavoriteClick()
                    onDismiss()
                }
            )

            BottomSheetActionItem(
                icon = Icons.Default.PlaylistAdd,
                label = "Tambah ke Playlist",
                onClick = {
                    onAddToPlaylistClick()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun BottomSheetActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, color = tint, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
