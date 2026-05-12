package com.reiny.mittord.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.reiny.mittord.database.entity.SemanticObjectEntity
import com.reiny.mittord.database.entity.SemanticObjectWithTranslations
import com.reiny.mittord.database.entity.TranslationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SemanticObjectDao {

    // ---------- INSERT ----------

    @Insert
    suspend fun insertObject(obj: SemanticObjectEntity): Long

    @Insert
    suspend fun insertTranslation(translation: TranslationEntity)

    @Transaction
    suspend fun insertObjectWithTranslations(
        baseWord: String,
        comment: String?,
        translations: List<Pair<String, String>> // (languageCode, text)
    ): Long {
        val objectId = insertObject(
            SemanticObjectEntity(baseWord = baseWord, comment = comment)
        )
        translations.forEach { (lang, text) ->
            insertTranslation(
                TranslationEntity(objectId = objectId, languageCode = lang, text = text)
            )
        }
        return objectId
    }

    // ---------- GET ----------

    @Query("SELECT * FROM semantic_object ORDER BY createdAt DESC")
    suspend fun getAllObjects(): List<SemanticObjectEntity>

    @Transaction
    @Query("SELECT * FROM semantic_object ORDER BY createdAt DESC")
    fun observeAllWithTranslations(): Flow<List<SemanticObjectWithTranslations>>

    @Transaction
    @Query("SELECT * FROM semantic_object ORDER BY createdAt DESC")
    suspend fun getAllObjectsWithTranslations(): List<SemanticObjectWithTranslations>

    @Transaction
    @Query("SELECT * FROM semantic_object WHERE id = :id")
    suspend fun getObjectWithTranslations(id: Long): SemanticObjectWithTranslations?

    @Query("SELECT * FROM translation WHERE objectId = :objectId ORDER BY languageCode ASC")
    suspend fun getTranslationsForObject(objectId: Long): List<TranslationEntity>

    // ---------- UPDATE ----------

    @Update
    suspend fun updateObject(obj: SemanticObjectEntity)

    @Query("UPDATE semantic_object SET wordLanguageCode = :code WHERE id = :id")
    suspend fun updateLanguageCode(id: Long, code: String)

    // ---------- DELETE ----------

    @Query("DELETE FROM translation WHERE objectId = :objectId")
    suspend fun deleteAllTranslationsForObject(objectId: Long)

    @Query("DELETE FROM semantic_object WHERE id = :id")
    suspend fun deleteObjectById(id: Long)
}
