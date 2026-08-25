package com.reiny.mittord.domain.usecase

import com.reiny.mittord.domain.model.LANGUAGES
import com.reiny.mittord.domain.model.Language
import com.reiny.mittord.util.AppPreferences
import javax.inject.Inject

class GetOrderedLanguagesUseCase @Inject constructor(
    private val getSupportedLanguagesUseCase: GetSupportedLanguagesUseCase,
    private val appPrefs: AppPreferences
) {
    suspend operator fun invoke(): List<Language> {
        val languages = getSupportedLanguagesUseCase().ifEmpty { LANGUAGES }
        return appPrefs.orderedLanguages(languages)
    }
}
