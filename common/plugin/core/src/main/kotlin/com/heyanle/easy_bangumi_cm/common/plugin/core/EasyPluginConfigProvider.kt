package com.heyanle.easy_bangumi_cm.common.plugin.core

import com.heyanle.easy_bangumi_cm.common.plugin.core.inner.InnerSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.lang.reflect.Method

/**
 * 整个 plugin 组件的配置，使用 Inject 注入
 * Created by heyanlin on 2025/1/29.
 */
interface EasyPluginConfigProvider {

    object Default: EasyPluginConfigProvider {
        override val componentProxy: ComponentProxy? = null
        override val innerSourceProvider: InnerSourceProvider = object : InnerSourceProvider{
            override fun flowInnerSource(): Flow<List<InnerSource>> {
                return flowOf()
            }
        }
    }

    interface ComponentProxy {
        fun onMethodStart(method: Method, vararg args: Any)
        fun onMethodEnd(method: Method, vararg args: Any)
    }
    // 如果为 null 则不 hook
    val componentProxy: ComponentProxy?

    val innerSourceProvider: InnerSourceProvider

}