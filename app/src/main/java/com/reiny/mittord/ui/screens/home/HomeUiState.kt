package com.reiny.mittord.ui.screens.home

import com.reiny.mittord.ui.screens.home.components.BottomNavState

/** A single row of the word list, already prepared for display. */
data class WordListItem(
    val id: Long,
    val word: String,
    val wordFlag: String?,
    val translation: String?,
    val translationFlag: String?
)

data class WordsUiState(
    val words: List<WordListItem> = emptyList(),
    val isLoading: Boolean = true,
    val isFiltered: Boolean = false
)

data class AddWordUiState(
    val word: String = "",
    val translation: String = "",
    val wordLanguageCode: String? = null,
    val translationLanguageCode: String? = null,
    val wordLanguageIsAuto: Boolean = true,
    val translationLanguageIsAuto: Boolean = true,
    val isTranslating: Boolean = false
)

data class NavUiState(
    val state: BottomNavState = BottomNavState.Default,
    val searchQuery: String = ""
)
