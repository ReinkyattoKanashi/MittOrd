package com.reiny.mittord.ui.screens.addWord.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun WordInputField(label: String, icon: ImageVector) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(icon, contentDescription = null)
        },
        placeholder = { Text(label) },
        shape = RoundedCornerShape(16.dp)
    )
}
