package com.heyanle.easybangumi.source.yhdm

import com.heyanle.easybangumi.EasyApplication
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.entity.Bangumi
import com.heyanle.easybangumi.entity.BangumiDetail
import com.heyanle.easybangumi.source.*
import com.heyanle.easybangumi.source.bimibimi.BimibimiParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * Created by HeYanLe on 2021/9/21 22:29.
 * https://github.com/heyanLE
 */
class YhdmParser: IParser, IHomeParser, IBangumiDetailParser, IPlayUrlParser, ISearchParser {
    override fun getKey(): String {
        return "yhdm"
    }

    override fun getLabel(): String {
        return "樱花动漫"
    }

    companion object{
        const val ROOT_URL = "http://www.yhdm.so"
    }

    private fun url(source: String): String{
        return when {
            source.startsWith("http") -> {
                source
            }
            source.startsWith("/") -> {
                ROOT_URL + source
            }
            else -> {
                "$ROOT_URL/$source"
            }
        }
    }

    override suspend fun home(): LinkedHashMap<String, List<Bangumi>> {
        return withContext(Dispatchers.IO){
            val map = LinkedHashMap<String, List<Bangumi>>()
            kotlin.runCatching {
                val doc = Jsoup.connect(ROOT_URL).timeout(10000)
                    .userAgent(EasyApplication.INSTANCE.getString(R.string.UA))
                    .get()

                val children = doc.select("div.firs.l")[0].children().iterator()
                while(children.hasNext()){
                    val title = children.next()
                    val child = children.next()
                    val titleString = title.child(0).text()
                    val list = arrayListOf<Bangumi>()
                    child.child(0).children().forEach {
                        val cover = url(it.child(0).child(0).attr("src"))
                        val name = it.child(1).text()
                        val detailUrl = url(it.child(0).attr("href"))
                        val intro = it.child(2).text()
                        val bangumi = Bangumi(
                            id = "${getLabel()}-$detailUrl",
                            source = getKey(),
                            name = name,
                            cover = cover,
                            intro = intro,
                            detailUrl = detailUrl,
                            visitTime = System.currentTimeMillis()
                        )
                        list.add(bangumi)

                    }
                    map[titleString] = list
                }

            }.onFailure {
                map.clear()
                it.printStackTrace()
            }

            return@withContext map
        }
    }

    private var bangumi:Bangumi ? = null
    private val temp: ArrayList<String> = arrayListOf()

    override suspend fun detail(bangumi: Bangumi): BangumiDetail? {

        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val doc = Jsoup.connect(bangumi.detailUrl).timeout(10000)
                    .userAgent(EasyApplication.INSTANCE.getString(R.string.UA))
                    .get()
                val description = doc.getElementsByClass("info")[0].text()
                return@withContext BangumiDetail(
                    id = bangumi.id,
                    source = getKey(),
                    name = bangumi.name,
                    cover = bangumi.cover,
                    intro = bangumi.intro,
                    detailUrl = bangumi.detailUrl,
                    description = description
                )
            }.onFailure {
                it.printStackTrace()
            }
            null
        }
    }

    override suspend fun getBangumiPlaySource(bangumi: Bangumi): LinkedHashMap<String, List<String>> {
        temp.clear()
        return withContext(Dispatchers.IO) {
            val map = LinkedHashMap<String, List<String>>()
            kotlin.runCatching {
                val doc = Jsoup.connect(bangumi.detailUrl).timeout(10000)
                    .userAgent(EasyApplication.INSTANCE.getString(R.string.UA))
                    .get()
                val sourceDiv = doc.getElementsByClass("movurl")[0].child(0)
                val list = arrayListOf<String>()
                sourceDiv.children().forEach {
                    list.add(it.text())
                    temp.add(it.child(0).attr("href"))
                }
                map.put("播放列表", list)
            }.onFailure {
                it.printStackTrace()
            }
            this@YhdmParser.bangumi = bangumi
            map
        }
    }

    override suspend fun getBangumiPlayUrl(
        bangumi: Bangumi,
        lineIndex: Int,
        episodes: Int
    ): String {
        var detailUrl = ""
        if(this.bangumi == bangumi){
            detailUrl = temp[episodes]
        }else{
            detailUrl = withContext(Dispatchers.IO) {
                var result = ""
                kotlin.runCatching {
                    val doc = Jsoup.connect(bangumi.detailUrl).timeout(10000)
                        .userAgent(EasyApplication.INSTANCE.getString(R.string.UA))
                        .get()
                    val sourceDiv = doc.getElementsByClass("movurl")[0].child(0)
                    val list = arrayListOf<String>()
                    result = sourceDiv.child(episodes).child(0).attr("href")

                }.onFailure {
                    it.printStackTrace()
                }
                result
            }
        }
        if(detailUrl.isEmpty()){
            return detailUrl
        }
        return withContext(Dispatchers.IO) {
            var result = ""
            kotlin.runCatching {
                val doc = Jsoup.connect(url(detailUrl)).timeout(10000)
                    .userAgent(EasyApplication.INSTANCE.getString(R.string.UA))
                    .get()
                result = doc.select("div.area div.bofang div#playbox")[0].attr("data-vid").split("$")[0]
            }.onFailure {
                it.printStackTrace()
                result = ""
            }
            result
        }

    }

    override fun getFirstPage(): Int {
        return 1
    }

    override suspend fun search(keyword: String, page: Int): ISearchParser.BangumiPageResult {
        var res = ISearchParser.BangumiPageResult(null, false, emptyList())
        withContext(Dispatchers.IO){
            val url = url("/search/$keyword?page=$page")
            kotlin.runCatching {
                val doc = Jsoup.connect(url).timeout(10000)
                    .userAgent(EasyApplication.INSTANCE.getString(R.string.UA))
                    .get()
                val r = arrayListOf<Bangumi>()
                doc.select("div.fire.l div.lpic ul li").forEach {
                    val detailUrl = url(it.child(0).attr("href"))
                    val b = Bangumi(
                        id = "${getLabel()}-$detailUrl",
                        name = it.child(1).text(),
                        detailUrl = detailUrl,
                        intro = it.child(2).text(),
                        cover = url(it.child(0).child(0).attr("src")),
                        visitTime = System.currentTimeMillis(),
                        source = getKey(),
                    )
                    r.add(b)
                }
                val pages = doc.select("div.pages")
                res = if(pages.isEmpty()){
                    ISearchParser.BangumiPageResult(null, true, r)
                }else{
                    val p = pages.select("a#lastn")
                    if(p.isEmpty()){
                        ISearchParser.BangumiPageResult(null, true, r)
                    }else{
                        ISearchParser.BangumiPageResult(page+1, true, r)
                    }
                }
            }.onFailure {
                it.printStackTrace()
                throw it
            }
        }
        return res
    }
}