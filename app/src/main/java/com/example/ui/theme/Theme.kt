package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = YouTubeRedContainerDark,
    onPrimaryContainer = Color.White,
    secondary = YouTubeRed,
    onSecondary = Color.White,
    secondaryContainer = YouTubeSurfaceVariantDark,
    onSecondaryContainer = Color.White,
    tertiary = ElectricBlue,
    onTertiary = Color.Black,
    background = YouTubeBgDark,
    onBackground = YouTubeTextDark,
    surface = YouTubeSurfaceDark,
    onSurface = YouTubeTextDark,
    surfaceVariant = YouTubeSurfaceVariantDark,
    onSurfaceVariant = YouTubeTextSecondaryDark,
    outline = YouTubeOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = YouTubeRedContainer,
    onPrimaryContainer = YouTubeRedDark,
    secondary = YouTubeRed,
    onSecondary = Color.White,
    secondaryContainer = YouTubeSurfaceVariantLight,
    onSecondaryContainer = YouTubeTextLight,
    tertiary = ElectricBlue,
    onTertiary = Color.White,
    background = YouTubeBgLight,
    onBackground = YouTubeTextLight,
    surface = YouTubeSurfaceLight,
    onSurface = YouTubeTextLight,
    surfaceVariant = YouTubeSurfaceVariantLight,
    onSurfaceVariant = YouTubeTextSecondaryLight,
    outline = YouTubeOutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // YouTube Mobile App Default Dark UI
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
