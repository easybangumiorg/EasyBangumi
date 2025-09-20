package org.easybangumi.next.source.inner.ggl

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.path
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.lib.utils.UrlUtils
import org.easybangumi.next.lib.utils.withResult
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
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
        return withResult {
            val host = prefHelper.get("host", "bgm.girigirilove.com")
            logger.info("GGLPlayComponent searchPlayCovers: host=$host, keyword=${keyword}, key=$key")
            val html = ktorClient.get {
                url {
                    this.host = host
                    path("search", "${keyword}----------${key}---" )
                }
            }.bodyAsText()
            val doc = Ksoup.parse(html)
            val list = arrayListOf<CartoonCover>()

            doc.select("div div.public-list-box.search-box").forEach {
                val uu = it.child(1).child(0).attr("href")
                val id = uu.subSequence(1, uu.length - 1).toString()

                val coverStyle = it.select("div.cover")[0].attr("style")
                val coverPattern = Regex("""(?<=url\().*(?=\))""")
                var cover = coverPattern.find(coverStyle)?.value ?: ""
                cover = UrlUtils.parse("https://${host}", cover)

                val title = it.select("div.thumb-content div.thumb-txt").first()?.text() ?: ""
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
            (if (list.isEmpty()) null else (key.toIntOrNull()?:0) + 1)?.toString() to list
        }
    }
}