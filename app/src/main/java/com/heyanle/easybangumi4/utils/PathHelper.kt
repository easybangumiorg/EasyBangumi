package com.heyanle.easybangumi4.utils

import android.content.Context
import java.io.File

/**
 * Created by HeYanLe on 2023/8/5 16:57.
 * https://github.com/heyanLE
 */
fun Context.getDataPath(): String {
    return getExternalFilesDir(null)?.absolutePath ?: cacheDir.absolutePath
}

fun Context.getDataPath(type: String): String {
    return getExternalFilesDir(type)?.absolutePath ?: File(cacheDir, type).absolutePath
}