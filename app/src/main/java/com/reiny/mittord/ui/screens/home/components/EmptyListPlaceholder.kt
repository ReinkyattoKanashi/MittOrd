package com.reiny.mittord.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.reiny.mittord.R
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography

@Composable
fun EmptyListPlaceholder(modifier: Modifier = Modifier, isFiltered: Boolean = false) {
    val title = if (isFiltered) stringResource(R.string.empty_search_title) else stringResource(R.string.empty_title)
    val subtitle = if (isFiltered) stringResource(R.string.empty_search_subtitle) else stringResource(R.string.empty_subtitle)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = Theme.typography.h1,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = Theme.typography.body,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
    }
}
