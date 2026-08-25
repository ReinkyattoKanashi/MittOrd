package com.reiny.mittord.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.reiny.mittord.R
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography
import kotlinx.coroutines.delay

/** Lets the center button finish widening before the keyboard is asked for. */
private const val FOCUS_DELAY_MS = 100L

/** Width of an IconButton, held by a spacer so the field does not jump when it appears. */
private val CLEAR_BUTTON_WIDTH = 48.dp

/** Search input that lives inside the widened center button of the navigation bar. */
@Composable
internal fun BoxScope.NavSearchField(
    state: () -> BottomNavState,
    search: SearchState
) {
    val focusRequester = remember { FocusRequester() }
    val isSearch = state() == BottomNavState.Search
    val placeholderSearch = stringResource(R.string.placeholder_search)
    val cdClear = stringResource(R.string.cd_clear)

    LaunchedEffect(isSearch) {
        if (isSearch) {
            delay(FOCUS_DELAY_MS)
            // Search can be closed again before the delay is over.
            runCatching { focusRequester.requestFocus() }
        }
    }

    AnimatedVisibility(isSearch) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(70.dp))
            PrimaryTextField(
                value = search.query(),
                onValueChange = search.onQueryChange,
                placeholder = placeholderSearch,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                style = Theme.typography.h2,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                cursorColor = MaterialTheme.colorScheme.onPrimary
            )
            if (search.query().isNotEmpty()) {
                IconButton(onClick = search.onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = cdClear,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else {
                Spacer(Modifier.width(CLEAR_BUTTON_WIDTH))
            }
        }
    }
}
