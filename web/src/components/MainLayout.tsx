import { ReactNode } from 'react'

interface MainLayoutProps {
  activeTab: string
  onTabChange: (tab: 'text' | 'camera' | 'audio' | 'history' | 'languages') => void
  children: ReactNode
}

const tabs = [
  { id: 'text', label: 'Texto', icon: '📝' },
  { id: 'camera', label: 'Câmera', icon: '📷' },
  { id: 'audio', label: 'Áudio', icon: '🎤' },
  { id: 'history', label: 'Histórico', icon: '📜' },
  { id: 'languages', label: 'Idiomas', icon: '🌐' },
]

export default function MainLayout({ activeTab, onTabChange, children }: MainLayoutProps) {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-indigo-600 text-white shadow-lg">
        <div className="max-w-4xl mx-auto px-4 py-4">
          <h1 className="text-2xl font-bold">🌐 Tradutor Offline</h1>
          <p className="text-indigo-200 text-sm mt-1">
            Tradução offline com inteligência artificial
          </p>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 py-6">
        {children}
      </main>

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 shadow-lg">
        <div className="max-w-4xl mx-auto flex justify-around">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => onTabChange(tab.id as typeof activeTab)}
              className={`flex flex-col items-center py-3 px-4 transition-colors ${
                activeTab === tab.id
                  ? 'text-indigo-600 bg-indigo-50'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              <span className="text-2xl">{tab.icon}</span>
              <span className="text-xs mt-1 font-medium">{tab.label}</span>
            </button>
          ))}
        </div>
      </nav>

      {/* Spacer for bottom nav */}
      <div className="h-20" />
    </div>
  )
}
