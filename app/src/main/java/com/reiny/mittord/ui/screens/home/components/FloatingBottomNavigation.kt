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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            targetValue = if (state == BottomNavState.Default) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary,
            animationSpec = NavBarAnimation.defaultTweenColor
        )
        val alpha by animateFloatAsState(
            targetValue = if (state == BottomNavState.Default) 1f else 0f,
            animationSpec = NavBarAnimation.defaultTween
        )
        Box(
            contentAlignment = Alignment.BottomCenter, modifier = Modifier
                .fillMaxWidth(0.85f)
        ) {
            Box(
                modifier = Modifier
                    .height(70.dp)
                    .width(animatedWidth)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { },
                contentAlignment = alignment
            ) {
                IconButton(
                    onClick = onMiddleClick,
                    modifier = Modifier.padding(end = animatedPadding)
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

            val animatedRowContentPadding by animateDpAsState(
                targetValue = if (state == BottomNavState.Default) 32.dp else 20.dp,
                animationSpec = NavBarAnimation.defaultDpTween
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = animatedRowContentPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLeftClick) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(34.dp),
                        tint = iconsTint
                    )
                }
                var text by remember { mutableStateOf("") }
                CleanPrimaryTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = "Введите слово…",
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 36.dp)
                        .alpha(1 - alpha),
                    enabled = state == BottomNavState.Search,
                )
                Spacer(modifier = Modifier.width(16.dp))
                AnimatedVisibility(
                    visible = state == BottomNavState.Default,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(
                        onClick = onRightClick
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(34.dp),
                            tint = iconsTint
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeasureAvailableWidth(
    fraction: Float = 1f,
    onWidthMeasured: (Dp) -> Unit
) {
    Layout(
        content = {},
        modifier = Modifier.fillMaxWidth(fraction)
    ) { _, constraints ->
        val widthPx = constraints.maxWidth
        val widthDp = widthPx.toDp()
        onWidthMeasured(widthDp)

        layout(widthPx, 0) {}
    }
}

@Preview
@Composable
fun PreviewBottomNav() {
    FloatingBottomNavigationDefault(
        BottomNavState.Search,
        {}, {}, {}
    )
}