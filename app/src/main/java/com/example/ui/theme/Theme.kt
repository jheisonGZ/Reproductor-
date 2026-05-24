package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentIndigo,
    secondary = AccentLavender,
    tertiary = AccentLavender,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkElevatedSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = ErrorColor
)

private val LightColorScheme = lightColorScheme(
    primary = SpotifyGreen,
    secondary = AccentGreen,
    tertiary = Color(0xFF1DB954),
    background = Color(0xFFF9F9F9),
    surface = Color.White,
    surfaceVariant = Color(0xFFECECEC),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF121212),
    onSurface = Color(0xFF121212),
    onSurfaceVariant = Color(0xFF535353)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default like Spotify
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
