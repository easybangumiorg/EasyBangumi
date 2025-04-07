package com.heyanle.lib.unifile.core.contract

import android.content.Context
import android.net.Uri
import android.provider.MediaStore


/**
 * Created by heyanlin on 2024/12/4.
 */
object MediaContract {

    fun getName(context: Context, self: Uri): String? {
        return Contract.queryForString(context, self, MediaStore.MediaColumns.DISPLAY_NAME, null)
    }

    fun getType(context: Context, self: Uri): String? {
        return Contract.queryForString(context, self, MediaStore.MediaColumns.MIME_TYPE, null)
    }

    fun getFilePath(context: Context, self: Uri): String? {
        return Contract.queryForString(context, self, MediaStore.MediaColumns.DATA, null)
    }

    fun lastModified(context: Context, self: Uri): Long {
        return Contract.queryForLong(context, self, MediaStore.MediaColumns.DATE_MODIFIED, -1L)
    }

    fun length(context: Context, self: Uri): Long {
        return Contract.queryForLong(context, self, MediaStore.MediaColumns.SIZE, -1L)
    }

}