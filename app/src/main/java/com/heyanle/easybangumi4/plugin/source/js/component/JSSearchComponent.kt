package com.heyanle.easybangumi4.plugin.source.js.component

import com.heyanle.easybangumi4.plugin.source.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.source.js.utils.JSFunction
import com.heyanle.easybangumi4.plugin.source.js.utils.jsUnwrap
import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import com.heyanle.easybangumi4.plugin.api.ParserException
import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.ComponentWrapper
import com.heyanle.easybangumi4.plugin.api.component.SearchNeedWebViewCheckBusinessException
import com.heyanle.easybangumi4.plugin.api.component.NeedWebViewCheckExceptionInner
import com.heyanle.easybangumi4.plugin.api.component.SearchWebViewCheckParam
import com.heyanle.easybangumi4.plugin.api.component.VerificationParam
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.api.component.search.SearchComponent
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCover
import com.heyanle.easybangumi4.plugin.api.withResult
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
                // 璇ュ嚱鏁颁负闈炲繀椤?
                val searchWithCheck = scriptable.get(FUNCTION_NAME_SEARCH_WITH_CHECK, scriptable) as? JSFunction
                return@runWithScope JSSearchComponent(jsScope, search, searchWithCheck)
            }
        }
    }


    // 杩欓噷鍥犱负涓嶆敮鎸佸紓姝ワ紝鍥犳寮鸿揩浠?0 寮€濮嬶紝浜ょ粰 js 绔鐞?
    override fun getFirstSearchKey(keyword: String): Int {
        URLEncoder.encode(keyword, "utf-8")
        return 0
    }

    override suspend fun search(
        pageKey: Int,
        keyword: String,
        verificationResult: VerificationResult,
    ): SourceResult<Pair<Int?, List<CartoonCover>>> {
        val iWebProxy = (verificationResult as? VerificationResult.WebView)?.iWebProxy
            ?: return SourceResult.Error(ParserException("unsupported verification result"), true)
        if (searchWithCheck == null){
            throw ParserException("js parse error")
        }
        return withResult(Dispatchers.IO) {
            return@withResult jsScope.requestRunWithScope { context, scriptable ->
                try {
                    // 灏嗚 webview 琛ュ叆鍒?webProxyManager 涓?
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
                    // 鏈夌偣 hard 锛屼絾鏄笉绠′簡锛屾棫鐗堟湰閮借涓嶇淮鎶や簡
                    // 濡傛灉闇€瑕佽繃楠岃瘉锛岄渶瑕佸厛鎶婇偅涓?WebView 鐨勭敓鍛藉懆鏈熸彁鍗?
                    // 閫掑綊楠岃瘉
                    if (e is WrappedException) {
                        val e = e.wrappedException
                        if (e is NeedWebViewCheckExceptionInner) {
                            webProxyManager?.removeWebProxy(e.iWebProxy)
                            webProxyManager?.close()
                            // 蹇呴』瑕?ParserException 鎵嶄細閫忎紶
                            throw ParserException(message = "need web check", exception = SearchNeedWebViewCheckBusinessException(
                                param = SearchWebViewCheckParam(
                                    key = pageKey,
                                    keyword = keyword,
                                    source = source.key,
                                    iWebProxy = e.iWebProxy,
                                    tips = e.tips
                                ),
                                verificationParam = VerificationParam.WebView(e.iWebProxy, e.tips),
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
                    // 鏈夌偣 hard 锛屼絾鏄笉绠′簡锛屾棫鐗堟湰閮借涓嶇淮鎶や簡
                    // 濡傛灉闇€瑕佽繃楠岃瘉锛岄渶瑕佸厛鎶婇偅涓?WebView 鐨勭敓鍛藉懆鏈熸彁鍗?
                    if (e is WrappedException) {
                        val e = e.wrappedException
                        if (e is NeedWebViewCheckExceptionInner) {
                            webProxyManager?.removeWebProxy(e.iWebProxy)
                            webProxyManager?.close()
                            // 蹇呴』瑕?ParserException 鎵嶄細閫忎紶
                            throw ParserException(message = "need web check", SearchNeedWebViewCheckBusinessException(
                                param = SearchWebViewCheckParam(
                                    key = pageKey,
                                    keyword = keyword,
                                    source = source.key,
                                    iWebProxy = e.iWebProxy,
                                    tips = e.tips
                                ),
                                verificationParam = VerificationParam.WebView(e.iWebProxy, e.tips),
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
suspend fun SearchComponent.searchWithCheck(
    pageKey: Int,
    keyword: String,
    iWebProxy: IWebProxy
): SourceResult<Pair<Int?, List<CartoonCover>>> {
    return search(pageKey, keyword, VerificationResult.WebView(iWebProxy))
}
