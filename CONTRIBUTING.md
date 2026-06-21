# 🤝 Contribuindo para o Translator Offline

Obrigado por seu interesse em contribuir! Este documento descreve o processo de contribuição.

## Código de Conduta

Por favor, seja respeitoso com todos os contribuidores e usuários.

## Como Contribuir

### 1. Fork o Repositório
```bash
git clone https://github.com/SEU_USERNAME/GZ-TRD.git
cd GZ-TRD
```

### 2. Crie uma Branch
```bash
git checkout -b feature/minha-feature
```

### 3. Faça Suas Mudanças
- Siga o style guide do projeto
- Escreva commits descritivos
- Teste suas mudanças

### 4. Push para a Branch
```bash
git push origin feature/minha-feature
```

### 5. Abra um Pull Request
- Descreva as mudanças claramente
- Referencie issues relacionadas
- Aguarde review

## Guidelines

### Commits
- Use mensagens descritivas em português
- Exemplo: `feat: Adiciona reconhecimento de fala`
- Tipos: feat, fix, docs, style, refactor, test, chore

### Código

**Android (Kotlin):**
- Use Kotlin style guide oficial
- Componha com Jetpack Compose
- Adicione tipos explícitos

**Web (React/TypeScript):**
- Use ESLint e Prettier
- Componentes funcionais com hooks
- TypeScript strict mode

### Testes
- Escreva testes para features novas
- Mantenha cobertura > 80%
- Execute testes localmente antes de PR

## Processo de Review

1. Pelo menos 1 review aprovado
2. Todos os checks passando
3. Sem conflitos com main/develop

## Reporte de Bugs

Use o template de bug report em `.github/ISSUE_TEMPLATE/`

## Sugestões de Features

Abra uma discussão ou issue com o template de feature request.
