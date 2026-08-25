package com.reiny.mittord.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.reiny.mittord.R
import com.reiny.mittord.ui.animations.NavBarAnimation
import com.reiny.mittord.util.height

/** Search icon on the left, profile icon on the right, both fading with the nav state. */
@Composable
internal fun NavIconsRow(
    state: () -> BottomNavState,
    iconsTint: () -> Color,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    val cdSearch = stringResource(R.string.cd_search)
    val cdProfile = stringResource(R.string.cd_profile)

    val animatedPadding by animateDpAsState(
        if (state() == BottomNavState.Default) 32.dp else 20.dp, NavBarAnimation.defaultDpTween
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height { 70.dp }
            // Hand-written layout instead of Modifier.padding: animatedPadding is read
            // inside the lambda, so the animation runs without recomposing this row.
            .layout { measurable, constraints ->
                val paddingPx = animatedPadding.roundToPx()
                val newConstraints = constraints.offset(
                    horizontal = -paddingPx * 2
                )
                val placeable = measurable.measure(newConstraints)
                layout(
                    width = placeable.width + paddingPx * 2, height = placeable.height
                ) {
                    placeable.placeRelative(paddingPx, 0)
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = state() != BottomNavState.AddWord, enter = fadeIn(), exit = fadeOut()
        ) {
            IconButton(onClick = onLeftClick) {
                Icon(
                    Icons.Default.Search, cdSearch, modifier = Modifier
                        .size(34.dp)
                        .graphicsLayer {
                            colorFilter = ColorFilter.tint(iconsTint())
                        })
            }
        }

        Spacer(Modifier.weight(1f))

        Spacer(Modifier.width(16.dp))

        AnimatedVisibility(
            visible = state() == BottomNavState.Default, enter = fadeIn(), exit = fadeOut()
        ) {
            IconButton(onClick = onRightClick) {
                Icon(
                    Icons.Default.Person, cdProfile, modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}
