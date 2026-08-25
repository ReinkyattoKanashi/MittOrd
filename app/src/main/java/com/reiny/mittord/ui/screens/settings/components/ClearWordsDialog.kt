package com.reiny.mittord.ui.screens.settings.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.reiny.mittord.R
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography

/** Confirmation for wiping the dictionary - the action cannot be undone. */
@Composable
internal fun ClearWordsDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_clear_words_title),
                style = Theme.typography.h2
            )
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_clear_words_message),
                style = Theme.typography.body,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.cd_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
