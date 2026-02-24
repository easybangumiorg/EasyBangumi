package com.heyanle.easybangumi4.plugin.js.component

import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.js.utils.JSFunction
import com.heyanle.easybangumi4.plugin.js.utils.jsUnwrap
import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import com.heyanle.easybangumi4.source_api.ParserException
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.SearchNeedWebViewCheckBusinessException
import com.heyanle.easybangumi4.source_api.component.NeedWebViewCheckExceptionInner
import com.heyanle.easybangumi4.source_api.component.SearchWebViewCheckParam
import com.heyanle.easybangumi4.source_api.component.search.SearchComponent
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.withResult
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.Dispatchers
import org.mozilla.javascript.WrappedException
import java.net.URLEncoder
import java.util.ArrayList

/**
 * Created by heyanle on 2024/7/28.
 * https://github.com/heyanLE
 */
class JSSearchComponent(
    private val jsScope: JSScope,
    private val search: JSFunction,
    private val searchWithCheck: JSFunction? = null,
): ComponentWrapper(), SearchComponent, JSBaseComponent {

    companion object {
        const val FUNCTION_NAME_SEARCH = "SearchComponent_search"
        const val FUNCTION_NAME_SEARCH_WITH_CHECK = "SearchComponent_searchWithCheck"

        suspend fun of (jsScope: JSScope) : JSSearchComponent ? {
            return jsScope.runWithScope { _, scriptable ->
                val search = scriptable.get(FUNCTION_NAME_SEARCH, scriptable) as? JSFunction
                    ?: return@runWithScope null
                // 该函数为非必须
                val searchWithCheck = scriptable.get(FUNCTION_NAME_SEARCH_WITH_CHECK, scriptable) as? JSFunction
                return@runWithScope JSSearchComponent(jsScope, search, searchWithCheck)
            }
        }
    }


    // 这里因为不支持异步，因此强迫从 0 开始，交给 js 端处理
    override fun getFirstSearchKey(keyword: String): Int {
        URLEncoder.encode(keyword, "utf-8")
        return 0
    }

    override suspend fun searchWithCheck(
        pageKey: Int,
        keyword: String,
        iWebProxy: IWebProxy
    ): SourceResult<Pair<Int?, List<CartoonCover>>> {
        if (searchWithCheck == null){
            throw ParserException("js parse error")
        }
        return withResult(Dispatchers.IO) {
            return@withResult jsScope.requestRunWithScope { context, scriptable ->
                try {
                    // 将该 webview 补入到 webProxyManager 中
                    webProxyManager?.addWebProxy(iWebProxy)
                    val res = search.call(
                        context,
                        scriptable,
                        scriptable,
                        arrayOf(
                            pageKey,
                            keyword,
                            iWebProxy
                        )
                    ).jsUnwrap() as? Pair<*, *>
                    if (res == null) {
                        throw ParserException("js parse error")
                    }
                    val nextKey = res.first?.toString()?.toDoubleOrNull()?.toInt()
                    val data = res.second as? ArrayList<*> ?: throw ParserException("js parse error").apply {
                    }
                    if (data.isNotEmpty() && data.first() !is CartoonCover) {
                        throw ParserException("js parse error")
                    }
                    webProxyManager?.close()
                    nextKey to data.filterIsInstance<CartoonCover>()
                } catch (e: Exception) {
                    // 有点 hard ，但是不管了，旧版本都要不维护了
                    // 如果需要过验证，需要先把那个 WebView 的生命周期提升
                    // 递归验证
                    if (e is WrappedException) {
                        val e = e.wrappedException
                        if (e is NeedWebViewCheckExceptionInner) {
                            webProxyManager?.removeWebProxy(e.iWebProxy)
                            webProxyManager?.close()
                            // 必须要 ParserException 才会透传
                            throw ParserException(message = "need web check", exception = SearchNeedWebViewCheckBusinessException(
                                param = SearchWebViewCheckParam(
                                    key = pageKey,
                                    keyword = keyword,
                                    source = source.key,
                                    iWebProxy = e.iWebProxy,
                                    tips = e.tips
                                )
                            ))
                        }

                    }
                    webProxyManager?.close()
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }

    override suspend fun search(
        pageKey: Int,
        keyword: String
    ): SourceResult<Pair<Int?, List<CartoonCover>>> {
        return withResult(Dispatchers.IO) {
            return@withResult jsScope.requestRunWithScope { context, scriptable ->
                try {
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
                    val nextKey = res.first?.toString()?.toDoubleOrNull()?.toInt()
                    val data = res.second as? ArrayList<*> ?: throw ParserException("js parse error").apply {
                    }
                    if (data.isNotEmpty() && data.first() !is CartoonCover) {
                        throw ParserException("js parse error")
                    }
                    (nextKey to data.filterIsInstance<CartoonCover>()).apply {
                        this.logi("JSSearchComponent")
                    }
                } catch (e: Exception) {
                    // 有点 hard ，但是不管了，旧版本都要不维护了
                    // 如果需要过验证，需要先把那个 WebView 的生命周期提升
                    if (e is WrappedException) {
                        val e = e.wrappedException
                        if (e is NeedWebViewCheckExceptionInner) {
                            webProxyManager?.removeWebProxy(e.iWebProxy)
                            webProxyManager?.close()
                            // 必须要 ParserException 才会透传
                            throw ParserException(message = "need web check", SearchNeedWebViewCheckBusinessException(
                                param = SearchWebViewCheckParam(
                                    key = pageKey,
                                    keyword = keyword,
                                    source = source.key,
                                    iWebProxy = e.iWebProxy,
                                    tips = e.tips
                                )
                            ))
                        }

                    }
                    webProxyManager?.close()
                    e.printStackTrace()
                    throw e
                }

            }
        }
    }

    private var webProxyManager: WebProxyManager? = null

    override fun setWebProxyManager(webProxyManager: WebProxyManager) {
        this.webProxyManager = webProxyManager
    }
}