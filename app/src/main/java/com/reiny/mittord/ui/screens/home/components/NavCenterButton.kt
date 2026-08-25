package com.reiny.mittord.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.reiny.mittord.R
import com.reiny.mittord.util.size

/**
 * The round button in the middle of the navigation bar.
 *
 * Every animated value arrives as a lambda on purpose: reading them here would put the
 * read in the composition phase and recompose the button on every animation frame.
 * Called inside `size {}` and `graphicsLayer {}` they stay in the layout/draw phase.
 */
@Composable
internal fun BoxScope.NavCenterButton(
    width: () -> Dp,
    rotation: () -> Float,
    bias: () -> Float,
    addIconAlpha: () -> Float,
    onMiddleClick: () -> Unit
) {
    val cdAdd = stringResource(R.string.btn_add)
    val cdCollapse = stringResource(R.string.cd_collapse)
    Box(
        modifier = Modifier
            .height(70.dp)
            .size { width() }
            .align(Alignment.TopCenter)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onMiddleClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = cdAdd,
            modifier = Modifier
                .size(36.dp)
                .graphicsLayer {
                    alpha = addIconAlpha()
                    rotationZ = rotation()
                    val max = width().roundToPx() / 2 - size.width
                    translationX = max * bias()
                },
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = cdCollapse,
            modifier = Modifier
                .size(36.dp)
                .graphicsLayer { alpha = 1f - addIconAlpha() },
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}
