package com.heyanle.easybangumi4.plugin.js.extension

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Javascript
import com.heyanle.easybangumi4.crash.SourceCrashController
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.loader.AbsExtensionLoader
import com.heyanle.easybangumi4.plugin.extension.loader.ExtensionLoader
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntime
import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntimeProvider
import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.js.source.JsSource
import org.jsoup.Jsoup

/**
 * Created by heyanle on 2024/7/30.
 * https://github.com/heyanLE
 */
class JSExtensionInnerLoader(
    val js: String,
    val jsRuntime: JSRuntimeProvider,
): ExtensionLoader {

    override val key: String
        get() = "js:inner"

    override fun load(): ExtensionInfo? {

        val map = HashMap<String, String>()

        val lineList = js.split("\n")
        for (line in lineList) {
            if (line.isEmpty() || !line.startsWith("//")){
                break
            }
            var firstAtIndex = -1
            var spacerAfterAtIndex = -1

            line.forEachIndexed { index, c ->
                if (firstAtIndex == -1 && c == '@'){
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
        }

        val jsScope = JSScope(jsRuntime.getRuntime())

        val label = map[JSExtensionLoader.JS_SOURCE_TAG_LABEL] ?: ""
        val key = map[JSExtensionLoader.JS_SOURCE_TAG_KEY] ?: ""
        val versionName = map[JSExtensionLoader.JS_SOURCE_TAG_VERSION_NAME] ?: ""
        val versionCode = map[JSExtensionLoader.JS_SOURCE_TAG_VERSION_CODE]?.toLongOrNull() ?: -1
        val libVersion = map[JSExtensionLoader.JS_SOURCE_TAG_LIB_VERSION]?.toIntOrNull() ?: -1


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
                sourcePath = "inner",
                publicPath =  "inner",
                folderPath =  "inner",
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
                sourcePath =  "inner",
                publicPath =  "inner",
                folderPath =  "inner",
                exception = null
            )
        }




        val errorInfo = ExtensionInfo.InstallError(
            key = key,
            label = map[JSExtensionLoader.JS_SOURCE_TAG_LABEL] ?: "",
            pkgName = map[JSExtensionLoader.JS_SOURCE_TAG_KEY] ?: "",
            versionName = map[JSExtensionLoader.JS_SOURCE_TAG_VERSION_NAME] ?: "",
            versionCode = map[JSExtensionLoader.JS_SOURCE_TAG_VERSION_CODE]?.toLongOrNull() ?: -1,
            libVersion = map[JSExtensionLoader.JS_SOURCE_TAG_LIB_VERSION]?.toIntOrNull() ?: -1,
            readme = "",
            icon = Icons.Filled.Javascript,
            loadType = ExtensionInfo.TYPE_JS_FILE,
            sourcePath =  "inner",
            publicPath = "inner",
            folderPath = "inner",
            exception = null,
            errMsg =  "元数据错误"
        )

        return ExtensionInfo.Installed(
            key = key,
            label = map[JSExtensionLoader.JS_SOURCE_TAG_LABEL] ?: return errorInfo,
            pkgName = map[JSExtensionLoader.JS_SOURCE_TAG_KEY] ?: return errorInfo,
            versionName = map[JSExtensionLoader.JS_SOURCE_TAG_VERSION_NAME] ?: return errorInfo,
            versionCode = map[JSExtensionLoader.JS_SOURCE_TAG_VERSION_CODE]?.toLongOrNull() ?: return errorInfo,
            libVersion = map[JSExtensionLoader.JS_SOURCE_TAG_LIB_VERSION]?.toIntOrNull() ?: return errorInfo,
            readme = "",
            icon = Icons.Filled.Javascript,
            sources = listOf(JsSource(map, js, jsScope)) ,
            resources = null,
            loadType = ExtensionInfo.TYPE_JS_FILE,
            sourcePath =  "inner",
            publicPath =  "inner",
            folderPath =  "inner",
            extension = null,
        )

    }

    override fun canLoad(): Boolean {
        return true
    }
}