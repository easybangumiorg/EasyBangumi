package com.heyanle.easy_extension

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.heyanle.easy_extension.utils.loge
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.SourceFactory
import com.heyanle.extension_api.ExtensionSource
import com.heyanle.extension_load.R
import dalvik.system.DexClassLoader
import dalvik.system.PathClassLoader
import java.io.File

/**
 * Created by heyanlin on 2023/10/24.
 */
class ExtensionLoader(
    private val context: Context
) {

    companion object {
        private const val TAG = "ExtensionLoader"
        private const val EXTENSION_FEATURE = "easybangumi.extension"
        private const val METADATA_SOURCE_CLASS = "easybangumi.extension.source"
        private const val METADATA_SOURCE_LIB_VERSION = "easybangumi.extension.lib.version"
        private const val METADATA_README = "easybangumi.extension.readme"

        // 当前容器支持的 扩展库 版本区间
        private const val LIB_VERSION_MIN = 3
        private const val LIB_VERSION_MAX = 3

        private const val PACKAGE_FLAGS =
            PackageManager.GET_CONFIGURATIONS or PackageManager.GET_SIGNATURES
    }


    private val packageManager = context.packageManager
    private val cacheFolder =
        context.externalCacheDir?.let { File(it, "extension-apk-cache") }?.absolutePath ?: File(
            context.cacheDir,
            "extension-apk-cache"
        ).absolutePath

    fun loadFromFile(path: String): Extension? {
        val file = File(path)
        if (!file.exists() || !file.canRead()) {
            return null
        }
        val pkgInfo =
            packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES) ?: return null
        val appInfo = pkgInfo.applicationInfo ?: return null
        appInfo.sourceDir = path
        appInfo.publicSourceDir = path
        val classLoader = DexClassLoader(path, cacheFolder, null, context.classLoader)
        return innerLoad(packageManager, pkgInfo, appInfo, classLoader)

    }

    fun loadFromApp(pkgName: String): Extension? {
        val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                pkgName, PackageManager.PackageInfoFlags.of(PACKAGE_FLAGS.toLong())
            )
        } else {
            packageManager.getPackageInfo(pkgName, PACKAGE_FLAGS)
        } ?: return null
        val appInfo = pkgInfo.applicationInfo ?: return null
        val classLoader = PathClassLoader(appInfo.sourceDir, null, context.classLoader)
        return innerLoad(packageManager, pkgInfo, appInfo, classLoader)
    }

    private fun innerLoad(
        pkgManager: PackageManager,
        pkgInfo: PackageInfo,
        appInfo: ApplicationInfo,
        classLoader: ClassLoader, // 最终的 pathClassLoader，已经指定了文件路径
    ): Extension? {
        if (!isPackageAnExtension(pkgInfo)) {
            return null
        }
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
                "Lib version is ${libVersion}, while only versions " + "$LIB_VERSION_MIN to $LIB_VERSION_MAX are allowed".loge(
                    TAG
                )
                return Extension.InstallError(
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
                )
            }
            if (libVersion > LIB_VERSION_MAX) {
                "Lib version is ${libVersion}, while only versions " + "$LIB_VERSION_MIN to $LIB_VERSION_MAX are allowed".loge(
                    "ExtensionLoader"
                )
                return Extension.InstallError(
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
                    )
                }
            }.map {
                if (it is ExtensionSource) {
                    it.packageName = pkgInfo.packageName
                }
                it
            }
            return Extension.Installed(
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
            )
        } catch (e: Exception) {
            return null
        }

    }

    private fun isPackageAnExtension(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.reqFeatures.orEmpty().any { it.name == EXTENSION_FEATURE }
    }
}