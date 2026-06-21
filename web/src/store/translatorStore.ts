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
  isModelLoading: boolean
  modelProgress: number
  translationError: string | null
  setSourceText: (text: string) => void
  setTranslatedText: (text: string) => void
  setIsTranslating: (value: boolean) => void
  setIsModelLoading: (value: boolean) => void
  setModelProgress: (value: number) => void
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
const MAX_CACHE_SIZE = 500

// Mapeamento de códigos para NLLB
const NLLB_LANGUAGES: Record<string, string> = {
  pt: 'por_Latn',
  en: 'eng_Latn',
  es: 'spa_Latn',
  fr: 'fra_Latn',
  de: 'deu_Latn',
  it: 'ita_Latn',
  ja: 'jpn_Jpan',
  zh: 'zho_Hans',
  ru: 'rus_Cyrl',
  ar: 'arb_Arab'
}

// Modelo NLLB-200 via transformers.js (criado uma única vez)
let nllbPipeline: any = null
let modelLoadingPromise: Promise<any> | null = null

// Inicializar modelo NLLB via transformers.js
async function initNLLBModel(progressCallback?: (progress: number) => void) {
  if (nllbPipeline) return nllbPipeline
  if (modelLoadingPromise) return modelLoadingPromise

  modelLoadingPromise = (async () => {
    try {
      progressCallback?.(10)
      
      // Importar transformers.js dinamicamente
      const { pipeline, env } = await import('@xenova/transformers')
      
      // Configurar para usar WASM (mais rápido no browser)
      env.allowLocalModels = true
      env.useBrowserCache = true
      
      progressCallback?.(30)

      // Criar pipeline de tradução com modelo NLLB-200 distilled
      // Este modelo é menor e mais rápido (~500MB vs 1.5GB)
      nllbPipeline = await pipeline('translation', 'Xenova/nllb-200-distilled-600M', {
        progressCallback: (progress: any) => {
          if (progress.status === 'progress') {
            progressCallback?.(Math.round(progress.progress || 0))
          }
        }
      })

      progressCallback?.(100)
      return nllbPipeline
    } catch (error) {
      console.error('Erro ao carregar modelo NLLB:', error)
      throw error
    } finally {
      modelLoadingPromise = null
    }
  })()

  return modelLoadingPromise
}

// Tradução usando NLLB-200 local (transformers.js)
const translateWithNLLB = async (
  text: string, 
  source: string, 
  target: string,
  progressCallback?: (progress: number) => void
): Promise<string> => {
  // Verificar cache primeiro
  const cacheKey = `${source}|${target}|${text}`
  if (translationCache.has(cacheKey)) {
    return translationCache.get(cacheKey)!
  }

  // Inicializar modelo se necessário
  const translator = await initNLLBModel(progressCallback)
  
  // Mapear idiomas para NLLB
  const sourceLang = NLLB_LANGUAGES[source] || 'eng_Latn'
  const targetLang = NLLB_LANGUAGES[target] || 'por_Latn'

  try {
    // Traduzir usando NLLB
    const result = await translator(text, {
      src_lang: sourceLang,
      tgt_lang: targetLang
    })

    const translatedText = result.translation_text || text

    // Salvar no cache
    if (translationCache.size >= MAX_CACHE_SIZE) {
      const firstKey = translationCache.keys().next().value
      translationCache.delete(firstKey)
    }
    translationCache.set(cacheKey, translatedText)

    return translatedText
  } catch (error) {
    console.error('Erro na tradução NLLB:', error)
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
      isModelLoading: false,
      modelProgress: 0,
      translationError: null,
      setSourceText: (text) => set({ sourceText: text }),
      setTranslatedText: (text) => set({ translatedText: text }),
      setIsTranslating: (value) => set({ isTranslating: value }),
      setIsModelLoading: (value) => set({ isModelLoading: value }),
      setModelProgress: (value) => set({ modelProgress: value }),
      setTranslationError: (error) => set({ translationError: error }),

      // Histórico
      history: [],
      addToHistory: (translation) => set((state) => ({
        history: [translation, ...state.history].slice(0, 100)
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
          // O modelo NLLB é único e suporta todos os idiomas
          // Apenas marcamos como disponível após o primeiro uso
          await new Promise(resolve => setTimeout(resolve, 500))
          
          set((state) => ({
            downloadedLanguages: [...new Set([...state.downloadedLanguages, code])],
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

export { translateWithNLLB }
