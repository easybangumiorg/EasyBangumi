package com.heyanle.easybangumi4.plugin.extension.provider

import android.content.Context
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoader
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoaderFactory
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File

/**
 * Created by heyanle on 2024/7/29.
 * https://github.com/heyanLE
 */
class FileApkExtensionProvider(
    private val context: Context,
    private val apkFileExtensionFolder: String,
    dispatcher: CoroutineDispatcher,
    cacheFolder: String,
) : AbsFolderExtensionProvider(apkFileExtensionFolder, cacheFolder, dispatcher) {

    companion object {
        const val TAG = "FileApkExtensionProvider"

        // 扩展名
        const val EXTENSION_SUFFIX = ".easybangumi.apk"
    }

    override fun getSuffix(): String {
        return EXTENSION_SUFFIX
    }

    override fun getExtensionLoader(fileList: List<File>): List<ExtensionLoader> {
        return ExtensionLoaderFactory.getFileApkExtensionLoaders(context, fileList.map { it.absolutePath })
    }


}