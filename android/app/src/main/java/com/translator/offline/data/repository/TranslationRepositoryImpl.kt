package com.translator.offline.data.repository

import com.translator.offline.data.db.dao.TranslationHistoryDao
import com.translator.offline.data.db.entity.TranslationHistoryEntity
import com.translator.offline.domain.model.Translation
import com.translator.offline.domain.repository.TranslationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TranslationRepositoryImpl @Inject constructor(
    private val dao: TranslationHistoryDao
) : TranslationRepository {

    override suspend fun translateText(text: String, sourceLang: String, targetLang: String): Result<String> {
        return try {
            // Simulação de tradução offline com MLKit
            // Em produção, usaria: ML Kit Translation API
            val translatedText = translateWithMock(text, sourceLang, targetLang)
            Result.success(translatedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun translateWithMock(text: String, sourceLang: String, targetLang: String): String {
        // Mock de tradução - em produção usar ML Kit offline
        return "[$targetLang] $text"
    }

    override fun getTranslationHistory(): Flow<List<Translation>> {
        return dao.getAllTranslations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveTranslation(translation: Translation) {
        dao.insertTranslation(translation.toEntity())
    }

    override suspend fun deleteTranslation(id: String) {
        dao.deleteTranslation(id)
    }

    override suspend fun clearHistory() {
        dao.clearAll()
    }

    private fun TranslationHistoryEntity.toDomain(): Translation {
        return Translation(
            id = id,
            sourceText = sourceText,
            translatedText = translatedText,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            timestamp = timestamp
        )
    }

    private fun Translation.toEntity(): TranslationHistoryEntity {
        return TranslationHistoryEntity(
            id = id,
            sourceText = sourceText,
            translatedText = translatedText,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            timestamp = timestamp
        )
    }
}
