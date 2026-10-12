package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimeViolet,
    onPrimary = Color.White,
    primaryContainer = CosmicSurfaceVariant,
    onPrimaryContainer = PrimeCyan,
    secondary = PrimeCyan,
    onSecondary = Color.Black,
    secondaryContainer = CosmicSurfaceVariant,
    onSecondaryContainer = Color.White,
    tertiary = PrimePink,
    background = CosmicBackground,
    onBackground = DarkTextPrimary,
    surface = CosmicSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = CosmicSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = CosmicBorder
)

@Composable
fun MyApplicationTheme(
    accentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val activeScheme = if (accentColor != null) {
        DarkColorScheme.copy(
            primary = accentColor,
            primaryContainer = accentColor.copy(alpha = 0.2f),
            onPrimaryContainer = Color.White
        )
    } else {
        DarkColorScheme
    }

    MaterialTheme(
        colorScheme = activeScheme,
        typography = Typography,
        content = content
    )
}
