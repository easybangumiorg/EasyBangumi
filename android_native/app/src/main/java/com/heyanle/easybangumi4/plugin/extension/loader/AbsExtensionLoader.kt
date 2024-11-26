package com.heyanle.easybangumi4.plugin.extension.loader

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.crash.SourceCrashController
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.SourceFactory
import com.heyanle.easybangumi4.utils.FileUtils
import com.heyanle.easybangumi4.utils.TimeLogUtils
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.getInnerFilePath
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.extension_api.Extension
import com.heyanle.extension_api.ExtensionBundle
import com.heyanle.extension_api.ExtensionSource
import net.lingala.zip4j.ZipFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

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
        const val LIB_VERSION_MIN = 6
        const val LIB_VERSION_MAX = 11

        const val PACKAGE_FLAGS =
            PackageManager.GET_CONFIGURATIONS or PackageManager.GET_SIGNATURES
    }

    protected val extensionFolderCache = context.getCachePath("extension_folder")
    protected val extensionFolderRoot = context.getFilePath("extension_folder")
    protected val packageManager: PackageManager = context.packageManager
    protected fun innerLoad(
        pkgManager: PackageManager,
        pkgInfo: PackageInfo,
        appInfo: ApplicationInfo,
        classLoader: ClassLoader, // 最终的 pathClassLoader，已经指定了文件路径
        loadType: Int,
    ): ExtensionInfo? {
        if (!isPackageAnExtension(pkgInfo)) {
            return null
        }


        TimeLogUtils.i("ExtensionLoader ${pkgInfo.packageName} inner start")
        appInfo.publicSourceDir.logi(TAG)
        try {

            val apkFile = File(appInfo.sourceDir)
            val apkFolder = File(extensionFolderRoot, pkgInfo.packageName)
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
                return ExtensionInfo.InstallError(
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
                    publicPath = appInfo.publicSourceDir,
                    folderPath = apkFolder.absolutePath,
                )
            }
            if (libVersion > LIB_VERSION_MAX) {
                "Lib version is ${libVersion}, while only versions " + "${LIB_VERSION_MIN} to ${LIB_VERSION_MAX} are allowed".loge(
                    "ExtensionLoader"
                )
                return ExtensionInfo.InstallError(
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
                    publicPath = appInfo.publicSourceDir,
                    folderPath = apkFolder.absolutePath,
                )
            }


            var extension: Extension? = null

            if (SourceCrashController.needBlock){
                return ExtensionInfo.InstallError(
                    key = key,
                    label = extName,
                    pkgName = pkgInfo.packageName,
                    versionName = versionName,
                    versionCode = versionCode,
                    libVersion = libVersion,
                    readme = readme,
                    icon = kotlin.runCatching { pkgManager.getApplicationIcon(pkgInfo.packageName) }
                        .getOrNull(),
                    errMsg = "安全模式加载阻断",
                    exception = null,
                    loadType = loadType,
                    sourcePath = appInfo.sourceDir,
                    publicPath = appInfo.publicSourceDir,
                    folderPath = apkFolder.absolutePath,
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
                        is Extension -> {
                            if (extension == null){
                                extension = obj
                            }else{
                                throw Exception("Only support one Extension")
                            }
                            obj.create()
                        }
                        is SourceFactory -> obj.create()
                        else -> throw Exception("Unknown source class type! ${obj.javaClass}")
                    }
                } catch (e: Exception) {
                    "Extension load error: $extName".loge("ExtensionLoader")
                    e.printStackTrace()
                    return ExtensionInfo.InstallError(
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
                        publicPath = appInfo.publicSourceDir,
                        folderPath = apkFolder.absolutePath,
                    )
                }
            }.flatMap {
                if (it is ExtensionSource) {
                    it.packageName = pkgInfo.packageName
                    listOf(it)
                }else if(pkgInfo.packageName == "org.easybangumi.extension"){
                    listOf(it)
                }else{
                    emptyList()
                }
            }
            appInfo.loadIcon(pkgManager)

            // 解压 apk

            try {

                val apkSoFolderIndex = File(apkFolder, "index.json")
                var needUnzip = false
                if (!apkSoFolderIndex.exists()) {
                    needUnzip = true
                } else {
                    val jsonText = apkSoFolderIndex.readText()
                    val jsonObject = JSONObject(jsonText)

                    val path = jsonObject.optString("apk_path")
                    if (File(path).absolutePath != apkFile.absolutePath) {
                        needUnzip = true
                    } else {
                        val size = jsonObject.optLong("apk_size")
                        if (size != apkFile.length()) {
                            needUnzip = true
                        } else {
                            val soArray = jsonObject.optJSONArray("file_list")
                            if (soArray == null) {
                                needUnzip = true
                            } else {
                                for (i in 0 until soArray.length()) {
                                    val fileObject = soArray.getJSONObject(i)
                                    val absPath = fileObject.optString("path")
                                    val fileSize = fileObject.optLong("size")

                                    val f = File(apkFolder, absPath)
                                    if (!f.exists() || f.length() != fileSize) {
                                        needUnzip = true
                                        break
                                    }
                                }
                            }
                        }
                    }
                }

                if (needUnzip) {
                    apkFolder.deleteRecursively()
                    apkFolder.mkdirs()

                    val cacheRoot = File(extensionFolderCache, pkgInfo.packageName)
                    cacheRoot.deleteRecursively()
                    cacheRoot.mkdirs()

                    // 解压
                    ZipFile(apkFile).extractAll(cacheRoot.absolutePath)

                    // 需要复制的
                    val sourceLib = File(cacheRoot, "lib")
                    val sourceAssets = File(cacheRoot, "assets")

                    val targetLib = File(apkFolder, "lib")
                    val targetAssets = File(apkFolder, "assets")

                    if(sourceLib.exists())
                        sourceLib.copyRecursively(targetLib, true)
                    if (sourceAssets.exists())
                        sourceAssets.copyRecursively(targetAssets, true)

                    // 生成 Index

                    val jsonObject = JSONObject()
                    jsonObject.put("apk_path", apkFile.absolutePath)
                    jsonObject.put("apk_size", apkFile.length())

                    val pathArray = JSONArray()

                    val pathArrayList = arrayListOf<Pair<String, Long>>()
                    FileUtils.traverseFolder(apkFolder, pathArrayList)

                    pathArrayList.forEach {
                        val o = JSONObject()
                        o.put("path", it.first).put("size", it.second)
                        pathArray.put(o)
                    }

                    jsonObject.put("file_list", pathArray)

                    apkSoFolderIndex.delete()
                    apkSoFolderIndex.createNewFile()
                    apkSoFolderIndex.writeText(jsonObject.toString())
                }
            }catch (e: Throwable){
                e.printStackTrace()
                return ExtensionInfo.InstallError(
                    key = key,
                    label = extName,
                    pkgName = pkgInfo.packageName,
                    versionName = versionName,
                    versionCode = versionCode,
                    libVersion = libVersion,
                    readme = readme,
                    icon = kotlin.runCatching { pkgManager.getApplicationIcon(pkgInfo.packageName) }
                        .getOrNull(),
                    errMsg = context.getString(com.heyanle.easy_i18n.R.string.load_error) + "${e.message}",
                    exception = e,
                    loadType = loadType,
                    sourcePath = appInfo.sourceDir,
                    publicPath = appInfo.publicSourceDir,
                    folderPath = apkFolder.absolutePath,
                )
            }


            TimeLogUtils.i("ExtensionLoader ${pkgInfo.packageName} inner completely")
            if (extension != null){
                extension?.bundle = ExtensionBundle(pkgInfo.packageName, File(apkFolder, "lib").absolutePath, File(apkFolder, "assets").absolutePath)
                extension?.onInit(APP)
            }
            return ExtensionInfo.Installed(
                key = key,
                label = extName,
                pkgName = pkgInfo.packageName,
                versionName = versionName,
                versionCode = versionCode,
                libVersion = libVersion,
                readme = readme,
                icon = kotlin.runCatching {   appInfo.loadIcon(pkgManager) }
                    .getOrNull(),
                sources = sources,
                resources = pkgManager.getResourcesForApplication(appInfo),
                loadType = loadType,
                sourcePath = appInfo.sourceDir,
                publicPath = appInfo.publicSourceDir,
                folderPath = apkFolder.absolutePath,
                extension = extension,
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