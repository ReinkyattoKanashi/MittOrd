package com.reiny.mittord.util

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordImageRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun save(uri: Uri, wordId: Long): String {
        val dir = File(context.filesDir, AppConstants.WORD_IMAGES_DIR).also { it.mkdirs() }
        val dest = File(dir, "$wordId${AppConstants.WORD_IMAGE_EXT}")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        return dest.absolutePath
    }
}
