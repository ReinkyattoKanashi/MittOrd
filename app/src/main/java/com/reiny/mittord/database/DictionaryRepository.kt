package com.reiny.mittord.database

import com.reiny.mittord.database.dao.SemanticObjectDao
import com.reiny.mittord.database.entity.SemanticObjectEntity
import com.reiny.mittord.database.entity.TranslationEntity
import javax.inject.Inject

interface DictionaryRepository {

    suspend fun list(): List<SemanticObjectEntity>
    suspend fun getTranslations(objectId: Long): List<TranslationEntity>

    class Base @Inject constructor(
        private val dao: SemanticObjectDao
    ) : DictionaryRepository {

        override suspend fun list(): List<SemanticObjectEntity> {
            return dao.getAllObjects()
        }

        override suspend fun getTranslations(objectId: Long): List<TranslationEntity> {
            return dao.getTranslationsForObject(objectId)
        }
    }
}