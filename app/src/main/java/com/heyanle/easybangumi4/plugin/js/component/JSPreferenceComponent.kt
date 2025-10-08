package com.heyanle.easybangumi4.plugin.js.component

import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.js.runtime.JSScopeException
import com.heyanle.easybangumi4.plugin.js.utils.JSFunction
import com.heyanle.easybangumi4.plugin.js.utils.jsUnwrap
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import com.heyanle.easybangumi4.source_api.ParserException
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.preference.PreferenceComponent
import com.heyanle.easybangumi4.source_api.component.preference.SourcePreference
import kotlinx.coroutines.TimeoutCancellationException
import org.mozilla.javascript.Function

/**
 * Created by heyanle on 2024/7/28.
 * https://github.com/heyanLE
 */
class JSPreferenceComponent(
    private val jsScope: JSScope,
    private val getPreference: JSFunction,
): ComponentWrapper(), PreferenceComponent, JSBaseComponent {

    companion object {
        const val FUNCTION_NAME_GET_PREFERENCE = "PreferenceComponent_getPreference"

        suspend fun of (jsScope: JSScope) : JSPreferenceComponent ? {
            return jsScope.runWithScope { _, scriptable ->
                val getPreference = scriptable.get(FUNCTION_NAME_GET_PREFERENCE, scriptable) as? JSFunction
                    ?: return@runWithScope null
                return@runWithScope JSPreferenceComponent(jsScope, getPreference)
            }
        }
    }

    private val preferenceList = arrayListOf<SourcePreference>()

    override suspend fun init() {
        try {
            jsScope.requestRunWithScope(
                5000
            ) { context, scriptable ->
                val res = getPreference.call(
                    context, scriptable, scriptable, arrayOf()
                ).jsUnwrap() as? java.util.ArrayList<*>
                if (res == null) {
                    throw ParserException("js parse error")
                }
                if (res.isNotEmpty() && res.first() !is SourcePreference) {
                    throw ParserException("js parse error")
                }
                preferenceList.clear()
                preferenceList.addAll(
                    res.filterIsInstance<SourcePreference>()
                )
            }
        } catch (e: TimeoutCancellationException) {
            e.printStackTrace()
            throw JSScopeException("${FUNCTION_NAME_GET_PREFERENCE} 方法需要同步返回，暂不支持异步配置项")
        }

    }
    private var webProxyManager: WebProxyManager? = null

    override fun setWebProxyManager(webProxyManager: WebProxyManager) {
        this.webProxyManager = webProxyManager
    }


    override fun register(): List<SourcePreference> {
       return preferenceList
    }
}