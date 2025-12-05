package com.reiny.mittord.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.reiny.mittord.R

class MittOrdTypography(
    val material: Typography,
    val h1: TextStyle,
    val h2: TextStyle,
    val body: TextStyle,
    val caption: TextStyle
)

val LocalMittOrdTypography = compositionLocalOf<MittOrdTypography> {
    error("MittOrdTypography not provided")
}

@Composable
fun mittOrdTypography(colors: MittOrdColors): MittOrdTypography {

    val sansation = FontFamily(
        Font(R.font.sansation_font, FontWeight.Normal),
        Font(R.font.sansation_font, FontWeight.Medium),
        Font(R.font.sansation_font, FontWeight.SemiBold)
    )

    return MittOrdTypography(
        material = Typography(),
        h1 = TextStyle(
            fontFamily = sansation,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        ),
        h2 = TextStyle(
            fontFamily = sansation,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        ),
        body = TextStyle(
            fontFamily = sansation,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = colors.textSecondary
        ),
        caption = TextStyle(
            fontFamily = sansation,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = colors.textSecondary
        )
    )
}

val Theme.typography: MittOrdTypography
    @Composable get() = LocalMittOrdTypography.current