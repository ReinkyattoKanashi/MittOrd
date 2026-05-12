package com.reiny.mittord.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.reiny.mittord.R
import com.reiny.mittord.domain.model.Language
import com.reiny.mittord.domain.model.LANGUAGES
import com.reiny.mittord.ui.screens.home.components.WordInputField
import com.reiny.mittord.ui.theme.Theme
import com.reiny.mittord.ui.theme.typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LanguagePickerSheet(
    title: String,
    selected: String,
    isAutoSelected: Boolean = false,
    showAutoOption: Boolean = false,
    orderedLanguages: List<Language> = LANGUAGES,
    onSelect: (Language?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var search by remember { mutableStateOf("") }
    val filtered = remember(search, orderedLanguages) {
        if (search.isBlank()) orderedLanguages
        else orderedLanguages.filter { it.name.contains(search, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { WindowInsets.navigationBars }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = Theme.typography.h2,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
            )
            WordInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = search,
                onValueChange = { search = it },
                placeholder = stringResource(R.string.placeholder_search_language),
                icon = Icons.Default.Search
            )
            Spacer(Modifier.size(8.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                if (showAutoOption) {
                    item {
                        AutoDetectItem(isAutoSelected = isAutoSelected, onSelect = onSelect)
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
                items(filtered, key = { it.name }) { language ->
                    LanguageItem(
                        language = language,
                        selected = !isAutoSelected && language.name == selected,
                        onClick = { onSelect(language) }
                    )
                }
                item { Spacer(Modifier.size(16.dp)) }
            }
        }
    }
}

@Composable
private fun AutoDetectItem(isAutoSelected: Boolean, onSelect: (Language?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(null) }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_sparkles),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = stringResource(R.string.auto_detect),
            style = Theme.typography.body,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (isAutoSelected) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LanguageItem(language: Language, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = language.flag, style = Theme.typography.h2)
        Spacer(Modifier.width(14.dp))
        Text(
            text = language.name,
            style = Theme.typography.body,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
