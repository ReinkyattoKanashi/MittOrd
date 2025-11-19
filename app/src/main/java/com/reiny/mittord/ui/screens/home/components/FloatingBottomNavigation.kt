package com.reiny.mittord.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.reiny.mittord.ui.animations.NavBarAnimation
import com.reiny.mittord.utils.animateAlignmentAsState

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

        val animatedWidth by animateDpAsState(
            targetValue = if (state == BottomNavState.Default) 70.dp else targetWidth,
            animationSpec = NavBarAnimation.tweenDpSpec
        )
        val animatedPadding by animateDpAsState(
            targetValue = if (state == BottomNavState.Default) 0.dp else 20.dp,
            animationSpec = NavBarAnimation.defaultDpTween
        )
        val rotation by animateFloatAsState(
            targetValue = if (state == BottomNavState.Default) 0f else 45f,
            animationSpec = NavBarAnimation.tweenFloatSpec
        )
        val alignment by animateAlignmentAsState(
            targetValue = if (state == BottomNavState.Default) Alignment.Center else Alignment.CenterEnd,
            animationSpec = NavBarAnimation.tweenFloatSpec
        )
        val iconsTint by animateColorAsState(
            targetValue = if (state == BottomNavState.Default)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onPrimary,
            animationSpec = NavBarAnimation.defaultTweenColor
        )
        val alpha by animateFloatAsState(
            targetValue = if (state == BottomNavState.Default) 1f else 0f,
            animationSpec = NavBarAnimation.defaultTween
        )

        Box(
            modifier = Modifier.fillMaxWidth(0.85f),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedCenterButton(
                width = animatedWidth,
                alignment = alignment,
                padding = animatedPadding,
                rotation = rotation,
                onMiddleClick = onMiddleClick
            )

            NavIconsRow(
                state = state,
                iconsTint = iconsTint,
                onLeftClick = onLeftClick,
                onRightClick = onRightClick
            )

            StaticSearchField(
                state = state,
                alpha = alpha
            )
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
private fun AnimatedCenterButton(
    width: Dp,
    alignment: Alignment,
    padding: Dp,
    rotation: Float,
    onMiddleClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(70.dp)
            .width(width)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable { },
        contentAlignment = alignment
    ) {
        IconButton(
            onClick = onMiddleClick,
            modifier = Modifier.padding(end = padding)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .rotate(rotation)
                    .size(36.dp)
            )
        }
    }
}

@Composable
private fun NavIconsRow(
    state: BottomNavState,
    iconsTint: androidx.compose.ui.graphics.Color,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {

    val animatedPadding by animateDpAsState(
        if (state == BottomNavState.Default) 32.dp else 20.dp,
        NavBarAnimation.defaultDpTween
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = animatedPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onLeftClick) {
            Icon(
                Icons.Default.Search,
                "Search",
                modifier = Modifier.size(34.dp),
                tint = iconsTint
            )
        }

        Spacer(Modifier.weight(1f))

        Spacer(Modifier.width(16.dp))

        AnimatedVisibility(
            visible = state == BottomNavState.Default,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IconButton(onClick = onRightClick) {
                Icon(
                    Icons.Default.Person,
                    "Profile",
                    modifier = Modifier.size(34.dp),
                    tint = iconsTint
                )
            }
        }
    }
}

@Composable
private fun BoxScope.StaticSearchField(
    state: BottomNavState,
    alpha: Float
) {
    var text by remember { mutableStateOf("") }

    CleanPrimaryTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = "Введите слово…",
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 70.dp, end = 70.dp)
            .align(Alignment.CenterStart)
            .alpha(1 - alpha),
        enabled = state == BottomNavState.Search,
    )
}

@Composable
fun MeasureAvailableWidth(fraction: Float = 1f, onWidthMeasured: (Dp) -> Unit) {
    Layout(content = {}, modifier = Modifier.fillMaxWidth(fraction)) { _, c ->
        onWidthMeasured(c.maxWidth.toDp())
        layout(c.maxWidth, 0) {}
    }
}

@Preview
@Composable
fun PreviewBottomNav() {
    FloatingBottomNavigationDefault(
        BottomNavState.Search,
        onLeftClick = {},
        onMiddleClick = {},
        onRightClick = {}
    )
}