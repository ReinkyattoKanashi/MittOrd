package com.reiny.mittord.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush

class MittOrdBrushes(
    val backgroundGradient: Brush, val brandGradient: Brush
)

val LocalMittOrdBrushes = compositionLocalOf<MittOrdBrushes> {
    error("MittOrdBrushes not provided")
}

@Composable
fun rememberMittOrdBrushes(colors: MittOrdColors): MittOrdBrushes {
    return MittOrdBrushes(
        backgroundGradient = Brush.verticalGradient(
            0f to colors.surface, // just for example
            1f to colors.surface // just for example
        ), brandGradient = Brush.horizontalGradient(
            0f to colors.brand, 1f to colors.brand.copy(alpha = 0.6f)
        )
    )
}

val Theme.brushes: MittOrdBrushes
    @Composable get() = LocalMittOrdBrushes.current