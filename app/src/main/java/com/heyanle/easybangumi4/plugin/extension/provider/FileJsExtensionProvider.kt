package com.heyanle.easybangumi4.plugin.extension.provider

import android.content.Context
import android.os.Build
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoader
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoaderFactory
import com.heyanle.easybangumi4.plugin.js.JsTestProvider
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionInnerLoader
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntime
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File

/**
 * Created by heyanle on 2024/7/29.
 * https://github.com/heyanLE
 */
class FileJsExtensionProvider(
    private val jsRuntime: JSRuntime,
    private val jsFileExtensionFolder: String,
    dispatcher: CoroutineDispatcher,
    cacheFolder: String,
): AbsFolderExtensionProvider(jsFileExtensionFolder, cacheFolder, dispatcher){

    companion object {
        const val TAG = "FileJsExtensionProvider"

        // 扩展名
        const val EXTENSION_SUFFIX = ".ebg.js"
    }

    override fun getSuffix(): String {
        return EXTENSION_SUFFIX
    }

    override fun getExtensionLoader(fileList: List<File>): List<ExtensionLoader> {
        return ExtensionLoaderFactory.getFileJsExtensionLoaders(fileList, jsRuntime)
    }

    override fun coverExtensionLoaderList(loaderList: List<ExtensionLoader>): List<ExtensionLoader> {
        if (BuildConfig.DEBUG && JsTestProvider.testJs.isNotEmpty()) {
            return loaderList + JSExtensionInnerLoader(JsTestProvider.testJs, jsRuntime)
        }
        return super.coverExtensionLoaderList(loaderList)
    }
}