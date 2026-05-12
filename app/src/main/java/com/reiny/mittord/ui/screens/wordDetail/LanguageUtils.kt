package com.reiny.mittord.ui.screens.wordDetail

val BCP47_TO_LANG_NAME = mapOf(
    "en" to "English", "ru" to "Russian", "no" to "Norwegian",
    "es" to "Spanish", "fr" to "French", "de" to "German",
    "it" to "Italian", "pt" to "Portuguese", "zh" to "Chinese",
    "ja" to "Japanese", "ko" to "Korean", "ar" to "Arabic",
    "tr" to "Turkish", "pl" to "Polish", "nl" to "Dutch",
    "sv" to "Swedish", "da" to "Danish", "fi" to "Finnish",
    "uk" to "Ukrainian", "cs" to "Czech", "ro" to "Romanian",
    "hu" to "Hungarian", "el" to "Greek", "he" to "Hebrew",
    "fa" to "Persian", "hi" to "Hindi", "bn" to "Bengali",
    "id" to "Indonesian", "vi" to "Vietnamese", "th" to "Thai"
)

val LANG_NAME_TO_BCP47 = BCP47_TO_LANG_NAME.entries.associate { (k, v) -> v to k }

// Language code → ISO 3166-1 alpha-2 country code
private val BCP47_TO_COUNTRY = mapOf(
    "af" to "za", "sq" to "al", "am" to "et", "ar" to "sa",
    "hy" to "am", "az" to "az", "eu" to "es", "be" to "by",
    "bn" to "bd", "bs" to "ba", "bg" to "bg", "ca" to "es",
    "ceb" to "ph", "zh" to "cn", "zh-tw" to "tw", "co" to "fr",
    "hr" to "hr", "cs" to "cz", "da" to "dk", "nl" to "nl",
    "en" to "us", "et" to "ee", "fi" to "fi", "fr" to "fr",
    "fy" to "nl", "gl" to "es", "ka" to "ge", "de" to "de",
    "el" to "gr", "gu" to "in", "ht" to "ht", "ha" to "ng",
    "haw" to "us", "he" to "il", "iw" to "il", "hi" to "in",
    "hmn" to "cn", "hu" to "hu", "is" to "is", "ig" to "ng",
    "id" to "id", "ga" to "ie", "it" to "it", "ja" to "jp",
    "jv" to "id", "kn" to "in", "kk" to "kz", "km" to "kh",
    "rw" to "rw", "ko" to "kr", "ku" to "iq", "ky" to "kg",
    "lo" to "la", "lv" to "lv", "lt" to "lt", "lb" to "lu",
    "mk" to "mk", "mg" to "mg", "ms" to "my", "ml" to "in",
    "mt" to "mt", "mi" to "nz", "mr" to "in", "mn" to "mn",
    "my" to "mm", "ne" to "np", "no" to "no", "nb" to "no",
    "nn" to "no", "ny" to "mw", "or" to "in", "ps" to "af",
    "fa" to "ir", "pl" to "pl", "pt" to "pt", "pa" to "in",
    "ro" to "ro", "ru" to "ru", "sm" to "ws", "gd" to "gb",
    "sr" to "rs", "st" to "ls", "sn" to "zw", "sd" to "pk",
    "si" to "lk", "sk" to "sk", "sl" to "si", "so" to "so",
    "es" to "es", "su" to "id", "sw" to "tz", "sv" to "se",
    "tl" to "ph", "tg" to "tj", "ta" to "in", "tt" to "ru",
    "te" to "in", "th" to "th", "tr" to "tr", "tk" to "tm",
    "uk" to "ua", "ur" to "pk", "ug" to "cn", "uz" to "uz",
    "vi" to "vn", "cy" to "gb", "xh" to "za", "yi" to "il",
    "yo" to "ng", "zu" to "za"
)

private fun countryCodeToFlag(cc: String): String {
    val a = cc[0].uppercaseChar().code - 'A'.code + 0x1F1E6
    val b = cc[1].uppercaseChar().code - 'A'.code + 0x1F1E6
    return String(Character.toChars(a)) + String(Character.toChars(b))
}

fun flagForCode(code: String?): String? {
    if (code == null) return null
    val lower = code.lowercase()
    val countryCode = BCP47_TO_COUNTRY[lower] ?: BCP47_TO_COUNTRY[normalizeCode(lower)] ?: return null
    return countryCodeToFlag(countryCode)
}

fun langNameForCode(code: String?): String? =
    BCP47_TO_LANG_NAME[code]

private val CODE_OVERRIDES = mapOf(
    "nb" to "no",
    "nn" to "no",
)

internal fun normalizeCode(raw: String): String =
    CODE_OVERRIDES[raw] ?: if ('-' in raw) raw.substringBefore('-') else raw
