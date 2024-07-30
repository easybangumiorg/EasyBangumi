package com.heyanle.easybangumi4.plugin.js.component

import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.js.utils.JSFunction
import com.heyanle.easybangumi4.plugin.js.utils.jsUnwrap
import com.heyanle.easybangumi4.source_api.ParserException
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import org.mozilla.javascript.Function

/**
 * Created by heyanle on 2024/7/28.
 * https://github.com/heyanLE
 */
class JSDetailedComponent(
    private val jsScope: JSScope,
    private val getDetailed: JSFunction,
): ComponentWrapper(), DetailedComponent, JSBaseComponent {

    companion object {
        const val FUNCTION_NAME_GET_DETAILED = "DetailedComponent_getDetailed"

        suspend fun of (jsScope: JSScope) : JSDetailedComponent ? {
            return jsScope.runWithScope { _, scriptable ->
                val getDetailed = scriptable.get(FUNCTION_NAME_GET_DETAILED, scriptable) as? JSFunction
                    ?: return@runWithScope null
                return@runWithScope JSDetailedComponent(jsScope, getDetailed)
            }
        }
    }

    override suspend fun getAll(summary: CartoonSummary): SourceResult<Pair<Cartoon, List<PlayLine>>> {
        return withResult(Dispatchers.IO) {
            jsScope.requestRunWithScope { context, scriptable ->
                val res = getDetailed.call(
                    context, scriptable, scriptable,
                    arrayOf(summary)
                ).jsUnwrap() as? Pair<*, *>
                if (res == null) {
                    throw ParserException("js parse error")
                }
                val cartoon = res.first as? Cartoon ?: throw ParserException("js parse error")
                val playLineList = res.second as? java.util.ArrayList<*> ?: throw ParserException("js parse error")
                if (playLineList.isNotEmpty() && playLineList.first() !is PlayLine) {
                    throw ParserException("js parse error")
                }
                return@requestRunWithScope cartoon to playLineList.filterIsInstance<PlayLine>()
            }
        }
    }

    override suspend fun getDetailed(summary: CartoonSummary): SourceResult<Cartoon> {
        return getAll(summary).map { it.first }
    }

    override suspend fun getPlayLine(summary: CartoonSummary): SourceResult<List<PlayLine>> {
        return getAll(summary).map { it.second }
    }

    private  fun <T, R> SourceResult<T>.map(transform: (T)->R): SourceResult<R> {
        return when (this) {
            is SourceResult.Complete -> {
                SourceResult.Complete<R>(transform(this.data))
            }
            is SourceResult.Error -> {
                SourceResult.Error(throwable, isParserError)
            }
        }
    }
}