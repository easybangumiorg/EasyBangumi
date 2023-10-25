package com.heyanle.easy_extension.loader

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
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
                if (it == null || it.absolutePath.isNullOrEmpty() || !it.exists() || it.isDirectory) {
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