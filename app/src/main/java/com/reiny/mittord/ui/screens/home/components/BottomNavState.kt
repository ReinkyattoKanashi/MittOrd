package com.reiny.mittord.ui.screens.home.components

enum class BottomNavState {
    Default, Search, AddWord
}

data class AddWordState(
    val wordInput: String,
    val translationInput: String,
    val wordLanguageCode: String?,
    val translationLanguageCode: String?,
    val wordLanguageIsAuto: Boolean,
    val translationLanguageIsAuto: Boolean,
    val isTranslating: Boolean = false,
    val onWordChange: (String) -> Unit,
    val onTranslationChange: (String) -> Unit,
    val onWordLanguageSelected: (String?) -> Unit,
    val onTranslationLanguageSelected: (String?) -> Unit,
    val onTranslateTranslation: (String) -> Unit,
    val onAddWord: () -> Unit
)

data class SearchState(
    val query: String,
    val onQueryChange: (String) -> Unit,
    val onClear: () -> Unit
)
