package com.example.ui.screens

import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.viewmodel.MainViewModel

@Composable
fun VideoPlayerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeVideo by viewModel.activeVideoItem.collectAsState()
    val context = LocalContext.current
    
    if (activeVideo == null) return

    var isBuffering by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("video_player_screen")
    ) {
        // Native Android Video Player
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    // Configure MediaController for seek, rewind, fast-forward, play/pauseHUD overlays!
                    val mediaController = MediaController(ctx)
                    mediaController.setAnchorView(this)
                    setMediaController(mediaController)
                    
                    // Bind content URI
                    setVideoPath(activeVideo!!.url)
                    
                    setOnPreparedListener { player ->
                        isBuffering = false
                        player.start()
                    }
                    setOnErrorListener { _, what, extra ->
                        isBuffering = false
                        false
                    }
                    setOnCompletionListener {
                        // Switch active video off
                        viewModel.closeVideoPlayer()
                    }
                }
            },
            update = { view ->
                // Ensure view is loading correct video path if it shifts dynamically
                if (view.tag != activeVideo!!.url) {
                    isBuffering = true
                    view.tag = activeVideo!!.url
                    view.setVideoPath(activeVideo!!.url)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        )

        // Overlay Header Bar Details
        Surface(
            color = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeVideo!!.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = activeVideo!!.artist,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(
                    onClick = { viewModel.closeVideoPlayer() },
                    modifier = Modifier.testTag("close_video_player")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar reproductor",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Overlay loader during network resource buffer prepares
        if (isBuffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Preparando video...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
