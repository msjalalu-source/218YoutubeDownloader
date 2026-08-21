package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HighDensityPurpleDark,
    onPrimary = HighDensityOnPurpleContainer,
    primaryContainer = HighDensityPurpleContainerDark,
    onPrimaryContainer = HighDensityOnPurpleContainerDark,
    secondary = HighDensityPurpleDark,
    onSecondary = HighDensityOnPurpleContainer,
    secondaryContainer = HighDensityPurpleContainerDark,
    onSecondaryContainer = HighDensityOnPurpleContainerDark,
    tertiary = HighDensityAmber,
    onTertiary = Color.Black,
    background = HighDensityBgDark,
    onBackground = HighDensityTextDark,
    surface = HighDensitySurfaceDark,
    onSurface = HighDensityTextDark,
    surfaceVariant = HighDensitySurfaceVariantDark,
    onSurfaceVariant = HighDensityTextSecondaryDark,
    outline = HighDensityOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = HighDensityPurple,
    onPrimary = Color.White,
    primaryContainer = HighDensityPurpleContainer,
    onPrimaryContainer = HighDensityOnPurpleContainer,
    secondary = HighDensityPurple,
    onSecondary = Color.White,
    secondaryContainer = HighDensitySurfaceVariant,
    onSecondaryContainer = HighDensityOnPurpleContainer,
    tertiary = HighDensityAmber,
    onTertiary = Color.Black,
    background = HighDensityBg,
    onBackground = HighDensityText,
    surface = HighDensitySurface,
    onSurface = HighDensityText,
    surfaceVariant = HighDensitySurfaceVariant,
    onSurfaceVariant = HighDensityTextSecondary,
    outline = HighDensityOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // High Density design palette defaults to the crisp warm light layout
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

