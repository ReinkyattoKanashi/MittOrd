package com.reiny.mittord.domain.usecase

import com.reiny.mittord.data.repository.TranslateRepository
import javax.inject.Inject

class DetectLanguageUseCase @Inject constructor(
    private val repository: TranslateRepository
) {
    suspend operator fun invoke(text: String): String? = repository.detectLanguage(text)
}
