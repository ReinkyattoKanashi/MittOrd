package com.reiny.mittord.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.foundation.Canvas
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import kotlin.random.Random

private data class Star(
    val xFraction: Float,
    val yFraction: Float,
    val baseRadiusDp: Float,
    val durationMs: Int,
    val delayMs: Int,
)

private val STARS: List<Star> = List(90) {
    Star(
        xFraction    = Random.nextFloat(),
        yFraction    = Random.nextFloat(),
        baseRadiusDp = Random.nextFloat() * 1.5f + 0.4f,
        durationMs   = (Random.nextFloat() * 2500f + 800f).toInt(),
        delayMs      = (Random.nextFloat() * 4000f).toInt(),
    )
}

@Composable
private fun rememberStarAlpha(durationMs: Int, delayMs: Int): State<Float> {
    val tr = rememberInfiniteTransition(label = "star")
    return tr.animateFloat(
        initialValue  = 0.05f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation          = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode         = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delayMs % durationMs),
        ),
        label = "starAlpha",
    )
}

@Composable
fun SplashScreen(onFinished: () -> Unit = {}) {
    val rippleProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(1200L)
        rippleProgress.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = 880, easing = FastOutSlowInEasing),
        )
        onFinished()
    }

    val starAlphas = STARS.map { rememberStarAlpha(it.durationMs, it.delayMs) }
    val graphicsContext = LocalGraphicsContext.current
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

        Canvas(modifier = Modifier.fillMaxSize()) {
            val splashLayer = graphicsContext.createGraphicsLayer().also { layer ->
                layer.compositingStrategy = CompositingStrategy.Offscreen
                layer.record {
                    drawRect(color = Color(0xFF07070F))

                    STARS.forEachIndexed { i, star ->
                        val alpha = starAlphas[i].value.coerceIn(0f, 1f)
                        val r = star.baseRadiusDp * (1f + alpha * 0.5f) * density.density
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

            drawLayer(splashLayer)
            graphicsContext.releaseGraphicsLayer(splashLayer)
        }
    }
}
