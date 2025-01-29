package com.heyanle.easy_bangumi_cm.common.plugin.core.inner

import com.heyanle.easy_bangumi_cm.plugin.api.component.Component
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentBundle
import com.heyanle.easy_bangumi_cm.plugin.api.source.Source

/**
 * Created by heyanlin on 2025/1/29.
 */
class InnerComponentBundle(
    private val innerSource: InnerSource
): ComponentBundle {

    override fun getSource(): Source {
        return innerSource
    }

    override fun <T : Component> getComponent(clazz: Class<T>): T? {
        TODO("Not yet implemented")
    }

    fun load() {

    }
}