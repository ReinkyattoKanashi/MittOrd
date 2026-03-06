package com.reiny.mittord.database

import com.reiny.mittord.database.dao.SemanticObjectDao
import com.reiny.mittord.database.entity.SemanticObjectEntity
import com.reiny.mittord.database.entity.SemanticObjectWithTranslations
import com.reiny.mittord.database.entity.TranslationEntity
import javax.inject.Inject

data class TranslationData(val text: String, val languageCode: String?)

interface DictionaryRepository {

    suspend fun list(): List<SemanticObjectEntity>
    suspend fun listWithTranslations(): List<SemanticObjectWithTranslations>
    suspend fun getWordWithTranslations(id: Long): SemanticObjectWithTranslations?
    suspend fun getTranslations(objectId: Long): List<TranslationEntity>
    suspend fun addWord(baseWord: String, translation: String, translationLanguageCode: String? = null): Long
    suspend fun updateLanguageCode(id: Long, code: String)
    suspend fun updateWordFull(
        id: Long,
        baseWord: String,
        translations: List<TranslationData>,
        comment: String?,
        imagePath: String?,
        wordLanguageCode: String?
    )
    suspend fun deleteWord(id: Long)

    class Base @Inject constructor(
        private val dao: SemanticObjectDao
    ) : DictionaryRepository {

        override suspend fun list(): List<SemanticObjectEntity> =
            dao.getAllObjects()

        override suspend fun listWithTranslations(): List<SemanticObjectWithTranslations> =
            dao.getAllObjectsWithTranslations()

        override suspend fun getWordWithTranslations(id: Long): SemanticObjectWithTranslations? =
            dao.getObjectWithTranslations(id)

        override suspend fun getTranslations(objectId: Long): List<TranslationEntity> =
            dao.getTranslationsForObject(objectId)

        override suspend fun addWord(baseWord: String, translation: String, translationLanguageCode: String?): Long =
            dao.insertObjectWithTranslations(
                baseWord = baseWord,
                comment = null,
                translations = listOf((translationLanguageCode ?: "") to translation)
            )

        override suspend fun updateLanguageCode(id: Long, code: String) =
            dao.updateLanguageCode(id, code)

        override suspend fun updateWordFull(
            id: Long,
            baseWord: String,
            translations: List<TranslationData>,
            comment: String?,
            imagePath: String?,
            wordLanguageCode: String?
        ) {
            val existing = dao.getObjectWithTranslations(id) ?: return
            dao.updateObject(
                existing.semanticObject.copy(
                    baseWord = baseWord,
                    comment = comment,
                    imagePath = imagePath,
                    wordLanguageCode = wordLanguageCode
                )
            )
            dao.deleteAllTranslationsForObject(id)
            translations.filter { it.text.isNotBlank() }.forEach { t ->
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
    }
}
