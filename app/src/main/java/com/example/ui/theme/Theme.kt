package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color.Black,
    primaryContainer = CyanAccentVariant,
    onPrimaryContainer = Color.White,
    secondary = MagentaAccent,
    onSecondary = Color.White,
    tertiary = GoldAccent,
    background = StudioDarkCanvas,
    onBackground = TextPrimaryDark,
    surface = StudioDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = StudioDarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark
)

@Composable
fun JianYingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}

