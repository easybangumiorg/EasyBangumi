package com.heyanle.easy_bangumi_cm.plugin.entity


/**
 * 源清单信息
 * Created by HeYanLe on 2024/12/8 22:21.
 * https://github.com/heyanLE
 */

data class SourceManifest (
    val id: String,
    val type: String,
    val label: String,
    val version: Int,
    val extensionManifest: ExtensionManifest,

    val author: String? = null,
    val description: String? = null,
    // base64/url/assets
    val icon: String? = null,
    val website: String? = null,

    val loadType: Int,      // 加载类型，决定用哪个 Loader 加载
    val sourceUri: String,  // 文件 Uri，含义由 Loader 决定
) {

    companion object {
        const val LOAD_TYPE_JS = 1
        const val LOAD_TYPE_CRY_JS = 2
    }

    val key: String = "${extensionManifest.key}-${id}"
}