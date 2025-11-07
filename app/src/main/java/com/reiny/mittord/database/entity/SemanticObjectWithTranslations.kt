package com.reiny.mittord.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SemanticObjectWithTranslations(
    @Embedded val semanticObject: SemanticObjectEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "objectId"
    )
    val translations: List<TranslationEntity>
)