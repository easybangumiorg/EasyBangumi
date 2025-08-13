package com.heyanle.easybangumi4.plugin.extension.loader

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.heyanle.easybangumi4.plugin.extension.provider.JsExtensionProvider
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionCryLoader
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionLoader
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntimeProvider
import com.heyanle.easybangumi4.utils.logi
import java.io.File

/**
 * Created by heyanlin on 2023/10/25.
 */
object ExtensionLoaderFactory {

    @SuppressLint("QueryPermissionsNeeded")
    fun getInstalledAppExtensionLoaders(context: Context): List<AppExtensionLoader> {
        val pkgManager = context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pkgManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(AbsExtensionLoader.PACKAGE_FLAGS.toLong()))
        } else {
            pkgManager.getInstalledPackages(AbsExtensionLoader.PACKAGE_FLAGS)
        }.map {
            AppExtensionLoader(context, it.packageName)
        }
    }

    fun getFileApkExtensionLoaders(
        context: Context,
        fileList: List<String>
    ): List<FileExtensionLoader> {
        return try {
            fileList.map {
                FileExtensionLoader(context, it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getFileJsExtensionLoaders(
        fileList: List<File>,
        jsRuntime: JSRuntimeProvider
    ): List<ExtensionLoader> {
        return try {
            fileList.map {
                if (it.name.endsWith(JsExtensionProvider.EXTENSION_CRY_SUFFIX)) {
                    logi("load js file: ${it.name}")
                    JSExtensionCryLoader(it, jsRuntime)
                } else {
                    logi("load js file: ${it.name}")
                    JSExtensionLoader(it, jsRuntime)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getFileJsExtensionLoader(
        file: File,
        jsRuntime: JSRuntimeProvider
    ): ExtensionLoader? {
        return try {
            if (file.name.endsWith(JsExtensionProvider.EXTENSION_CRY_SUFFIX)) {
                logi("load js file: ${file.name}")
                JSExtensionCryLoader(file, jsRuntime)
            } else {
                logi("load js file: ${file.name}")
                JSExtensionLoader(file, jsRuntime)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}