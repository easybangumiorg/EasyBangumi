package com.heyanle.easybangumi4.plugin.extension

import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.heyanle.easybangumi4.plugin.extension.provider.JsExtensionProvider
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.utils.getMatchReg
import com.heyanle.extension_api.Extension
import java.io.File

/**
 * Created by heyanlin on 2023/10/24.
 */
sealed class ExtensionInfo {
    abstract val key: String
    abstract val label: String
    abstract val pkgName: String // 对于 js 文件，为 org.easybangumi.js.[key]
    abstract val versionName: String
    abstract val versionCode: Long
    abstract val libVersion: Int
    abstract val readme: String?
    abstract val icon: Any?
    abstract val loadType: Int
    abstract val sourcePath: String // 文件位置 js or apk
    abstract val publicPath: String
    abstract val folderPath: String // 解压路径

    companion object {
        const val TYPE_APK_INSTALL = 0
        const val TYPE_APK_FILE = 1
        const val TYPE_JS_FILE = 2
    }

    fun suffix(): String {
        // TODO 后续再想想怎么优化，ExtensionInfo 应该需要和 Provider 对应比较合理？
        return when(loadType){
            TYPE_APK_INSTALL -> "apk"
            TYPE_APK_FILE -> "apk"
            TYPE_JS_FILE -> {
                if (sourcePath.endsWith(JsExtensionProvider.EXTENSION_CRY_SUFFIX)) {
                    JsExtensionProvider.EXTENSION_CRY_SUFFIX
                } else {
                    JsExtensionProvider.EXTENSION_SUFFIX
                }
            }
            else -> "unknown"
        }
    }

    data class Installed(
        override val key: String,
        override val label: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        override val libVersion: Int,
        override val readme: String?,
        override val icon: Any?,
        override val loadType: Int,
        override val sourcePath: String,
        override val publicPath: String,
        override val folderPath: String,
        val extension: Extension?,
        val sources: List<Source>,
        val resources: Resources?,

    ): ExtensionInfo()

    data class InstallError(
        override val key: String,
        override val label: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        override val libVersion: Int,
        override val readme: String?,
        override val icon: Any?,
        override val loadType: Int,
        override val sourcePath: String,
        override val publicPath: String,
        override val folderPath: String,
        val exception: Throwable?,
        val errMsg: String,
    ): ExtensionInfo()

    fun match(key: String): Boolean {
        var matched = false
        for (match in key.split(',')) {
            val regex = match.getMatchReg()
            if (label.matches(regex)) {
                matched = true
                break
            }
            if (pkgName.matches(regex)) {
                matched = true
                break
            }
            if (libVersion.toString().matches(regex)) {
                matched = true
                break
            }
            if ((readme?:"").matches(regex)) {
                matched = true
                break
            }
        }
        return matched
    }

    val fileName : String by lazy {
        File(sourcePath).name
    }

    val isLoadFromFile : Boolean by lazy {
        loadType == TYPE_JS_FILE || loadType == TYPE_APK_FILE
    }

}
