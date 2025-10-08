package com.heyanle.easybangumi4.plugin.js.component

import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.js.utils.JSFunction
import com.heyanle.easybangumi4.plugin.js.utils.jsUnwrap
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import com.heyanle.easybangumi4.source_api.ParserException
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.withResult
import kotlinx.coroutines.Dispatchers
import org.mozilla.javascript.Function
import java.net.URLEncoder
import java.util.ArrayList

/**
 * Created by heyanle on 2024/7/28.
 * https://github.com/heyanLE
 */
class JSSearchComponent(
    private val jsScope: JSScope,
    private val search: JSFunction,
): ComponentWrapper(), SearchComponent, JSBaseComponent {

    companion object {
        const val FUNCTION_NAME_SEARCH = "SearchComponent_search"

        suspend fun of (jsScope: JSScope) : JSSearchComponent ? {
            return jsScope.runWithScope { _, scriptable ->
                val search = scriptable.get(FUNCTION_NAME_SEARCH, scriptable) as? JSFunction
                    ?: return@runWithScope null
                return@runWithScope JSSearchComponent(jsScope, search)
            }
        }
    }


    // 这里因为不支持异步，因此强迫从 0 开始，交给 js 端处理
    override fun getFirstSearchKey(keyword: String): Int {
        URLEncoder.encode(keyword, "utf-8")
        return 0
    }

    override suspend fun search(
        pageKey: Int,
        keyword: String
    ): SourceResult<Pair<Int?, List<CartoonCover>>> {
        return withResult(Dispatchers.IO) {
            return@withResult jsScope.requestRunWithScope { context, scriptable ->
                val res = search.call(
                    context,
                    scriptable,
                    scriptable,
                    arrayOf(
                        pageKey,
                        keyword
                    )
                ).jsUnwrap() as? Pair<*, *>
                if (res == null) {
                    throw ParserException("js parse error")
                }
                val nextKey = res.first as? Int
                val data = res.second as? ArrayList<*> ?: throw ParserException("js parse error")
                if (data.isNotEmpty() && data.first() !is CartoonCover) {
                    throw ParserException("js parse error")
                }
                return@requestRunWithScope nextKey to data.filterIsInstance<CartoonCover>()
            }
        }.apply {
            webProxyManager?.close()
        }
    }

    private var webProxyManager: WebProxyManager? = null

    override fun setWebProxyManager(webProxyManager: WebProxyManager) {
        this.webProxyManager = webProxyManager
    }
}