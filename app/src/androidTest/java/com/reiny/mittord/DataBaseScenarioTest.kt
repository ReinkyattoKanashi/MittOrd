package com.reiny.mittord

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reiny.mittord.database.DictionaryDatabase
import com.reiny.mittord.database.dao.SemanticObjectDao
import com.reiny.mittord.database.entity.TranslationEntity
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataBaseScenarioTest {

    private lateinit var db: DictionaryDatabase
    private lateinit var dao: SemanticObjectDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, DictionaryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun fullCrudScenario() = runBlocking {
        dao.insertObjectWithTranslations(
            baseWord = "cat",
            comment = null,
            translations = listOf("en" to "cat")
        )

        var objects = dao.getAllObjects()
        assertEquals(1, objects.size)
        val firstId = objects.single { it.baseWord == "cat" }.id

        var firstTranslations = dao.getTranslationsForObject(firstId)
        assertEquals(1, firstTranslations.size)
        assertEquals("en", firstTranslations[0].languageCode)
        assertEquals("cat", firstTranslations[0].text)

        dao.insertTranslation(
            TranslationEntity(
                objectId = firstId,
                languageCode = "no",
                text = "katt"
            )
        )
        firstTranslations = dao.getTranslationsForObject(firstId)
        assertEquals(2, firstTranslations.size)
        assertTrue(firstTranslations.any { it.languageCode == "no" && it.text == "katt" })

        dao.insertObjectWithTranslations(
            baseWord = "dog",
            comment = null,
            translations = listOf("en" to "dog")
        )

        objects = dao.getAllObjects()
        assertEquals(2, objects.size)
        val secondId = objects.single { it.baseWord == "dog" }.id

        val firstObject = objects.single { it.id == firstId }
        dao.updateObject(firstObject.copy(comment = "Домашнее животное"))
        var updatedFirst = dao.getAllObjects().single { it.id == firstId }
        assertEquals("Домашнее животное", updatedFirst.comment)

        firstTranslations = dao.getTranslationsForObject(firstId)
        val enTranslationFirst = firstTranslations.single { it.languageCode == "en" }
        dao.updateTranslationText(enTranslationFirst.id, "kitty")

        firstTranslations = dao.getTranslationsForObject(firstId)
        val updatedEnFirst = firstTranslations.single { it.languageCode == "en" }
        assertEquals("kitty", updatedEnFirst.text)

        dao.insertTranslation(
            TranslationEntity(
                objectId = secondId,
                languageCode = "no",
                text = "hund"
            )
        )
        var secondTranslations = dao.getTranslationsForObject(secondId)
        assertEquals(2, secondTranslations.size)
        assertTrue(secondTranslations.any { it.languageCode == "no" && it.text == "hund" })

        val enSecond = secondTranslations.single { it.languageCode == "en" }
        dao.deleteTranslationAndMaybeObject(enSecond)

        secondTranslations = dao.getTranslationsForObject(secondId)
        assertEquals(1, secondTranslations.size)
        assertEquals("no", secondTranslations[0].languageCode)
        assertEquals("hund", secondTranslations[0].text)

        val lastSecond = secondTranslations[0]
        dao.deleteTranslationAndMaybeObject(lastSecond)

        objects = dao.getAllObjects()
        assertNull(objects.find { it.id == secondId })

        secondTranslations = dao.getTranslationsForObject(secondId)
        assertTrue(secondTranslations.isEmpty())

        objects = dao.getAllObjects()
        assertEquals(1, objects.size)
        updatedFirst = objects.single()
        assertEquals(firstId, updatedFirst.id)
        assertEquals("cat", updatedFirst.baseWord)
        assertEquals("Домашнее животное", updatedFirst.comment)

        firstTranslations = dao.getTranslationsForObject(firstId)
        assertEquals(2, firstTranslations.size)

        val langsFirst = firstTranslations.map { it.languageCode }.sorted()
        assertEquals(listOf("en", "no"), langsFirst)
        assertEquals("kitty", firstTranslations.single { it.languageCode == "en" }.text)
        assertEquals("katt", firstTranslations.single { it.languageCode == "no" }.text)
    }
}
