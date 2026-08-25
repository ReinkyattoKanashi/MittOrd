package com.reiny.mittord.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvatarRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun saveJpeg(bitmap: Bitmap): String {
        clearOldFiles()
        val file = File(context.filesDir, AppConstants.AVATAR_FILE_JPG)
        file.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, AppConstants.JPEG_QUALITY, output)
        }
        return file.absolutePath
    }

    fun saveGif(uri: Uri): String {
        clearOldFiles()
        val file = File(context.filesDir, AppConstants.AVATAR_FILE_GIF)
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }

    fun delete() = clearOldFiles()

    private fun clearOldFiles() {
        listOf(AppConstants.AVATAR_FILE_JPG, AppConstants.AVATAR_FILE_GIF).forEach { name ->
            File(context.filesDir, name).takeIf { it.exists() }?.delete()
        }
    }
}
