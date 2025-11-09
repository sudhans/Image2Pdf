package com.msd.image2pdf

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    val imageUris = mutableStateListOf<Uri>()
    val pdfFiles = mutableStateListOf<Uri>()

    fun onImagesSelected(uris: List<Uri>) {
        imageUris.clear()
        imageUris.addAll(uris)
    }

    fun removeImage(uri: Uri) {
        imageUris.remove(uri)
    }

    suspend fun findPdfFiles(context: Context) {
        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.RELATIVE_PATH
            )

            val selection = "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE ? AND ${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
            val selectionArgs = arrayOf("%${Environment.DIRECTORY_DOWNLOADS}/Image2Pdf%", "application/pdf")

            val queryUri = MediaStore.Files.getContentUri("external")
            context.contentResolver.query(
                queryUri,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                pdfFiles.clear()
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = Uri.withAppendedPath(queryUri, id.toString())
                    pdfFiles.add(contentUri)
                }
            }
        }
    }
}
