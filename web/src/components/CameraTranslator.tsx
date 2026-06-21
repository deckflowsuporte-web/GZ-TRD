import { useState, useRef } from 'react'
import useTranslatorStore, { translateWithAPI } from '../store/translatorStore'

export default function CameraTranslator() {
  const {
    sourceLanguage,
    targetLanguage,
    setSourceLanguage,
    setTargetLanguage,
    addToHistory
  } = useTranslatorStore()

  const [isCameraActive, setIsCameraActive] = useState(false)
  const [recognizedText, setRecognizedText] = useState('')
  const [translatedText, setTranslatedText] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const videoRef = useRef<HTMLVideoElement>(null)
  const canvasRef = useRef<HTMLCanvasElement>(null)

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

  const toggleCamera = async () => {
    if (isCameraActive) {
      if (videoRef.current?.srcObject) {
        const tracks = (videoRef.current.srcObject as MediaStream).getTracks()
        tracks.forEach(track => track.stop())
        videoRef.current.srcObject = null
      }
      setIsCameraActive(false)
    } else {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ 
          video: { facingMode: 'environment' } 
        })
        if (videoRef.current) {
          videoRef.current.srcObject = stream
          setIsCameraActive(true)
          setError(null)
        }
      } catch (err) {
        setError('Não foi possível acessar a câmera. Verifique as permissões.')
        console.error('Erro ao acessar câmera:', err)
      }
    }
  }

  const captureAndTranslate = async () => {
    if (!videoRef.current || !canvasRef.current) return

    setIsLoading(true)
    setError(null)
    try {
      // Capturar frame do vídeo
      const video = videoRef.current
      const canvas = canvasRef.current
      const context = canvas.getContext('2d')
      
      if (context) {
        canvas.width = video.videoWidth
        canvas.height = video.videoHeight
        context.drawImage(video, 0, 0)
        
        // Usar Tesseract.js para OCR (vou adicionar depois)
        // Por enquanto, simulamos com texto de exemplo
        const mockText = 'Hello world, how are you today?'
        setRecognizedText(mockText)
        
        // Traduzir com API real
        const result = await translateWithAPI(mockText, sourceLanguage, targetLanguage)
        setTranslatedText(result)

        addToHistory({
          id: Date.now().toString(),
          sourceText: mockText,
          translatedText: result,
          sourceLanguage,
          targetLanguage,
          timestamp: Date.now()
        })
      }
    } catch (err) {
      setError('Erro ao processar imagem')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-xl shadow-lg p-6">
        <h2 className="text-xl font-bold text-gray-800 mb-4">📷 Tradutor de Câmera</h2>

        {/* Seletor de idiomas */}
        <div className="flex items-center gap-4 mb-4">
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

        {/* Preview da câmera */}
        <div className="relative bg-black rounded-lg overflow-hidden aspect-video mb-4">
          <video
            ref={videoRef}
            autoPlay
            playsInline
            muted
            className={`w-full h-full object-cover ${isCameraActive ? 'block' : 'hidden'}`}
          />
          
          {!isCameraActive && (
            <div className="absolute inset-0 flex flex-col items-center justify-center text-gray-400">
              <span className="text-6xl mb-2">📷</span>
              <p>Câmera inativa</p>
            </div>
          )}

          {/* Overlay de processamento */}
          {isLoading && (
            <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center">
              <div className="text-white text-center">
                <div className="animate-spin text-4xl mb-2">⏳</div>
                <p>Processando...</p>
              </div>
            </div>
          )}
          
          {/* Canvas hidden para captura */}
          <canvas ref={canvasRef} className="hidden" />
        </div>

        {/* Botões */}
        <div className="flex gap-3">
          <button
            onClick={toggleCamera}
            className={`flex-1 py-3 rounded-lg font-semibold transition ${
              isCameraActive
                ? 'bg-red-500 hover:bg-red-600 text-white'
                : 'bg-indigo-600 hover:bg-indigo-700 text-white'
            }`}
          >
            {isCameraActive ? '⏹️ Parar Câmera' : '▶️ Iniciar Câmera'}
          </button>

          {isCameraActive && (
            <button
              onClick={captureAndTranslate}
              disabled={isLoading}
              className="flex-1 py-3 bg-green-600 hover:bg-green-700 text-white rounded-lg font-semibold transition disabled:opacity-50"
            >
              📸 Capturar e Traduzir
            </button>
          )}
        </div>

        {/* Erro */}
        {error && (
          <div className="mt-4 p-3 bg-red-100 text-red-700 rounded-lg">
            {error}
          </div>
        )}

        {/* Resultado */}
        {(recognizedText || translatedText) && (
          <div className="mt-6 space-y-4">
            <div className="p-4 bg-gray-50 rounded-lg">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Texto Reconhecido
              </label>
              <p className="text-gray-900">{recognizedText}</p>
            </div>

            <div className="p-4 bg-indigo-50 rounded-lg">
              <label className="block text-sm font-medium text-indigo-700 mb-1">
                Tradução
              </label>
              <p className="text-indigo-900 font-medium">{translatedText}</p>
            </div>
          </div>
        )}
      </div>

      {/* Instruções */}
      <div className="bg-blue-50 border border-blue-200 rounded-xl p-4">
        <h3 className="font-semibold text-blue-800 mb-2">💡 Como usar</h3>
        <ul className="text-sm text-blue-700 space-y-1">
          <li>• Clique em "Iniciar Câmera" para ativar a câmera</li>
          <li>• Aponte para o texto que deseja traduzir</li>
          <li>• Clique em "Capturar e Traduzir" para processar</li>
          <li>• O texto será reconhecido e traduzido automaticamente</li>
        </ul>
      </div>
    </div>
  )
}
