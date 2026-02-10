package org.easybangumi.next.lib.utils

import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.unifile.UFD
import java.io.File

actual interface PathProvider {

    actual fun getFilePath(type: String): UFD

    actual fun getCachePath(type: String): UFD
}

class PathProviderImpl : PathProvider {

    val logger = logger()

    // sdcard/Android/data/org.easybangumi.next/cache
    // data/data/org.easybangumi.next/cache
    private val cachePathRoot: String by lazy {
        global.appContext.externalCacheDir?.absolutePath ?: global.appContext.cacheDir?.absolutePath ?: throw Exception("Android can not find cache path")
    }

    // sdcard/Android/data/org.easybangumi.next/files
    // data/data/org.easybangumi.next/files
    // cachePathRoot
    override fun getFilePath(type: String): UFD {
        return global.appContext.getExternalFilesDir(type) ?.absolutePath?.let {
            UFD(UFD.TYPE_JVM, it)
        } ?: global.appContext.filesDir?.let {
            UFD(UFD.TYPE_JVM, it.absolutePath)
        } ?: getCachePath(type)
    }

    override fun getCachePath(type: String): UFD {
        val file = File(cachePathRoot, type)
        return UFD(UFD.TYPE_JVM, file.absolutePath)
    }
}
