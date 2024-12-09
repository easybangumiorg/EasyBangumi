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

    val author: String? = null,
    val description: String? = null,
    // base64/url/assets
    val icon: String? = null,
    val website: String? = null,
)