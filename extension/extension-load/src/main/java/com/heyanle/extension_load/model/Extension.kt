package com.heyanle.extension_load.model

import android.graphics.drawable.Drawable
import com.heyanle.bangumi_source_api.api2.Source

/**
 * Created by HeYanLe on 2023/2/19 16:16.
 * https://github.com/heyanLE
 */
class Extension(
    val label: String,
    val pkgName: String,
    val versionName: String,
    val versionCode: Long,
    val libVersion: Long,
    val sources: List<Source>,
    val loadType: Int,
    val readme: String?,
    val icon: Drawable?,
){
    companion object {
        const val TYPE_APP = 0
        const val TYPE_FILE = 1
    }
}