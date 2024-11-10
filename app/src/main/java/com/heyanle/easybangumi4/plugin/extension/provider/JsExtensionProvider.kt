package com.heyanle.easybangumi4.plugin.extension.provider

import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoader
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoaderFactory
import com.heyanle.easybangumi4.plugin.js.JsTestProvider
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionInnerLoader
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionLoader
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File

/**
 * Created by heyanle on 2024/7/29.
 * https://github.com/heyanLE
 */
class JsExtensionProvider(
    private val jsRuntimeProvider: JSRuntimeProvider,
    private val jsFileExtensionFolder: String,
    dispatcher: CoroutineDispatcher,
    cacheFolder: String,
): AbsFolderExtensionProvider(jsFileExtensionFolder, cacheFolder, dispatcher){

    companion object {
        const val TAG = "FileJsExtensionProvider"

        // 扩展名
        const val EXTENSION_SUFFIX = "ebg.js"

        // 加密后的后缀
        const val EXTENSION_CRY_SUFFIX = "ebg.jsc"

        fun isEndWithJsExtensionSuffix(path: String) = path.endsWith(EXTENSION_SUFFIX) || path.endsWith(EXTENSION_CRY_SUFFIX)
    }

    override fun checkName(displayName: String): Boolean {
        return displayName.endsWith(EXTENSION_CRY_SUFFIX)
                || displayName.endsWith(EXTENSION_SUFFIX)

    }

    override fun getNameWhenLoad(displayName: String, time: Long, atomicLong: Long): String {
        val suffix = when  {
            displayName.endsWith(EXTENSION_CRY_SUFFIX) -> EXTENSION_CRY_SUFFIX
            else -> EXTENSION_SUFFIX
        }
        return "${time}-${atomicLong}.${suffix}"
    }

    override fun loadExtensionLoader(fileList: List<File>): List<ExtensionLoader> {
        return ExtensionLoaderFactory.getFileJsExtensionLoaders(fileList, jsRuntimeProvider)
    }

    override fun coverExtensionLoaderList(loaderList: List<ExtensionLoader>): List<ExtensionLoader> {
        if (BuildConfig.DEBUG) {
            val file = APP.assets.open("extension_test.js").use {
                File(cacheFolder).mkdirs()
                val file = File(cacheFolder, "test.js")
                file.outputStream().use { output ->
                    it.copyTo(output)
                }
                file
            }
            return loaderList + JSExtensionLoader(file, jsRuntimeProvider)
        }
        return super.coverExtensionLoaderList(loaderList)
    }
}