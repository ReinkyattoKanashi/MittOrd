package com.reiny.mittord.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.constrain
import androidx.compose.ui.unit.offset

fun Modifier.width(width: () -> Dp) = dpSize { DpSize(width = width(), height = Dp.Unspecified) }
fun Modifier.height(height: () -> Dp) = dpSize { DpSize(height = height(), width = Dp.Unspecified) }
fun Modifier.size(width: () -> Dp, height: () -> Dp) = dpSize { DpSize(width = width(), height = height()) }
fun Modifier.size(size: () -> Dp) = dpSize { DpSize(size(), size()) }

fun Modifier.dpSize(size: () -> DpSize) = layout { measurable, constraints ->
    val width = size().width
    val height = size().height

    val targetConstraints = targetConstraints(
        width = width,
        height = height,
        incomingConstraints = constraints
    )

    val placeable = measurable.measure(targetConstraints)

    layout(placeable.width, placeable.height) {
        placeable.placeRelative(0, 0)
    }
}

/**
 * Modified version of the code in androidx.compose.foundation.layout.SizeNode
 */
private fun Density.targetConstraints(width: Dp, height: Dp, incomingConstraints: Constraints): Constraints {
    val maxWidth = if (width != Dp.Unspecified) {
        width.roundToPx().coerceAtLeast(0)
    } else {
        Constraints.Infinity
    }
    val maxHeight = if (height != Dp.Unspecified) {
        height.roundToPx().coerceAtLeast(0)
    } else {
        Constraints.Infinity
    }
    val minWidth = if (width != Dp.Unspecified) {
        width.roundToPx().coerceAtMost(maxWidth).coerceAtLeast(0).let {
            if (it != Constraints.Infinity) it else 0
        }
    } else {
        0
    }
    val minHeight = if (height != Dp.Unspecified) {
        height.roundToPx().coerceAtMost(maxHeight).coerceAtLeast(0).let {
            if (it != Constraints.Infinity) it else 0
        }
    } else {
        0
    }

    return incomingConstraints.constrain(
        Constraints(
            minWidth = minWidth,
            minHeight = minHeight,
            maxWidth = maxWidth,
            maxHeight = maxHeight
        )
    )
}

//fun Modifier.paddingLayout(all: () -> Dp) =
//    paddingLayoutInternal(
//        start = all,
//        top = all,
//        end = all,
//        bottom = all
//    )

fun Modifier.paddingLayout(
    horizontal: () -> Dp,
    vertical: () -> Dp
) =
    paddingLayoutInternal(
        start = horizontal,
        end = horizontal,
        top = vertical,
        bottom = vertical
    )

fun Modifier.paddingLayout(
    start: () -> Dp = { Dp.Unspecified },
    top: () -> Dp = { Dp.Unspecified },
    end: () -> Dp = { Dp.Unspecified },
    bottom: () -> Dp = { Dp.Unspecified }
) =
    paddingLayoutInternal(
        start = start,
        top = top,
        end = end,
        bottom = bottom
    )

private fun Modifier.paddingLayoutInternal(
    start: () -> Dp,
    top: () -> Dp,
    end: () -> Dp,
    bottom: () -> Dp
): Modifier = this.then(
    Modifier.layout { measurable, constraints ->

        val startPx = start().roundToPx()
        val endPx = end().roundToPx()
        val topPx = top().roundToPx()
        val bottomPx = bottom().roundToPx()

        val newConstraints = constraints.offset(
            horizontal = -(startPx + endPx),
            vertical = -(topPx + bottomPx)
        )

        val placeable = measurable.measure(newConstraints)

        val width = placeable.width + startPx + endPx
        val height = placeable.height + topPx + bottomPx

        layout(width, height) {
            placeable.placeRelative(startPx, topPx)
        }
    }
)