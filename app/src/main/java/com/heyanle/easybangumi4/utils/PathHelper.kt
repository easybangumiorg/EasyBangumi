package com.heyanle.easybangumi4.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Context.MODE_WORLD_WRITEABLE
import java.io.File

/**
 * Created by HeYanLe on 2023/8/5 16:57.
 * https://github.com/heyanLE
 */
fun Context.getFilePath(): String {
    return getExternalFilesDir(null)?.absolutePath ?: cacheDir.absolutePath
}

fun Context.getInnerFilePath(): String {
    return File(getDir("files", MODE_PRIVATE)?.absolutePath ?: cacheDir.absolutePath).absolutePath
}


fun Context.getInnerFilePath(type: String): String {
    return File(getDir("files", MODE_PRIVATE)?.absolutePath ?: cacheDir.absolutePath, type).absolutePath
}


fun Context.getFilePath(type: String): String {
    return getExternalFilesDir(type)?.absolutePath ?: File(cacheDir, type).absolutePath
}

fun Context.getCachePath(): String {
    return externalCacheDir?.absolutePath ?: cacheDir.absolutePath
}

fun Context.getCachePath(type: String): String {
    return externalCacheDir?.let { File(it, type) }?.absolutePath ?: File(
        cacheDir,
        type
    ).absolutePath
}