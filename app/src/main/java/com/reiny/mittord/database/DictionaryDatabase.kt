package com.reiny.mittord.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.reiny.mittord.database.dao.SemanticObjectDao
import com.reiny.mittord.database.entity.SemanticObjectEntity
import com.reiny.mittord.database.entity.TranslationEntity

@Database(entities = [SemanticObjectEntity::class, TranslationEntity::class], version = 1)
abstract class DictionaryDatabase : RoomDatabase() {
    abstract fun dao(): SemanticObjectDao
}