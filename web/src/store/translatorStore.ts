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
  setSourceText: (text: string) => void
  setTranslatedText: (text: string) => void

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

const translateOffline = async (text: string, source: string, target: string): Promise<string> => {
  // Simulação de tradução offline
  // Em produção, usaria ML Kit Translation API
  await new Promise(resolve => setTimeout(resolve, 300))
  return `[${target.toUpperCase()}] ${text}`
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
      setSourceText: (text) => set({ sourceText: text }),
      setTranslatedText: (text) => set({ translatedText: text }),

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
          // Simular download de modelo offline
          await new Promise(resolve => setTimeout(resolve, 2000))
          
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

export { translateOffline }
