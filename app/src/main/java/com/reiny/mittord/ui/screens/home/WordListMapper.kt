package com.reiny.mittord.ui.screens.home

import com.reiny.mittord.database.entity.SemanticObjectWithTranslations
import com.reiny.mittord.domain.model.Language
import com.reiny.mittord.domain.util.LANG_NAME_TO_BCP47
import com.reiny.mittord.domain.util.flagForCode
import com.reiny.mittord.domain.util.normalizeCode

/*
 * Turning a stored word into a list row. Kept out of the ViewModel as plain
 * functions: they hold no state, and the rule for picking a translation is product
 * behaviour worth testing on its own.
 */

/** Matches the query against the word itself and any of its translations. */
internal fun SemanticObjectWithTranslations.matches(query: String): Boolean =
    semanticObject.baseWord.contains(query, ignoreCase = true) ||
        translations.any { it.text.contains(query, ignoreCase = true) }

/**
 * BCP-47 code of the language named [name], or null when it is unknown. The list
 * loaded from the API already carries codes; the bundled fallback list is matched
 * by name.
 */
internal fun nativeLanguageCode(languages: List<Language>, name: String): String? {
    val code = languages.firstOrNull { it.name == name }?.code?.takeIf { it.isNotEmpty() }
        ?: LANG_NAME_TO_BCP47[name]
    return code?.let { normalizeCode(it) }
}

/**
 * Picks the translation shown in the list: the one in the user's native language,
 * falling back to the first stored one. The relation has no guaranteed order, so the
 * entries are sorted by id to keep the choice stable between openings.
 */
internal fun SemanticObjectWithTranslations.toListItem(nativeCode: String?): WordListItem {
    val ordered = translations.sortedBy { it.id }
    val chosen = nativeCode
        ?.let { code -> ordered.firstOrNull { normalizeCode(it.languageCode) == code } }
        ?: ordered.firstOrNull()
    return WordListItem(
        id = semanticObject.id,
        word = semanticObject.baseWord,
        wordFlag = flagForCode(semanticObject.wordLanguageCode),
        translation = chosen?.text?.takeIf { it.isNotBlank() },
        translationFlag = chosen?.let { flagForCode(it.languageCode) }
    )
}
