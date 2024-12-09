package com.heyanle.easy_bangumi_cm.plugin.api

import com.heyanle.easy_bangumi_cm.plugin.api.source.Source


/**
 * Created by HeYanLe on 2024/12/8 22:25.
 * https://github.com/heyanLE
 */

sealed class ExtensionInfo {

    abstract val key: String
    abstract val label: String

    abstract val versionCode: Long

    abstract val libVersion: Int

    abstract val readme: String?
    abstract val icon: Any?
    abstract val loadType: Int
    abstract val sourcePath: String // 文件位置
    abstract val publicPath: String
    abstract val folderPath: String // 解压路径


    companion object {
        const val TYPE_JS_FILE = 2
        const val TYPE_JS_PKG = 3
    }

    data class Installed(
        override val key: String,
        override val label: String,
        override val versionCode: Long,
        override val libVersion: Int,
        override val readme: String?,
        override val icon: Any?,
        override val loadType: Int,
        override val sourcePath: String,
        override val publicPath: String,
        override val folderPath: String,
        val extensionInfo: ExtensionInfo?,
        val sources: List<Source>,
    ): ExtensionInfo()

    data class InstallError(
        override val key: String,
        override val label: String,
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



}