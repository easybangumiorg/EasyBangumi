package com.heyanle.easy_bangumi_cm.shared.platform

import android.app.Application
import com.heyanle.easy_bangumi_cm.base.model.provider.IPathProvider
import com.heyanle.lib.inject.core.injectLazy
import java.io.File

actual class PlatformPath : IPathProvider {
    private val context by injectLazy<Application>()

    override fun getCachePath(type: String): String {
        val root = context.externalCacheDir?:context.cacheDir
        return File(root, type).absolutePath
    }

    override fun getFilePath(type: String): String {
        return context.getExternalFilesDir(type)?.absolutePath ?: getCachePath(type)
    }
}