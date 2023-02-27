package com.heyanle.extension_load.model

import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.heyanle.bangumi_source_api.api.Source

/**
 * Created by HeYanLe on 2023/2/19 16:16.
 * https://github.com/heyanLE
 */
sealed class Extension {
    abstract val label: String
    abstract val pkgName: String
    abstract val versionName: String
    abstract val versionCode: Long
    abstract val libVersion: Int
    abstract val readme: String?
    abstract val icon: Drawable?

    data class Installed(
        override val label: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        override val libVersion: Int,
        override val readme: String?,
        override val icon: Drawable?,
        val sources: List<Source>,
        val resources: Resources?,
    ): Extension()

    data class InstallError(
        override val label: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        override val libVersion: Int,
        override val readme: String?,
        override val icon: Drawable?,
        val exception: Exception?,
        val errMsg: String,
    ): Extension()

}