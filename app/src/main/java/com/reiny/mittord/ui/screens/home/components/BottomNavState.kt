package com.reiny.mittord.ui.screens.home.components

import com.reiny.mittord.ui.screens.home.AddWordUiState

enum class BottomNavState {
    Default, Search, AddWord
}

/**
 * [state] and [query] are lambdas rather than values: invoked inside the composable
 * that renders them, the state read is recorded in that leaf's recomposition scope,
 * so typing does not recompose the home screen or the navigation bar around it.
 */
data class AddWordState(
    val state: () -> AddWordUiState,
    val onWordChange: (String) -> Unit,
    val onTranslationChange: (String) -> Unit,
    val onWordLanguageSelected: (String?) -> Unit,
    val onTranslationLanguageSelected: (String?) -> Unit,
    val onTranslateTranslation: (String) -> Unit,
    val onAddWord: () -> Unit
)

data class SearchState(
    val query: () -> String,
    val onQueryChange: (String) -> Unit,
    val onClear: () -> Unit
)
