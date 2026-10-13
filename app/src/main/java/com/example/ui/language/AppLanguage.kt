package com.example.ui.language

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flag: String,
    val isRtl: Boolean
) {
    AR(
        code = "ar",
        displayName = "Arabic",
        nativeName = "العربية",
        flag = "🇸🇦",
        isRtl = true
    ),
    EN(
        code = "en",
        displayName = "English",
        nativeName = "English",
        flag = "🇬🇧",
        isRtl = false
    ),
    FR(
        code = "fr",
        displayName = "French",
        nativeName = "Français",
        flag = "🇫🇷",
        isRtl = false
    );

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: AR
        }
    }
}
