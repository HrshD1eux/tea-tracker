package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TeaPrimaryDark,
    secondary = TeaSecondaryDark,
    tertiary = TeaTertiaryDark,
    background = TeaBackgroundDark,
    surface = TeaSurfaceDark,
    onPrimary = TeaOnPrimaryDark,
    onSecondary = TeaOnSecondaryDark,
    onBackground = TeaOnBackgroundDark,
    onSurface = TeaOnSurfaceDark,
    primaryContainer = TeaSurfaceDark,
    onPrimaryContainer = TeaPrimaryDark,
    secondaryContainer = TeaSurfaceDark,
    onSecondaryContainer = TeaPrimaryLightDark,
    tertiaryContainer = TeaSurfaceDark,
    onTertiaryContainer = TeaTertiaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = TeaPrimary,
    secondary = TeaSecondary,
    tertiary = TeaTertiary,
    background = TeaBackground,
    surface = TeaSurface,
    onPrimary = TeaOnPrimary,
    onSecondary = TeaOnSecondary,
    onBackground = TeaOnBackground,
    onSurface = TeaOnSurface,
    primaryContainer = Color(0xFFEFEBE9),     // Elegant light cocoa tint
    onPrimaryContainer = TeaPrimary,
    secondaryContainer = Color(0xFFE6BEA5),   // Dynamic peach/biscuit tint
    onSecondaryContainer = TeaSecondary,
    tertiaryContainer = Color(0xFFFDF0E6),     // Warm biscuit glow
    onTertiaryContainer = TeaPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We disable dynamic color to preserve the custom hand-picked Tea branding of our palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
