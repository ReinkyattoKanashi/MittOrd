package com.reiny.mittord.ui.screens.home

sealed interface HomeEvent {
    data object TranslationFailed : HomeEvent
}
