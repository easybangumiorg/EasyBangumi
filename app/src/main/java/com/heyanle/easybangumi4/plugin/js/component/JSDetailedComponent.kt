package com.heyanle.easybangumi4.plugin.js.component

import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import org.mozilla.javascript.Function

/**
 * Created by heyanle on 2024/7/28.
 * https://github.com/heyanLE
 */
class JSDetailedComponent(
    private val jsScope: JSScope,
    private val getDetailed: Function,
): ComponentWrapper(), DetailedComponent {

    companion object {
        const val FUNCTION_NAME_GET_DETAILED = "DetailedComponent_getDetailed"

        suspend fun of (jsScope: JSScope) : JSDetailedComponent ? {
            return jsScope.runWithScope { _, scriptable ->
                val getDetailed = scriptable.get(FUNCTION_NAME_GET_DETAILED, scriptable) as? Function
                    ?: return@runWithScope null
                return@runWithScope JSDetailedComponent(jsScope, getDetailed)
            }
        }
    }

    override suspend fun getAll(summary: CartoonSummary): SourceResult<Pair<Cartoon, List<PlayLine>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getDetailed(summary: CartoonSummary): SourceResult<Cartoon> {
        TODO("Not yet implemented")
    }

    override suspend fun getPlayLine(summary: CartoonSummary): SourceResult<List<PlayLine>> {
        TODO("Not yet implemented")
    }
}