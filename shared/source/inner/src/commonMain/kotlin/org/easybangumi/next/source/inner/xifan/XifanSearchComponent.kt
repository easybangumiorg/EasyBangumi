package org.easybangumi.next.source.inner.xifan

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.encodeURLPath
import io.ktor.http.path
import io.ktor.http.userAgent
import kotlinx.coroutines.delay
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.lib.utils.UrlUtils
import org.easybangumi.next.lib.utils.withResult
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.source.api.component.BaseComponent
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
        return search(keyword, key, true)
    }

    private suspend fun search(
        keyword: String,
        key: String,
        allowedRetry: Boolean
    ): DataState<PagingFrame<CartoonCover>> {
        var needRetry = false
        val res = withResult {
            val host = prefHelper.get("host", "dm.xifanacg.com")
            logger.info("GGLPlayComponent searchPlayCovers: host=$host, keyword=${keyword}, key=$key")
            val html = ktorClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    this.host = host
                    path("search", "wd", keyword.encodeURLPath(encodeSlash = true), "page",  "${key}.html" )
                }
                userAgent(networkHelper.defaultWindowsUA)
            }.bodyAsText()
            val doc = Ksoup.parse(html)
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
                .apply {
                    logger.info(" ${key} ${keyword} SearchPlayCovers: $this")
                    if (this.second.isEmpty()) {
                        logger.warn("SearchPlayCovers: $html")
                    }
                }
        }
        if (needRetry && allowedRetry) {
            delay(3000)
            return search(keyword, key, false)
        }
        return res
    }
}