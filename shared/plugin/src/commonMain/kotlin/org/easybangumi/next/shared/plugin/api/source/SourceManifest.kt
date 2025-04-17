package org.easybangumi.next.shared.plugin.api.source

import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.plugin.api.extension.ExtensionManifest


/**
 * 源清单信息
 * Created by HeYanLe on 2024/12/8 22:21.
 * https://github.com/heyanLE
 */

data class SourceManifest (
    val id: String,
    val type: String,
    val label: ResourceOr,
    val version: Int,
    val extensionManifest: ExtensionManifest,

    val author: String? = null,
    val description: String? = null,
    // base64/url/assets/DrawableResources
    val icon: ResourceOr? = null,
    val website: String? = null,
    val map: Map<String, String> = emptyMap(),

    val loadType: Int,      // 加载类型，决定用哪个 Loader 加载
    val sourceUri: UFD?,  // 文件 Uri，含义由 Loader 决定

    val lastModified: Long, // 最后修改时间，用于判断是否需要重新加载
) {

    companion object {
        const val LOAD_TYPE_INNER = 0
        const val LOAD_TYPE_JS = 1
        const val LOAD_TYPE_CRY_JS = 2
    }

    val key: String = "${extensionManifest.key}-${id}"
}