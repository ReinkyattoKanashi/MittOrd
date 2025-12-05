package com.reiny.mittord.ui.screens.home.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography

@Composable
fun PrimaryTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    enabled: Boolean = true,
    style: TextStyle = Theme.typography.body
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        enabled = enabled,
        placeholder = {
            if (placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    style = style
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,

            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,

            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            disabledTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
            cursorColor = MaterialTheme.colorScheme.onPrimary
        ),
        textStyle = style
    )
}

@Preview
@Composable
fun CleanPrimaryTextFieldPreview(){
    PrimaryTextField(
        value = "Мой текст",
        onValueChange = {}
    )
}