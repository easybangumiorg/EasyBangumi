package org.easybangumi.next.source.inner.xifan

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.*
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
        logger.info("XifanSearchComponentsearch $keyword : $key")
        Exception("test").printStackTrace()
        return search(keyword, key, true)
    }

    override suspend fun searchWithCheck(
        keyword: String,
        key: String,
        web: IWebView
    ): DataState<PagingFrame<CartoonCover>> {
        val res = withResult {
            val host = prefHelper.get("host", "dm.xifanacg.com")
            val content = web.getContent(1000) ?: throw DataStateException("解析错误")
            val doc = Ksoup.parse(content)
            if (doc.select("button.verify-submit").isNotEmpty()) {
                throw NeedWebViewCheckException(
                    WebViewCheckParam(
                        iWebView = web,
                        tips = "请手动输入验证码",
                        check = {
                            val content = it.getContent(1000) ?: return@WebViewCheckParam false
                            val doc = Ksoup.parse(content)
                            if (doc.select("button.verify-submit").isEmpty()
                                && doc.select("body > div.box-width > div div.vod-detail").isNotEmpty()) {
                                return@WebViewCheckParam true
                            }
                            return@WebViewCheckParam false
                        }
                    )
                )
            } else {
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
                // 输入验证码后就不适配频繁操作了
//                if (list.isEmpty()) {
//                    val msgContent = doc.select("div.msg-content").firstOrNull()
//                    if (msgContent?.text()?.contains("频繁操作") == true) {
//                        needRetry = true
//                    }
//                }
                ((if (list.isEmpty()) null else (key.toIntOrNull()?:0) + 1)?.toString() to list.toList())
            }
        }
        return res
    }

    private suspend fun search(
        keyword: String,
        key: String,
        allowedRetry: Boolean
    ): DataState<PagingFrame<CartoonCover>> {
        var needRetry = false
        val res = withResult {
            val host = prefHelper.get("host", "dm.xifanacg.com")
            logger.info("XifanSearchComponent searchPlayCovers: host=$host, keyword=${keyword}, key=$key")
            val url = URLBuilder().apply {
                protocol = URLProtocol.HTTPS
                this.host = host
                path("search", "wd", keyword.encodeURLPath(encodeSlash = true), "page",  "${key}.html" )
            }.toString()
            val iWeb = webViewHelper.newWebView()
            val doc = iWeb.useWithHook {
                it.loadUrl(url, networkHelper.defaultWindowsUA, interceptResRegex = null)
                it.waitingForPageLoaded(2000)
                val content = it.getContent(1000) ?: throw DataStateException("解析错误")
                var doc = Ksoup.parse(content)
                if (doc.select("button.verify-submit").isNotEmpty()) {
                    throw NeedWebViewCheckException(
                        WebViewCheckParam(
                            iWebView = it,
                            tips = "请手动输入验证码",
                            check = {
                                val content = it.getContent(1000) ?: return@WebViewCheckParam false
                                val doc = Ksoup.parse(content)
                                if (doc.select("button.verify-submit").isEmpty()
                                    && doc.select("body > div.box-width > div div.vod-detail").isNotEmpty()) {
                                    return@WebViewCheckParam true
                                }
                                return@WebViewCheckParam false
                            }
                        )
                    )
                } else {
                    val msgContent = doc.select("div.msg-content").firstOrNull()
                    if (msgContent?.text()?.contains("频繁操作") == true) {
                        needRetry = true
                        delay(3000)
                        val content = it.getContent(2000) ?: throw DataStateException("解析错误")
                        doc = Ksoup.parse(content)
                        if (doc.select("button.verify-submit").isNotEmpty()) {
                            throw NeedWebViewCheckException(
                                WebViewCheckParam(
                                    iWebView = it,
                                    tips = "请手动输入验证码",
                                    check = {
                                        val content = it.getContent(1000) ?: return@WebViewCheckParam false
                                        val doc = Ksoup.parse(content)
                                        if (doc.select("button.verify-submit").isEmpty()
                                            && doc.select("body > div.box-width > div div.vod-detail").isNotEmpty()) {
                                            return@WebViewCheckParam true
                                        }
                                        return@WebViewCheckParam false
                                    }
                                )
                            )
                        }
                    }

                    doc
                }
            }
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
            if (list.isEmpty()) {
                val msgContent = doc.select("div.msg-content").firstOrNull()
                if (msgContent?.text()?.contains("频繁操作") == true) {
                    needRetry = true
                }
            }
            ((if (list.isEmpty()) null else (key.toIntOrNull()?:0) + 1)?.toString() to list.toList())
        }
        if (needRetry && allowedRetry) {
            delay(5000)
            return search(keyword, key, false)
        }
        return res
    }
}