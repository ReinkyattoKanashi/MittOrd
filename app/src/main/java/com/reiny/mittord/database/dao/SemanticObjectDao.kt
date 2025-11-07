package com.reiny.mittord.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.reiny.mittord.database.entity.SemanticObjectEntity
import com.reiny.mittord.database.entity.SemanticObjectWithTranslations
import com.reiny.mittord.database.entity.TranslationEntity

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
    ) {
        val objectId = insertObject(
            SemanticObjectEntity(baseWord = baseWord, comment = comment)
        )
        translations.forEach { (lang, text) ->
            insertTranslation(
                TranslationEntity(objectId = objectId, languageCode = lang, text = text)
            )
        }
    }

    // ---------- GET ----------

    @Query("SELECT * FROM semantic_object ORDER BY createdAt DESC")
    suspend fun getAllObjects(): List<SemanticObjectEntity>

    @Query("SELECT * FROM translation WHERE objectId = :objectId ORDER BY languageCode ASC")
    suspend fun getTranslationsForObject(objectId: Long): List<TranslationEntity>

    @Transaction
    @Query("SELECT * FROM semantic_object WHERE isFavorite = 1 ORDER BY createdAt DESC")
    suspend fun getFavorites(): List<SemanticObjectWithTranslations>

    @Query("SELECT * FROM translation WHERE languageCode = :lang ORDER BY text ASC")
    suspend fun getTranslationsByLanguage(lang: String): List<TranslationEntity>

    // ---------- UPDATE ----------

    @Update
    suspend fun updateObject(obj: SemanticObjectEntity)

    @Update
    suspend fun updateTranslation(translation: TranslationEntity)

    @Query("UPDATE translation SET text = :newText WHERE id = :translationId")
    suspend fun updateTranslationText(translationId: Long, newText: String)

    @Query("UPDATE semantic_object SET isFavorite = :isFav WHERE id = :objectId")
    suspend fun updateFavorite(objectId: Long, isFav: Boolean)

    // ---------- DELETE ----------

    @Delete
    suspend fun deleteObject(obj: SemanticObjectEntity)

    @Delete
    suspend fun deleteTranslation(translation: TranslationEntity)

    @Transaction
    suspend fun deleteTranslationAndMaybeObject(
        translation: TranslationEntity
    ) {
        deleteTranslation(translation)
        val remaining = getTranslationsCount(translation.objectId)
        if (remaining == 0) {
            deleteObjectById(translation.objectId)
        }
    }

    @Query("SELECT COUNT(*) FROM translation WHERE objectId = :objectId")
    suspend fun getTranslationsCount(objectId: Long): Int

    @Query("DELETE FROM semantic_object WHERE id = :id")
    suspend fun deleteObjectById(id: Long)
}