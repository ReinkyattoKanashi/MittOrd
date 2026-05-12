package com.reiny.mittord.util

object AppConstants {

    // SharedPreferences
    const val PREFS_NAME = "settings"
    const val PREF_AVATAR_PATH = "avatar_path"
    const val PREF_LEARNING_LANG = "learning_lang"
    const val PREF_NATIVE_LANG = "native_lang"
    const val PREF_RECENT_LANGS = "recent_langs"
    const val PREF_DARK_THEME = "dark_theme"

    // Default language values
    const val DEFAULT_LEARNING_LANG = "Norwegian"
    const val DEFAULT_NATIVE_LANG = "Russian"

    // File system
    const val AVATAR_FILE_JPG = "avatar.jpg"
    const val AVATAR_FILE_GIF = "avatar.gif"
    const val WORD_IMAGES_DIR = "word_images"
    const val WORD_IMAGE_EXT = ".jpg"

    // MIME types
    const val MIME_IMAGE_ALL = "image/*"
    const val MIME_IMAGE_GIF = "image/gif"

    // Image encoding
    const val JPEG_QUALITY = 90

    // UI timing (ms)
    const val BACK_PRESS_TIMEOUT_MS = 2000L
    const val FOCUS_REQUEST_DELAY_MS = 80L
    const val LANG_DETECT_DEBOUNCE_MS = 600L

    // Language detection
    const val LANG_DETECT_MIN_LENGTH = 2
    const val LANG_CODE_UNDETERMINED = "und"

    // Network
    const val TRANSLATE_BASE_URL = "https://translate.googleapis.com/"
    const val TRANSLATE_API_URL =
        "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=en&dt=t&q="
    const val TRANSLATE_USER_AGENT = "Mozilla/5.0"
    const val NETWORK_TIMEOUT_MS = 5000

    // UI defaults
    const val DEFAULT_FLAG_EMOJI = "🏳"
}
