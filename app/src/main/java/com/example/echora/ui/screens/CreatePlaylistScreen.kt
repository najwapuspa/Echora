package com.example.echora.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echora.R
import com.example.echora.data.model.Song
import com.example.echora.ui.theme.EchoraCard
import com.example.echora.ui.theme.EchoraPrimary
import com.example.echora.ui.theme.EchoraSurface
import com.example.echora.ui.theme.EchoraTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistScreen(
    allSongs: List<Song>,
    onBackClick: () -> Unit,
    onSavePlaylist: (String, List<String>, String, Boolean, Int) -> Unit
) {
    var playlistName by remember { mutableStateOf("New Playlist") }
    var isPrivate by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    val selectedSongIds = remember { mutableStateListOf<String>() }
    var selectedTab by remember { mutableStateOf(0) } // 0: Songs, 1: Details
    var selectedCoverRes by remember { mutableStateOf(R.drawable.album_cover_1) }
    var showCoverPicker by remember { mutableStateOf(false) }

    val covers = listOf(
        R.drawable.album_cover_1,
        R.drawable.album_cover_2,
        R.drawable.album_cover_3,
        R.drawable.album_cover_4,
        R.drawable.album_cover_5,
        R.drawable.album_cover_6,
        R.drawable.album_cover_7,
        R.drawable.album_cover_8
    )

    val filteredSongs = allSongs.filter {
        searchQuery.isBlank() ||
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true)
    }

    var isSavedClicked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(bottom = 90.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(EchoraCard)
                    .clickable { showCoverPicker = true },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = selectedCoverRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Change Cover", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = EchoraCard,
                        unfocusedContainerColor = EchoraCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(220.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Edit, contentDescription = "Edit Title", tint = EchoraPrimary, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (playlistName.isNotBlank() && !isSavedClicked) {
                        isSavedClicked = true
                        onSavePlaylist(playlistName, selectedSongIds, description, isPrivate, selectedCoverRes)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                enabled = !isSavedClicked
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(EchoraPrimary, Color(0xFFD946EF))
                            ),
                            shape = RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SAVE", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = { selectedTab = 0 }) {
                    Text(
                        "Songs",
                        color = if (selectedTab == 0) Color.White else EchoraTextSecondary,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                TextButton(onClick = { selectedTab = 1 }) {
                    Text(
                        "Details",
                        color = if (selectedTab == 1) Color.White else EchoraTextSecondary,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("SEARCH MUSIC", color = EchoraTextSecondary, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EchoraTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoraPrimary,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedContainerColor = EchoraSurface,
                            unfocusedContainerColor = EchoraSurface,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    filteredSongs.forEach { song ->
                        val isSelected = selectedSongIds.contains(song.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedSongIds.remove(song.id)
                                    else selectedSongIds.add(song.id)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = song.coverRes),
                                contentDescription = song.title,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EchoraCard)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = song.artist, color = EchoraTextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text(text = song.duration, color = EchoraTextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = {
                                    if (isSelected) selectedSongIds.remove(song.id)
                                    else selectedSongIds.add(song.id)
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (isSelected) EchoraPrimary else EchoraTextSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Private Playlist", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Playable only by you or invited users", color = EchoraTextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EchoraPrimary)
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Playlist Description", color = EchoraTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EchoraPrimary,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedContainerColor = EchoraSurface,
                            unfocusedContainerColor = EchoraSurface,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }
        }
    }

    if (showCoverPicker) {
        AlertDialog(
            onDismissRequest = { showCoverPicker = false },
            title = { Text("Pilih Cover Playlist", color = Color.White) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    covers.chunked(2).forEach { rowCovers ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowCovers.forEach { cover ->
                                Image(
                                    painter = painterResource(id = cover),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedCoverRes = cover
                                            showCoverPicker = false
                                        }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCoverPicker = false }) {
                    Text("Tutup", color = EchoraPrimary)
                }
            },
            containerColor = EchoraCard
        )
    }
}
