package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SoulLightColorScheme = lightColorScheme(
    primary = OrchidPrimary,
    onPrimary = Color.White,
    primaryContainer = ElevatedSurface,
    onPrimaryContainer = DarkAccent,
    secondary = RosePrimary,
    onSecondary = Color.White,
    secondaryContainer = ElevatedSurface,
    onSecondaryContainer = DarkAccent,
    tertiary = PeriwinklePrimary,
    onTertiary = Color.White,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderFocus,
    error = SemanticError,
    onError = Color.White
)

@Composable
fun SoulmateTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SoulLightColorScheme,
        typography = SoulTypography,
        shapes = SoulShapes,
        content = content
    )
}

// Backward compatible alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SoulmateTheme(content = content)
}
