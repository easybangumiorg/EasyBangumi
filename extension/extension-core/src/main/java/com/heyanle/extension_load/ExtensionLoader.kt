package com.heyanle.extension_load

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Build
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.bangumi_source_api.api2.SourceFactory
import com.heyanle.extension_load.model.Extension
import com.heyanle.extension_load.model.LoadResult
import com.heyanle.extension_load.utils.loge
import dalvik.system.PathClassLoader
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

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
    private const val LIB_VERSION_MAX = 1

    private const val PACKAGE_FLAGS =
        PackageManager.GET_CONFIGURATIONS or PackageManager.GET_SIGNATURES


    /**
     * 获取扩展列表
     * @param loadPkgName 需要直接加载的扩展包名列表
     */
    fun getAllExtension(context: Context): List<Extension> {
        val pkgManager = context.packageManager

        @Suppress("DEPRECATION") val installedPkgs =
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
            // 转换 Extension.Available
            innerLoadExtension(context,pkgManager, it.packageName)
        }.filterIsInstance<Extension>()
    }



    fun innerLoadExtension(
        context: Context, pkgManager: PackageManager, pkgName: String,
    ): Extension? {
        return kotlin.runCatching {
            val pkgManager = context.packageManager
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
            if (libVersion < LIB_VERSION_MIN || libVersion > LIB_VERSION_MAX) {
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
                    errMsg = "插件版本过旧",
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
            }.map {
                // 动态代理，返回 key 前面加上 包名-
                Proxy.newProxyInstance(it.javaClass.classLoader,
                    it.javaClass.interfaces,
                    object : InvocationHandler {
                        override fun invoke(
                            proxy: Any?, method: Method?, args: Array<out Any>?
                        ): Any? {
                            if (method?.name == "getKey") {
                                return pkgInfo.packageName + "-" + it.key
                            }
                            return method?.invoke(it, *(args ?: arrayOfNulls<Any>(0)))
                        }
                    }) as Source
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