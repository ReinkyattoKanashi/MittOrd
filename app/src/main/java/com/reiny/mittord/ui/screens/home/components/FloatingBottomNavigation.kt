package com.reiny.mittord.ui.screens.home.components

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.reiny.mittord.ui.animations.NavBarAnimation
import com.reiny.mittord.utils.size

@Composable
fun FloatingBottomNavigationDefault(
    state: BottomNavState,
    onLeftClick: () -> Unit,
    onMiddleClick: () -> Unit,
    onRightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .height(70.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        BackgroundSurface()

        var targetWidth by remember { mutableStateOf(70.dp) }
        MeasureAvailableWidth(fraction = 0.85f) { width ->
            targetWidth = width
        }
        val width by animateDpAsState(
            targetValue = if (state == BottomNavState.Default) 70.dp else targetWidth,
            animationSpec = NavBarAnimation.defaultDpTween,
            label = "centerWidth"
        )
        val bias by animateFloatAsState(
            targetValue = if (state == BottomNavState.Default) 0f else 1f,
            animationSpec = NavBarAnimation.tweenFloatSpec,
            label = "centerBias"
        )
        val rotation by animateFloatAsState(
            targetValue = if (state == BottomNavState.Default) 0f else 45f,
            animationSpec = NavBarAnimation.tweenFloatSpec,
            label = "centerRotation"
        )
        val iconsTint = rememberBottomNavTint(state)

        Box(
            modifier = Modifier.fillMaxWidth(0.85f), contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedCenterButton(
                width = { width },
                rotation = { rotation },
                bias = { bias },
                onMiddleClick = onMiddleClick
            )

            NavIconsRow(
                state = { state },
                iconsTint = { iconsTint.value },
                onLeftClick = onLeftClick,
                onRightClick = onRightClick
            )

            StaticSearchField(
                state = { state })
        }
    }
}

@Composable
private fun BoxScope.BackgroundSurface() {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(60.dp)
            .align(Alignment.Center)
    ) {}
}

@Composable
fun AnimatedCenterButton(
    width: () -> Dp, rotation: () -> Float, bias: () -> Float, onMiddleClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(70.dp)
            .size { width() }
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onMiddleClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add",
            modifier = Modifier
                .size(36.dp)
                .graphicsLayer {
                    rotationZ = rotation()
                    val max = width().roundToPx() / 2 - size.width
                    translationX = max * bias()
                },
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun NavIconsRow(
    state: () -> BottomNavState,
    iconsTint: () -> Color,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {

    val animatedPadding by animateDpAsState(
        if (state() == BottomNavState.Default) 32.dp else 20.dp, NavBarAnimation.defaultDpTween
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
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
        IconButton(onClick = onLeftClick) {
            Icon(
                Icons.Default.Search, "Search", modifier = Modifier
                    .size(34.dp)
                    .graphicsLayer {
                        colorFilter = ColorFilter.tint(iconsTint())
                    })
        }

        Spacer(Modifier.weight(1f))

        Spacer(Modifier.width(16.dp))

        AnimatedVisibility(
            visible = state() == BottomNavState.Default, enter = fadeIn(), exit = fadeOut()
        ) {
            IconButton(onClick = onRightClick) {
                Icon(
                    Icons.Default.Person, "Profile", modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
private fun BoxScope.StaticSearchField(
    state: () -> BottomNavState
) {
    var text by remember { mutableStateOf("") }
    AnimatedVisibility(state() == BottomNavState.Search) {
        PrimaryTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "Start typing…",
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 70.dp, end = 70.dp)
                .align(Alignment.CenterStart),
        )
    }
}

@Composable
fun MeasureAvailableWidth(fraction: Float = 1f, onWidthMeasured: (Dp) -> Unit) {
    Layout(content = {}, modifier = Modifier.fillMaxWidth(fraction)) { _, c ->
        onWidthMeasured(c.maxWidth.toDp())
        layout(c.maxWidth, 0) {}
    }
}

@Composable
fun rememberBottomNavTint(state: BottomNavState): Animatable<Color, AnimationVector4D> {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    val anim = remember { Animatable(onSurface) }

    LaunchedEffect(state) {
        val target = if (state == BottomNavState.Default) onSurface else onPrimary
        anim.animateTo(
            targetValue = target, animationSpec = NavBarAnimation.defaultTweenColor
        )
    }

    return anim
}

@Preview
@Composable
fun PreviewBottomNav() {
    FloatingBottomNavigationDefault(
        BottomNavState.Default,
        onLeftClick = {},
        onMiddleClick = {},
        onRightClick = {})
}

@Preview
@Composable
fun PreviewBottomNavSearch() {
    FloatingBottomNavigationDefault(
        BottomNavState.Search,
        onLeftClick = {},
        onMiddleClick = {},
        onRightClick = {})
}