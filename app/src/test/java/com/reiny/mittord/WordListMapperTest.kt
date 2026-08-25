package com.reiny.mittord

import com.reiny.mittord.database.entity.SemanticObjectEntity
import com.reiny.mittord.database.entity.SemanticObjectWithTranslations
import com.reiny.mittord.database.entity.TranslationEntity
import com.reiny.mittord.domain.model.Language
import com.reiny.mittord.ui.screens.home.matches
import com.reiny.mittord.ui.screens.home.nativeLanguageCode
import com.reiny.mittord.ui.screens.home.toListItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Rules for the word list row:
 *  - show the translation in the user's native language;
 *  - if there is none, show the first stored one;
 *  - "first" means lowest id, because the database does not promise an order.
 */
class WordListMapperTest {

    /** Builds a word with translations, ids assigned in the order given. */
    private fun word(
        base: String,
        wordLanguage: String? = "no",
        vararg translations: Pair<String, String>
    ) = SemanticObjectWithTranslations(
        semanticObject = SemanticObjectEntity(
            id = 1,
            baseWord = base,
            wordLanguageCode = wordLanguage
        ),
        translations = translations.mapIndexed { index, (code, text) ->
            TranslationEntity(id = index + 1L, objectId = 1, languageCode = code, text = text)
        }
    )

    @Test
    fun `shows the translation in the native language, not the first one`() {
        val hund = word("hund", "no", "en" to "dog", "ru" to "собака", "de" to "Hund")

        val row = hund.toListItem(nativeCode = "ru")

        assertEquals("собака", row.translation)
        assertEquals("🇷🇺", row.translationFlag)
    }

    @Test
    fun `falls back to the first translation when the native language is missing`() {
        val hund = word("hund", "no", "en" to "dog", "de" to "Hund")

        val row = hund.toListItem(nativeCode = "ru")

        assertEquals("dog", row.translation)
    }

    @Test
    fun `first translation means lowest id, not list order`() {
        val shuffled = SemanticObjectWithTranslations(
            semanticObject = SemanticObjectEntity(id = 1, baseWord = "hund"),
            translations = listOf(
                TranslationEntity(id = 7, objectId = 1, languageCode = "de", text = "Hund"),
                TranslationEntity(id = 2, objectId = 1, languageCode = "en", text = "dog")
            )
        )

        val row = shuffled.toListItem(nativeCode = null)

        assertEquals("dog", row.translation)
    }

    @Test
    fun `a word without translations has no translation line`() {
        val row = word("hund").toListItem(nativeCode = "ru")

        assertNull(row.translation)
        assertNull(row.translationFlag)
    }

    @Test
    fun `regional codes still match the native language`() {
        // Detection may answer "nb" (Bokmal) where the setting says "no".
        val bok = word("hund", "no", "nb" to "hund-nb")

        val row = bok.toListItem(nativeCode = "no")

        assertEquals("hund-nb", row.translation)
    }

    @Test
    fun `unknown word language leaves the flag empty instead of guessing`() {
        val row = word("hund", wordLanguage = "zzz").toListItem(nativeCode = null)

        assertNull(row.wordFlag)
    }

    @Test
    fun `native language code is taken from the loaded list first`() {
        val languages = listOf(Language("Norwegian", "🇳🇴", "no"))

        assertEquals("no", nativeLanguageCode(languages, "Norwegian"))
    }

    @Test
    fun `native language code falls back to the bundled name mapping`() {
        assertEquals("ru", nativeLanguageCode(emptyList(), "Russian"))
        assertNull(nativeLanguageCode(emptyList(), "Klingon"))
    }

    @Test
    fun `search matches the word and its translations, ignoring case`() {
        val hund = word("hund", "no", "en" to "dog", "ru" to "собака")

        assertTrue(hund.matches("HUN"))
        assertTrue(hund.matches("Dog"))
        assertTrue(hund.matches("соба"))
        assertFalse(hund.matches("katt"))
    }
}
