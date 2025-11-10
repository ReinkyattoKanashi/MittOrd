package com.reiny.mittord.ui.animations

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

object NavBarAnimation {
    const val DURATION = 400

    val defaultEasing: Easing = FastOutSlowInEasing

    val defaultTween = tween<Float>(
        durationMillis = DURATION,
        easing = defaultEasing
    )

    val defaultTweenColor = tween<Color>(
        durationMillis = DURATION,
        easing = defaultEasing
    )

    val defaultDpTween = tween<Dp>(
        durationMillis = DURATION,
        easing = defaultEasing
    )

    val bounceSpring = spring<Float>(
        dampingRatio = 0.9f,
        stiffness = 350f
    )

    val tweenDpSpec = tween<Dp>(
        durationMillis = DURATION + 100,
        easing = FastOutSlowInEasing
    )
    val tweenFloatSpec = tween<Float>(
        durationMillis = DURATION - 100,
        easing = FastOutSlowInEasing
    )
}