# 📥 Guia de Instalação - GZ-TRD (Tradutor Offline)

Este guia fornece instruções detalhadas para configurar e executar o projeto em seu ambiente local.

## 📋 Pré-requisitos

### Para Android
- **Java Development Kit (JDK) 17** ou superior
- **Android Studio** Hedgehog (2023.1.1) ou superior
- **Android SDK** (API level 24 ou superior)
- **Gradle** 8.1 ou superior (incluído no wrapper)

### Para Web
- **Node.js** 18.x ou superior
- **npm** 9.x ou superior (ou yarn/pnpm)

## 🚀 Instalação

### 1. Clonar o Repositório

```bash
git clone https://github.com/deckflowsuporte-web/GZ-TRD.git
cd GZ-TRD
```

### 2. Configuração Android

#### Usando Android Studio

1. Abra o Android Studio
2. Selecione "Open an existing project"
3. Navegue até a pasta `android/` do projeto
4. Aguarde a sincronização do Gradle
5. Conecte um dispositivo ou crie um emulador
6. Clique em "Run" (▶️) ou use `Ctrl+R`

#### Usando Linha de Comando

```bash
cd android

# Conceder permissão de execução ao Gradle wrapper
chmod +x gradlew

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease

# Instalar no dispositivo conectado
./gradlew installDebug
```

O APK gerado estará em: `android/app/build/outputs/apk/debug/app-debug.apk`

### 3. Configuração Web

```bash
cd web

# Instalar dependências
npm install

# Iniciar servidor de desenvolvimento
npm run dev

# O app estará disponível em http://localhost:3000
```

#### Build para Produção

```bash
cd web

# Build otimizado
npm run build

# Preview do build
npm run preview
```

Os arquivos de produção estarão em: `web/dist/`

## 📱 Instalação do App Android

### Via USB Debugging

1. Habilite "USB Debugging" nas Opções do Desenvolvedor do Android
2. Conecte o dispositivo via USB
3. Execute `./gradlew installDebug` na pasta android

### Via APK (Release)

1. Copie o arquivo `app-release.apk` para o dispositivo
2. Habilite "Fontes desconhecidas" nas configurações
3. Toque no APK para instalar

## 🌐 Instalação da Web App (PWA)

### Localmente
```bash
cd web
npm run build
npx serve dist
```

### GitHub Pages
O deploy é automático via GitHub Actions quando há push na branch `main`.

### Netlify / Vercel
1. Conecte seu repositório GitHub
2. Configure o diretório de build como `web`
3. Comando de build: `npm run build`
4. Diretório de output: `web/dist`

## 🔧 Configuração de Variáveis de Ambiente

### Android
Crie o arquivo `android/local.properties`:
```properties
sdk.dir=/caminho/para/android/sdk
```

### Web
Crie o arquivo `web/.env`:
```env
VITE_API_URL=https://api.exemplo.com
VITE_ENABLE_ANALYTICS=true
```

## 🧪 Testes

### Android
```bash
cd android
./gradlew test          # Testes unitários
./gradlew connectedAndroidTest  # Testes instrumentados
```

### Web
```bash
cd web
npm test               # Testes unitários
npm run test:coverage  # Com cobertura
```

## 🐛 Solução de Problemas

### Android: "Gradle sync failed"
- Limpe o cache: `./gradlew clean`
- Invalidar caches do Android Studio: File > Invalidate Caches

### Android: "SDK location not found"
- Verifique o `local.properties` com o caminho correto do SDK

### Web: "Module not found"
- Delete `node_modules` e execute `npm install` novamente

### Web: Erro de porta em uso
- Use `npm run dev -- --port 3001` para outra porta

## 📚 Recursos Adicionais

- [Documentação do Android](https://developer.android.com/docs)
- [Documentação do React](https://react.dev)
- [Documentação do Jetpack Compose](https://developer.android.com/compose)
- [Guia de Contribuição](./CONTRIBUTING.md)

## 🤝 Suporte

Para problemas, abra uma issue em: https://github.com/deckflowsuporte-web/GZ-TRD/issues

---

**Boa instalação! 🚀**
