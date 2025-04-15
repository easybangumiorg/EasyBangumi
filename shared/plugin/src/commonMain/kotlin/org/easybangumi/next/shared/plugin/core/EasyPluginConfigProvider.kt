package org.easybangumi.next.shared.plugin.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.easybangumi.next.shared.plugin.api.source.Source
import org.easybangumi.next.shared.plugin.core.inner.InnerSource
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * 整个 plugin 组件的配置，使用 koin 注入
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

        override val utilsProvider: UtilsProvider
            get() = object : UtilsProvider {
                override fun <T : Any> get(clazz: KClass<T>, source: Source): T? {
                    return null
                }
            }
    }

    interface ComponentProxy {
        fun onMethodStart(method: KFunction<*>, vararg args: Any)
        fun onMethodEnd(method: KFunction<*>, vararg args: Any)
    }
    // 如果为 null 则不 hook
    val componentProxy: ComponentProxy?

    interface InnerSourceProvider {

        fun flowInnerSource(): Flow<List<InnerSource>>

    }

    val innerSourceProvider: InnerSourceProvider

    interface UtilsProvider {
        fun <T: Any> get(clazz: KClass<T>, source: Source): T?
    }

    val utilsProvider: UtilsProvider

}