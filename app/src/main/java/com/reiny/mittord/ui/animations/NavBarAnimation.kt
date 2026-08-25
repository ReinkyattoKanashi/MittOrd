package com.reiny.mittord.ui.animations

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

object NavBarAnimation {
    const val DURATION = 400

    val defaultEasing: Easing = FastOutSlowInEasing

    val defaultTweenColor = tween<Color>(
        durationMillis = DURATION,
        easing = defaultEasing
    )

    val defaultDpTween = tween<Dp>(
        durationMillis = DURATION,
        easing = defaultEasing
    )

    val tweenFloatSpec = tween<Float>(
        durationMillis = DURATION - 100,
        easing = defaultEasing
    )

    private val slideEasing: Easing = CubicBezierEasing(
        0.20f, 0.00f, 0.00f, 1.00f
    )

    val slideDpSpec = tween<Dp>(
        durationMillis = DURATION,
        easing = slideEasing
    )

}