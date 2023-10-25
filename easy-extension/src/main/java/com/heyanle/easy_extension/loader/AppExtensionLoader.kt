package com.heyanle.easy_extension.loader

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.heyanle.easy_extension.Extension
import dalvik.system.PathClassLoader

/**
 * 已安装拓展加载
 * Created by heyanlin on 2023/10/25.
 */
class AppExtensionLoader(
    context: Context,
    private val pkgName: String,
): AbsExtensionLoader(context) {

    override val key: String
        get() = "app:${pkgName}"

    override fun load(): Extension? {
        val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                pkgName, PackageManager.PackageInfoFlags.of(PACKAGE_FLAGS.toLong())
            )
        } else {
            packageManager.getPackageInfo(pkgName,
                PACKAGE_FLAGS
            )
        } ?: return null
        val appInfo = pkgInfo.applicationInfo ?: return null
        val classLoader = PathClassLoader(appInfo.sourceDir, null, context.classLoader)
        return innerLoad(packageManager, pkgInfo, appInfo, classLoader, Extension.TYPE_APP)
    }

    override fun canLoad(): Boolean {
        val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                pkgName, PackageManager.PackageInfoFlags.of(PACKAGE_FLAGS.toLong())
            )
        } else {
            packageManager.getPackageInfo(pkgName,
                PACKAGE_FLAGS
            )
        } ?: return false
        return isPackageAnExtension(pkgInfo)
    }

}