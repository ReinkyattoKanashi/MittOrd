package com.reiny.mittord

import com.reiny.mittord.database.DictionaryRepository
import com.reiny.mittord.database.TranslationData
import com.reiny.mittord.database.WordUpdate
import com.reiny.mittord.database.entity.SemanticObjectEntity
import com.reiny.mittord.database.entity.SemanticObjectWithTranslations
import com.reiny.mittord.database.entity.TranslationEntity
import com.reiny.mittord.domain.usecase.SeedDatabaseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SeedDatabaseUseCaseTest {

    private var addWordCallCount = 0
    private var updateLanguageCodeCallCount = 0
    private var lastId = 0L

    private val fakeRepository = object : DictionaryRepository {
        override fun observeAll(): Flow<List<SemanticObjectWithTranslations>> = emptyFlow()
        override suspend fun list(): List<SemanticObjectEntity> = emptyList()
        override suspend fun getWordWithTranslations(id: Long): SemanticObjectWithTranslations? = null
        override suspend fun getTranslations(objectId: Long): List<TranslationEntity> = emptyList()
        override suspend fun addWord(baseWord: String, translation: String, translationLanguageCode: String?): Long {
            addWordCallCount++
            return ++lastId
        }
        override suspend fun updateLanguageCode(id: Long, code: String) { updateLanguageCodeCallCount++ }
        override suspend fun updateWordFull(id: Long, update: WordUpdate) {}
        override suspend fun deleteWord(id: Long) {}
    }

    @Before
    fun setUp() = runBlocking {
        SeedDatabaseUseCase(fakeRepository)()
    }

    @Test
    fun seedInsertsExpectedWordCount() {
        assertEquals(20, addWordCallCount)
    }

    @Test
    fun seedSetsLanguageCodeForEachWord() {
        assertEquals(20, updateLanguageCodeCallCount)
    }

    @Test
    fun seedAssignsUniqueIds() {
        assertEquals(20L, lastId)
    }
}
