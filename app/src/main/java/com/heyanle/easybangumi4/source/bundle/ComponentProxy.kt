package com.heyanle.easybangumi4.source.bundle

import com.heyanle.easy_crasher.CrashHandler
import com.heyanle.easybangumi4.base.preferences.mmkv.MMKVPreference
import com.heyanle.easybangumi4.crash.SourceCrashController
import com.heyanle.easybangumi4.source.SourcePreferences
import com.heyanle.easybangumi4.source_api.component.Component
import com.tencent.mmkv.MMKV
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
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