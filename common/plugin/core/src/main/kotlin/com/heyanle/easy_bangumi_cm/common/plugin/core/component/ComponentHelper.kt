package com.heyanle.easy_bangumi_cm.common.plugin.core.component

import com.heyanle.easy_bangumi_cm.common.plugin.core.EasyPluginConfigProvider
import com.heyanle.easy_bangumi_cm.plugin.api.component.Component
import com.heyanle.easy_bangumi_cm.plugin.api.component.pref.PrefComponent
import com.heyanle.easy_bangumi_cm.plugin.utils.PreferenceHelper
import java.lang.reflect.Proxy

/**
 * Created by heyanlin on 2025/2/5.
 */
object ComponentHelper {


    fun getComponentProxy(
        component: Component,
        componentProxy: EasyPluginConfigProvider.ComponentProxy
    ): Component {
        return  Proxy.newProxyInstance(component.javaClass.classLoader, component.javaClass.interfaces,
            ComponentProxyHandler(component, componentProxy)
        ) as Component
    }


    fun getPrefComponentWrapper(prefComponent: PrefComponent): PrefComponentWrapper {
        return PrefComponentWrapper(prefComponent)
    }
}