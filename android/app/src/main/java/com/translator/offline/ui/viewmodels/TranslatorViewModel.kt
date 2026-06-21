package com.translator.offline.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.translator.offline.domain.model.Language
import com.translator.offline.domain.model.Translation
import com.translator.offline.domain.repository.TranslationRepository
import com.translator.offline.domain.usecase.GetTranslationHistoryUseCase
import com.translator.offline.domain.usecase.TranslateTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TranslatorUiState(
    val sourceText: String = "",
    val translatedText: String = "",
    val sourceLanguage: Language = Language.PORTUGUESE,
    val targetLanguage: Language = Language.ENGLISH,
    val isLoading: Boolean = false,
    val isModelDownloading: Boolean = false,
    val error: String? = null,
    val history: List<Translation> = emptyList(),
    val translationMode: String = "light" // light, advanced, auto
)

@HiltViewModel
class TranslatorViewModel @Inject constructor(
    private val translateTextUseCase: TranslateTextUseCase,
    private val getTranslationHistoryUseCase: GetTranslationHistoryUseCase,
    private val repository: TranslationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TranslatorUiState())
    val uiState: StateFlow<TranslatorUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
        loadCurrentMode()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getTranslationHistoryUseCase().collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
    }

    private fun loadCurrentMode() {
        // Obter modo salvo (se repository implementar)
        try {
            val mode = (repository as? com.translator.offline.data.repository.TranslationRepositoryImpl)?.getCurrentMode() ?: "light"
            _uiState.update { it.copy(translationMode = mode) }
        } catch (e: Exception) {
            // Ignora erro, usa padrão
        }
    }

    fun setTranslationMode(mode: String) {
        _uiState.update { it.copy(translationMode = mode) }
        try {
            (repository as? com.translator.offline.data.repository.TranslationRepositoryImpl)?.setMode(mode)
        } catch (e: Exception) {
            // Ignora
        }
    }

    fun updateSourceText(text: String) {
        _uiState.update { it.copy(sourceText = text) }
    }

    fun updateSourceLanguage(language: Language) {
        _uiState.update { it.copy(sourceLanguage = language) }
    }

    fun updateTargetLanguage(language: Language) {
        _uiState.update { it.copy(targetLanguage = language) }
    }

    fun swapLanguages() {
        _uiState.update { state ->
            state.copy(
                sourceLanguage = state.targetLanguage,
                targetLanguage = state.sourceLanguage,
                sourceText = state.translatedText,
                translatedText = state.sourceText
            )
        }
    }

    fun translate() {
        val currentState = _uiState.value
        if (currentState.sourceText.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isModelDownloading = true, error = null) }

            translateTextUseCase(
                text = currentState.sourceText,
                sourceLang = currentState.sourceLanguage.code,
                targetLang = currentState.targetLanguage.code
            ).onSuccess { translatedText ->
                // Salvar no histórico
                val translation = Translation(
                    id = UUID.randomUUID().toString(),
                    sourceText = currentState.sourceText,
                    translatedText = translatedText,
                    sourceLanguage = currentState.sourceLanguage.code,
                    targetLanguage = currentState.targetLanguage.code,
                    timestamp = System.currentTimeMillis()
                )
                repository.saveTranslation(translation)
                
                _uiState.update { 
                    it.copy(
                        translatedText = translatedText, 
                        isLoading = false,
                        isModelDownloading = false
                    ) 
                }
            }.onFailure { exception ->
                _uiState.update { 
                    it.copy(
                        error = exception.message ?: "Erro na tradução",
                        isLoading = false,
                        isModelDownloading = false
                    ) 
                }
            }
        }
    }

    fun clearText() {
        _uiState.update { it.copy(sourceText = "", translatedText = "") }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun loadFromHistory(translation: Translation) {
        _uiState.update {
            it.copy(
                sourceText = translation.sourceText,
                translatedText = translation.translatedText,
                sourceLanguage = Language.fromCode(translation.sourceLanguage),
                targetLanguage = Language.fromCode(translation.targetLanguage)
            )
        }
    }
}
