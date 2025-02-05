package com.heyanle.easy_bangumi_cm.common.plugin.core.component

import com.heyanle.easy_bangumi_cm.common.plugin.core.EasyPluginConfigProvider
import com.heyanle.easy_bangumi_cm.plugin.api.component.Component
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * 动态代理 Component
 * Created by heyanlin on 2025/2/5.
 */
class ComponentProxyHandler(
    private val component: Component,
    private val componentProxy: EasyPluginConfigProvider.ComponentProxy
): InvocationHandler {

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        method ?: return null
        try {
            componentProxy.onMethodStart(method, *args.orEmpty())
        } catch (e: Throwable){
            e.printStackTrace()
        }

        val result = try {
            method.invoke(component, *args.orEmpty())
        } catch (e: Throwable) {
            throw e
        }
        try {
            componentProxy.onMethodEnd(method, *args.orEmpty())
        } catch (e: Throwable){
            e.printStackTrace()
        }
        return result
    }
}