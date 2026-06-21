package com.translator.offline.domain.repository

import com.translator.offline.domain.model.Translation
import kotlinx.coroutines.flow.Flow

interface TranslationRepository {
    suspend fun translateText(text: String, sourceLang: String, targetLang: String): Result<String>
    fun getTranslationHistory(): Flow<List<Translation>>
    suspend fun saveTranslation(translation: Translation)
    suspend fun deleteTranslation(id: String)
    suspend fun clearHistory()
}
