package com.reiny.mittord

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.reiny.mittord.database.DictionaryDatabase
import com.reiny.mittord.database.dao.SemanticObjectDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DatabaseTest {

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
    fun insert_Retrieve() = runBlocking {
        dao.insertObjectWithTranslations(
            baseWord = "cat",
            comment = "животное",
            translations = listOf(
                "en" to "cat",
                "no" to "katt",
                "uk" to "кіт"
            )
        )

        val allObjects = dao.getAllObjects()
        Assert.assertEquals(1, allObjects.size)
        Assert.assertEquals("cat", allObjects.first().baseWord)
        Assert.assertEquals("животное", allObjects.first().comment)

        val objectId = allObjects.first().id
        val translations = dao.getTranslationsForObject(objectId)
        Assert.assertEquals(3, translations.size)

        val langs = translations.map { it.languageCode }.sorted()
        Assert.assertEquals(listOf("en", "no", "uk"), langs)
    }

    @Test
    fun update_delete_removeObject() = runBlocking {
        dao.insertObjectWithTranslations(
            baseWord = "dog",
            comment = "животное",
            translations = listOf("en" to "dog")
        )
        val obj = dao.getAllObjects().first()
        val translation = dao.getTranslationsForObject(obj.id).first()

        dao.updateFavorite(obj.id, true)
        val updated = dao.getAllObjects().first()
        Assert.assertTrue(updated.isFavorite)

        dao.deleteTranslationAndMaybeObject(translation)

        val remainingObjects = dao.getAllObjects()
        val remainingTranslations = dao.getTranslationsForObject(obj.id)
        Assert.assertTrue(remainingObjects.isEmpty())
        Assert.assertTrue(remainingTranslations.isEmpty())
    }

    @Test
    fun getTranslationsByLanguage() = runBlocking {
        dao.insertObjectWithTranslations(
            baseWord = "tree",
            comment = null,
            translations = listOf(
                "en" to "tree",
                "no" to "tre"
            )
        )

        val englishTranslations = dao.getTranslationsByLanguage("en")
        Assert.assertEquals(1, englishTranslations.size)
        Assert.assertEquals("tree", englishTranslations.first().text)
    }
}