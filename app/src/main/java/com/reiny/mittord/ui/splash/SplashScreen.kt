package com.reiny.mittord.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private data class Star(
    val xFraction: Float,
    val yFraction: Float,
    val baseRadiusDp: Float,
    val durationMs: Int,
    val delayMs: Int,
)

private val STARS: List<Star> = List(45) {
    Star(
        xFraction    = Random.nextFloat(),
        yFraction    = Random.nextFloat(),
        baseRadiusDp = Random.nextFloat() * 1.5f + 0.4f,
        durationMs   = (Random.nextFloat() * 2500f + 800f).toInt(),
        delayMs      = (Random.nextFloat() * 4000f).toInt(),
    )
}

@Composable
fun SplashScreen(onFinished: () -> Unit = {}) {
    val rippleProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(1000L)
        rippleProgress.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        )
        onFinished()
    }

    // Single transition for all stars — one coroutine instead of 45
    val transition = rememberInfiniteTransition(label = "stars")
    val masterTime by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "masterTime",
    )

    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx  = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        val originX = widthPx / 2f
        val originY = heightPx - with(density) { 80f * density.density }
        val maxR = sqrt(
            maxOf(originX, widthPx - originX).let { it * it } +
            maxOf(originY, heightPx - originY).let { it * it }
        ) + 10f
        val currentR = rippleProgress.value * maxR

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            drawRect(color = Color(0xFF07070F))

            STARS.forEach { star ->
                val cyclesIn4s = 4000f / star.durationMs
                val phaseRad   = star.delayMs / 4000f * (2f * PI.toFloat())
                val angle      = masterTime * 2f * PI.toFloat() * cyclesIn4s + phaseRad
                val alpha      = (sin(angle).toFloat() * 0.475f + 0.525f).coerceIn(0.05f, 1f)
                val r          = star.baseRadiusDp * (1f + alpha * 0.5f) * density.density
                drawCircle(
                    color  = Color.White.copy(alpha = alpha),
                    radius = r,
                    center = Offset(star.xFraction * widthPx, star.yFraction * heightPx),
                )
            }

            if (currentR > 0f) {
                drawCircle(
                    color     = Color.Transparent,
                    radius    = currentR,
                    center    = Offset(originX, originY),
                    blendMode = BlendMode.Clear,
                )
            }
        }
    }
}
