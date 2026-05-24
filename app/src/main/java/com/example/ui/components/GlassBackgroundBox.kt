package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassBackgroundBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E11))
            .drawBehind {
                // Glow 1: Top-left vibrant purple ambient light blur
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x664B3D8F), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(x = size.width * 0.1f, y = size.height * 0.15f),
                        radius = size.width * 0.8f
                    ),
                    radius = size.width * 0.8f,
                    center = androidx.compose.ui.geometry.Offset(x = size.width * 0.1f, y = size.height * 0.15f)
                )

                // Glow 2: Bottom-right deep indigo/teal ambient light blur
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x661E4A6E), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(x = size.width * 0.9f, y = size.height * 0.85f),
                        radius = size.width * 0.8f
                    ),
                    radius = size.width * 0.8f,
                    center = androidx.compose.ui.geometry.Offset(x = size.width * 0.9f, y = size.height * 0.85f)
                )
            }
    ) {
        content()
    }
}

// Reusable glassy modifier to make items look like beautiful card glass panes
fun Modifier.glassmorphism(
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = Color(0x1AFFFFFF), // Semi-transparent white
    borderColor: Color = Color(0x24FFFFFF)      // Transparent fine highlight
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
