package com.heyanle.easy_bangumi_cm.common.plugin.inner.debug.component

import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentWrapper
import com.heyanle.easy_bangumi_cm.plugin.api.component.pref.PrefComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.pref.SourcePreference

/**
 * Created by heyanlin on 2025/2/6.
 */
class DebugPrefComponent : PrefComponent, ComponentWrapper() {

    override suspend fun register(): List<SourcePreference> {
        return listOf()
    }
}