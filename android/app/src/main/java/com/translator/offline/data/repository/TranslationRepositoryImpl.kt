package com.translator.offline.data.repository

import com.translator.offline.data.db.dao.TranslationHistoryDao
import com.translator.offline.data.db.entity.TranslationHistoryEntity
import com.translator.offline.data.ml.TranslatorManager
import com.translator.offline.domain.model.Translation
import com.translator.offline.domain.repository.TranslationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository que usa TranslatorManager para tradução
 * 
 * Oferece 2 modos ao usuário:
 * - MODO LEVE: ~5MB, rápido, 60-70% precisão
 * - MODO AVANÇADO: ~150MB, NLLB-200, 85-95% precisão
 */
@Singleton
class TranslationRepositoryImpl @Inject constructor(
    private val dao: TranslationHistoryDao,
    private val translatorManager: TranslatorManager
) : TranslationRepository {

    override suspend fun translateText(text: String, sourceLang: String, targetLang: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val result = translatorManager.translate(text, sourceLang, targetLang)
                result
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
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

    // Métodos do TranslatorManager para UI
    fun getCurrentMode(): String = translatorManager.getCurrentMode()
    fun setMode(mode: String) = translatorManager.setMode(mode)
    fun getRecommendedMode(): String = translatorManager.getRecommendedMode()
    fun getModeInfo() = translatorManager.getModeInfo()

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
