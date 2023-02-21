package com.heyanle.extension_load.model

import android.content.res.AssetManager
import android.graphics.drawable.Drawable
import com.heyanle.bangumi_source_api.api2.Source
import java.lang.Exception

/**
 * Created by HeYanLe on 2023/2/19 16:16.
 * https://github.com/heyanLE
 */
sealed class Extension {
    abstract val label: String
    abstract val pkgName: String
    abstract val versionName: String
    abstract val versionCode: Long
    abstract val libVersion: Long
    abstract val readme: String?
    abstract val icon: Drawable?

    data class Available(
        override val label: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        override val libVersion: Long,
        override val readme: String?,
        override val icon: Drawable?,
    ): Extension()

    data class Installed(
        override val label: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        override val libVersion: Long,
        override val readme: String?,
        override val icon: Drawable?,
        val sources: List<Source>,
        val assetManager: AssetManager?
    ): Extension()

    data class InstallError(
        override val label: String,
        override val pkgName: String,
        override val versionName: String,
        override val versionCode: Long,
        override val libVersion: Long,
        override val readme: String?,
        override val icon: Drawable?,
        val exception: Exception?,
        val errMsg: String,
    ): Extension()

}