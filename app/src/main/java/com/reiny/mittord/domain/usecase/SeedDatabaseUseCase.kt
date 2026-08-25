package com.reiny.mittord.domain.usecase

import com.reiny.mittord.database.DictionaryRepository
import javax.inject.Inject

class SeedDatabaseUseCase @Inject constructor(
    private val repository: DictionaryRepository
) {
    suspend operator fun invoke() {
        listOf(
            "hund" to "dog", "katt" to "cat", "hus" to "house", "bil" to "car",
            "bok" to "book", "vann" to "water", "mat" to "food", "dag" to "day",
            "natt" to "night", "sol" to "sun", "måne" to "moon", "tre" to "tree",
            "blomst" to "flower", "fugl" to "bird", "fisk" to "fish", "himmel" to "sky",
            "fjell" to "mountain", "hav" to "sea", "elv" to "river", "vind" to "wind"
        ).forEach { (word, translation) ->
            val id = repository.addWord(word, translation, "en")
            repository.updateLanguageCode(id, "no")
        }
    }
}
