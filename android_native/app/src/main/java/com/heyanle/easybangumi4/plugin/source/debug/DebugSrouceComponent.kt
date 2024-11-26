package com.heyanle.easybangumi4.plugin.source.debug;

import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.page.PageComponent
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.CartoonCoverImpl
import com.heyanle.easybangumi4.source_api.utils.api.NetworkHelper
import com.heyanle.easybangumi4.source_api.utils.core.SourceUtils
import com.heyanle.easybangumi4.source_api.withResult
import io.ktor.util.valuesOf
import kotlinx.coroutines.Dispatchers
import org.jsoup.Jsoup

class DebugSourceComponent(
    private val networkHelper: NetworkHelper
) : ComponentWrapper(), PageComponent {

    override fun getPages(): List<SourcePage> {
        return listOf(
            SourcePage.Group(
                "首页",
                false,
            ) {
                withResult {
                    listOf(
                        SourcePage.SingleCartoonPage.WithCover(
                            "首页",
                            {1},
                        ) {
                            withResult(Dispatchers.IO) {
                                val doc = Jsoup.connect("https://www.catwcy.com/").userAgent(networkHelper.randomUA).get()
                                val homeCenter = doc.select("div.slide-a.slide-c.rel div.slide-time-list.mySwiper div.swiper-wrapper").first()
                                if (homeCenter == null) {
                                    return@withResult null to emptyList()
                                }
                                var list = arrayListOf<CartoonCover>()
                                val children = homeCenter.children()
                                for (i in 0 until  children.size) {
                                    var item = children.get(i)
                                    if (item == null) {
                                        continue
                                    }
                                    val title = item.select("a div h3").text()
                                    val url =  SourceUtils.urlParser("https://www.catwcy.com", item.select("a").attr("href"))
                                    val array = url.split("/")
                                    val lastIndex = array.size - 1
                                    var id = array[lastIndex]
                                    if (id.endsWith(".html")) {
                                        id = id.substring(0, id.length - 5)
                                    }
                                    val coverPattern = Regex("""(?<=url\().*(?=\))""")
                                    val coverStyle = item.select("a div.slide-time-img3").attr("style")
                                    var cover = coverPattern.find(coverStyle)?.value ?: ""
                                    if (cover.startsWith("'")) {
                                        cover = cover.substring(1)
                                    }
                                    if (cover.endsWith("'")) {
                                        cover = cover.substring(0, cover.length - 1)
                                    }
                                    list.add(CartoonCoverImpl(
                                        id,
                                        "catwcy",
                                        url,
                                        title,
                                        "",
                                        cover
                                    ))
                                }
                                return@withResult null to list

                            }
                        }
                    )
                }
            }
        )
    }
}