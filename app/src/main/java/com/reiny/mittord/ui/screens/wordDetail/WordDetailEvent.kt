package com.reiny.mittord.ui.screens.wordDetail

sealed interface WordDetailEvent {
    data object Saved : WordDetailEvent
    data object Deleted : WordDetailEvent
    data object TranslationFailed : WordDetailEvent
    data object ImageSaveFailed : WordDetailEvent
}
