package com.heyanle.easybangumi4.plugin.js.component

import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.js.utils.jsUnwrap
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import org.mozilla.javascript.Function

/**
 * Created by heyanle on 2024/7/28.
 * https://github.com/heyanLE
 */
class JSSearchComponent(
    private val jsScope: JSScope,
    private val search: Function,
): ComponentWrapper(), SearchComponent, JSBaseComponent {

    companion object {
        const val FUNCTION_NAME_SEARCH = "SearchComponent_search"

        suspend fun of (jsScope: JSScope) : JSSearchComponent ? {
            return jsScope.runWithScope { _, scriptable ->
                val search = scriptable.get(FUNCTION_NAME_SEARCH, scriptable) as? Function
                    ?: return@runWithScope null
                return@runWithScope JSSearchComponent(jsScope, search)
            }
        }
    }


    // 这里因为不支持异步，因此强迫从 0 开始，交给 js 端处理
    override fun getFirstSearchKey(keyword: String): Int {
        return 0
    }

    override suspend fun search(
        pageKey: Int,
        keyword: String
    ): SourceResult<Pair<Int?, List<CartoonCover>>> {
        jsScope.runWithScope { context, scriptable ->
            val res = search.call(
                context,
                scriptable,
                scriptable,
                arrayOf(
                    pageKey,
                    keyword
                )
            ).jsUnwrap()
            if (res is Map<*, *>) {

            } else if (res is Pair<*, *>) {

            }
        }
        TODO("Not yet implemented")
    }
}