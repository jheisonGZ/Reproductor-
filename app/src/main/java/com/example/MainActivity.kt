package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentItem by viewModel.currentMediaItem.collectAsState()
    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsState()
    val activeVideo by viewModel.activeVideoItem.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F0F0F) // Slate background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        // Embed bottom float Mini Player (only appears if there is an active media track)
                        if (currentItem != null) {
                            MiniPlayer(
                                viewModel = viewModel,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .padding(bottom = 4.dp)
                            )
                        }

                        // Bottom navigation bar
                        NavigationBar(
                            containerColor = Color(0xFF121212),
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("app_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == "Inicio",
                                onClick = { viewModel.setTab("Inicio") },
                                modifier = Modifier.testTag("nav_tab_inicio"),
                                label = { Text("Inicio", color = if (currentTab == "Inicio") MaterialTheme.colorScheme.primary else Color.Gray) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Inicio",
                                        tint = if (currentTab == "Inicio") MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color(0xFF1E1E1E)
                                )
                            )

                            NavigationBarItem(
                                selected = currentTab == "Buscar",
                                onClick = { viewModel.setTab("Buscar") },
                                modifier = Modifier.testTag("nav_tab_buscar"),
                                label = { Text("Buscar", color = if (currentTab == "Buscar") MaterialTheme.colorScheme.primary else Color.Gray) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar",
                                        tint = if (currentTab == "Buscar") MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color(0xFF1E1E1E)
                                )
                            )

                            NavigationBarItem(
                                selected = currentTab == "Tu Biblioteca",
                                onClick = { viewModel.setTab("Tu Biblioteca") },
                                modifier = Modifier.testTag("nav_tab_biblioteca"),
                                label = { Text("Biblioteca", color = if (currentTab == "Tu Biblioteca") MaterialTheme.colorScheme.primary else Color.Gray) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.LibraryMusic,
                                        contentDescription = "Tu Biblioteca",
                                        tint = if (currentTab == "Tu Biblioteca") MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color(0xFF1E1E1E)
                                )
                            )
                        }
                    }
                },
                contentWindowInsets = WindowInsets.safeDrawing
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()) // Respect navigation insets smoothly!
                ) {
                    when (currentTab) {
                        "Inicio" -> HomeScreen(viewModel = viewModel)
                        "Buscar" -> SearchScreen(viewModel = viewModel)
                        "Tu Biblioteca" -> LibraryScreen(viewModel = viewModel)
                    }
                }
            }

            // Slide Up Full-screen player animation overlay
            AnimatedVisibility(
                visible = isPlayerExpanded,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.fillMaxSize()
            ) {
                FullPlayer(viewModel = viewModel)
            }

            // Real-time video player reproduction overlay layer
            if (activeVideo != null) {
                VideoPlayerScreen(viewModel = viewModel)
            }
        }
    }
}
