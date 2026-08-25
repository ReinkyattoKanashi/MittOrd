package com.reiny.mittord.database

import com.reiny.mittord.database.dao.SemanticObjectDao
import com.reiny.mittord.database.entity.SemanticObjectWithTranslations
import com.reiny.mittord.database.entity.TranslationEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class DictionaryRepositoryImpl @Inject constructor(
    private val dao: SemanticObjectDao
) : DictionaryRepository {

    override fun observeAll(): Flow<List<SemanticObjectWithTranslations>> =
        dao.observeAllWithTranslations()

    override suspend fun getWordWithTranslations(id: Long): SemanticObjectWithTranslations? =
        dao.getObjectWithTranslations(id)

    override suspend fun addWord(
        baseWord: String,
        translation: String,
        translationLanguageCode: String?
    ): Long = dao.insertObjectWithTranslations(
        baseWord = baseWord,
        comment = null,
        translations = listOf((translationLanguageCode ?: "") to translation)
    )

    override suspend fun updateLanguageCode(id: Long, code: String) =
        dao.updateLanguageCode(id, code)

    override suspend fun updateWordFull(id: Long, update: WordUpdate) {
        val existing = dao.getObjectWithTranslations(id) ?: return
        dao.updateObject(
            existing.semanticObject.copy(
                baseWord = update.baseWord,
                comment = update.comment,
                imagePath = update.imagePath,
                wordLanguageCode = update.wordLanguageCode
            )
        )
        dao.deleteAllTranslationsForObject(id)
        update.translations.filter { it.text.isNotBlank() }.forEach { t ->
            dao.insertTranslation(
                TranslationEntity(
                    objectId = id,
                    languageCode = t.languageCode ?: "",
                    text = t.text
                )
            )
        }
    }

    override suspend fun deleteWord(id: Long) {
        dao.deleteObjectById(id)
    }

    override suspend fun deleteAllWords() {
        dao.deleteAllObjects()
    }
}
