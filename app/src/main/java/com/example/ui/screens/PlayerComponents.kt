package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.ui.components.GlassBackgroundBox
import com.example.ui.components.glassmorphism
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiniPlayer(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentItem by viewModel.currentMediaItem.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val currentPosMs by viewModel.currentPositionMs.collectAsState()
    val isFavorite by viewModel.isCurrentItemFavorite.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (currentItem == null) return

    val progress = if (durationMs > 0) currentPosMs.toFloat() / durationMs else 0f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { viewModel.setPlayerExpanded(true) }
            .testTag("mini_player_surface")
            .glassmorphism(cornerRadius = 16.dp, backgroundColor = Color(0x2EFFFFFF), borderColor = Color(0x3DFFFFFF)),
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Artwork thumbnail
                AsyncImage(
                    model = currentItem!!.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Text labels with horizontal marquee scroll block!
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentItem!!.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.basicMarquee()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentItem!!.artist,
                        color = Color.LightGray.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        modifier = Modifier.basicMarquee()
                    )
                }

                // Small favorite button
                IconButton(
                    onClick = { viewModel.toggleFavorite(currentItem!!) },
                    modifier = Modifier.testTag("mini_player_favorite_toggle")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) MaterialTheme.colorScheme.secondary else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play / Pause micro click trigger
                IconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.testTag("mini_player_play_pause")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Real-time linear progress bar mapped directly to bottom border
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = Color(0x26FFFFFF)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullPlayer(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentItem by viewModel.currentMediaItem.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val currentPosMs by viewModel.currentPositionMs.collectAsState()
    val isFavorite by viewModel.isCurrentItemFavorite.collectAsState()
    val isShuffle by viewModel.isShuffleEnabled.collectAsState()
    val isRepeat by viewModel.isRepeatEnabled.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (currentItem == null) return

    // Slider state tracking
    var sliderValue by remember { mutableFloatStateOf(0f) }
    var isUserSeeking by remember { mutableStateOf(false) }

    val trackProgress = if (durationMs > 0) currentPosMs.toFloat() / durationMs else 0f

    // Sync slider with player position unless the user is dragging the track pointer manually
    LaunchedEffect(currentPosMs, isUserSeeking) {
        if (!isUserSeeking) {
            sliderValue = trackProgress
        }
    }

    GlassBackgroundBox(
        modifier = modifier.testTag("full_player")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.setPlayerExpanded(false) },
                    modifier = Modifier.testTag("collapse_player_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Minimizar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "REPRODUCIENDO DESDE LA LISTA",
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Cola",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Beautiful Large Album Art
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(40.dp)) // Tailwind's rounded-[40px]
                    .scale(if (isPlaying) 1.0f else 0.93f) // pulse aesthetic shift depending on play state
                    .testTag("player_album_art")
                    .glassmorphism(cornerRadius = 40.dp, backgroundColor = Color(0x33000000), borderColor = Color(0x26FFFFFF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                AsyncImage(
                    model = currentItem!!.thumbnailUrl,
                    contentDescription = currentItem!!.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.weight(0.12f))

            // Glassmorphism card for metadata details and all playing widgets
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphism(
                        cornerRadius = 32.dp, // Tailwind rounded-[32px]
                        backgroundColor = Color(0x19FFFFFF), // white/10 translucent glass background
                        borderColor = Color(0x26FFFFFF)      // white/10 light highlights
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Metadata Detail & Favorite toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentItem!!.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .basicMarquee()
                                .fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentItem!!.artist,
                            color = Color(0xB3C7D2FE), // light indigo text
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .basicMarquee()
                                .fillMaxWidth()
                        )
                    }

                    // Favorite heart toggle
                    IconButton(
                        onClick = { viewModel.toggleFavorite(currentItem!!) },
                        modifier = Modifier.testTag("full_player_favorite_toggle")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) MaterialTheme.colorScheme.secondary else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Track Seek Slider Timeline
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            isUserSeeking = true
                            sliderValue = it
                        },
                        onValueChangeFinished = {
                            isUserSeeking = false
                            viewModel.seekTo((sliderValue * durationMs).toLong())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tracker_progress_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = MaterialTheme.colorScheme.secondary,
                            inactiveTrackColor = Color(0x26FFFFFF)
                        )
                    )

                    // Timestamp counters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val progressMs = (sliderValue * durationMs).toLong()
                        Text(
                            text = formatMillis(progressMs),
                            color = Color.LightGray.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = formatMillis(durationMs),
                            color = Color.LightGray.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Complete Player deck control row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle Button indicator
                    IconButton(
                        onClick = { viewModel.toggleShuffle() },
                        modifier = Modifier.testTag("shuffle_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Aleatorio",
                            tint = if (isShuffle) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Skip Backwards
                    IconButton(
                        onClick = { viewModel.playPrevious() },
                        modifier = Modifier
                            .scale(1.2f)
                            .testTag("skip_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Anterior",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Large center Play/Pause control circle card matching tailwind rounded-[24px] box-shadows style
                    Card(
                        onClick = { viewModel.togglePlayPause() },
                        shape = RoundedCornerShape(24.dp), // custom modern Tailwind shape instead of standard circle
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .size(width = 64.dp, height = 64.dp)
                            .testTag("fab_play_pause")
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    modifier = Modifier.size(26.dp),
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Skip Forwards
                    IconButton(
                        onClick = { viewModel.playNext() },
                        modifier = Modifier
                            .scale(1.2f)
                            .testTag("skip_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Siguiente",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Repeat Button indicator
                    IconButton(
                        onClick = { viewModel.toggleRepeat() },
                        modifier = Modifier.testTag("repeat_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repetir",
                            tint = if (isRepeat) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.15f))
        }
    }
}

// Timing conversion formatting tool helper
private fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}

