package com.example.data.model

import java.util.Locale

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flag: String,
    val locale: Locale
) {
    ENGLISH("en", "English", "English", "🇺🇸", Locale.US),
    SPANISH("es", "Spanish", "Español", "🇪🇸", Locale.forLanguageTag("es-ES")),
    FRENCH("fr", "French", "Français", "🇫🇷", Locale.FRANCE),
    GERMAN("de", "German", "Deutsch", "🇩🇪", Locale.GERMANY),
    JAPANESE("ja", "Japanese", "日本語", "🇯🇵", Locale.JAPAN),
    CHINESE("zh", "Chinese", "中文", "🇨🇳", Locale.SIMPLIFIED_CHINESE),
    PORTUGUESE("pt", "Portuguese", "Português", "🇧🇷", Locale.forLanguageTag("pt-BR")),
    ITALIAN("it", "Italian", "Italiano", "🇮🇹", Locale.ITALY),
    ARABIC("ar", "Arabic", "العربية", "🇸🇦", Locale.forLanguageTag("ar-SA")),
    HINDI("hi", "Hindi", "हिन्दी", "🇮🇳", Locale.forLanguageTag("hi-IN")),
    KOREAN("ko", "Korean", "한국어", "🇰🇷", Locale.KOREA);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
