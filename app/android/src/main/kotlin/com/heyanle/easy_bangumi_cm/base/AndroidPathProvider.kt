package com.heyanle.easy_bangumi_cm.base

import android.content.Context
import com.heyanle.easy_bangumi_cm.base.path_provider.PathProvider
import java.io.File


/**
 * Created by HeYanLe on 2024/12/3 0:20.
 * https://github.com/heyanLE
 */

class AndroidPathProvider(
    private val context: Context
): PathProvider {


    override fun getCachePath(type: String): String {
        val root = context.externalCacheDir?:context.cacheDir
        return File(root, type).absolutePath
    }

    override fun getFilePath(type: String): String {
        return context.getExternalFilesDir(type)?.absolutePath ?: getCachePath(type)
    }
}