package com.reiny.mittord.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFFEEEEEE),
    onPrimaryContainer = Color(0xFF000000),
    background = Color(0xFF000000),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFFFEF7FF),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFFEF7FF),
    outline = Color(0xFF2C2E32),
    outlineVariant = Color(0xFF2C2E32),
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
    outline = Color(0xFFFFFFFF),
    outlineVariant = Color(0xFFD5DAE7),
    scrim = Color(0xFF000000)
)

object Theme

@Composable
fun MittOrdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val customColors = rememberMittOrdColors(darkTheme)
    val brushes = rememberMittOrdBrushes(customColors)
    val typography = mittOrdTypography(customColors)

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalMittOrdColors provides customColors,
        LocalMittOrdBrushes provides brushes,
        LocalMittOrdTypography provides typography
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography.material,
            content = content
        )
    }
}

