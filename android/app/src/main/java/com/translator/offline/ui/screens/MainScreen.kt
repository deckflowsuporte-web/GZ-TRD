package com.translator.offline.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwapHoriz
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tradutor Offline") },
                actions = {
                    IconButton(onClick = { showHistory = !showHistory }) {
                        Text(if (showHistory) "Traduzir" else "Histórico")
                    }
                }
            )
        }
    ) { padding ->
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
                } else {
                    Text("Traduzir")
                }
            }

            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpar")
            }
        }

        // Erro
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
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
