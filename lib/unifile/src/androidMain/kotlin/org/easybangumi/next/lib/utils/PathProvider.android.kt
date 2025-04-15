package org.easybangumi.next.lib.utils

import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.unifile.UFD
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.pathString

actual interface PathProvider {

    actual fun getFilePath(path: String): UFD

    actual fun getCachePath(path: String): UFD
}

private class PathProviderImpl : PathProvider {

    val logger = logger()

    // sdcard/Android/data/org.easybangumi.next/cache
    // data/data/org.easybangumi.next/cache
    private val cachePathRoot: String by lazy {
        global.appContext.externalCacheDir?.absolutePath ?: global.appContext.cacheDir?.absolutePath ?: throw Exception("Android can not find cache path")
    }

    // sdcard/Android/data/org.easybangumi.next/files
    // data/data/org.easybangumi.next/files
    // cachePathRoot
    override fun getFilePath(path: String): UFD {

        return global.appContext.getExternalFilesDir(path) ?.absolutePath?.let {
            UFD(UFD.TYPE_JVM, it)
        } ?: global.appContext.filesDir?.let {
            UFD(UFD.TYPE_JVM, it.absolutePath)
        } ?: getCachePath(path)
    }

    override fun getCachePath(path: String): UFD {
        val file = File(cachePathRoot, path)
        return UFD(UFD.TYPE_JVM, file.absolutePath)
    }
}

actual val pathProvider: PathProvider by lazy {
    PathProviderImpl()
}