package com.heyanle.easy_bangumi_cm.common.plugin.core.inner

import com.heyanle.easy_bangumi_cm.common.plugin.core.EasyPluginConfigProvider
import com.heyanle.easy_bangumi_cm.plugin.api.component.Component
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentBundle
import com.heyanle.easy_bangumi_cm.plugin.api.source.Source
import com.heyanle.lib.inject.core.Inject
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

/**
 * Created by heyanlin on 2025/1/29.
 */
class InnerComponentBundle(
    private val innerSource: InnerSource
): ComponentBundle {

    private val configProvider: EasyPluginConfigProvider by Inject.injectLazy()
    private val bundle: HashMap<KClass<*>, Any> = hashMapOf()
    private val componentProxy:  HashMap<KClass<*>, Any> = hashMapOf()
    private val init = AtomicBoolean(false)

    override fun getSource(): Source {
        return innerSource
    }

    override fun <T : Component> getComponent(clazz: Class<T>): T? {
        TODO("Not yet implemented")
    }

    suspend fun load() {

    }
}