package com.reiny.mittord.ui.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.reiny.mittord.R

@Composable
fun EmptyListPlaceholder(modifier: Modifier = Modifier) {
    val family = FontFamily(Font(R.font.sansation_font))
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "No words added",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            fontFamily = family
        )
        Text(
            "Tap the + button to add a new word",
            fontSize = 16.sp,
            fontFamily = family
        )
    }
}
