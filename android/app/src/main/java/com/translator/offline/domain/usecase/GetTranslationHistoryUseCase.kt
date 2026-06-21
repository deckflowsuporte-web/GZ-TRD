package com.translator.offline.domain.usecase

import com.translator.offline.domain.model.Translation
import com.translator.offline.domain.repository.TranslationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTranslationHistoryUseCase @Inject constructor(
    private val repository: TranslationRepository
) {
    operator fun invoke(): Flow<List<Translation>> {
        return repository.getTranslationHistory()
    }
}
