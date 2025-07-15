package org.easybangumi.next.lib.utils

import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.unifile.UFD
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.pathString

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

actual interface PathProvider {

    actual fun getFilePath(type: String): UFD

    actual fun getCachePath(type: String): UFD

    fun getCacheJvmPath(type: String): String

    fun getFileJvmPath(type: String): String
}

private class PathProviderImpl : PathProvider {

    val logger = logger()

    private val cachePathRoot: String by lazy {
        // 1. 先找 jvm 缓存
        val jvmCache = System.getProperty("java.io.tmpdir")?.let {
            Path(it).parent?.resolve("easybangumi.next")?.toAbsolutePath()?.pathString
        }
        if (!jvmCache.isNullOrEmpty()) {
            logger.info("find jvm cache path: $jvmCache")
            return@lazy jvmCache
        }

        // 2. 找 compose 资源目录的父目录
        val comResCache = System.getProperty("compose.application.resources.dir")?.let {
            Path(it).parent?.resolve("cache")?.toAbsolutePath()?.pathString
        }
        if (!comResCache.isNullOrEmpty()) {
            logger.info( "find compose resource cache path: $comResCache")
            return@lazy comResCache
        }

        // 3. 找用户目录
        val userHome = System.getProperty("user.home")?.let {
            Path(it).resolve("easybangumi.next").resolve("cache").toAbsolutePath().pathString
        }
        if (!userHome.isNullOrEmpty()) {
            logger.info("find user home cache path: $userHome")
            return@lazy userHome
        }

        throw Exception("Desktop can not find cache path")
    }

    private val filePathRoot: String by lazy {

        // 1. 找 compose 资源目录的父目录
        val comResFile = System.getProperty("compose.application.resources.dir")?.let {
            Path(it).parent?.resolve("file")?.toAbsolutePath()?.pathString
        }
        if (!comResFile.isNullOrEmpty()) {
            logger.info("find compose resource file path: $comResFile")
            return@lazy comResFile
        }

        // 2. 找用户目录
        val userHome = System.getProperty("user.home")?.let {
            Path(it).resolve("easybangumi.next").resolve("file").toAbsolutePath().pathString
        }
        if (!userHome.isNullOrEmpty()) {
            logger.info("find user home file path: $userHome")
            return@lazy userHome
        }

        throw Exception("Desktop can not find cache path")
    }

    override fun getFilePath(type: String): UFD {
        val filePath = Path(filePathRoot).resolve(type).toAbsolutePath().pathString
        return UFD(UFD.TYPE_JVM, filePath)
    }

    override fun getCachePath(type: String): UFD {
        val filePath = Path(cachePathRoot).resolve(type).toAbsolutePath().pathString
        return UFD(UFD.TYPE_JVM, filePath)
    }

    override fun getCacheJvmPath(type: String): String {
        return File(cachePathRoot, type).absolutePath
    }

    override fun getFileJvmPath(type: String): String {
        return File(filePathRoot, type).absolutePath
    }
}

actual val pathProvider: PathProvider by lazy {
    PathProviderImpl()
}