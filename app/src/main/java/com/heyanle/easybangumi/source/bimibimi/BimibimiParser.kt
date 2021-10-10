package com.heyanle.easybangumi.source.bimibimi

import com.heyanle.easybangumi.EasyApplication
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.entity.Bangumi
import com.heyanle.easybangumi.entity.BangumiDetail
import com.heyanle.easybangumi.source.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * Created by HeYanLe on 2021/9/20 21:48.
 * https://github.com/heyanLE
 */
class BimibimiParser: IParser, IHomeParser, IBangumiDetailParser, IPlayUrlParser, ISearchParser {

    companion object{
        const val ROOT_URL = "http://bimiacg2.net"
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

    override fun getKey(): String {
        return "Bimibimi"
    }

    override fun getLabel(): String {
        return "Bimibimi"
    }

    override suspend fun home(): LinkedHashMap<String, List<Bangumi>> {
        return withContext(Dispatchers.IO){
            val map = LinkedHashMap<String, List<Bangumi>>()
            kotlin.runCatching {
                val doc = Jsoup.connect(ROOT_URL).timeout(10000)
                    .userAgent(EasyApplication.INSTANCE.getString(R.string.UA))
                    .get()
                val elements = doc.getElementsByClass("area-cont")

                fun load(element:Element){
                    val columnTitle = element.getElementsByClass("title")[0].child(1).text()
                    val uls = element.getElementsByClass("tab-cont")
                    val list = arrayListOf<Bangumi>()
                    val ul = uls[0]
                    ul.children().forEach { ele ->
                        val detailUrl =  url(ele.child(0).attr("href"))
                        val imgUrl = url(ele.getElementsByTag("img")[0].attr("src"))
                        val title = ele.child(1).child(0).text()
                        val intro = ele.child(1).child(1).text()
                        val bangumi = Bangumi(
                            id = "${getLabel()}-$detailUrl",
                            source = getKey(),
                            name = title,
                            cover = imgUrl,
                            intro = intro,
                            detailUrl = detailUrl,
                            visitTime = System.currentTimeMillis()
                        )
                        list.add(bangumi)
                    }
                    map[columnTitle] = list

                }

                // 今日热播
                load(elements[0])

                // 新番放送
                load(elements[1])

                // 大陆动漫
                load(elements[2])

                // 番组计划
                load(elements[3])

                // 剧场动画
                load(elements[4])




            }.onFailure {
                it.printStackTrace()
            }

            return@withContext map
        }

    }

    override suspend fun detail(bangumi: Bangumi): BangumiDetail? {
        return withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val doc = Jsoup.connect(bangumi.detailUrl).timeout(10000)
                    .userAgent(EasyApplication.INSTANCE.getString(R.string.UA))
                    .get()
                val description = doc.getElementsByClass("vod-jianjie")[0].text()
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

    private var bangumi:Bangumi? = null
    private val temp: ArrayList<ArrayList<String>> = arrayListOf()

    override suspend fun getBangumiPlaySource(bangumi: Bangumi): LinkedHashMap<String, List<String>> {
        this@BimibimiParser.temp.clear()
        return withContext(Dispatchers.IO) {
            val map = LinkedHashMap<String, List<String>>()

            kotlin.runCatching {
                val doc = Jsoup.connect(bangumi.detailUrl).timeout(10000)
                    .userAgent(EasyApplication.INSTANCE.getString(R.string.UA))
                    .get()
                val sourceDiv = doc.getElementsByClass("play_source_tab")[0]
                val ite = sourceDiv.getElementsByTag("a").iterator()
                val playBoxIte = doc.getElementsByClass("play_box").iterator()
                while(ite.hasNext() && playBoxIte.hasNext()){
                    val sourceA = ite.next()
                    val list = arrayListOf<String>()
                    val urlList = arrayListOf<String>()

                    val playBox = playBoxIte.next()
                    playBox.getElementsByTag("a").forEach {
                        list.add(it.text())
                        urlList.add(url(it.attr("href")))
                    }

                    map[sourceA.text()] = list
                    this@BimibimiParser.temp.add(urlList)
                }
            }.onFailure {
                it.printStackTrace()
            }
            this@BimibimiParser.bangumi = bangumi
            map
        }
    }

    override suspend fun getBangumiPlayUrl(
        bangumi: Bangumi,
        lineIndex: Int,
        episodes: Int
    ): String {

        var detailUrl = if(this.bangumi == bangumi){
            temp[lineIndex][episodes]
        }else ""
        if(detailUrl.isEmpty()){
            kotlin.runCatching {
                val doc = Jsoup.connect(bangumi.detailUrl).timeout(10000)
                    .userAgent(EasyApplication.INSTANCE.getString(R.string.UA))
                    .get()
                val playBox = doc.getElementsByClass("play_box")[lineIndex]
                playBox.getElementsByTag("a")[episodes].let {
                    detailUrl = url(it.attr("href"))
                }

            }.onFailure {
                it.printStackTrace()
            }
        }

        if(detailUrl.isEmpty()){
            return detailUrl
        }

        return withContext(Dispatchers.IO) {
            var result = ""
            kotlin.runCatching {
                val doc = Jsoup.connect(detailUrl).timeout(10000)
                    .userAgent(EasyApplication.INSTANCE.getString(R.string.UA))
                    .get()


                val jsonData = doc.getElementById("video").toString().run {
                    substring(indexOf("{"), lastIndexOf("}") + 1)
                }
                val jsonObject = JSONObject(jsonData)
                val jsonUrl = jsonObject.getString("url")

                if (jsonUrl.contains("http")) {
                    result = jsonUrl
                } else {
                    var from = jsonObject.getString("from")
                    from = when (from) {
                        "wei" -> {
                            "wy"
                        }
                        "ksyun" -> {
                            "ksyun"
                        }
                        else -> {
                            "play"
                        }
                    }
                    val videoHtmlUrl = "$ROOT_URL/static/danmu/$from.php?url=$jsonUrl&myurl=$detailUrl"
                    val d = Jsoup.connect(videoHtmlUrl).timeout(10000)
                        .userAgent(EasyApplication.INSTANCE.getString(R.string.UA))
                        .get()
                    result = d.select("video#video source")[0].attr("src")

                }

            }.onFailure {
                result = ""
                it.printStackTrace()
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
            val url = url("/vod/search/wd/$keyword/page/$page")

            runCatching {
                val doc = Jsoup.connect(url).timeout(10000)
                    .userAgent(EasyApplication.INSTANCE.getString(R.string.UA))
                    .get()
                val r = arrayListOf<Bangumi>()
                doc.select("div.v_tb ul.drama-module.clearfix.tab-cont li.item").forEach {
                    val detailUrl = url(it.child(0).attr("href"))
                    val b = Bangumi(
                        id = "${getLabel()}-$detailUrl",
                        name = it.child(1).child(0).text(),
                        detailUrl = detailUrl,
                        intro = it.child(1).child(1).text(),
                        cover = url(it.child(0).child(0).attr("src")),
                        visitTime = System.currentTimeMillis(),
                        source = getKey(),
                    )
                    r.add(b)
                }
                val pages = doc.select("div.pages ul.pagination li a.next.pagegbk")
                res = if(pages.isEmpty()){
                    ISearchParser.BangumiPageResult(null, true, r)
                }else{
                    ISearchParser.BangumiPageResult(page+1, true, r)
                }
            }.onFailure {
                it.printStackTrace()
                throw it

            }
        }
        return res
    }
}