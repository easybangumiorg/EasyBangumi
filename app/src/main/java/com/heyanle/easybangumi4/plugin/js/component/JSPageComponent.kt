package com.heyanle.easybangumi4.plugin.js.component

import androidx.annotation.WorkerThread
import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import org.mozilla.javascript.Function
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.Context as JSContext

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class JSPageComponent(
    private val jsScope: JSScope,
    private val getMainTabs: Function,
    private val getSubTabs: Function,
    private val initPageKey: Function,
    private val getContent: Function,
): ComponentWrapper(), PageComponent {


    companion object {


        const val FUNCTION_NAME_GET_MAIN_TABS = "PageComponent_getMainTabs"
        const val FUNCTION_NAME_GET_SUB_TABS = "PageComponent_getSubTabs"
        const val FUNCTION_NAME_INIT_PAGE_KEY = "PageComponent_initPageKey"
        const val FUNCTION_NAME_GET_CONTENT = "PageComponent_getContent"

        suspend fun of (jsScope: JSScope) : JSPageComponent ? {
            return jsScope.runWithScope { _, scriptable ->
                val getMainTabs = scriptable.get(FUNCTION_NAME_GET_MAIN_TABS, scriptable) as? Function
                val getSubTabs = scriptable.get(FUNCTION_NAME_GET_SUB_TABS, scriptable) as? Function
                val initPageKey = scriptable.get(FUNCTION_NAME_INIT_PAGE_KEY, scriptable) as? Function
                val getContent = scriptable.get(FUNCTION_NAME_GET_CONTENT, scriptable) as? Function
                if(getMainTabs == null || getSubTabs == null || initPageKey == null || getContent == null){
                    return@runWithScope null
                }
                return@runWithScope JSPageComponent(jsScope, getMainTabs, getSubTabs, initPageKey, getContent)
            }
        }
    }

    override fun getPages(): List<SourcePage> {
        TODO("Not yet implemented")
    }
}