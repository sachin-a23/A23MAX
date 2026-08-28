package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonGold,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF332002),
    onPrimaryContainer = NeonGoldBright,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF032D38),
    onSecondaryContainer = NeonCyanBright,
    tertiary = NeonGreen,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimaryLight,
    surface = DarkSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = GlassBorderNeon,
    outlineVariant = GlassBorderCyan,
    error = NeonRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
