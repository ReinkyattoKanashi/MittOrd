package com.reiny.mittord.utils

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment

@Composable
fun animateAlignmentAsState(
    targetValue: Alignment,
    animationSpec: AnimationSpec<Float> = spring(),
    label: String = "alignmentAnim"
): State<Alignment> {
    val targetBias = (targetValue as? BiasAlignment)
        ?: throw IllegalArgumentException("Alignment must be BiasAlignment")

    val animatedHorizontal by animateFloatAsState(
        targetValue = targetBias.horizontalBias,
        animationSpec = animationSpec,
        label = "$label-horizontal"
    )
    val animatedVertical by animateFloatAsState(
        targetValue = targetBias.verticalBias,
        animationSpec = animationSpec,
        label = "$label-vertical"
    )

    return remember {
        derivedStateOf {
            BiasAlignment(horizontalBias = animatedHorizontal, verticalBias = animatedVertical)
        }
    }
}