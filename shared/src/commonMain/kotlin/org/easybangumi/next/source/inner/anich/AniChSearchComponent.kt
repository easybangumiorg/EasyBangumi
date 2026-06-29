package org.easybangumi.next.source.inner.anich

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.DataStateException
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.lib.utils.withResult
import org.easybangumi.next.lib.webview.IWebView
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.NeedWebViewCheckException
import org.easybangumi.next.shared.source.api.component.WebViewCheckParam
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.api.utils.NetworkHelper
import org.easybangumi.next.shared.source.api.utils.PreferenceHelper
import org.easybangumi.next.shared.source.api.utils.WebViewHelper
import org.easybangumi.next.shared.source.api.utils.closeFinally
import org.koin.core.component.inject
import kotlin.getValue
import org.easybangumi.next.source.inner.anich.AniChManager

class AniChSearchComponent : SearchComponent, BaseComponent() {

    private val logger = logger()
    private val networkHelper: NetworkHelper by inject()
    private val prefHelper: PreferenceHelper by inject()
    private val webViewHelper: WebViewHelper by inject()
    private val aniChManager: AniChManager by inject()

    override fun firstKey(): String = "1"

    override suspend fun search(
        keyword: String,
        key: String
    ): DataState<PagingFrame<CartoonCover>> {
        logger.info("AniChSearchComponent search start keyword=$keyword key=$key")
        return withResult {
            val result = aniChManager.search(keyword)
            when (result) {
                is DataState.Ok -> {
                    val cartoons = result.data
                    // 分页逻辑
                    val nextKey = if (cartoons.isEmpty()) null else (key.toIntOrNull()?.plus(1))?.toString()
                    nextKey to cartoons
                }
                is DataState.Error -> {
                    throw DataStateException(result.errorMsg ?: "搜索失败")
                }
                else -> {
                    throw DataStateException("搜索请求未完成")
                }
            }
        }
    }

    override suspend fun searchWithCheck(
        keyword: String,
        key: String,
        web: IWebView
    ): DataState<PagingFrame<CartoonCover>> {
        return withResult {
            searchWithWebView(keyword, key, web)
        }
    }

    private suspend fun getHostUrl(): String {
        return prefHelper.get("web_host", "anich.emmmm.eu.org")
    }

    private suspend fun searchWithWebView(
        keyword: String,
        key: String,
        web: IWebView,
    ): PagingFrame<CartoonCover> {
        val host = getHostUrl()
        val url = "https://$host/bangumi/search/$keyword"

        logger.info("AniChSearchComponent loading url: $url")
        web.loadUrl(url)
        web.waitingForPageLoaded(5000L)

        // 检测 Cloudflare 验证
        logger.info("AniChSearchComponent checking captcha for url: $url")
        if (web.checkNeedCaptcha()) {
            logger.warn("AniChSearchComponent captcha required for url: $url")
            throw NeedWebViewCheckException(
                WebViewCheckParam(
                    tips = "请手动完成验证后返回",
                    iWebView = web,
                    check = { !it.checkNeedCaptcha() }
                )
            )
        }

        // 提取 window.$data
        val script = """
            (function() {
                try {
                    var data = window.${'$'}data;
                    var keys = Object.keys(data);
                    for (var i = 0; i < keys.length; i++) {
                        if (keys[i].startsWith('bangumi-home')) {
                            return JSON.stringify(data[keys[i]].data || []);
                        }
                    }
                    return JSON.stringify([]);
                } catch(e) {
                    return JSON.stringify([]);
                }
            })()
        """.trimIndent()

        val jsonData = web.executeJavaScriptWithCallback(script, 5000L) ?: "[]"
        web.closeFinally()

        val results = parseSearchResults(jsonData, host)

        logger.info("AniChSearchComponent search results=${results.size} keyword=$keyword key=$key")

        // 分页逻辑
        val nextKey = if (results.isEmpty()) null else (key.toIntOrNull()?.plus(1))?.toString()
        return nextKey to results
    }

    private fun parseSearchResults(jsonData: String, host: String): List<CartoonCover> {
        return try {
            val items = Json.parseToJsonElement(jsonData).jsonArray
            items.map { item ->
                val obj = item.jsonObject
                CartoonCover(
                    id = obj["id"]?.jsonPrimitive?.content ?: "",
                    source = source.key,
                    name = obj["title"]?.jsonPrimitive?.content ?: "",
                    coverUrl = obj["image"]?.jsonPrimitive?.content ?: "",
                    intro = obj["tagline"]?.jsonPrimitive?.content ?: "",
                    webUrl = "https://$host/b/${obj["id"]?.jsonPrimitive?.content}"
                )
            }.filter { it.id.isNotEmpty() && it.name.isNotEmpty() }
        } catch (e: Exception) {
            logger.error("AniCh parseSearchResults error", e)
            emptyList()
        }
    }

    private suspend fun IWebView.checkNeedCaptcha(): Boolean {
        return try {
            val content = this.getContent(2000) ?: return false
            val hasCaptcha = content.contains("challenge-platform") || content.contains("cf-challenge")
            if (hasCaptcha) {
                logger.warn("AniChSearchComponent detected Cloudflare captcha, content length: ${content.length}")
            }
            hasCaptcha
        } catch (e: Exception) {
            logger.error("AniChSearchComponent checkNeedCaptcha error", e)
            false
        }
    }
}
