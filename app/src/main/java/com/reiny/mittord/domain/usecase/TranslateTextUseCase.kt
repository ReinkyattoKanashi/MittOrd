package com.reiny.mittord.domain.usecase

import com.reiny.mittord.data.repository.TranslateRepository
import javax.inject.Inject

class TranslateTextUseCase @Inject constructor(
    private val repository: TranslateRepository
) {
    suspend operator fun invoke(text: String, targetLanguageCode: String): String? =
        repository.translateText(text, targetLanguageCode)
}
