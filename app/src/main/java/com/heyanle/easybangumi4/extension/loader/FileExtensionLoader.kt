package com.heyanle.easybangumi4.extension.loader

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.heyanle.easybangumi4.extension.ExtensionInfo
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

    private val pkgInfo: PackageInfo? by lazy {
        val f = File(path)
        f.setReadOnly()
//        val os = FileOutputStream(f)
//        os.use {
//            if(!f.setReadOnly()){
//                "setReadOnly Failed 1".loge(TAG)
//            }
//        }
        // 安卓 14 适配，需要已读才能加载
        //f.setReadOnly()
        //PackageManagerCompat.getPermissionRevocationVerifierApp()
        packageManager.getPackageArchiveInfo(path, PackageManager.GET_META_DATA or PackageManager.GET_CONFIGURATIONS or PackageManager.GET_SIGNATURES)
    }

    override val key: String
        get() = "file:${path}"

    override fun load(): ExtensionInfo? {
        val file = File(path)
        if (!file.exists() || !file.canRead()) {
            return null
        }
        val pkgInfo = this.pkgInfo ?: return null
        val appInfo = pkgInfo.applicationInfo ?: return null
        if(appInfo.sourceDir.isNullOrEmpty()){
            appInfo.sourceDir = path
        }
        if(appInfo.publicSourceDir.isNullOrEmpty()){
            appInfo.publicSourceDir = path
        }
        val classLoader = kotlin.runCatching {
            val f = File(path)
            f.setReadOnly()
            PathClassLoader(path, context.classLoader)
        }.getOrElse {
            it.printStackTrace()
            return null
        }
        return innerLoad(packageManager, pkgInfo, appInfo, classLoader, ExtensionInfo.TYPE_FILE)
    }

    override fun canLoad(): Boolean {
        val file = File(path)
        if(!file.exists() || !file.canRead() || file.isDirectory){
            return false
        }
        val pkgInfo = this.pkgInfo ?: return false
       return isPackageAnExtension(pkgInfo)
    }
}