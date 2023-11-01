package com.heyanle.easybangumi4.extension.loader

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import com.heyanle.easybangumi4.extension.Extension
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.SourceFactory
import com.heyanle.easybangumi4.utils.TimeLogUtils
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.extension_api.ExtensionSource

/**
 * Created by heyanlin on 2023/10/25.
 */
abstract class AbsExtensionLoader(
    protected val context: Context
): ExtensionLoader {

    companion object {
        const val TAG = "AbsExtensionLoader"
        const val EXTENSION_FEATURE = "easybangumi.extension"
        const val METADATA_SOURCE_CLASS = "easybangumi.extension.source"
        const val METADATA_SOURCE_LIB_VERSION = "easybangumi.extension.lib.version"
        const val METADATA_README = "easybangumi.extension.readme"

        // 当前容器支持的 扩展库 版本区间
        const val LIB_VERSION_MIN = 3
        const val LIB_VERSION_MAX = 3

        const val PACKAGE_FLAGS =
            PackageManager.GET_CONFIGURATIONS or PackageManager.GET_SIGNATURES
    }

    protected val packageManager = context.packageManager
    protected fun innerLoad(
        pkgManager: PackageManager,
        pkgInfo: PackageInfo,
        appInfo: ApplicationInfo,
        classLoader: ClassLoader, // 最终的 pathClassLoader，已经指定了文件路径
        loadType: Int,
    ): Extension? {
        if (!isPackageAnExtension(pkgInfo)) {
            return null
        }
        TimeLogUtils.i("ExtensionLoader ${pkgInfo.packageName} inner start")
        try {
            val extName =
                appInfo.loadLabel(pkgManager).toString().substringAfter("EasyBangumi: ")
            val versionName = pkgInfo.versionName ?: ""
            val versionCode = PackageInfoCompat.getLongVersionCode(pkgInfo)
            // Validate lib version
            val libVersion = appInfo.metaData.getInt(METADATA_SOURCE_LIB_VERSION)
            val readme = appInfo.metaData.getString(METADATA_README)
            // 库版本管理
            if (libVersion < LIB_VERSION_MIN) {
                "Lib version is ${libVersion}, while only versions " + "${LIB_VERSION_MIN} to ${LIB_VERSION_MAX} are allowed".loge(
                    TAG
                )
                return Extension.InstallError(
                    key = key,
                    label = extName,
                    pkgName = pkgInfo.packageName,
                    versionName = versionName,
                    versionCode = versionCode,
                    libVersion = libVersion,
                    readme = readme,
                    icon = kotlin.runCatching { appInfo.loadIcon(pkgManager) }
                        .getOrNull(),
                    errMsg = context.getString(com.heyanle.easy_i18n.R.string.extension_too_old),
                    exception = null,
                    loadType = loadType,
                    sourcePath = appInfo.sourceDir,
                )
            }
            if (libVersion > LIB_VERSION_MAX) {
                "Lib version is ${libVersion}, while only versions " + "${LIB_VERSION_MIN} to ${LIB_VERSION_MAX} are allowed".loge(
                    "ExtensionLoader"
                )
                return Extension.InstallError(
                    key = key,
                    label = extName,
                    pkgName = pkgInfo.packageName,
                    versionName = versionName,
                    versionCode = versionCode,
                    libVersion = libVersion,
                    readme = readme,
                    icon = kotlin.runCatching {
                        appInfo.loadIcon(pkgManager)
                    }.getOrNull(),
                    errMsg = context.getString(com.heyanle.easy_i18n.R.string.app_too_old),
                    exception = null,
                    loadType = loadType,
                    sourcePath = appInfo.sourceDir,
                )
            }
            val sources = (appInfo.metaData.getString(METADATA_SOURCE_CLASS) ?: "").split(";").map {
                val sourceClass = it.trim()
                if (sourceClass.startsWith(".")) {
                    pkgInfo.packageName + sourceClass
                } else {
                    sourceClass
                }
            }.flatMap {
                try {
                    val clazz = Class.forName(it, false, classLoader)
                    val con = clazz.constructors.firstOrNull()
                        ?: throw Exception("Unknown source class type! ${clazz}}")
                    val obj = con.newInstance()
                    when (obj) {
                        is Source -> listOf(obj)
                        is SourceFactory -> obj.create()
                        else -> throw Exception("Unknown source class type! ${obj.javaClass}")
                    }
                } catch (e: Exception) {
                    "Extension load error: $extName".loge("ExtensionLoader")
                    e.printStackTrace()
                    return Extension.InstallError(
                        key = key,
                        label = extName,
                        pkgName = pkgInfo.packageName,
                        versionName = versionName,
                        versionCode = versionCode,
                        libVersion = libVersion,
                        readme = readme,
                        icon = kotlin.runCatching { pkgManager.getApplicationIcon(pkgInfo.packageName) }
                            .getOrNull(),
                        errMsg = context.getString(com.heyanle.easy_i18n.R.string.load_error),
                        exception = e,
                        loadType = loadType,
                        sourcePath = appInfo.sourceDir,
                    )
                }
            }.map {
                if (it is ExtensionSource) {
                    it.packageName = pkgInfo.packageName
                }
                it
            }
            TimeLogUtils.i("ExtensionLoader ${pkgInfo.packageName} inner completely")
            return Extension.Installed(
                key = key,
                label = extName,
                pkgName = pkgInfo.packageName,
                versionName = versionName,
                versionCode = versionCode,
                libVersion = libVersion,
                readme = readme,
                icon = kotlin.runCatching { pkgManager.getApplicationIcon(pkgInfo.packageName) }
                    .getOrNull(),
                sources = sources,
                resources = pkgManager.getResourcesForApplication(appInfo),
                loadType = loadType,
                sourcePath = appInfo.sourceDir,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    protected fun isPackageAnExtension(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.reqFeatures.orEmpty().any { it.name == EXTENSION_FEATURE }
    }

}