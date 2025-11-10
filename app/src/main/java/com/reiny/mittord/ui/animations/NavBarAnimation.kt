package com.reiny.mittord.ui.animations

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp

object NavBarAnimation {
    const val DURATION = 400 // мс

    val defaultEasing: Easing = FastOutSlowInEasing

    val defaultTween = tween<Float>(
        durationMillis = DURATION,
        easing = defaultEasing
    )

    val defaultDpTween = tween<Dp>(
        durationMillis = DURATION,
        easing = defaultEasing
    )

    val bounceSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val bounceDpSpring = spring<Dp>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
}