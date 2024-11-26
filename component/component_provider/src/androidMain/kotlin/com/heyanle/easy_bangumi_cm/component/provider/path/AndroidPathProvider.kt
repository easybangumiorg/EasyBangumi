package com.heyanle.easy_bangumi_cm.component.provider.path

import android.content.Context
import java.io.File


/**
 * Created by HeYanLe on 2024/11/27 0:21.
 * https://github.com/heyanLE
 */

class AndroidPathProvider(
    private val context: Context
): PathProvider {

    override fun getCachePath(type: String): String {
        return File(context.externalCacheDir?.absolutePath ?: context.cacheDir.absolutePath, type).absolutePath
    }

    override fun getFilePath(type: String): String {
        return context.getExternalFilesDir(type)?.absolutePath ?:  File(context.cacheDir, type).absolutePath
    }

    override fun getLibraryPath(type: String): String {
        // TODO 用户选择目录
        return ""
    }
}