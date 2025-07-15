package org.easybangumi.next.shared.source.api.source

import org.easybangumi.next.lib.utils.ResourceOr


/**
 * 源清单信息
 * Created by HeYanLe on 2024/12/8 22:21.
 * https://github.com/heyanLE
 */

enum class SourceType {
    INNER, // 内置源
    JS,  // JS 源
}

data class SourceManifest (
    val key: String,
    val label: ResourceOr,
    val version: Int,
    val author: String? = null,
    val description: String? = null,
    // base64/url/assets/DrawableResources
    val icon: ResourceOr? = null,
    val website: String? = null,
    val map: Map<String, String> = emptyMap(),
    val lastModified: Long, // 最后修改时间，用于判断是否需要重新加载
    val type: SourceType,      // 源类型
    val param: Any? = null,      // 参数，含义由 type 决定
)