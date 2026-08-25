package com.reiny.mittord.ui.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.reiny.mittord.BuildConfig
import com.reiny.mittord.R
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography

/** Name, version and author, straight on the background - no card, no dividers. */
@Composable
internal fun AppInfoBlock(onAuthorClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.settings_app_name),
            style = Theme.typography.h1
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.settings_version) + " " + BuildConfig.VERSION_NAME,
            style = Theme.typography.caption,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
        Spacer(Modifier.height(12.dp))
        // Only the handle opens GitHub; the legal name below is plain text.
        Text(
            text = stringResource(R.string.settings_author),
            style = Theme.typography.body,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAuthorClick)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
        Text(
            text = stringResource(R.string.settings_author_name),
            style = Theme.typography.caption,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
    }
}
