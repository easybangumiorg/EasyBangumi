package org.easybangumi.next.source.inner.xifan

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.*
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.delay
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.*
import org.easybangumi.next.lib.webview.IWebView
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.NeedWebViewCheckException
import org.easybangumi.next.shared.source.api.component.WebViewCheckParam
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.api.utils.NetworkHelper
import org.easybangumi.next.shared.source.api.utils.PreferenceHelper
import org.easybangumi.next.shared.source.api.utils.WebViewHelper
import org.easybangumi.next.shared.source.api.utils.useWithHook
import org.koin.core.component.inject
import kotlin.math.log


/**
 * Created by HeYanLe on 2025/8/24 15:16.
 * https://github.com/heyanLE
 */

class XifanSearchComponent: SearchComponent, BaseComponent() {

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
        logger.info("XifanSearchComponent search start keyword=$keyword key=$key")
        logger.info("XifanSearchComponent search dispatch allowedRetry=true")
        Exception("test").printStackTrace()
        val res = withResult {
            try {
                // 先使用 ktor 试试能不能用，如果可以就不开 webView 了
                val html = ktorClient.get(getUrl(keyword, key).build()) {
                    userAgent(networkHelper.defaultWindowsUA)
                }.bodyAsText()
                val doc = Ksoup.parse(html)
                val tr = searchWithDoc(keyword, key, doc)
                if (tr.second.isNotEmpty()) {
                    return@withResult tr
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }



            val web = webViewHelper.newWebView()
            web.init(
                userAgent = networkHelper.defaultWindowsUA,
                needBlob = false
            )
            searchWithWebView(keyword, key, web)
        }
        return res
    }

    private suspend fun getHostUrl(): String {
        return prefHelper.get("host", "dm.xifanacg.com")
    }

    private suspend fun getUrl(
        keyword: String,
        key: String,
    ): URLBuilder {
        val host = getHostUrl()
        val url = URLBuilder().apply {
            protocol = URLProtocol.HTTPS
            this.host = host
            path("search", "wd", keyword.encodeURLPath(encodeSlash = true), "page",  "${key}.html" )
        }
        return url;
    }

    override suspend fun searchWithCheck(
        keyword: String,
        key: String,
        web: IWebView
    ): DataState<PagingFrame<CartoonCover>> {
        val res = withResult {
            searchWithWebView(keyword, key, web)
        }
        return res
    }


    private suspend fun searchWithWebView(
        keyword: String,
        key: String,
        web: IWebView
    ):PagingFrame<CartoonCover> {
        val url = getUrl(keyword, key).build().toString()
        val host = getHostUrl()
        web.loadUrl(url)
        web.waitingForPageLoaded(5000)
        // 目前 pc 首次必频繁，没辙了，等一次吧
        web.waitingForFrequencyLimit(url, 3)
        // 需要验证码
        if (web.checkNeedCaptcha()) {
            val param = WebViewCheckParam(
                tips = "请手动输入验证码",
                iWebView = web,
                check = {
                    !it.checkNeedCaptcha()
                }
            )
            throw NeedWebViewCheckException(
                param
            )
        }
        val content = web.getContent(5000) ?: throw DataStateException("解析错误")
        logger.info("XifanSearchComponent searchWithCheck contentLength=${content.length}")
        val doc = Ksoup.parse(content)
        return searchWithDoc(
            keyword = keyword,
            key = key,
            doc = doc,
        )
    }

    private suspend fun searchWithDoc(
        keyword: String,
        key: String,
        doc: Document
    ):PagingFrame<CartoonCover> {
        val host = getHostUrl()
        val list = arrayListOf<CartoonCover>()
        doc.select("body > div.box-width > div div.vod-detail").forEach { ro ->
            val it = ro.child(0);
            val uu = it.child(1).child(0).attr("href")
            val id = uu.subSequence(9, uu.length - 5).toString()

            var cover = it.select("img.gen-movie-img")[0].attr("data-src")
            cover = UrlUtils.parse("https://${host}", cover)

            val detailInfo = it.select("div.detail-info").first()
            val titleEle = detailInfo?.select("h3.slide-info-title")?.first()
            var title = "";
            if (titleEle != null) {
                title = titleEle.text();
            }
            val b = CartoonCover(
                id = id,
                source = source.key,
                name = title,
                coverUrl = cover,
                intro = "",
                webUrl = UrlUtils.parse("https://${host}", uu)
            )
            list.add(b)
        }
        logger.info("XifanSearchComponent searchWithCheck parsedCount=${list.size} keyword=$keyword key=$key")
        return ((if (list.isEmpty()) null else (key.toIntOrNull()?:0) + 1)?.toString() to list.toList())
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
                logger.info("XifanSearchComponent waitingForFrequencyLimit retryCount=$retryCount")
                retryCount++
                delay(3000)
                // 重新刷新
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
        if (doc.select("button.verify-submit").isNotEmpty()) {
            logger.info("XifanSearchComponent checkNeedCaptcha captchaRequired")
            return true
        }
        return false
    }

}