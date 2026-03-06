package com.reiny.mittord.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.reiny.mittord.database.dao.SemanticObjectDao
import com.reiny.mittord.database.entity.SemanticObjectEntity
import com.reiny.mittord.database.entity.TranslationEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE semantic_object ADD COLUMN imagePath TEXT")
        db.execSQL("ALTER TABLE semantic_object ADD COLUMN wordLanguageCode TEXT")
    }
}

@Database(entities = [SemanticObjectEntity::class, TranslationEntity::class], version = 2)
abstract class DictionaryDatabase : RoomDatabase() {
    abstract fun dao(): SemanticObjectDao
}
