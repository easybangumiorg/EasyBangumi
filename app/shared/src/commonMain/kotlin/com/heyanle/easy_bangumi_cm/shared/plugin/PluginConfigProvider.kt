package com.heyanle.easy_bangumi_cm.shared.plugin

import com.heyanle.easy_bangumi_cm.common.plugin.core.EasyPluginConfigProvider
import com.heyanle.easy_bangumi_cm.common.plugin.core.InnerSourceProvider
import com.heyanle.easy_bangumi_cm.common.plugin.core.inner.InnerSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow


/**
 * Created by HeYanLe on 2025/2/5 22:43.
 * https://github.com/heyanLE
 */

class PluginConfigProvider: EasyPluginConfigProvider {
    override val componentProxy: EasyPluginConfigProvider.ComponentProxy?
        get() = null

    override val innerSourceProvider: InnerSourceProvider = object: InnerSourceProvider {
        override fun flowInnerSource(): Flow<List<InnerSource>> {
            return emptyFlow()
        }
    }
}