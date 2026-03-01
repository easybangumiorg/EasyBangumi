package org.easybangumi.next.source.inner.ggl

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.path
import kotlinx.coroutines.delay
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.DataStateException
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.lib.utils.UrlUtils
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
import org.koin.core.component.inject
import kotlin.getValue


/**
 * Created by HeYanLe on 2025/8/24 15:16.
 * https://github.com/heyanLE
 */

class GGLSearchComponent: SearchComponent, BaseComponent() {

    private val logger = logger()

    private val ktorClient: HttpClient by inject()
    private val networkHelper: NetworkHelper by inject()
    private val prefHelper: PreferenceHelper by inject()
    private val webViewHelper: WebViewHelper by inject()

    override fun firstKey(): String {
        return "1"
    }

    override suspend fun search(
        keyword: String,
        key: String
    ): DataState<PagingFrame<CartoonCover>> {
        logger.info("GGLSearchComponent search start keyword=$keyword key=$key")
        return withResult {
            try {
                val html = ktorClient.get(getUrl(keyword, key).build()) {
                    headers.append(HttpHeaders.UserAgent, networkHelper.defaultWindowsUA)
                }.bodyAsText()
                val doc = Ksoup.parse(html)
                val result = searchWithDoc(keyword, key, doc)
                if (result.second.isNotEmpty()) {
                    return@withResult result
                }
            } catch (e: Exception) {
                logger.warn("GGLSearchComponent search by ktor failed, fallback to webview", e)
            }

            val web = webViewHelper.newWebView()
            web.init(
                userAgent = networkHelper.defaultWindowsUA,
                needBlob = false
            )
            searchWithWebView(keyword, key, web)
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
        return prefHelper.get("host", "bgm.girigirilove.com")
    }

    private suspend fun getUrl(
        keyword: String,
        key: String,
    ): URLBuilder {
        val host = getHostUrl()
        return URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            this.host = host
            path("search", "${keyword}----------${key}---")
        }
    }

    private suspend fun searchWithWebView(
        keyword: String,
        key: String,
        web: IWebView,
    ): PagingFrame<CartoonCover> {
        val url = getUrl(keyword, key).build().toString()
        web.loadUrl(url)
        web.waitingForPageLoaded(5000)
        web.waitingForFrequencyLimit(url, 3)
        if (web.checkNeedCaptcha()) {
            throw NeedWebViewCheckException(
                WebViewCheckParam(
                    tips = "请手动输入验证码后返回",
                    iWebView = web,
                    check = {
                        !it.checkNeedCaptcha()
                    }
                )
            )
        }
        val content = web.getContent(5000) ?: throw DataStateException("解析错误")
        val doc = Ksoup.parse(content)
        return searchWithDoc(keyword, key, doc)
    }

    private suspend fun searchWithDoc(
        keyword: String,
        key: String,
        doc: Document,
    ): PagingFrame<CartoonCover> {
        val host = getHostUrl()
        val list = arrayListOf<CartoonCover>()
        doc.select("div.box-width div.row div.search-list.vod-detail").forEach { ro ->
            val item = ro.child(0)
            val webPath = item.child(1).child(0).attr("href")
            if (webPath.length <= 2) {
                return@forEach
            }

            val id = webPath.subSequence(1, webPath.length - 1).toString()

            var cover = item.select("img.gen-movie-img").firstOrNull()?.attr("data-src").orEmpty()
            cover = UrlUtils.parse("https://$host", cover)

            val detailInfo = item.select("div.detail-info").first()
            val title = detailInfo?.select("h3.slide-info-title")?.firstOrNull()?.text().orEmpty()

            list.add(
                CartoonCover(
                    id = id,
                    source = source.key,
                    name = title,
                    coverUrl = cover,
                    intro = "",
                    webUrl = UrlUtils.parse("https://$host", webPath)
                )
            )
        }

        logger.info("GGLSearchComponent searchWithDoc parsedCount=${list.size} keyword=$keyword key=$key")
        return ((if (list.isEmpty()) null else (key.toIntOrNull() ?: 0) + 1)?.toString() to list.toList())
    }

    private suspend fun IWebView.waitingForFrequencyLimit(
        url: String,
        maxRetryCount: Int,
    ): Boolean {
        var retryCount = 0
        while (retryCount < maxRetryCount) {
            val content = this.getContent(2000) ?: return false
            val doc = Ksoup.parse(content)
            val msgContent = doc.select("div.msg-content").firstOrNull()
            if (msgContent?.text()?.contains("频繁操作") == true) {
                retryCount++
                logger.info("GGLSearchComponent waitingForFrequencyLimit retryCount=$retryCount")
                delay(3000)
                this.loadUrl(url)
                this.waitingForPageLoaded(1000)
            } else {
                return true
            }
        }
        return false
    }

    private suspend fun IWebView.checkNeedCaptcha(): Boolean {
        val content = this.getContent(2000) ?: return false
        val doc = Ksoup.parse(content)
        val hasCaptcha = doc.select("button.verify-submit").isNotEmpty()
        if (hasCaptcha) {
            logger.info("GGLSearchComponent checkNeedCaptcha captchaRequired")
        }
        return hasCaptcha
    }
}
