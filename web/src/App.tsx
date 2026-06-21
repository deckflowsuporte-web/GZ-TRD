import { useState } from 'react'
import MainLayout from './components/MainLayout'
import TextTranslator from './components/TextTranslator'
import CameraTranslator from './components/CameraTranslator'
import AudioTranslator from './components/AudioTranslator'
import History from './components/History'
import LanguageManager from './components/LanguageManager'

type Tab = 'text' | 'camera' | 'audio' | 'history' | 'languages'

function App() {
  const [activeTab, setActiveTab] = useState<Tab>('text')

  return (
    <MainLayout activeTab={activeTab} onTabChange={setActiveTab}>
      {activeTab === 'text' && <TextTranslator />}
      {activeTab === 'camera' && <CameraTranslator />}
      {activeTab === 'audio' && <AudioTranslator />}
      {activeTab === 'history' && <History />}
      {activeTab === 'languages' && <LanguageManager />}
    </MainLayout>
  )
}

export default App
