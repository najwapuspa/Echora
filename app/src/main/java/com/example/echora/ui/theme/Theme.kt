package com.example.echora.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EchoraDarkColorScheme = darkColorScheme(
    primary = EchoraPrimary,
    onPrimary = Color.White,
    secondary = EchoraSecondary,
    onSecondary = Color.Black,
    background = EchoraBackground,
    onBackground = EchoraTextPrimary,
    surface = EchoraSurface,
    onSurface = EchoraTextPrimary,
    surfaceVariant = EchoraSurfaceVariant,
    onSurfaceVariant = EchoraTextSecondary,
    outline = EchoraDivider
)

@Composable
fun EchoraTheme(
    darkTheme: Boolean = true, // Force dark theme as requested for Echora concept
    content: @Composable () -> Unit
) {
    val colorScheme = EchoraDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
