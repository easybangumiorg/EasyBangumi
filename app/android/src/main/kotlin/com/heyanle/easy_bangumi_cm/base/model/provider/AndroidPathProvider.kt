package com.heyanle.easy_bangumi_cm.base.model.provider

import android.content.Context
import java.io.File


/**
 * Created by HeYanLe on 2025/2/4 17:41.
 * https://github.com/heyanLE
 */

class AndroidPathProvider(
    private val context: Context
): IPathProvider {

    override fun getCachePath(type: String): String {
        val root = context.externalCacheDir?:context.cacheDir
        return File(root, type).absolutePath
    }

    override fun getFilePath(type: String): String {
        return context.getExternalFilesDir(type)?.absolutePath ?: getCachePath(type)
    }
}