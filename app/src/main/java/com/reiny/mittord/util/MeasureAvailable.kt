package com.reiny.mittord.util

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp

/*
 * Zero-sized probes that report the space their parent offers.
 *
 * Caveat: the callback fires during the measure phase, so writing its result into a
 * State that is read during composition makes every size change recompose the caller.
 * Only feed them values that are stable while the screen is interactive - see the
 * guard around parentHeight in HomeScreen.
 *
 * BoxWithConstraints is not a drop-in replacement: it re-invokes its whole content
 * lambda whenever the incoming constraints change, which is exactly what has to be
 * avoided while the IME animates a padded parent. The guard above is the point.
 */

@Composable
internal fun MeasureAvailableWidth(fraction: Float = 1f, onWidthMeasured: (Dp) -> Unit) {
    Layout(content = {}, modifier = Modifier.fillMaxWidth(fraction)) { _, c ->
        onWidthMeasured(c.maxWidth.toDp())
        layout(c.maxWidth, 0) {}
    }
}

@Composable
internal fun MeasureAvailableHeight(fraction: Float = 1f, onHeightMeasured: (Dp) -> Unit) {
    Layout(content = {}, modifier = Modifier.fillMaxHeight(fraction)) { _, c ->
        onHeightMeasured(c.maxHeight.toDp())
        layout(0, c.maxHeight) {}
    }
}
