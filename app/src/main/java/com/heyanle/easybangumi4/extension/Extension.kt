package com.heyanle.easybangumi4.extension

import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.heyanle.easybangumi4.source_api.Source

/**
 * Created by heyanlin on 2023/10/24.
 */
sealed class Extension {
    abstract val key: String
    abstract val label: String
    abstract val pkgName: String
    abstract val versionName: String
    abstract val versionCode: Long
    abstract val libVersion: Int
    abstract val readme: String?
    abstract val icon: Drawable?
    abstract val loadType: Int
    abstract val sourcePath: String

    companion object {
        const val TYPE_APP = 0
        const val TYPE_FILE = 1
    }

    data class Installed(
        override val key: String,
        override val label: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        override val libVersion: Int,
        override val readme: String?,
        override val icon: Drawable?,
        override val loadType: Int,
        override val sourcePath: String,
        val sources: List<Source>,
        val resources: Resources?,
        val conflictExtension: List<Extension> = emptyList() // 包名重复的拓展，不会最终加载，只是展示出来供用户删除/卸载
    ): Extension()

    data class InstallError(
        override val key: String,
        override val label: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        override val libVersion: Int,
        override val readme: String?,
        override val icon: Drawable?,
        override val loadType: Int,
        override val sourcePath: String,
        val exception: Exception?,
        val errMsg: String,
    ): Extension()

}
