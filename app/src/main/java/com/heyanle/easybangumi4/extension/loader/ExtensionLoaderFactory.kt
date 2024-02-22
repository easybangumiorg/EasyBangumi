package com.heyanle.easybangumi4.extension.loader

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.heyanle.easybangumi4.extension.ExtensionController
import com.heyanle.easybangumi4.utils.logi
import java.io.File

/**
 * Created by heyanlin on 2023/10/25.
 */
object ExtensionLoaderFactory {

    @SuppressLint("QueryPermissionsNeeded")
    fun getAppExtensionLoaders(context: Context): List<AppExtensionLoader> {
        val pkgManager = context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pkgManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(AbsExtensionLoader.PACKAGE_FLAGS.toLong()))
        } else {
            pkgManager.getInstalledPackages(AbsExtensionLoader.PACKAGE_FLAGS)
        }.map {
            AppExtensionLoader(context, it.packageName)
        }
    }

    fun getFileExtensionLoaders(context: Context, folder: String): List<FileExtensionLoader> {
        return try {
            val file = File(folder)
            val files = file.listFiles() ?: emptyArray()
            files.flatMap {
                // 只加载一级文件
                it.name.logi("ExtensionLoaderFactory")
                if (it == null || !it.name.endsWith(ExtensionController.EXTENSION_SUFFIX) || it.absolutePath.isNullOrEmpty() || !it.exists() || it.isDirectory) {
                    emptyList()
                } else {
                    listOf(FileExtensionLoader(context, it.absolutePath))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}