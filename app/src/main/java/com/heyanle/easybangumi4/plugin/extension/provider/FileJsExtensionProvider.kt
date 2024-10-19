package com.heyanle.easybangumi4.plugin.extension.provider

import android.content.Context
import android.os.Build
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoader
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoaderFactory
import com.heyanle.easybangumi4.plugin.js.JsTestProvider
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionInnerLoader
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntime
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import org.jsoup.Jsoup
import java.io.File

/**
 * Created by heyanle on 2024/7/29.
 * https://github.com/heyanLE
 */
class FileJsExtensionProvider(
    private val jsRuntimeProvider: JSRuntimeProvider,
    private val jsFileExtensionFolder: String,
    dispatcher: CoroutineDispatcher,
    cacheFolder: String,
): AbsFolderExtensionProvider(jsFileExtensionFolder, cacheFolder, dispatcher){

    companion object {
        const val TAG = "FileJsExtensionProvider"

        // 扩展名
        const val EXTENSION_SUFFIX = ".ebg.js"

        // 加密后的后缀
        const val EXTENSION_CRY_SUFFIX = ".ebg.jsc"
    }

    override fun checkName(path: String): Boolean {
        return path.endsWith(EXTENSION_CRY_SUFFIX) || path.endsWith(EXTENSION_SUFFIX)
    }

    override fun getNameWhenLoad(path: String, time: Long, atomicLong: Long): String {
        val suffix = if (path.endsWith(EXTENSION_CRY_SUFFIX)) EXTENSION_CRY_SUFFIX else EXTENSION_SUFFIX
        return "${time}-${atomicLong}${suffix}"
    }

    override fun getExtensionLoader(fileList: List<File>): List<ExtensionLoader> {
        return ExtensionLoaderFactory.getFileJsExtensionLoaders(fileList, jsRuntimeProvider)
    }

    override fun coverExtensionLoaderList(loaderList: List<ExtensionLoader>): List<ExtensionLoader> {
        if (BuildConfig.DEBUG && JsTestProvider.testJs.isNotEmpty()) {
            return loaderList + JSExtensionInnerLoader(JsTestProvider.testJs, jsRuntimeProvider)
        }
        return super.coverExtensionLoaderList(loaderList)
    }
}