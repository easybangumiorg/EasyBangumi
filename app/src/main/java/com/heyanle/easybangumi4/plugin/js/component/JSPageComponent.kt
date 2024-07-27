package com.heyanle.easybangumi4.plugin.js.component

import androidx.annotation.WorkerThread
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.Context as JSContext

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class JSPageComponent: ComponentWrapper(), PageComponent {


    companion object {

        const val CheckJsCode = """
            typeof PageComponent_getMainTabs == 'function' &&
            typeof PageComponent_getSubTabs == 'function' &&
            typeof PageComponent_initPageKey == 'function' &&
            typeof PageComponent_getContent == 'function' 
        """

        @WorkerThread
        fun check( jsContext: JSContext, scope: ScriptableObject) {
            jsContext.evaluateString(scope, CheckJsCode, null, 1, null)
        }
    }

    override fun getPages(): List<SourcePage> {
        TODO("Not yet implemented")
    }
}