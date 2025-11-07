package com.reiny.mittord.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "semantic_object")
data class SemanticObjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val baseWord: String,
    val comment: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)