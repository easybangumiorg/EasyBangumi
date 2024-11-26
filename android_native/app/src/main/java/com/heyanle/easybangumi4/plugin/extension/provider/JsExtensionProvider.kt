package com.heyanle.easybangumi4.plugin.extension.provider

import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoader
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoaderFactory
import com.heyanle.easybangumi4.plugin.js.JsTestProvider
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionInnerLoader
import com.heyanle.easybangumi4.plugin.js.extension.JSExtensionLoader
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File
import java.io.InputStream

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

    override fun innerAppendExtension(displayName: String, inputStream: InputStream) {
        fileObserver.stopWatching()
        val fileName = getNameWhenLoad(displayName, System.currentTimeMillis(), atomicLong.getAndIncrement())
        // "${System.currentTimeMillis()}-${atomicLong.getAndIncrement()}${getSuffix()}"
        val cacheFile = File(cacheFolder, fileName)

        val targetFileTemp = File(folderPath, "${fileName}.temp")
        cacheFolderFile.mkdirs()
        cacheFile.createNewFile()
        inputStream.use { input ->
            cacheFile.outputStream().use { out ->
                input.copyTo(out)
            }
        }
        cacheFile.deleteOnExit()
        val loader = loadExtensionLoader(listOf(cacheFile)).firstOrNull() ?: return
        if (loader.canLoad()) {
            // 改名为 key
            val ext = loader.load() as? ExtensionInfo.Installed
            val source = ext?.sources?.firstOrNull()
            val targetFile = if (ext != null && source != null) {
                val suffix = when  {
                    displayName.endsWith(EXTENSION_CRY_SUFFIX) -> EXTENSION_CRY_SUFFIX
                    else -> EXTENSION_SUFFIX
                }
                File(folderPath, source.key + "." + suffix)
            } else {
                File(folderPath, fileName)
            }
            targetFile.delete()
            cacheFile.copyTo(targetFileTemp)
            targetFileTemp.renameTo(targetFile)
        }
        cacheFolderFile.deleteRecursively()
        cacheFolderFile.mkdirs()
        scanFolder()
        fileObserver.startWatching()
    }
}