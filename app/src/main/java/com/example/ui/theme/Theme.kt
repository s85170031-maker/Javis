package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisDarkColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisDeepDark,
    primaryContainer = JarvisSurfaceElevated,
    onPrimaryContainer = JarvisCyan,
    secondary = JarvisTeal,
    onSecondary = JarvisDeepDark,
    secondaryContainer = JarvisSurfaceDark,
    onSecondaryContainer = JarvisCoreGlow,
    tertiary = JarvisElectricBlue,
    onTertiary = Color.White,
    background = JarvisDeepDark,
    onBackground = TextPrimary,
    surface = JarvisSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = JarvisSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = JarvisCardBorder,
    error = JarvisAlertRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve distinctive Stark HUD theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisDarkColorScheme,
        typography = Typography,
        content = content
    )
}
