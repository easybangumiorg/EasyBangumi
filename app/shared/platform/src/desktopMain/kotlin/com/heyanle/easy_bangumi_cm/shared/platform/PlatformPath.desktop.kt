package com.heyanle.easy_bangumi_cm.shared.platform

import com.heyanle.easy_bangumi_cm.shared.model.provider.IPathProvider
import com.heyanle.easy_bangumi_cm.shared.model.system.ILogger
import com.heyanle.inject.core.injectLazy
import kotlin.io.path.Path
import kotlin.io.path.pathString

actual class PlatformPath : IPathProvider {
    val logger by injectLazy<ILogger>()

    private val cachePathRoot: String by lazy {
        // 1. 先找 jvm 缓存
        val jvmCache = System.getProperty("java.io.tmpdir")
        if (!jvmCache.isNullOrEmpty()) {
            logger.i("DesktopPathProvider", "find jvm cache path: $jvmCache")
            return@lazy jvmCache
        }

        // 2. 找 compose 资源目录的父目录
        val comResCache = System.getProperty("compose.application.resources.dir")?.let {
            Path(it).parent?.resolve("cache")?.toAbsolutePath()?.pathString
        }
        if (!comResCache.isNullOrEmpty()) {
            logger.i("DesktopPathProvider", "find compose resource cache path: $comResCache")
            return@lazy comResCache
        }

        // 3. 找用户目录
        val userHome = System.getProperty("user.home")?.let {
            Path(it).resolve(".cache").toAbsolutePath().pathString
        }
        if (!userHome.isNullOrEmpty()) {
            logger.i("DesktopPathProvider", "find user home cache path: $userHome")
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
            logger.i("DesktopPathProvider", "find compose resource file path: $comResFile")
            return@lazy comResFile
        }

        // 2. 找用户目录
        val userHome = System.getProperty("user.home")?.let {
            Path(it).resolve("file").toAbsolutePath().pathString
        }
        if (!userHome.isNullOrEmpty()) {
            logger.i("DesktopPathProvider", "find user home file path: $userHome")
            return@lazy userHome
        }

        throw Exception("Desktop can not find cache path")
    }

    override fun getCachePath(type: String): String {
        return Path(cachePathRoot).resolve(type).toAbsolutePath().pathString
    }

    override fun getFilePath(type: String): String {
        return Path(filePathRoot).resolve(type).toAbsolutePath().pathString
    }
}