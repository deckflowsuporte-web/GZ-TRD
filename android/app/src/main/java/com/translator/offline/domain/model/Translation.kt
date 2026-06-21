package com.translator.offline.domain.model

data class Translation(
    val id: String,
    val sourceText: String,
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val timestamp: Long
)

enum class Language(val code: String, val name: String) {
    PORTUGUESE("pt", "Português"),
    ENGLISH("en", "English"),
    SPANISH("es", "Español"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
    ITALIAN("it", "Italiano"),
    JAPANESE("ja", "日本語"),
    CHINESE("zh", "中文"),
    RUSSIAN("ru", "Русский"),
    ARABIC("ar", "العربية");

    companion object {
        fun fromCode(code: String): Language {
            return entries.find { it.code == code } ?: PORTUGUESE
        }
    }
}
