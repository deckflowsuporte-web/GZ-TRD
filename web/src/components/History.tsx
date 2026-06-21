import useTranslatorStore from '../store/translatorStore'

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

export default function History() {
  const { history, clearHistory, removeFromHistory } = useTranslatorStore()

  const formatDate = (timestamp: number) => {
    const date = new Date(timestamp)
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  if (history.length === 0) {
    return (
      <div className="bg-white rounded-xl shadow-lg p-8 text-center">
        <div className="text-6xl mb-4">📜</div>
        <h2 className="text-xl font-bold text-gray-800 mb-2">Histórico Vazio</h2>
        <p className="text-gray-500">
          Suas traduções aparecerão aqui
        </p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-bold text-gray-800">
          📜 Histórico de Traduções
        </h2>
        <button
          onClick={clearHistory}
          className="px-4 py-2 text-red-600 hover:bg-red-50 rounded-lg transition font-medium"
        >
          🗑️ Limpar Tudo
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-lg overflow-hidden">
        <div className="divide-y divide-gray-100">
          {history.map((item) => (
            <div key={item.id} className="p-4 hover:bg-gray-50 transition">
              <div className="flex justify-between items-start">
                <div className="flex-1 min-w-0">
                  {/* Idiomas */}
                  <div className="flex items-center gap-2 text-sm text-gray-500 mb-2">
                    <span className="bg-gray-100 px-2 py-1 rounded">
                      {LANGUAGES[item.sourceLanguage] || item.sourceLanguage}
                    </span>
                    <span>→</span>
                    <span className="bg-indigo-100 text-indigo-700 px-2 py-1 rounded">
                      {LANGUAGES[item.targetLanguage] || item.targetLanguage}
                    </span>
                  </div>

                  {/* Textos */}
                  <div className="space-y-2">
                    <p className="text-gray-900">
                      <span className="text-gray-500 text-sm">Original:</span>{' '}
                      {item.sourceText}
                    </p>
                    <p className="text-indigo-700 font-medium">
                      <span className="text-indigo-400 text-sm">Tradução:</span>{' '}
                      {item.translatedText}
                    </p>
                  </div>

                  {/* Data */}
                  <p className="text-xs text-gray-400 mt-2">
                    {formatDate(item.timestamp)}
                  </p>
                </div>

                {/* Botão de excluir */}
                <button
                  onClick={() => removeFromHistory(item.id)}
                  className="ml-4 p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition"
                  title="Remover"
                >
                  🗑️
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      <p className="text-center text-sm text-gray-500">
        Total: {history.length} tradução{history.length !== 1 ? 'ões' : ''}
      </p>
    </div>
  )
}
