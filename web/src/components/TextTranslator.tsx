import { useState } from 'react'
import useTranslatorStore from '../store/translatorStore'

const LANGUAGES = {
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
    addToHistory
  } = useTranslatorStore()

  const [isLoading, setIsLoading] = useState(false)

  const handleTranslate = async () => {
    if (!sourceText.trim()) return

    setIsLoading(true)
    try {
      await new Promise(resolve => setTimeout(resolve, 500))
      const result = `Tradução de: "${sourceText}"`
      setTranslatedText(result)
      
      addToHistory({
        id: Date.now().toString(),
        sourceText,
        translatedText: result,
        sourceLanguage,
        targetLanguage,
        timestamp: Date.now()
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <div className="flex items-center gap-4">
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

      <div className="flex gap-3">
        <button
          onClick={handleTranslate}
          disabled={!sourceText.trim() || isLoading}
          className="flex-1 bg-indigo-600 text-white font-semibold py-3 rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
        >
          {isLoading ? '⏳ Traduzindo...' : '🔄 Traduzir'}
        </button>
        <button
          onClick={() => setSourceText('')}
          className="px-6 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition font-medium"
        >
          Limpar
        </button>
      </div>
    </div>
  )
}
