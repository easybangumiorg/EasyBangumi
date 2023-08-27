package com.heyanle.extension_load

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.bangumi_source_api.api.SourceFactory
import com.heyanle.extension_api.ExtensionSource
import com.heyanle.extension_load.model.Extension
import com.heyanle.extension_load.utils.loge
import dalvik.system.PathClassLoader

/**
 * Created by HeYanLe on 2023/2/21 21:50.
 * https://github.com/heyanLE
 */
object ExtensionLoader {

    private const val EXTENSION_FEATURE = "easybangumi.extension"
    private const val METADATA_SOURCE_CLASS = "easybangumi.extension.source"
    private const val METADATA_SOURCE_LIB_VERSION = "easybangumi.extension.lib.version"
    private const val METADATA_README = "easybangumi.extension.readme"

    // 当前容器支持的 扩展库 版本区间
    private const val LIB_VERSION_MIN = 1
    private const val LIB_VERSION_MAX = 2

    private const val PACKAGE_FLAGS =
        PackageManager.GET_CONFIGURATIONS or PackageManager.GET_SIGNATURES


    /**
     * 获取扩展列表
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun getAllExtension(context: Context): List<Extension> {
        val pkgManager = context.packageManager

        val installedPkgs =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pkgManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PACKAGE_FLAGS.toLong()))
            } else {
                pkgManager.getInstalledPackages(PACKAGE_FLAGS)
            }

        val extPkgs = installedPkgs.filter {
            it.packageName.loge("ExtensionLoader")
            isPackageAnExtension(it) }

        if (extPkgs.isEmpty()) return emptyList()

        return extPkgs.map {
            // 加载
            innerLoadExtension(context,pkgManager, it.packageName)
        }.filterIsInstance<Extension>()
    }



    fun innerLoadExtension(
        context: Context, pkgManager: PackageManager, pkgName: String,
    ): Extension? {
        return kotlin.runCatching {
            val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pkgManager.getPackageInfo(
                    pkgName, PackageManager.PackageInfoFlags.of(PACKAGE_FLAGS.toLong())
                )
            } else {
                pkgManager.getPackageInfo(pkgName, PACKAGE_FLAGS)
            }
            val appInfo = pkgManager.getApplicationInfo(pkgInfo.packageName, PackageManager.GET_META_DATA)
            val classLoader = PathClassLoader(appInfo.sourceDir, null, context.classLoader)

            val extName =
                pkgManager.getApplicationLabel(appInfo).toString().substringAfter("EasyBangumi: ")
            val versionName = pkgInfo.versionName
            val versionCode = PackageInfoCompat.getLongVersionCode(pkgInfo)
            // Validate lib version
            val libVersion = appInfo.metaData.getInt(METADATA_SOURCE_LIB_VERSION)
            val readme = appInfo.metaData.getString(METADATA_README)
            // 库版本管理
            if (libVersion < LIB_VERSION_MIN) {
                "Lib version is ${libVersion}, while only versions " + "${LIB_VERSION_MIN} to ${LIB_VERSION_MAX} are allowed".loge("ExtensionLoader")
                return Extension.InstallError(
                    label = extName,
                    pkgName = pkgInfo.packageName,
                    versionName = versionName,
                    versionCode = versionCode,
                    libVersion = libVersion,
                    readme = readme,
                    icon = kotlin.runCatching { pkgManager.getApplicationIcon(pkgInfo.packageName) }
                        .getOrNull(),
                    errMsg = "拓展版本过旧",
                    exception = null,
                )
            }
            if (libVersion > LIB_VERSION_MAX) {
                "Lib version is ${libVersion}, while only versions " + "${LIB_VERSION_MIN} to ${LIB_VERSION_MAX} are allowed".loge("ExtensionLoader")
                return Extension.InstallError(
                    label = extName,
                    pkgName = pkgInfo.packageName,
                    versionName = versionName,
                    versionCode = versionCode,
                    libVersion = libVersion,
                    readme = readme,
                    icon = kotlin.runCatching { pkgManager.getApplicationIcon(pkgInfo.packageName) }
                        .getOrNull(),
                    errMsg = "纯纯看番本体 APP 版本过旧",
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
                    when (val obj = Class.forName(it, false, classLoader).newInstance()) {
                        is Source -> listOf(obj)
                        is SourceFactory -> obj.create()
                        else -> throw Exception("Unknown source class type! ${obj.javaClass}")
                    }
                } catch (e: Exception) {
                    "Extension load error: ${extName}".loge("ExtensionLoader")
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
                        errMsg = "加载异常",
                        exception = e,
                    )
                }
            }
                .map {
                    if(it is ExtensionSource) {
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

        }.getOrElse {
            it.printStackTrace()
            null
        }
    }

    private fun isPackageAnExtension(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.reqFeatures.orEmpty().any { it.name == EXTENSION_FEATURE }
    }

}