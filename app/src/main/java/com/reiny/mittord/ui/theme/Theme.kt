package com.reiny.mittord.ui.theme

import android.app.Activity
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
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFFEEEEEE),
    onPrimaryContainer = Color(0xFF000000),
    background = Color(0xFF000000),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFFFEF7FF),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color.Black,
    onSurfaceVariant = Color(0xFFFEF7FF),
    outline = Color(0xFFD5DAE7),
    outlineVariant = Color(0xFFD5DAE7),
    scrim = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEEEEE),
    onPrimaryContainer = Color(0xFF000000),
    background = Color(0xFFEEEEEE),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFEF7FF),
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000),
    onSurfaceVariant = Color(0xFFFEF7FF),
    outline = Color(0xFFD5DAE7),
    outlineVariant = Color(0xFFD5DAE7),
    scrim = Color(0xFF000000)
)

@Composable
fun MittOrdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

