package com.reiny.mittord.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Immutable
class MittOrdColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val scrim: Color,

    val brand: Color,
    val warning: Color,
    val success: Color,

    val textPrimary: Color = onPrimary,
    val textSecondary: Color = onPrimary
)

val LocalMittOrdColors = compositionLocalOf<MittOrdColors> {
    error("MittOrdColors not provided")
}

private fun lightMittOrdColors() = MittOrdColors(
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEEEEE),
    onPrimaryContainer = Color(0xFF000000),
    background = Color(0xFFEEEEEE),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFEF7FF),
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFFFFFFF),
    outlineVariant = Color(0xFFD5DAE7),
    scrim = Color(0xFF000000),

    brand = Color(0xFF00897B),
    warning = Color(0xFFF57C00),
    success = Color(0xFF43A047)
)

private fun darkMittOrdColors() = MittOrdColors(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFFEEEEEE),
    onPrimaryContainer = Color(0xFF000000),
    background = Color(0xFF000000),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF2C2E32),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF2C2E32),
    outlineVariant = Color(0xFF2C2E32),
    scrim = Color(0xFF000000),

    brand = Color(0xFF80CBC4),
    warning = Color(0xFFFFB74D),
    success = Color(0xFFA5D6A7)
)

@Composable
fun rememberMittOrdColors(isDark: Boolean): MittOrdColors {
    return remember(isDark) {
        if (isDark) darkMittOrdColors() else lightMittOrdColors()
    }
}

val Theme.colors: MittOrdColors
    @Composable get() = LocalMittOrdColors.current
