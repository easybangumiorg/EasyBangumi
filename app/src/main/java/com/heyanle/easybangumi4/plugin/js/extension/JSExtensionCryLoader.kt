package com.heyanle.easybangumi4.plugin.js.extension

import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoader
import com.heyanle.easybangumi4.plugin.extension.provider.FileJsExtensionProvider
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntimeProvider
import com.heyanle.easybangumi4.utils.PackageHelper
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
    }

    private val plaintextCacheFolder = APP.getInnerCachePath("js_plaintext")


    override val key: String
        get() = "js:${file.path}"

    override fun load(): ExtensionInfo? {
        val plaintextFileName = file.absolutePath.getMD5()
        val plaintextFile = File(plaintextCacheFolder, plaintextFileName)
        plaintextFile.delete()
        // 解密
        file.aesEncryptTo(plaintextFile, PackageHelper.appSignature, CHUNK_SIZE)
        plaintextFile.deleteOnExit()
        return JSExtensionLoader(plaintextFile, jsRuntime).load()
    }

    override fun canLoad(): Boolean {
        return file.isFile && file.exists() && file.canRead() && file.name.endsWith(
            FileJsExtensionProvider.EXTENSION_CRY_SUFFIX)
    }
}