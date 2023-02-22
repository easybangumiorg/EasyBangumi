package com.heyanle.extension_load

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Build
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
    fun getAllExtension(context: Context, loadPkgName: List<String>): List<Extension> {
        val pkgManager = context.packageManager

        @Suppress("DEPRECATION") val installedPkgs =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pkgManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PACKAGE_FLAGS.toLong()))
            } else {
                pkgManager.getInstalledPackages(PACKAGE_FLAGS)
            }

        val extPkgs = installedPkgs.filter { isPackageAnExtension(it) }

        if (extPkgs.isEmpty()) return emptyList()

        return extPkgs.map {
            val res = arrayListOf<Extension.Available>()
            // 转换 Extension.Available
            getAvailableExtension(pkgManager, it)
        }.filterIsInstance<LoadResult.Success<Extension.Available>>()
            .map {
                it.extension
            }.map { ext ->
                // 加载 loadPkgName 中的扩展
                if (loadPkgName.contains(ext.pkgName)) {
                    loadExtension(context, ext)
                } else {
                    ext
                }
            }
    }


    fun getExtension(
        context: Context,
        pkgName: String,
    ): LoadResult<Extension.Available> {

        return try {
            val pkgManager = context.packageManager
            val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pkgManager.getPackageInfo(
                    pkgName, PackageManager.PackageInfoFlags.of(PACKAGE_FLAGS.toLong())
                )
            } else {
                pkgManager.getPackageInfo(pkgName, PACKAGE_FLAGS)
            }
            getAvailableExtension(pkgManager, pkgInfo)

        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e, e.message.toString())
        }

    }

    fun loadExtension(
        context: Context, ext: Extension.Available
    ): Extension {
        return when (val res = innerLoadExtension(context, ext)) {
            is LoadResult.Success -> {
                res.extension
            }

            is LoadResult.Error -> {
                Extension.InstallError(
                    label = ext.label,
                    pkgName = ext.pkgName,
                    versionName = ext.versionName,
                    versionCode = ext.versionCode,
                    libVersion = ext.libVersion,
                    readme = ext.readme,
                    icon = ext.icon,
                    exception = res.exception,
                    errMsg = res.errMsg,
                )
                ext
            }
        }
    }

    private fun getAvailableExtension(
        pkgManager: PackageManager,
        packageInfo: PackageInfo,
    ): LoadResult<Extension.Available> {
        try {
            val appInfo =
                pkgManager.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA)
            val extName =
                pkgManager.getApplicationLabel(appInfo).toString().substringAfter("EasyBangumi: ")
            val versionName = packageInfo.versionName
            val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
            // Validate lib version
            val libVersion = appInfo.metaData.getLong(METADATA_SOURCE_LIB_VERSION)
            val readme = appInfo.metaData.getString(METADATA_README)
            return LoadResult.Success(Extension.Available(label = extName,
                pkgName = packageInfo.packageName,
                versionName = versionName,
                versionCode = versionCode,
                libVersion = libVersion,
                readme = readme,
                icon = kotlin.runCatching { pkgManager.getApplicationIcon(packageInfo.packageName) }
                    .getOrNull()))
        } catch (e: Exception) {
            return LoadResult.Error(e, e.message.toString())
        }

    }

    private fun innerLoadExtension(
        context: Context, ext: Extension.Available
    ): LoadResult<Extension.Installed> {
        return kotlin.runCatching {
            val pkgManager = context.packageManager
            val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pkgManager.getPackageInfo(
                    ext.pkgName, PackageManager.PackageInfoFlags.of(PACKAGE_FLAGS.toLong())
                )
            } else {
                pkgManager.getPackageInfo(ext.pkgName, PACKAGE_FLAGS)
            }
            val appInfo = pkgManager.getApplicationInfo(ext.pkgName, PackageManager.GET_META_DATA)
            val classLoader = PathClassLoader(appInfo.sourceDir, null, context.classLoader)

            // 库版本管理
            if (ext.libVersion < LIB_VERSION_MIN || ext.libVersion > LIB_VERSION_MAX) {
                "Lib version is ${ext.libVersion}, while only versions " + "${LIB_VERSION_MIN} to ${LIB_VERSION_MAX} are allowed".loge()
                return LoadResult.Error(
                    exception = null,
                    errMsg = "Lib version is ${ext.libVersion}, while only versions " + "${LIB_VERSION_MIN} to ${LIB_VERSION_MAX} are allowed"
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
                    "Extension load error: ${ext.label}".loge()
                    e.printStackTrace()
                    return LoadResult.Error(e, "Extension load error")
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
                                return pkgInfo.packageName + "-" + method.invoke(args)
                            }
                            return method?.invoke(args)
                        }
                    }) as Source
            }

            return LoadResult.Success(
                Extension.Installed(
                    label = ext.label,
                    pkgName = ext.pkgName,
                    versionName = ext.versionName,
                    versionCode = ext.versionCode,
                    libVersion = ext.libVersion,
                    readme = ext.readme,
                    icon = ext.icon,
                    sources = sources,
                    resources = pkgManager.getResourcesForApplication(appInfo),
                )
            )

        }.getOrElse {
            it.printStackTrace()
            LoadResult.Error(exception = null, errMsg = it.message.toString())
        }
    }

    private fun isPackageAnExtension(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.reqFeatures.orEmpty().any { it.name == EXTENSION_FEATURE }
    }

}