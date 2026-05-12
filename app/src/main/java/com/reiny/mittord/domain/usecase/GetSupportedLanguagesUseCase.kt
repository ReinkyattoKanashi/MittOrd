package com.reiny.mittord.domain.usecase

import com.reiny.mittord.data.repository.TranslateRepository
import com.reiny.mittord.domain.model.Language
import javax.inject.Inject

class GetSupportedLanguagesUseCase @Inject constructor(
    private val repository: TranslateRepository
) {
    suspend operator fun invoke(): List<Language> = repository.getSupportedLanguages()
}
