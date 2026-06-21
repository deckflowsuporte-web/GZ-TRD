package com.translator.offline.domain.usecase

import com.translator.offline.domain.repository.TranslationRepository
import javax.inject.Inject

class TranslateTextUseCase @Inject constructor(
    private val repository: TranslationRepository
) {
    suspend operator fun invoke(text: String, sourceLang: String, targetLang: String): Result<String> {
        if (text.isBlank()) {
            return Result.failure(IllegalArgumentException("Texto não pode estar vazio"))
        }
        return repository.translateText(text, sourceLang, targetLang)
    }
}
