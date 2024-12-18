package com.example.finalproject_mobdev.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode

// Custom color schemes
private val NightClubDarkColors = darkColorScheme(
    primary = Color(0xFF8E24AA),
    secondary = Color(0xFFAB47BC),
    tertiary = Color(0xFFE1BEE7),
    background = Color(0xFF1A1A2E),
    surface = Color(0xFF121212),
    onPrimary = Color.White,
    onBackground = Color(0xFFEDE7F6),
    onSurface = Color.White
)

private val NightClubLightColors = lightColorScheme(
    primary = Color(0xFF8E24AA),
    secondary = Color(0xFFBA68C8),
    tertiary = Color(0xFFD1C4E9),
    background = Color(0xFFF3E5F5),
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color(0xFF4A148C),
    onSurface = Color.Black
)

// Gradient background brush
val gradientBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF4A148C),
        Color(0xFF8E24AA),
        Color(0xFFBA68C8)
    ),
    startY = 0.0f,
    endY = 1000.0f,
    tileMode = TileMode.Clamp
)


// Apply the custom theme
@Composable
fun Finalproject_MOBDEVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NightClubDarkColors else NightClubLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Default typography
        shapes = Shapes,         // Default shapes
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradientBrush) // Gradient background
            ) {
                content()
            }
        }
    )
}
