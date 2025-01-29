package com.heyanle.easy_bangumi_cm.common.plugin.core.source

import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentBundle

/**
 * Created by heyanlin on 2024/12/9.
 */
class SourceBundle(
    map: Map<String, ComponentBundle>,
) {
    val map: LinkedHashMap<String, ComponentBundle> = LinkedHashMap(map)
    fun key(key: String) = map[key]
    fun keys() = map.keys
}