package com.heyanle.easybangumi4.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

/**
 * Created by heyanlin on 2024/5/21.
 */
object UriHelper {

    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            fileName = cursor.getString(nameIndex)
        }
        return fileName
    }


}