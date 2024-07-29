package com.heyanle.easybangumi4.plugin.js.extension

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Javascript
import com.heyanle.easybangumi4.crash.SourceCrashController
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.loader.AbsExtensionLoader
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoader
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntime
import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.js.source.JsSource
import java.io.File

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class JSExtensionLoader(
    private val file: File,
    private val jsRuntime: JSRuntime,
): ExtensionLoader {

    companion object {
        const val TAG = "JSExtensionLoader"

        const val JS_IMPORT = """
            importPackage(Packages.com.heyanle.easybangumi4.plugin.extension);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.js.runtime);
            importPackage(Packages.com.heyanle.easybangumi4.plugin.js.entity);
            importPackage(Packages.org.jsoup);
            importPackage(Packages.okhttp3);
            importPackage(Packages.com.heyanle.easybangumi4.source_api.utils.api);
            importPackage(Packages.com.heyanle.easybangumi4.source_api.entity);
            importPackage(Packages.kotlin.text);
            importPackage(Packages.java.util);
        """

        const val JS_SOURCE_TAG_KEY = "key"
        const val JS_SOURCE_TAG_LABEL = "label"
        const val JS_SOURCE_TAG_VERSION_NAME = "versionName"
        const val JS_SOURCE_TAG_VERSION_CODE = "versionCode"
        const val JS_SOURCE_TAG_LIB_VERSION = "libVersion"
        const val JS_SOURCE_TAG_COVER = "cover"

    }

    override val key: String
        get() = "js:${file.path}"

    override fun canLoad(): Boolean {
        return file.isFile && file.exists() && file.canRead()
    }

    override fun load(): ExtensionInfo? {
        if (!file.exists() || !file.canRead()) {
            return null
        }



        val map = HashMap<String, String>()

        file.reader().buffered().use {
            var line = it.readLine()
            while(line != null) {
                if (line.isEmpty() || !line.startsWith("//")){
                    break
                }
                var firstAtIndex = -1
                var spacerAfterAtIndex = -1

                line.forEachIndexed { index, c ->
                    if (firstAtIndex == -1 && c != '@'){
                        firstAtIndex = index
                    }
                    if (firstAtIndex != -1 && spacerAfterAtIndex == -1 && c == ' '){
                        spacerAfterAtIndex = index
                    }
                    if (firstAtIndex != -1 && spacerAfterAtIndex != -1){
                        return@forEachIndexed
                    }
                }

                if (firstAtIndex == -1 || spacerAfterAtIndex == -1){
                    continue
                }

                val key = line.substring(firstAtIndex + 1, spacerAfterAtIndex)
                val value = line.substring(spacerAfterAtIndex + 1)
                map[key] = value
                line = it.readLine()
            }
        }

        val jsScope = JSScope(jsRuntime)

        val label = map[JS_SOURCE_TAG_LABEL] ?: ""
        val key = map[JS_SOURCE_TAG_KEY] ?: ""
        val versionName = map[JS_SOURCE_TAG_VERSION_NAME] ?: ""
        val versionCode = map[JS_SOURCE_TAG_VERSION_CODE]?.toLongOrNull() ?: -1
        val libVersion = map[JS_SOURCE_TAG_LIB_VERSION]?.toIntOrNull() ?: -1


        if (SourceCrashController.needBlock){
            return ExtensionInfo.InstallError(
                key = key,
                label = label,
                pkgName = key,
                versionName = versionName,
                versionCode = versionCode,
                libVersion = libVersion,
                readme = "",
                icon = Icons.Filled.Javascript,
                errMsg = "安全模式阻断",
                loadType = ExtensionInfo.TYPE_JS_FILE,
                sourcePath = file.absolutePath,
                publicPath = file.absolutePath,
                folderPath = file.absolutePath,
                exception = null
            )
        }

        val libErrorMsg = if (libVersion == -1) {
            "元数据错误"
        } else if (libVersion > AbsExtensionLoader.LIB_VERSION_MAX) {
            "纯纯看番版本过低"
        } else if (libVersion < AbsExtensionLoader.LIB_VERSION_MIN) {
            "插件版本过低"
        } else {
            null
        }

        if (libErrorMsg != null) {
            return ExtensionInfo.InstallError(
                key = key,
                label = label,
                pkgName = key,
                versionName = versionName,
                versionCode = versionCode,
                libVersion = libVersion,
                readme = "",
                icon = Icons.Filled.Javascript,
                errMsg = libErrorMsg,
                loadType = ExtensionInfo.TYPE_JS_FILE,
                sourcePath = file.absolutePath,
                publicPath = file.absolutePath,
                folderPath = file.absolutePath,
                exception = null
            )
        }




        val errorInfo = ExtensionInfo.InstallError(
            key = key,
            label = map[JS_SOURCE_TAG_LABEL] ?: "",
            pkgName = map[JS_SOURCE_TAG_KEY] ?: "",
            versionName = map[JS_SOURCE_TAG_VERSION_NAME] ?: "",
            versionCode = map[JS_SOURCE_TAG_VERSION_CODE]?.toLongOrNull() ?: -1,
            libVersion = map[JS_SOURCE_TAG_LIB_VERSION]?.toIntOrNull() ?: -1,
            readme = "",
            icon = Icons.Filled.Javascript,
            loadType = ExtensionInfo.TYPE_JS_FILE,
            sourcePath = file.absolutePath,
            publicPath = file.absolutePath,
            folderPath = file.absolutePath,
            exception = null,
            errMsg =  "元数据错误"
        )

        return ExtensionInfo.Installed(
            key = key,
            label = map[JS_SOURCE_TAG_LABEL] ?: return errorInfo,
            pkgName = map[JS_SOURCE_TAG_KEY] ?: return errorInfo,
            versionName = map[JS_SOURCE_TAG_VERSION_NAME] ?: return errorInfo,
            versionCode = map[JS_SOURCE_TAG_VERSION_CODE]?.toLongOrNull() ?: return errorInfo,
            libVersion = map[JS_SOURCE_TAG_LIB_VERSION]?.toIntOrNull() ?: return errorInfo,
            readme = "",
            icon = Icons.Filled.Javascript,
            sources = listOf(JsSource(map, file, jsScope)) ,
            resources = null,
            loadType = ExtensionInfo.TYPE_JS_FILE,
            sourcePath = file.absolutePath,
            publicPath = file.absolutePath,
            folderPath = file.absolutePath,
            extension = null,
        )

    }


}