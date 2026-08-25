package com.reiny.mittord.ui.screens.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.reiny.mittord.R
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography

@Composable
fun AvatarCropDialog(
    sourceBitmap: ImageBitmap,
    onConfirm: (ImageBitmap) -> Unit,
    onCancel: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val boxW = constraints.maxWidth.toFloat()
            val boxH = constraints.maxHeight.toFloat()
            val cropRadius = minOf(boxW, boxH) * 0.42f

            val imgW = sourceBitmap.width.toFloat()
            val imgH = sourceBitmap.height.toFloat()
            // Fill the crop circle by default (no empty gaps)
            val fitScale = maxOf(cropRadius * 2f / imgW, cropRadius * 2f / imgH)
            val totalScale = fitScale * scale
            val displayW = imgW * totalScale
            val displayH = imgH * totalScale

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 8f)
                            val newTs = fitScale * newScale
                            val maxOffX = maxOf(0f, imgW * newTs / 2f - cropRadius)
                            val maxOffY = maxOf(0f, imgH * newTs / 2f - cropRadius)
                            scale = newScale
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-maxOffX, maxOffX),
                                y = (offset.y + pan.y).coerceIn(-maxOffY, maxOffY)
                            )
                        }
                    }
            ) {
                val imgLeft = boxW / 2f + offset.x - displayW / 2f
                val imgTop = boxH / 2f + offset.y - displayH / 2f
                drawImage(
                    image = sourceBitmap,
                    dstOffset = IntOffset(imgLeft.toInt(), imgTop.toInt()),
                    dstSize = IntSize(displayW.toInt(), displayH.toInt())
                )
            }

            // EvenOdd path: dark overlay with circular cutout
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    addRect(Rect(Offset.Zero, Size(boxW, boxH)))
                    addOval(Rect(center = Offset(boxW / 2f, boxH / 2f), radius = cropRadius))
                    fillType = PathFillType.EvenOdd
                }
                drawPath(path, Color.Black.copy(alpha = 0.65f))
                drawCircle(
                    color = Color.White.copy(alpha = 0.55f),
                    radius = cropRadius,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.btn_cancel_crop), color = Color.White, style = Theme.typography.body)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.crop_hint),
                    color = Color.White.copy(alpha = 0.45f),
                    style = Theme.typography.caption
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    val cropped = performCrop(sourceBitmap, totalScale, offset, cropRadius)
                    onConfirm(cropped.asImageBitmap())
                }) {
                    Text(stringResource(R.string.btn_done), color = Color.White, style = Theme.typography.body)
                }
            }
        }
    }
}

private fun performCrop(
    sourceBitmap: ImageBitmap,
    totalScale: Float,
    offset: Offset,
    cropRadius: Float
): android.graphics.Bitmap {
    val imgW = sourceBitmap.width.toFloat()
    val imgH = sourceBitmap.height.toFloat()

    val imgCX = imgW / 2f - offset.x / totalScale
    val imgCY = imgH / 2f - offset.y / totalScale
    val imgRadius = cropRadius / totalScale

    val cropLeft = (imgCX - imgRadius).toInt().coerceIn(0, sourceBitmap.width - 1)
    val cropTop = (imgCY - imgRadius).toInt().coerceIn(0, sourceBitmap.height - 1)
    val cropW = (imgRadius * 2f).toInt().coerceIn(1, sourceBitmap.width - cropLeft)
    val cropH = (imgRadius * 2f).toInt().coerceIn(1, sourceBitmap.height - cropTop)

    val androidBitmap = sourceBitmap.asAndroidBitmap()
    val cropped = android.graphics.Bitmap.createBitmap(androidBitmap, cropLeft, cropTop, cropW, cropH)
    return android.graphics.Bitmap.createScaledBitmap(cropped, 512, 512, true)
}
