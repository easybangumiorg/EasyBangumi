package com.heyanle.easybangumi4.plugin.js.extension

import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoader
import com.heyanle.easybangumi4.plugin.extension.provider.JsExtensionProvider
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntimeProvider
import com.heyanle.easybangumi4.utils.PackageHelper
import com.heyanle.easybangumi4.utils.aesDecryptTo
import com.heyanle.easybangumi4.utils.aesEncryptTo
import com.heyanle.easybangumi4.utils.getInnerCachePath
import com.heyanle.easybangumi4.utils.getMD5
import java.io.File

/**
 * 加密 js 加载，解密后委托给 JsExtensionLoader
 * Created by heyanle on 2024/10/13
 * https://github.com/heyanLE
 */

class JSExtensionCryLoader(
    private val file: File,
    private val jsRuntime: JSRuntimeProvider,
): ExtensionLoader {

    companion object {
        val CHUNK_SIZE = 1024

        // 加密 js 文件首行
        val FIRST_LINE_MARK = "easybangumi.cryjs"
    }

    private val plaintextCacheFolder = APP.getInnerCachePath("js_plaintext")


    override val key: String
        get() = "js:${file.path}"

    override fun load(): ExtensionInfo? {
        File(plaintextCacheFolder).mkdirs()

        val plaintextDisplayName = file.absolutePath.getMD5()
        val plaintextFileCacheName = plaintextDisplayName + ".${JsExtensionProvider.EXTENSION_CRY_SUFFIX}"
        val plaintextFileName = plaintextDisplayName + ".${JsExtensionProvider.EXTENSION_SUFFIX}"
        val plaintextCacheFile = File(plaintextCacheFolder, plaintextFileCacheName)
        val plaintextFile = File(plaintextCacheFolder, plaintextFileName)

        plaintextCacheFile.delete()
        plaintextCacheFile.createNewFile()

        // 1. 把第一行标志拆分
        var needBlock = false
        plaintextFile.bufferedReader().use { reader ->
            plaintextCacheFile.bufferedWriter().use { writer ->
                var isFirstLineRead = false
                while(reader.ready()) {
                    val it = reader.readLine()
                    if (!isFirstLineRead) {
                        isFirstLineRead = true
                        if (it != FIRST_LINE_MARK) {
                            needBlock = true
                            break
                        }
                    } else {
                        writer.write(it)
                    }
                }
            }
        }
        if (needBlock) {
            plaintextCacheFile.delete()
            plaintextFile.delete()
            return null
        }

        plaintextFile.delete()

        // 2. 解密
        plaintextFile.createNewFile()
        plaintextCacheFile.aesDecryptTo(plaintextFile, PackageHelper.appSignature, CHUNK_SIZE)
        plaintextFile.deleteOnExit()
        return JSExtensionLoader(plaintextFile, jsRuntime).load()
    }

    override fun canLoad(): Boolean {
        return file.isFile && file.exists() && file.canRead() && file.name.endsWith(
            JsExtensionProvider.EXTENSION_CRY_SUFFIX)
    }
}