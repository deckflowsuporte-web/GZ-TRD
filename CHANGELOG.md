# 📝 Changelog - GZ-TRD (Tradutor Offline)

Todas as alterações notáveis deste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [1.0.0] - 2024-XX-XX

### ✨ Adicionado

#### Android App
- **Estrutura Clean Architecture**
  - Camada de domínio (models, repositories, use cases)
  - Camada de dados (Room database, DAOs, entities)
  - Camada de apresentação (Compose UI, ViewModels)
  - Injeção de dependências com Hilt

- **Funcionalidades Core**
  - Tela principal de tradução
  - Seleção de idioma de origem/destino (10 idiomas)
  - Troca rápida de idiomas
  - Histórico de traduções
  - Tema claro/escuro automático

- **Tecnologias**
  - Kotlin
  - Jetpack Compose
  - Material Design 3
  - Room Database
  - Hilt
  - Coroutines + Flow
  - MVVM

#### Web App (PWA)
- **Interface**
  - Layout responsivo mobile-first
  - Navegação por tabs (Texto, Câmera, Áudio, Histórico, Idiomas)
  - Componentes reutilizáveis
  - Tailwind CSS

- **Funcionalidades**
  - Tradução de texto
  - Câmera para OCR (simulação)
  - Áudio para texto (Web Speech API)
  - Gerenciamento de idiomas offline
  - Histórico persistente (localStorage)

- **PWA**
  - Service Worker
  - Manifest para instalação
  - Funciona offline

#### DevOps
- GitHub Actions CI/CD
- Workflow de build Android (Debug + Release)
- Workflow de build e deploy Web
- Artefatos para download

#### Documentação
- README.md
- CONTRIBUTING.md
- INSTALL.md
- ROADMAP.md
- ESTRUTURA_COMPLETA.md

### 📦 Dependencies

#### Android
```
- androidx.core:core-ktx:1.12.0
- androidx.lifecycle:lifecycle-runtime-ktx:2.6.2
- androidx.activity:activity-compose:1.8.1
- androidx.compose:compose-bom:2023.10.01
- androidx.navigation:navigation-compose:2.7.5
- androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2
- androidx.room:room-runtime:2.6.1
- com.google.dagger:hilt-android:2.48.1
- org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3
```

#### Web
```
- react: ^18.2.0
- react-dom: ^18.2.0
- zustand: ^4.4.7
- @vitejs/plugin-react: ^4.2.1
- tailwindcss: ^3.3.6
- typescript: ^5.2.2
- vite: ^5.0.8
```

---

## [0.0.1] - 2024-06-21

### 🚀 Inicial

- Repositório criado
- Estrutura inicial
- Configurações básicas de build
- CONTRIBUTING.md
- README.md básico

---

## 📋 Formato de Commits

Este projeto segue o formato Conventional Commits:

```
feat: nova funcionalidade
fix: correção de bug
docs: documentação
style: formatação (sem mudança de código)
refactor: refatoração
test: testes
chore: manutenção
```

Exemplos:
- `feat: adicionar tradução por câmera`
- `fix: corrigir crash ao trocar idioma`
- `docs: atualizar README`

---

## 🔄 Processo de Release

1. Atualizar CHANGELOG.md
2. Criar tag git (v1.0.0)
3. Push que dispara GitHub Actions
4. APK gerado e disponibilizado
5. Release no GitHub criado

---

*Gerado automaticamente*
