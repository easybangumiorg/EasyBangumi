package com.heyanle.easybangumi4.plugin.source.bundle

import com.heyanle.easy_crasher.CrashHandler
import com.heyanle.easybangumi4.base.preferences.mmkv.MMKVPreference
import com.heyanle.easybangumi4.crash.SourceCrashController
import com.heyanle.easybangumi4.plugin.source.SourcePreferences
import com.heyanle.easybangumi4.source_api.component.Component
import com.tencent.mmkv.MMKV
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * hook 源的所有方法，遇到不能闭环的在下次启动时进入安全模式
 * Created by heyanle on 2024/5/31.
 * https://github.com/heyanLE
 */
class ComponentProxy(
    private val component: Component,
): InvocationHandler {

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        method ?: return null
        SourceCrashController.onComponentStart()
        val result = try {
            method.invoke(component, *args.orEmpty())
        }catch (e: Throwable){
            throw e
        }
        SourceCrashController.onComponentEnd()
        return result
    }
}