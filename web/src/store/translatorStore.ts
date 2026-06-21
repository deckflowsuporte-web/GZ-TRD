import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface Translation {
  id: string
  sourceText: string
  translatedText: string
  sourceLanguage: string
  targetLanguage: string
  timestamp: number
}

interface TranslatorState {
  // Idiomas
  sourceLanguage: string
  targetLanguage: string
  setSourceLanguage: (lang: string) => void
  setTargetLanguage: (lang: string) => void
  swapLanguages: () => void

  // Tradução
  sourceText: string
  translatedText: string
  isTranslating: boolean
  translationError: string | null
  setSourceText: (text: string) => void
  setTranslatedText: (text: string) => void
  setIsTranslating: (value: boolean) => void
  setTranslationError: (error: string | null) => void

  // Histórico
  history: Translation[]
  addToHistory: (translation: Translation) => void
  removeFromHistory: (id: string) => void
  clearHistory: () => void

  // Idiomas baixados (offline)
  downloadedLanguages: string[]
  isDownloading: string | null
  downloadLanguage: (code: string) => Promise<void>
}

// Cache local para traduções (LRU cache)
const translationCache = new Map<string, string>()
const MAX_CACHE_SIZE = 100

// Tradução usando MyMemory API (gratuita, 1000req/dia)
const translateWithAPI = async (text: string, source: string, target: string): Promise<string> => {
  // Verificar cache primeiro
  const cacheKey = `${source}|${target}|${text}`
  if (translationCache.has(cacheKey)) {
    return translationCache.get(cacheKey)!
  }

  // Preparar texto para API
  const encodedText = encodeURIComponent(text)
  const langPair = `${source}|${target}`
  
  try {
    // MyMemory API - gratuito até 1000 palavras/dia
    const response = await fetch(
      `https://api.mymemory.translated.net/get?q=${encodedText}&langpair=${langPair}`
    )
    
    if (!response.ok) {
      throw new Error(`API Error: ${response.status}`)
    }
    
    const data = await response.json()
    
    if (data.responseStatus === 200 && data.responseData) {
      const translatedText = data.responseData.translatedText
      
      // Salvar no cache
      if (translationCache.size >= MAX_CACHE_SIZE) {
        // Remover entrada mais antiga
        const firstKey = translationCache.keys().next().value
        translationCache.delete(firstKey)
      }
      translationCache.set(cacheKey, translatedText)
      
      return translatedText
    } else {
      throw new Error(data.responseDetails || 'Translation failed')
    }
  } catch (error) {
    console.error('Translation error:', error)
    throw error
  }
}

export const useTranslatorStore = create<TranslatorState>()(
  persist(
    (set, get) => ({
      // Idiomas padrão
      sourceLanguage: 'pt',
      targetLanguage: 'en',
      setSourceLanguage: (lang) => set({ sourceLanguage: lang }),
      setTargetLanguage: (lang) => set({ targetLanguage: lang }),
      swapLanguages: () => set((state) => ({
        sourceLanguage: state.targetLanguage,
        targetLanguage: state.sourceLanguage,
        sourceText: state.translatedText,
        translatedText: state.sourceText
      })),

      // Textos
      sourceText: '',
      translatedText: '',
      isTranslating: false,
      translationError: null,
      setSourceText: (text) => set({ sourceText: text }),
      setTranslatedText: (text) => set({ translatedText: text }),
      setIsTranslating: (value) => set({ isTranslating: value }),
      setTranslationError: (error) => set({ translationError: error }),

      // Histórico
      history: [],
      addToHistory: (translation) => set((state) => ({
        history: [translation, ...state.history].slice(0, 100) // Limite de 100 itens
      })),
      removeFromHistory: (id) => set((state) => ({
        history: state.history.filter((item) => item.id !== id)
      })),
      clearHistory: () => set({ history: [] }),

      // Idiomas offline
      downloadedLanguages: ['pt', 'en'],
      isDownloading: null,
      downloadLanguage: async (code) => {
        set({ isDownloading: code })
        try {
          // MyMemory não precisa de download, mas simulamos para UI consistente
          await new Promise(resolve => setTimeout(resolve, 1000))
          
          set((state) => ({
            downloadedLanguages: [...state.downloadedLanguages, code],
            isDownloading: null
          }))
        } catch (error) {
          set({ isDownloading: null })
          throw error
        }
      }
    }),
    {
      name: 'translator-storage',
      partialize: (state) => ({
        sourceLanguage: state.sourceLanguage,
        targetLanguage: state.targetLanguage,
        history: state.history,
        downloadedLanguages: state.downloadedLanguages
      })
    }
  )
)

export { translateWithAPI }
