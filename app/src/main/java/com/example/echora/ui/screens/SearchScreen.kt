package com.example.echora.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echora.data.model.Song
import com.example.echora.data.repository.MusicRepository
import com.example.echora.ui.components.SongRow
import com.example.echora.ui.theme.EchoraPrimary
import com.example.echora.ui.theme.EchoraSurfaceVariant
import com.example.echora.ui.theme.EchoraTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchQuery: String,
    selectedCategory: String,
    songs: List<Song>,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onMoreClick: (Song) -> Unit
) {
    val filteredSongs = songs.filter { song ->
        val matchesQuery = searchQuery.isBlank() ||
                song.title.contains(searchQuery, ignoreCase = true) ||
                song.artist.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || song.genre.equals(selectedCategory, ignoreCase = true)
        matchesQuery && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
    ) {
        Text(
            text = "Search",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Search songs or artists...", color = EchoraTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = EchoraTextSecondary) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = EchoraTextSecondary)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EchoraPrimary,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = EchoraSurfaceVariant,
                unfocusedContainerColor = EchoraSurfaceVariant,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MusicRepository.categories.forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category) },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EchoraPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = EchoraSurfaceVariant,
                        labelColor = EchoraTextSecondary
                    ),
                    border = null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No songs found",
                    color = EchoraTextSecondary,
                    fontSize = 16.sp
                )
            }
        } else {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                filteredSongs.forEach { song ->
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
