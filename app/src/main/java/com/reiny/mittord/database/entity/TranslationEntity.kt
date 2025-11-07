package com.reiny.mittord.database.entity

import androidx.room.*

@Entity(
    tableName = "translation",
    foreignKeys = [
        ForeignKey(
            entity = SemanticObjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["objectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("objectId")]
)
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val objectId: Long,
    val languageCode: String,
    val text: String
)