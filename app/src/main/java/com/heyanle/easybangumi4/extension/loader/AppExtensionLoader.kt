package com.heyanle.easybangumi4.extension.loader

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.heyanle.easybangumi4.extension.ExtensionInfo
import com.heyanle.easybangumi4.utils.TimeLogUtils
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

    val pkgInfo: PackageInfo? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                pkgName, PackageManager.PackageInfoFlags.of(PACKAGE_FLAGS.toLong())
            )
        } else {
            packageManager.getPackageInfo(pkgName,
                PACKAGE_FLAGS
            )
        }
    }.getOrElse {
        it.printStackTrace()
        null
    }

    override fun load(): ExtensionInfo? {
        pkgInfo ?: return null
        val appInfo = packageManager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA) ?: return null
        TimeLogUtils.i("classLoader start ${pkgName}")
        val classLoader = PathClassLoader(appInfo.sourceDir, null, context.classLoader)
        TimeLogUtils.i("classLoader end ${pkgName}")
        return innerLoad(packageManager, pkgInfo, appInfo, classLoader, ExtensionInfo.TYPE_APK_INSTALL)
    }

    override fun canLoad(): Boolean {
        pkgInfo  ?: return false
        return isPackageAnExtension(pkgInfo)
    }

}