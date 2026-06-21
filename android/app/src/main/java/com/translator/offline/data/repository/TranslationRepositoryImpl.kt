package com.translator.offline.data.repository

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.translator.offline.data.db.dao.TranslationHistoryDao
import com.translator.offline.data.db.entity.TranslationHistoryEntity
import com.translator.offline.domain.model.Translation
import com.translator.offline.domain.repository.TranslationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TranslationRepositoryImpl @Inject constructor(
    private val dao: TranslationHistoryDao
) : TranslationRepository {

    override suspend fun translateText(text: String, sourceLang: String, targetLang: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val translatedText = performTranslation(text, sourceLang, targetLang)
                Result.success(translatedText)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun performTranslation(text: String, sourceLang: String, targetLang: String): String {
        val sourceLanguage = mapToMlKitLanguage(sourceLang)
        val targetLanguage = mapToMlKitLanguage(targetLang)

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage)
            .setTargetLanguage(targetLanguage)
            .build()

        val translator = Translation.getClient(options)

        // Condições de download para tradução offline
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        // Garantir que o modelo está disponível
        suspendCancellableCoroutine { continuation ->
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    // Modelo baixado, agora traduzir
                    translator.translate(text)
                        .addOnSuccessListener { translatedText ->
                            continuation.resume(translatedText)
                        }
                        .addOnFailureListener { exception ->
                            continuation.resumeWithException(exception)
                        }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    private fun mapToMlKitLanguage(code: String): String {
        return when (code) {
            "pt" -> TranslateLanguage.PORTUGUESE
            "en" -> TranslateLanguage.ENGLISH
            "es" -> TranslateLanguage.SPANISH
            "fr" -> TranslateLanguage.FRENCH
            "de" -> TranslateLanguage.GERMAN
            "it" -> TranslateLanguage.ITALIAN
            "ja" -> TranslateLanguage.JAPANESE
            "zh" -> TranslateLanguage.CHINESE
            "ru" -> TranslateLanguage.RUSSIAN
            "ar" -> TranslateLanguage.ARABIC
            else -> TranslateLanguage.ENGLISH
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
