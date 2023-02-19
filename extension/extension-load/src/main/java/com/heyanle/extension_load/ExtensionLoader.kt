package com.heyanle.extension_load

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.bangumi_source_api.api2.SourceFactory
import com.heyanle.extension_load.model.Extension
import com.heyanle.extension_load.model.LoadResult
import com.heyanle.extension_load.utils.loge
import dalvik.system.PathClassLoader
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Created by HeYanLe on 2023/2/19 16:14.
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

    private const val PACKAGE_FLAGS = PackageManager.GET_CONFIGURATIONS or PackageManager.GET_SIGNATURES


    @SuppressLint("QueryPermissionsNeeded")
    suspend fun loadExtensions(context: Context): List<LoadResult> {
        val pkgManager = context.packageManager

        @Suppress("DEPRECATION")
        val installedPkgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pkgManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PACKAGE_FLAGS.toLong()))
        } else {
            pkgManager.getInstalledPackages(PACKAGE_FLAGS)
        }

        val extPkgs = installedPkgs.filter { isPackageAnExtension(it) }

        if (extPkgs.isEmpty()) return emptyList()

        return runBlocking {
            val deferred = extPkgs.map {
                async { loadExtension(context, it.packageName, it) }
            }
            deferred.map { it.await() }
        }
    }



    suspend fun loadExtensionByFile(context: Context, path: String): LoadResult {
        val file = File(path)
        if(!file.exists() || !file.name.endsWith(".apk")){
            return LoadResult.Error
        }

        val pkgManager = context.packageManager
        val pkgInfo = try {
            pkgManager.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_META_DATA) ?: throw NullPointerException()
        } catch (error: java.lang.Exception) {
            error.printStackTrace()
            return LoadResult.Error
        }

        val appInfo = try {
            pkgInfo.applicationInfo ?: throw NullPointerException()
        } catch (error: java.lang.Exception) {
            error.printStackTrace()
            return LoadResult.Error
        }

        return loadExtension(context, pkgManager, pkgInfo, appInfo, Extension.TYPE_FILE)

    }

    /**
     * Attempts to load an extension from the given package name. It checks if the extension
     * contains the required feature flag before trying to load it.
     */
    fun loadExtensionFromPkgName(context: Context, pkgName: String): LoadResult {
        val pkgInfo = try {
            context.packageManager.getPackageInfo(pkgName, PACKAGE_FLAGS)
        } catch (error: PackageManager.NameNotFoundException) {
            // Unlikely, but the package may have been uninstalled at this point
            error.printStackTrace()
            return LoadResult.Error
        }
        if (!isPackageAnExtension(pkgInfo)) {
            "Tried to load a package that wasn't a extension ($pkgName)".loge()
            return LoadResult.Error
        }
        return loadExtension(context, pkgName, pkgInfo)
    }

    private fun loadExtension(context: Context, pkgName: String, pkgInfo: PackageInfo): LoadResult {
        val pkgManager = context.packageManager

        val appInfo = try {
            pkgManager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)
        } catch (error: PackageManager.NameNotFoundException) {
            // Unlikely, but the package may have been uninstalled at this point
            error.printStackTrace()
            return LoadResult.Error
        }

        return loadExtension(context, pkgManager, pkgInfo, appInfo, Extension.TYPE_APP)

    }

    private fun loadExtension(context: Context, pkgManager: PackageManager, pkgInfo: PackageInfo, appInfo: ApplicationInfo, loadType: Int): LoadResult {

        val extName = pkgManager.getApplicationLabel(appInfo).toString().substringAfter("EasyBangumi: ")
        val versionName = pkgInfo.versionName
        val versionCode = PackageInfoCompat.getLongVersionCode(pkgInfo)

        if (versionName.isNullOrEmpty()) {
            "Missing versionName for extension $extName".loge()
            return LoadResult.Error
        }


        // Validate lib version
        val libVersion = appInfo.metaData.getLong(METADATA_SOURCE_LIB_VERSION)
        if (libVersion < LIB_VERSION_MIN || libVersion > LIB_VERSION_MAX) {
            "Lib version is $libVersion, while only versions " +
                    "$LIB_VERSION_MIN to $LIB_VERSION_MAX are allowed".loge()
            return LoadResult.Error
        }

        val readme = appInfo.metaData.getString(METADATA_README)

        val classLoader = PathClassLoader(appInfo.sourceDir, null, context.classLoader)

        val sources = (appInfo.metaData.getString(METADATA_SOURCE_CLASS)?:"")
            .split(";")
            .map {
                val sourceClass = it.trim()
                if (sourceClass.startsWith(".")) {
                    pkgInfo.packageName + sourceClass
                } else {
                    sourceClass
                }
            }.flatMap {
                kotlin.runCatching {
                    when (val obj = Class.forName(it, false, classLoader).newInstance()) {
                        is Source -> listOf(obj)
                        is SourceFactory -> obj.create()
                        else -> throw Exception("Unknown source class type! ${obj.javaClass}")
                    }
                }.getOrElse {
                    "Extension load error: $extName".loge()
                    it.printStackTrace()
                    return LoadResult.Error
                }
            }.map {
                Proxy.newProxyInstance(it.javaClass.classLoader, it.javaClass.interfaces, object: InvocationHandler {
                    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
                        if(method?.name == "getKey"){
                            return pkgInfo.packageName + "-" + method.invoke(args)
                        }
                        return method?.invoke(args)
                    }
                } ) as Source
            }

        val extension = Extension(
            label = extName,
            pkgName = pkgInfo.packageName,
            versionName = versionName,
            versionCode = versionCode,
            libVersion = libVersion,
            icon = kotlin.runCatching { pkgManager.getApplicationIcon(pkgInfo.packageName) }.getOrNull(),
            sources = sources,
            readme = readme,
            loadType = loadType
        )
        return LoadResult.Success(extension)
    }

    private fun isPackageAnExtension(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.reqFeatures.orEmpty().any { it.name == EXTENSION_FEATURE }
    }


}