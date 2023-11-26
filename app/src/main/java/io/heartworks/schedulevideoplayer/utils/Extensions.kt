package io.heartworks.schedulevideoplayer.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun Uri.getFileName(context: Context): String {
    val fileName: String?
    val cursor = context.contentResolver.query(this, null, null, null, null)
    cursor?.moveToFirst()
    val columnIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: 0
    fileName = cursor?.getString(columnIndex)
    cursor?.close()
    return fileName ?: ""
}