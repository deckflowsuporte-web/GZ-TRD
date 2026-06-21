package com.translator.offline.data.ml

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager de tradução que oferece 2 modos ao usuário:
 * 
 * 1. MODO LEVE (~5MB)
 *    - Baseado em dicionário local
 *    - Frases pré-traduzidas
 *    - Super rápido
 *    - Funciona em qualquer celular
 *    - Precisão: ~60-70%
 * 
 * 2. MODO AVANÇADO (~150MB)
 *    - NLLB-200 quantizado (INT8)
 *    - Inferência neural real
 *    - 200+ idiomas
 *    - Precisão: ~85-95%
 *    - Requer celular mais potente
 */
@Singleton
class TranslatorManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "translator_prefs"
        private const val KEY_TRANSLATION_MODE = "translation_mode"
        
        const val MODE_LIGHT = "light"
        const val MODE_ADVANCED = "advanced"
        const val MODE_AUTO = "auto" // Escolhe baseado no dispositivo
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val lightTranslator by lazy { LightweightTranslator(context) }
    private val advancedTranslator by lazy { NLLBTranslator(context) }

    // Callbacks para UI
    var onProgressUpdate: ((String, Int) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    /**
     * Obtém o modo atual selecionado
     */
    fun getCurrentMode(): String {
        return prefs.getString(KEY_TRANSLATION_MODE, MODE_LIGHT) ?: MODE_LIGHT
    }

    /**
     * Define o modo de tradução
     */
    fun setMode(mode: String) {
        prefs.edit().putString(KEY_TRANSLATION_MODE, mode).apply()
    }

    /**
     * Traduz texto usando o modo atual
     */
    suspend fun translate(text: String, sourceLang: String, targetLang: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val mode = getCurrentMode()
                val result = when (mode) {
                    MODE_LIGHT -> translateWithLight(text, sourceLang, targetLang)
                    MODE_ADVANCED -> translateWithAdvanced(text, sourceLang, targetLang)
                    MODE_AUTO -> translateAuto(text, sourceLang, targetLang)
                    else -> translateWithLight(text, sourceLang, targetLang)
                }
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun translateWithLight(text: String, source: String, target: String): String {
        return lightTranslator.translate(text, source, target)
    }

    private suspend fun translateWithAdvanced(text: String, source: String, target: String): String {
        advancedTranslator.onProgressUpdate = onProgressUpdate
        advancedTranslator.onError = onError
        return advancedTranslator.translate(text, source, target)
    }

    private suspend fun translateAuto(text: String, source: String, target: String): String {
        // Auto escolhe baseado na disponibilidade de idiomas
        // Se o par é suportado pelo modo leve, usa leve
        // Caso contrário, usa avançado
        return if (lightTranslator.isLanguagePairSupported(source, target)) {
            translateWithLight(text, source, target)
        } else {
            translateWithAdvanced(text, source, target)
        }
    }

    /**
     * Verifica qual modo é recomendado para o dispositivo
     */
    fun getRecommendedMode(): String {
        // Heurística simples baseada em memória disponível
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / (1024 * 1024) // MB
        
        return when {
            maxMemory < 128 -> MODE_LIGHT // Dispositivos com pouca RAM
            maxMemory < 256 -> MODE_AUTO
            else -> MODE_ADVANCED // Dispositivos potentes podem usar modo avançado
        }
    }

    /**
     * Retorna informações sobre cada modo
     */
    fun getModeInfo(): Map<String, ModeInfo> = mapOf(
        MODE_LIGHT to ModeInfo(
            name = "Modo Leve",
            description = "Dicionário local",
            size = "5 MB",
            languages = 6,
            precision = "60-70%",
            speed = "Instantâneo",
            requirements = "Qualquer celular"
        ),
        MODE_ADVANCED to ModeInfo(
            name = "Modo Avançado",
            description = "NLLB-200 (Inteligência Artificial)",
            size = "150 MB",
            languages = 20,
            precision = "85-95%",
            speed = "2-5 segundos",
            requirements = "Celular potente recomendado"
        ),
        MODE_AUTO to ModeInfo(
            name = "Automático",
            description = "Escolhe o melhor modo",
            size = "5-150 MB",
            languages = "6-20",
            precision = "60-95%",
            speed = "Variável",
            requirements = "Adaptativo"
        )
    )

    /**
     * Lista idiomas suportados por cada modo
     */
    fun getSupportedLanguages(mode: String): List<String> {
        return when (mode) {
            MODE_LIGHT -> lightTranslator.getSupportedLanguages()
            MODE_ADVANCED -> NLLBTranslator.SUPPORTED_LANGUAGES.keys.toList()
            MODE_AUTO -> (lightTranslator.getSupportedLanguages() + NLLBTranslator.SUPPORTED_LANGUAGES.keys).distinct()
            else -> lightTranslator.getSupportedLanguages()
        }
    }

    /**
     * Limpa modelos baixados do modo avançado
     */
    fun clearAdvancedModels() {
        advancedTranslator.clearAllModels()
    }

    /**
     * Obtém tamanho dos modelos avançados
     */
    fun getAdvancedModelsSize(): Long {
        return advancedTranslator.getTotalModelsSize()
    }
}

data class ModeInfo(
    val name: String,
    val description: String,
    val size: String,
    val languages: Any,
    val precision: String,
    val speed: String,
    val requirements: String
)
