package com.reiny.mittord.database

import com.reiny.mittord.database.entity.SemanticObjectWithTranslations
import kotlinx.coroutines.flow.Flow

data class TranslationData(val text: String, val languageCode: String?)

data class WordUpdate(
    val baseWord: String,
    val translations: List<TranslationData>,
    val comment: String?,
    val imagePath: String?,
    val wordLanguageCode: String?
)

interface DictionaryRepository {
    fun observeAll(): Flow<List<SemanticObjectWithTranslations>>
    suspend fun getWordWithTranslations(id: Long): SemanticObjectWithTranslations?
    suspend fun addWord(baseWord: String, translation: String, translationLanguageCode: String? = null): Long
    suspend fun updateLanguageCode(id: Long, code: String)
    suspend fun updateWordFull(id: Long, update: WordUpdate)
    suspend fun deleteWord(id: Long)
    suspend fun deleteAllWords()
}
