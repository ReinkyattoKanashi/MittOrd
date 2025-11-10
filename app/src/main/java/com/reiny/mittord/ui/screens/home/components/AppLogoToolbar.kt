package com.reiny.mittord.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.reiny.mittord.R

@Composable
fun AppLogoToolbar(modifier: Modifier = Modifier) {
    Image(
        imageVector = ImageVector.vectorResource(id = R.drawable.app_logo),
        contentDescription = "App Logo",
        modifier = modifier,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
    )
}
