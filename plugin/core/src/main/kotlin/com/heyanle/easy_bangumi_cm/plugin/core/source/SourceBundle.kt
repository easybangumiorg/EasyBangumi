package com.heyanle.easy_bangumi_cm.plugin.core.source

import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentContainer

/**
 * Created by heyanlin on 2024/12/9.
 */
class SourceBundle(
    private val map: LinkedHashMap<String, ComponentContainer>,
) {
    fun key(key: String) = map[key]
    fun keys() = map.keys
}