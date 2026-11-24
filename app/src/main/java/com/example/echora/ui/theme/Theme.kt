package com.example.echora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EchoraDarkColorScheme = darkColorScheme(
    primary = EchoraDarkPrimary,
    onPrimary = Color.White,
    secondary = EchoraDarkSecondary,
    onSecondary = Color.Black,
    background = EchoraDarkBackground,
    onBackground = EchoraDarkTextPrimary,
    surface = EchoraDarkSurface,
    onSurface = EchoraDarkTextPrimary,
    surfaceVariant = EchoraDarkCard,
    onSurfaceVariant = EchoraDarkTextSecondary,
    outline = EchoraDarkDivider
)

private val EchoraLightColorScheme = lightColorScheme(
    primary = EchoraLightPrimary,
    onPrimary = Color.White,
    secondary = EchoraLightSecondary,
    onSecondary = Color.White,
    background = EchoraLightBackground,
    onBackground = EchoraLightTextPrimary,
    surface = EchoraLightSurface,
    onSurface = EchoraLightTextPrimary,
    surfaceVariant = EchoraLightCard,
    onSurfaceVariant = EchoraLightTextSecondary,
    outline = EchoraLightDivider
)

@Composable
fun EchoraTheme(
    themeMode: String = "system", // "system", "light", "dark"
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) EchoraDarkColorScheme else EchoraLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
