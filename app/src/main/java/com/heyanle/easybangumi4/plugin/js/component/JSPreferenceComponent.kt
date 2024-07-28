package com.heyanle.easybangumi4.plugin.js.component

import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.preference.PreferenceComponent
import com.heyanle.easybangumi4.source_api.component.preference.SourcePreference
import org.mozilla.javascript.Function

/**
 * Created by heyanle on 2024/7/28.
 * https://github.com/heyanLE
 */
class JSPreferenceComponent(
    private val jsScope: JSScope,
    private val getPreference: Function,
): ComponentWrapper(), PreferenceComponent {

    companion object {
        const val FUNCTION_NAME_GET_PREFERENCE = "PreferenceComponent_getPreference"

        suspend fun of (jsScope: JSScope) : JSPreferenceComponent ? {
            return jsScope.runWithScope { _, scriptable ->
                val getPreference = scriptable.get(FUNCTION_NAME_GET_PREFERENCE, scriptable) as? Function
                    ?: return@runWithScope null
                return@runWithScope JSPreferenceComponent(jsScope, getPreference)
            }
        }
    }



    override fun register(): List<SourcePreference> {
        TODO("Not yet implemented")
    }
}