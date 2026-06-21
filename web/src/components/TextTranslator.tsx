import { useState } from 'react'
import useTranslatorStore, { translateWithAPI } from '../store/translatorStore'

const LANGUAGES: Record<string, string> = {
  pt: 'Português',
  en: 'English',
  es: 'Español',
  fr: 'Français',
  de: 'Deutsch',
  it: 'Italiano',
  ja: '日本語',
  zh: '中文',
  ru: 'Русский',
  ar: 'العربية'
}

export default function TextTranslator() {
  const {
    sourceLanguage,
    targetLanguage,
    sourceText,
    translatedText,
    setSourceLanguage,
    setTargetLanguage,
    setSourceText,
    setTranslatedText,
    swapLanguages,
    addToHistory,
    isTranslating,
    setIsTranslating,
    translationError,
    setTranslationError
  } = useTranslatorStore()

  const handleTranslate = async () => {
    if (!sourceText.trim()) return

    setIsTranslating(true)
    setTranslationError(null)
    
    try {
      const result = await translateWithAPI(sourceText, sourceLanguage, targetLanguage)
      setTranslatedText(result)
      
      addToHistory({
        id: Date.now().toString(),
        sourceText,
        translatedText: result,
        sourceLanguage,
        targetLanguage,
        timestamp: Date.now()
      })
    } catch (error) {
      setTranslationError(error instanceof Error ? error.message : 'Erro na tradução')
    } finally {
      setIsTranslating(false)
    }
  }

  return (
    <div className="bg-white rounded-xl shadow-lg p-6">
      <h2 className="text-xl font-bold text-gray-800 mb-4">📝 Tradutor de Texto</h2>
      
      <div className="flex items-center gap-4 mb-4">
        <select
          value={sourceLanguage}
          onChange={(e) => setSourceLanguage(e.target.value)}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-600 focus:border-transparent"
        >
          {Object.entries(LANGUAGES).map(([code, name]) => (
            <option key={code} value={code}>{name}</option>
          ))}
        </select>

        <button
          onClick={swapLanguages}
          className="p-2 hover:bg-gray-100 rounded-lg transition"
          title="Trocar idiomas"
        >
          ⇄
        </button>

        <select
          value={targetLanguage}
          onChange={(e) => setTargetLanguage(e.target.value)}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-600 focus:border-transparent"
        >
          {Object.entries(LANGUAGES).map(([code, name]) => (
            <option key={code} value={code}>{name}</option>
          ))}
        </select>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <label className="block text-sm font-medium text-gray-700">
            Texto Original
          </label>
          <textarea
            value={sourceText}
            onChange={(e) => setSourceText(e.target.value)}
            placeholder="Digite o texto para traduzir..."
            className="w-full h-48 px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-600 focus:border-transparent resize-none"
          />
          <div className="text-xs text-gray-500 text-right">
            {sourceText.length} caracteres
          </div>
        </div>

        <div className="space-y-2">
          <label className="block text-sm font-medium text-gray-700">
            Texto Traduzido
          </label>
          <textarea
            value={translatedText}
            readOnly
            placeholder="Resultado da tradução aparecerá aqui..."
            className="w-full h-48 px-4 py-3 border border-gray-300 rounded-lg bg-gray-50 resize-none"
          />
          <div className="text-xs text-gray-500 text-right">
            {translatedText.length} caracteres
          </div>
        </div>
      </div>

      {/* Erro */}
      {translationError && (
        <div className="mb-4 p-3 bg-red-100 text-red-700 rounded-lg text-sm">
          ⚠️ {translationError}
        </div>
      )}

      <div className="flex gap-3">
        <button
          onClick={handleTranslate}
          disabled={!sourceText.trim() || isTranslating}
          className="flex-1 bg-indigo-600 text-white font-semibold py-3 rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition flex items-center justify-center gap-2"
        >
          {isTranslating ? (
            <>
              <span className="animate-spin">⏳</span>
              Traduzindo...
            </>
          ) : (
            '🔄 Traduzir'
          )}
        </button>
        <button
          onClick={() => {
            setSourceText('')
            setTranslatedText('')
            setTranslationError(null)
          }}
          className="px-6 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition font-medium"
        >
          Limpar
        </button>
      </div>

      {/* Info sobre API */}
      <div className="mt-4 p-3 bg-blue-50 rounded-lg">
        <p className="text-xs text-blue-700">
          💡 Powered by MyMemory Translation API
        </p>
      </div>
    </div>
  )
}
