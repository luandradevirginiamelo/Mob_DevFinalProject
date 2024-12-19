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
    primary = Color(0xFFFFEB3B), // Amarelo vibrante
    secondary = Color(0xFFFFF176), // Amarelo suave
    tertiary = Color(0xFFFFFFE0), // Amarelo muito claro
    background = Color(0xFFFFFFFF), // Branco para fundo
    surface = Color(0xFFFDF5E6), // Branco com tom quente
    onPrimary = Color.Black, // Texto preto para contraste no amarelo
    onBackground = Color(0xFF212121), // Preto suave para texto no fundo branco
    onSurface = Color.Black // Texto preto para superfícies claras
)

private val NightClubLightColors = lightColorScheme(
    primary = Color(0xFF212121), // Preto suave
    secondary = Color(0xFF424242), // Cinza escuro
    tertiary = Color(0xFF616161), // Cinza médio
    background = Color(0xFF9E9E9E), // Cinza claro para o fundo
    surface = Color(0xFFBDBDBD), // Cinza mais claro para a superfície
    onPrimary = Color.White, // Texto branco para contraste em fundo preto
    onBackground = Color.Black, // Texto preto em fundo cinza claro
    onSurface = Color.Black // Texto preto em superfícies claras

    )

// Gradient background brush
val gradientBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF212121), // Preto suave
        Color(0xFF424242), // Cinza escuro
        Color(0xFF757575)  // Cinza médio
    ),
    startY = 0.0f,
    endY = 1000.0f,
    tileMode = TileMode.Clamp
)

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