package com.reiny.mittord.ui.screens.home.components

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography

@Composable
fun WordInputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    icon: ImageVector = Icons.Default.Edit,
    flagEmoji: String? = null,
    isAutoLanguage: Boolean = true,
    iconTint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    textStyle: TextStyle = Theme.typography.body,
    singleLine: Boolean = true,
    onIconClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val focusedBorderColor = MaterialTheme.colorScheme.primary
    val targetBorderColor = if (isFocused) focusedBorderColor else borderColor

    val borderColorAnim = remember { Animatable(borderColor) }
    LaunchedEffect(targetBorderColor) {
        borderColorAnim.animateTo(targetBorderColor, tween(150))
    }

    val strokeDp = remember { Animatable(1f) }
    LaunchedEffect(isFocused) {
        strokeDp.animateTo(if (isFocused) 2f else 1f, tween(150))
    }

    val focusManager = LocalFocusManager.current

    val sizeModifier = if (singleLine) {
        Modifier.height(52.dp)
    } else {
        Modifier.heightIn(min = 100.dp)
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .then(sizeModifier)
            .drawWithContent {
                drawContent()
                val stroke = strokeDp.value.dp.toPx()
                val corner = 12.dp.toPx()
                drawRoundRect(
                    color = borderColorAnim.value,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius(corner),
                    style = Stroke(width = stroke)
                )
            }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(imeAction = if (singleLine) ImeAction.Done else ImeAction.Default),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = onIconClick != null,
                            onClick = { onIconClick?.invoke() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (flagEmoji != null) {
                        // Auto-detected: slightly faded (might still change)
                        // Manually selected: full opacity (locked)
                        val alpha = if (isAutoLanguage) 0.6f else 1f
                        Text(
                            text = flagEmoji,
                            style = Theme.typography.body,
                            modifier = Modifier.graphicsLayer { this.alpha = alpha }
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isFocused) focusedBorderColor else iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = textStyle.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}
