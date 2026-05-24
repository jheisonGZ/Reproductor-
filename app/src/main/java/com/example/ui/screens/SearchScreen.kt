package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.ui.components.GlassBackgroundBox
import com.example.ui.components.glassmorphism
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    // Pastel-colored visual genres
    val genreCards = listOf(
        GenreItem("Pop Latino", Color(0xC0C3965F), "https://picsum.photos/id/111/300/300"),
        GenreItem("Lo-Fi Study", Color(0xC0E4868A), "https://picsum.photos/id/212/300/300"),
        GenreItem("Podcasts", Color(0xC027855A), "https://picsum.photos/id/313/300/300"),
        GenreItem("Nuevos", Color(0xC08D67AB), "https://picsum.photos/id/414/300/300"),
        GenreItem("Gaming", Color(0xC0477AAB), "https://picsum.photos/id/515/300/300"),
        GenreItem("Deporte", Color(0xC0E13300), "https://picsum.photos/id/616/300/300"),
        GenreItem("Rock Clasico", Color(0xC0148A08), "https://picsum.photos/id/717/300/300"),
        GenreItem("Cinemático", Color(0xC07D4B32), "https://picsum.photos/id/818/300/300")
    )

    GlassBackgroundBox(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Buscar",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            )

            // Modern Glass Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("search_text_input")
                    .glassmorphism(cornerRadius = 24.dp, backgroundColor = Color(0x1FFFFFFF)),
                placeholder = { Text("¿Qué deseas escuchar?", color = Color.LightGray.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.setSearchQuery("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            if (searchQuery.isEmpty()) {
                // Recommendation genres
                Text(
                    text = "Explorar todo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 120.dp) // padding for floating controls
                ) {
                    items(genreCards) { genre ->
                        GenreCard(genre = genre, onSelect = { viewModel.setSearchQuery(genre.title) })
                    }
                }
            } else {
                // Filtered results list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(searchResults) { item ->
                        SearchResultRow(
                            item = item,
                            onClick = {
                                // Find corresponding playlist (e.g. video playlist vs music playlist)
                                val matchedQueue = if (item.isVideo) viewModel.videoCatalog else viewModel.musicCatalog
                                viewModel.playMedia(item, matchedQueue)
                            }
                        )
                    }

                    if (searchResults.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Ningún resultado encontrado",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Asegúrate de que las palabras estén bien escritas o prueba palabras clave.",
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class GenreItem(val title: String, val color: Color, val image: String)

@Composable
fun GenreCard(genre: GenreItem, onSelect: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onSelect() }
            .testTag("genre_card_${genre.title}")
            .glassmorphism(
                cornerRadius = 12.dp,
                backgroundColor = genre.color.copy(alpha = 0.25f),
                borderColor = genre.color.copy(alpha = 0.5f)
            )
            .padding(12.dp)
    ) {
        Text(
            text = genre.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.6f)
        )

        // Corner artwork
        AsyncImage(
            model = genre.image,
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .offset(x = 20.dp, y = 20.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.BottomEnd),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun SearchResultRow(item: MediaItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("search_result_${item.id}")
            .glassmorphism(cornerRadius = 10.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.thumbnailUrl,
            contentDescription = item.title,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (item.isVideo) Icons.Default.Tv else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (item.isVideo) Color.Cyan else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${if (item.isVideo) "Video" else "Música"} • ${item.artist}",
                    color = Color.LightGray.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
