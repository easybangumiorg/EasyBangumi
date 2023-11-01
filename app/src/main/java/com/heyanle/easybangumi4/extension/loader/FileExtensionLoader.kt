package com.heyanle.easybangumi4.extension.loader

import android.content.Context
import android.content.pm.PackageManager
import com.heyanle.easybangumi4.extension.Extension
import dalvik.system.DexClassLoader
import dalvik.system.PathClassLoader
import java.io.File

/**
 * 从文件加载
 * Created by heyanlin on 2023/10/25.
 */
class FileExtensionLoader(
    context: Context,
    private val path: String,
): AbsExtensionLoader(context) {

    override val key: String
        get() = "file:${path}"

    override fun load(): Extension? {
        val file = File(path)
        if (!file.exists() || !file.canRead()) {
            return null
        }
        val pkgInfo =
            packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES or PackageManager.GET_META_DATA) ?: return null
        val appInfo = pkgInfo.applicationInfo ?: return null
        if(appInfo.sourceDir.isNullOrEmpty()){
            appInfo.sourceDir = path
        }
        if(appInfo.publicSourceDir.isNullOrEmpty()){
            appInfo.publicSourceDir = path
        }
        val classLoader = PathClassLoader(path, null, context.classLoader)
        return innerLoad(packageManager, pkgInfo, appInfo, classLoader, Extension.TYPE_FILE)
    }

    override fun canLoad(): Boolean {
        val file = File(path)
        if(!file.exists() || !file.canRead() || file.isDirectory){
            return false
        }
        val pkgInfo =
            packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES) ?: return false
        return isPackageAnExtension(pkgInfo)
    }
}