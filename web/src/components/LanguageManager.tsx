import useTranslatorStore from '../store/translatorStore'

const LANGUAGES: Record<string, { name: string; flag: string; downloaded: boolean }> = {
  pt: { name: 'Português', flag: '🇧🇷', downloaded: true },
  en: { name: 'English', flag: '🇺🇸', downloaded: true },
  es: { name: 'Español', flag: '🇪🇸', downloaded: false },
  fr: { name: 'Français', flag: '🇫🇷', downloaded: false },
  de: { name: 'Deutsch', flag: '🇩🇪', downloaded: false },
  it: { name: 'Italiano', flag: '🇮🇹', downloaded: false },
  ja: { name: '日本語', flag: '🇯🇵', downloaded: false },
  zh: { name: '中文', flag: '🇨🇳', downloaded: false },
  ru: { name: 'Русский', flag: '🇷🇺', downloaded: false },
  ar: { name: 'العربية', flag: '🇸🇦', downloaded: false },
}

export default function LanguageManager() {
  const { downloadLanguage, downloadedLanguages, isDownloading } = useTranslatorStore()

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-xl shadow-lg p-6">
        <h2 className="text-xl font-bold text-gray-800 mb-2">🌐 Gerenciar Idiomas</h2>
        <p className="text-gray-500 text-sm mb-6">
          Baixe idiomas para tradução offline. Cada idioma ocupa aproximadamente 20-35 MB.
        </p>

        <div className="space-y-3">
          {Object.entries(LANGUAGES).map(([code, lang]) => {
            const isDownloaded = downloadedLanguages.includes(code)
            const isCurrentlyDownloading = isDownloading === code

            return (
              <div
                key={code}
                className={`flex items-center justify-between p-4 rounded-lg border ${
                  isDownloaded
                    ? 'border-green-200 bg-green-50'
                    : 'border-gray-200 bg-gray-50'
                }`}
              >
                <div className="flex items-center gap-3">
                  <span className="text-2xl">{lang.flag}</span>
                  <div>
                    <p className="font-medium text-gray-900">{lang.name}</p>
                    <p className="text-xs text-gray-500">
                      {isDownloaded
                        ? '✓ Baixado - Disponível offline'
                        : '~20-35 MB para download'}
                    </p>
                  </div>
                </div>

                <button
                  onClick={() => !isDownloaded && downloadLanguage(code)}
                  disabled={isDownloaded || isCurrentlyDownloading}
                  className={`px-4 py-2 rounded-lg font-medium transition ${
                    isDownloaded
                      ? 'bg-green-100 text-green-700 cursor-default'
                      : isCurrentlyDownloading
                      ? 'bg-indigo-100 text-indigo-400 cursor-wait'
                      : 'bg-indigo-600 text-white hover:bg-indigo-700'
                  }`}
                >
                  {isCurrentlyDownloading ? (
                    <span className="flex items-center gap-2">
                      <span className="animate-spin">⏳</span>
                      Baixando...
                    </span>
                  ) : isDownloaded ? (
                    '✓ Baixado'
                  ) : (
                    '⬇️ Baixar'
                  )}
                </button>
              </div>
            )
          })}
        </div>
      </div>

      {/* Info */}
      <div className="bg-amber-50 border border-amber-200 rounded-xl p-4">
        <h3 className="font-semibold text-amber-800 mb-2">💡 Dica</h3>
        <p className="text-sm text-amber-700">
          Idiomas baixados funcionam offline! Isso é ideal para quando você está 
          viajando ou não tem conexão com a internet.
        </p>
      </div>
    </div>
  )
}
