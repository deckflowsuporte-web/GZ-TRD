import { useState } from 'react'
import useTranslatorStore, { translateWithAPI } from '../store/translatorStore'

// Mapeamento de códigos de idioma para Speech API
const SPEECH_LANGUAGES: Record<string, string> = {
  pt: 'pt-BR',
  en: 'en-US',
  es: 'es-ES',
  fr: 'fr-FR',
  de: 'de-DE',
  it: 'it-IT',
  ja: 'ja-JP',
  zh: 'zh-CN',
  ru: 'ru-RU',
  ar: 'ar-SA'
}

export default function AudioTranslator() {
  const {
    sourceLanguage,
    targetLanguage,
    setSourceLanguage,
    setTargetLanguage,
    addToHistory
  } = useTranslatorStore()

  const [isListening, setIsListening] = useState(false)
  const [recognizedText, setRecognizedText] = useState('')
  const [translatedText, setTranslatedText] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

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

  const startListening = () => {
    const SpeechRecognitionAPI = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    
    if (!SpeechRecognitionAPI) {
      setError('Seu navegador não suporta reconhecimento de voz. Tente usar Chrome.')
      return
    }

    const recognition = new SpeechRecognitionAPI()
    
    const speechLang = SPEECH_LANGUAGES[sourceLanguage] || 'en-US'
    recognition.lang = speechLang
    recognition.continuous = false
    recognition.interimResults = true
    recognition.maxAlternatives = 1

    recognition.onstart = () => {
      setIsListening(true)
      setError(null)
      setRecognizedText('')
      setTranslatedText('')
    }

    recognition.onresult = (event: any) => {
      const transcript = Array.from(event.results)
        .map((result: any) => result[0].transcript)
        .join('')
      
      setRecognizedText(transcript)
    }

    recognition.onerror = (event: any) => {
      if (event.error === 'no-speech') {
        setError('Nenhum discurso detectado. Tente falar mais alto.')
      } else {
        setError(`Erro no reconhecimento: ${event.error}`)
      }
      setIsListening(false)
    }

    recognition.onend = () => {
      setIsListening(false)
      if (recognizedText) {
        translateText(recognizedText)
      }
    }

    try {
      recognition.start()
    } catch (err) {
      setError('Erro ao iniciar o reconhecimento de voz')
    }
  }

  const translateText = async (text: string) => {
    setIsLoading(true)
    setError(null)
    try {
      const result = await translateWithAPI(text, sourceLanguage, targetLanguage)
      setTranslatedText(result)

      addToHistory({
        id: Date.now().toString(),
        sourceText: text,
        translatedText: result,
        sourceLanguage,
        targetLanguage,
        timestamp: Date.now()
      })
    } catch (err) {
      setError('Erro ao traduzir')
    } finally {
      setIsLoading(false)
    }
  }

  const speakText = (text: string, lang: string) => {
    if (!('speechSynthesis' in window)) {
      setError('Seu navegador não suporta síntese de voz')
      return
    }
    
    const utterance = new SpeechSynthesisUtterance(text)
    const speechLang = SPEECH_LANGUAGES[lang] || 'en-US'
    utterance.lang = speechLang
    speechSynthesis.speak(utterance)
  }

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-xl shadow-lg p-6">
        <h2 className="text-xl font-bold text-gray-800 mb-4">🎤 Tradutor de Áudio</h2>

        {/* Seletor de idiomas */}
        <div className="flex items-center gap-4 mb-6">
          <select
            value={sourceLanguage}
            onChange={(e) => setSourceLanguage(e.target.value)}
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-600"
          >
            {Object.entries(LANGUAGES).map(([code, name]) => (
              <option key={code} value={code}>{name}</option>
            ))}
          </select>

          <span className="text-gray-400">→</span>

          <select
            value={targetLanguage}
            onChange={(e) => setTargetLanguage(e.target.value)}
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-600"
          >
            {Object.entries(LANGUAGES).map(([code, name]) => (
              <option key={code} value={code}>{name}</option>
            ))}
          </select>
        </div>

        {/* Botão de gravação */}
        <div className="flex flex-col items-center mb-6">
          <button
            onClick={isListening ? () => {} : startListening}
            disabled={isLoading}
            className={`w-24 h-24 rounded-full flex items-center justify-center text-4xl transition-all transform ${
              isListening
                ? 'bg-red-500 animate-pulse scale-110'
                : 'bg-indigo-600 hover:bg-indigo-700 hover:scale-105'
            } text-white shadow-lg disabled:opacity-50`}
          >
            {isListening ? '🔴' : '🎤'}
          </button>
          <p className="mt-3 text-gray-600 font-medium">
            {isListening ? '🎙️ Ouvindo...' : 'Toque para falar'}
          </p>
        </div>

        {/* Erro */}
        {error && (
          <div className="mb-4 p-3 bg-red-100 text-red-700 rounded-lg">
            {error}
          </div>
        )}

        {/* Resultados */}
        <div className="space-y-4">
          {/* Texto reconhecido */}
          <div className="p-4 bg-gray-50 rounded-lg">
            <div className="flex justify-between items-center mb-2">
              <label className="text-sm font-medium text-gray-700">
                Texto Reconhecido
              </label>
              {recognizedText && (
                <button
                  onClick={() => speakText(recognizedText, sourceLanguage)}
                  className="text-indigo-600 hover:text-indigo-800"
                  title="Reproduzir áudio"
                >
                  🔊
                </button>
              )}
            </div>
            <p className="text-gray-900 min-h-[2rem]">
              {recognizedText || <span className="text-gray-400">Aguardando voz...</span>}
            </p>
          </div>

          {/* Tradução */}
          <div className="p-4 bg-indigo-50 rounded-lg">
            <div className="flex justify-between items-center mb-2">
              <label className="text-sm font-medium text-indigo-700">
                Tradução
              </label>
              {translatedText && (
                <button
                  onClick={() => speakText(translatedText, targetLanguage)}
                  className="text-indigo-600 hover:text-indigo-800"
                  title="Reproduzir áudio"
                >
                  🔊
                </button>
              )}
            </div>
            <p className="text-indigo-900 font-medium min-h-[2rem]">
              {isLoading ? (
                <span className="text-gray-400">Traduzindo...</span>
              ) : translatedText || <span className="text-gray-400">Resultado aparecerá aqui</span>}
            </p>
          </div>
        </div>
      </div>

      {/* Instruções */}
      <div className="bg-blue-50 border border-blue-200 rounded-xl p-4">
        <h3 className="font-semibold text-blue-800 mb-2">💡 Como usar</h3>
        <ul className="text-sm text-blue-700 space-y-1">
          <li>• Selecione o idioma de origem e destino</li>
          <li>• Clique no microfone e fale claramente</li>
          <li>• O texto será reconhecido automaticamente</li>
          <li>• Use os alto-falantes 🔊 para ouvir as traduções</li>
        </ul>
      </div>
    </div>
  )
}
