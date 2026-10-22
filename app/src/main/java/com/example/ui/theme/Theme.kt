package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmerald,
    onPrimary = Color(0xFF003822),
    primaryContainer = DarkEmeraldContainer,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = MintSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFA7F3D0),
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldOnContainer,
    secondary = MintSecondary,
    onSecondary = MintOnSecondary,
    secondaryContainer = MintContainer,
    onSecondaryContainer = MintOnContainer,
    background = WhiteBackground,
    onBackground = TextPrimary,
    surface = WhiteSurface,
    onSurface = TextPrimary,
    surfaceVariant = EmeraldContainer,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = Color(0xFFF1F5F9)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional white-green styling
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
