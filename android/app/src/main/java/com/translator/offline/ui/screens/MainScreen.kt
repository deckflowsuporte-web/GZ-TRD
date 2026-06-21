package com.translator.offline.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.translator.offline.domain.model.Language
import com.translator.offline.domain.model.Translation
import com.translator.offline.ui.viewmodels.TranslatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TranslatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showHistory by remember { mutableStateOf(false) }
    var showModeSelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tradutor Offline") },
                actions = {
                    // Indicador do modo atual
                    TextButton(onClick = { showModeSelector = true }) {
                        Text(
                            text = when (uiState.translationMode) {
                                "light" -> "⚡ Leve"
                                "advanced" -> "🤖 Avançado"
                                else -> "🔄 Auto"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    IconButton(onClick = { showHistory = !showHistory }) {
                        Text(if (showHistory) "📝" else "📜")
                    }
                }
            )
        }
    ) { padding ->
        // Dialog para selecionar modo
        if (showModeSelector) {
            ModeSelectionDialog(
                currentMode = uiState.translationMode,
                onModeSelected = { mode ->
                    viewModel.setTranslationMode(mode)
                    showModeSelector = false
                },
                onDismiss = { showModeSelector = false }
            )
        }
        if (showHistory) {
            HistoryContent(
                history = uiState.history,
                onItemClick = { translation ->
                    viewModel.loadFromHistory(translation)
                    showHistory = false
                },
                modifier = Modifier.padding(padding)
            )
        } else {
            TranslatorContent(
                uiState = uiState,
                onSourceTextChange = viewModel::updateSourceText,
                onSourceLanguageChange = viewModel::updateSourceLanguage,
                onTargetLanguageChange = viewModel::updateTargetLanguage,
                onSwapLanguages = viewModel::swapLanguages,
                onTranslate = viewModel::translate,
                onClear = viewModel::clearText,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun TranslatorContent(
    uiState: com.translator.offline.ui.viewmodels.TranslatorUiState,
    onSourceTextChange: (String) -> Unit,
    onSourceLanguageChange: (Language) -> Unit,
    onTargetLanguageChange: (Language) -> Unit,
    onSwapLanguages: () -> Unit,
    onTranslate: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Seletor de idiomas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageDropdown(
                selectedLanguage = uiState.sourceLanguage,
                onLanguageSelected = onSourceLanguageChange,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onSwapLanguages) {
                Icon(Icons.Default.SwapHoriz, contentDescription = "Trocar idiomas")
            }

            LanguageDropdown(
                selectedLanguage = uiState.targetLanguage,
                onLanguageSelected = onTargetLanguageChange,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de texto original
        OutlinedTextField(
            value = uiState.sourceText,
            onValueChange = onSourceTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            label = { Text("Texto original") },
            placeholder = { Text("Digite o texto para traduzir...") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de texto traduzido
        OutlinedTextField(
            value = uiState.translatedText,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            label = { Text("Tradução") },
            readOnly = true,
            placeholder = { Text("Resultado aparecerá aqui...") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botões
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onTranslate,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading && uiState.sourceText.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    if (uiState.isModelDownloading) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Baixando modelo...")
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Traduzindo...")
                    }
                } else {
                    Text("🔄 Traduzir")
                }
            }

            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpar")
            }
        }

        // Info sobre download de modelo
        if (uiState.isModelDownloading) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Baixando modelo de tradução offline...",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Erro
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠️ $error",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedLanguage.name,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Language.entries.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.name) },
                    onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun HistoryContent(
    history: List<Translation>,
    onItemClick: (Translation) -> Unit,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nenhum histórico ainda",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history) { translation ->
                HistoryItem(
                    translation = translation,
                    onClick = { onItemClick(translation) }
                )
            }
        }
    }
}

@Composable
private fun HistoryItem(
    translation: Translation,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = translation.sourceText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = translation.translatedText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${Language.fromCode(translation.sourceLanguage).name} → ${Language.fromCode(translation.targetLanguage).name}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Dialog para selecionar o modo de tradução
 */
@Composable
private fun ModeSelectionDialog(
    currentMode: String,
    onModeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Modo de Tradução")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Escolha como deseja traduzir:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Modo Leve
                ModeOptionCard(
                    title = "⚡ Modo Leve",
                    description = "Dicionário local, rápido",
                    details = "5MB • 6 idiomas • 60-70% precisão",
                    isSelected = currentMode == "light",
                    onClick = { onModeSelected("light") }
                )
                
                // Modo Avançado
                ModeOptionCard(
                    title = "🤖 Modo Avançado",
                    description = "NLLB-200 (IA), máxima qualidade",
                    details = "150MB • 20 idiomas • 85-95% precisão",
                    isSelected = currentMode == "advanced",
                    onClick = { onModeSelected("advanced") }
                )
                
                // Modo Auto
                ModeOptionCard(
                    title = "🔄 Automático",
                    description = "Escolhe o melhor modo",
                    details = "Adaptativo • Baseado no dispositivo",
                    isSelected = currentMode == "auto",
                    onClick = { onModeSelected("auto") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
private fun ModeOptionCard(
    title: String,
    description: String,
    details: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = details,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
}
