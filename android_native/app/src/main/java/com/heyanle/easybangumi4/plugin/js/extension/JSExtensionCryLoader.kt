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
        val FIRST_LINE_MARK = "easybangumi.cryjs".toByteArray()
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



        // 1. 复制 Mask 以外的内容
        file.inputStream().buffered().use {  i ->
            val maskBuffer = ByteArray(FIRST_LINE_MARK.size)
            i.read(maskBuffer, 0, maskBuffer.size)
            if (!maskBuffer.contentEquals(FIRST_LINE_MARK)){
                // 不是加密文件
                return null
            }

            plaintextCacheFile.outputStream().buffered().use {  o ->
                i.copyTo(o)
            }
        }


        // 2. 解密
        plaintextCacheFile.aesDecryptTo(plaintextFile, PackageHelper.appSignatureMD5, CHUNK_SIZE)
        if (!plaintextFile.exists() || plaintextFile.length() <= 0) {
            return null
        }
        plaintextFile.deleteOnExit()

        // 3. 加载
        return JSExtensionLoader(plaintextFile, jsRuntime).load()?.let {
            when(it) {
                is ExtensionInfo.InstallError -> {
                    it.copy(sourcePath = file.absolutePath)
                }
                is ExtensionInfo.Installed -> {
                    it.copy(sourcePath = file.absolutePath)
                }
                else -> null
            }
        }
    }

    override fun canLoad(): Boolean {
        return file.isFile && file.exists() && file.canRead() && file.name.endsWith(
            JsExtensionProvider.EXTENSION_CRY_SUFFIX)
    }
}