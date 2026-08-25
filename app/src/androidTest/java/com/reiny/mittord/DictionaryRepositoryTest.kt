package com.reiny.mittord

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.reiny.mittord.database.DictionaryDatabase
import com.reiny.mittord.database.DictionaryRepository
import com.reiny.mittord.database.DictionaryRepositoryImpl
import com.reiny.mittord.database.TranslationData
import com.reiny.mittord.database.WordUpdate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Exercises the repository against a real in-memory Room database - the same path
 * the app takes, rather than the DAO in isolation.
 */
class DictionaryRepositoryTest {

    private lateinit var db: DictionaryDatabase
    private lateinit var repository: DictionaryRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, DictionaryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DictionaryRepositoryImpl(db.dao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun addWordStoresTheWordWithItsTranslation() = runBlocking {
        val id = repository.addWord("katt", "cat", "en")

        val stored = repository.getWordWithTranslations(id)

        assertEquals("katt", stored?.semanticObject?.baseWord)
        assertEquals(1, stored?.translations?.size)
        assertEquals("cat", stored?.translations?.first()?.text)
        assertEquals("en", stored?.translations?.first()?.languageCode)
    }

    @Test
    fun observeAllSeesWordsAsTheyAreAdded() = runBlocking {
        repository.addWord("katt", "cat", "en")
        repository.addWord("hund", "dog", "en")

        val words = repository.observeAll().first()

        assertEquals(2, words.size)
        assertEquals(setOf("katt", "hund"), words.map { it.semanticObject.baseWord }.toSet())
    }

    @Test
    fun updateLanguageCodeSetsTheLanguageOfTheWord() = runBlocking {
        val id = repository.addWord("katt", "cat", "en")

        repository.updateLanguageCode(id, "no")

        assertEquals("no", repository.getWordWithTranslations(id)?.semanticObject?.wordLanguageCode)
    }

    @Test
    fun updateWordFullReplacesEveryTranslation() = runBlocking {
        val id = repository.addWord("katt", "cat", "en")

        repository.updateWordFull(
            id,
            WordUpdate(
                baseWord = "katt",
                translations = listOf(
                    TranslationData("кот", "ru"),
                    TranslationData("Katze", "de")
                ),
                comment = "an animal",
                imagePath = null,
                wordLanguageCode = "no"
            )
        )

        val stored = repository.getWordWithTranslations(id)
        assertEquals(2, stored?.translations?.size)
        assertEquals(
            setOf("ru", "de"),
            stored?.translations?.map { it.languageCode }?.toSet()
        )
        // the English one was not in the update, so it is gone
        assertEquals("an animal", stored?.semanticObject?.comment)
    }

    @Test
    fun updateWordFullDropsBlankTranslations() = runBlocking {
        val id = repository.addWord("katt", "cat", "en")

        repository.updateWordFull(
            id,
            WordUpdate(
                baseWord = "katt",
                translations = listOf(TranslationData("кот", "ru"), TranslationData("  ", "de")),
                comment = null,
                imagePath = null,
                wordLanguageCode = null
            )
        )

        val stored = repository.getWordWithTranslations(id)
        assertEquals(1, stored?.translations?.size)
        assertEquals("кот", stored?.translations?.first()?.text)
    }

    @Test
    fun deletingAWordTakesItsTranslationsWithIt() = runBlocking {
        val id = repository.addWord("katt", "cat", "en")

        repository.deleteWord(id)

        assertNull(repository.getWordWithTranslations(id))
        // the CASCADE foreign key is what removes the translation rows
        assertTrue(db.dao().observeAllWithTranslations().first().isEmpty())
    }

    @Test
    fun deleteAllWordsEmptiesTheDictionary() = runBlocking {
        repository.addWord("katt", "cat", "en")
        repository.addWord("hund", "dog", "en")

        repository.deleteAllWords()

        assertTrue(repository.observeAll().first().isEmpty())
    }
}
