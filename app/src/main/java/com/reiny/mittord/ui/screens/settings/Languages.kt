package com.reiny.mittord.ui.screens.settings

data class Language(val name: String, val flag: String, val code: String = "")

val LANGUAGES = listOf(
    Language("English",    "🇺🇸", "en"),
    Language("Russian",    "🇷🇺", "ru"),
    Language("Norwegian",  "🇳🇴", "no"),
    Language("Spanish",    "🇪🇸", "es"),
    Language("French",     "🇫🇷", "fr"),
    Language("German",     "🇩🇪", "de"),
    Language("Italian",    "🇮🇹", "it"),
    Language("Portuguese", "🇵🇹", "pt"),
    Language("Chinese",    "🇨🇳", "zh"),
    Language("Japanese",   "🇯🇵", "ja"),
    Language("Korean",     "🇰🇷", "ko"),
    Language("Arabic",     "🇸🇦", "ar"),
    Language("Turkish",    "🇹🇷", "tr"),
    Language("Polish",     "🇵🇱", "pl"),
    Language("Dutch",      "🇳🇱", "nl"),
    Language("Swedish",    "🇸🇪", "sv"),
    Language("Danish",     "🇩🇰", "da"),
    Language("Finnish",    "🇫🇮", "fi"),
    Language("Ukrainian",  "🇺🇦", "uk"),
    Language("Czech",      "🇨🇿", "cs"),
    Language("Romanian",   "🇷🇴", "ro"),
    Language("Hungarian",  "🇭🇺", "hu"),
    Language("Greek",      "🇬🇷", "el"),
    Language("Hebrew",     "🇮🇱", "he"),
    Language("Persian",    "🇮🇷", "fa"),
    Language("Hindi",      "🇮🇳", "hi"),
    Language("Bengali",    "🇧🇩", "bn"),
    Language("Indonesian", "🇮🇩", "id"),
    Language("Vietnamese", "🇻🇳", "vi"),
    Language("Thai",       "🇹🇭", "th")
)
