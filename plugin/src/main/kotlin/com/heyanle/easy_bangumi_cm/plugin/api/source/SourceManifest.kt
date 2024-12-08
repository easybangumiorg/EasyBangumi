package com.heyanle.easy_bangumi_cm.plugin.api.source


/**
 * Created by HeYanLe on 2024/12/8 22:21.
 * https://github.com/heyanLE
 */

data class SourceManifest (
    val key: String,
    val type: String,
    val label: String,
    val version: Int,

    val author: String?,
    val description: String?,
    // base64/url/assets
    val icon: String?,
    val website: String?,
)