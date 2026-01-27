package org.easybangumi.next.source.inner.age

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.encodeURLPath
import io.ktor.http.parameters
import io.ktor.http.path
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

class AgeSearchComponent: SearchComponent, BaseComponent() {

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
            val host = prefHelper.ageHost()
            logger.info("GGLPlayComponent searchPlayCovers: host=$host, keyword=${keyword}, key=$key")
            val html = ktorClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    this.host = host
                    path("search")
                    parameters.append("query", keyword)
                    parameters.append("page", key)
                }
            }.bodyAsText()
            val doc = Ksoup.parse(html)
            val list = arrayListOf<CartoonCover>()

            doc.select("#cata_video_list > div div.card.cata_video_item").forEach { ro ->
                val it = ro.child(0);
                val uu = it.child(1).child(0).child(0).attr("href")

                val id = uu.split("/").lastOrNull() ?: return@forEach

                var cover = it.select("img.video_thumbs")[0].attr("data-original")
                cover = UrlUtils.parse("https://${host}", cover)

                val detailInfo = it.select("div.card-body").first()
                val titleEle = detailInfo?.select("h5.card-title")?.first()
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
            (if (list.isEmpty()) null else (key.toIntOrNull()?:0) + 1)?.toString() to list.toList()
        }
    }


}