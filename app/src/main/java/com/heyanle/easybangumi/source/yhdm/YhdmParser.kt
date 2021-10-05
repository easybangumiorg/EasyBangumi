package com.heyanle.easybangumi.source.yhdm

import com.heyanle.easybangumi.EasyApplication
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.entity.Bangumi
import com.heyanle.easybangumi.entity.BangumiDetail
import com.heyanle.easybangumi.source.IBangumiDetailParser
import com.heyanle.easybangumi.source.IHomeParser
import com.heyanle.easybangumi.source.IParser
import com.heyanle.easybangumi.source.IPlayUrlParser
import com.heyanle.easybangumi.source.bimibimi.BimibimiParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * Created by HeYanLe on 2021/9/21 22:29.
 * https://github.com/heyanLE
 */
class YhdmParser: IParser, IHomeParser, IBangumiDetailParser, IPlayUrlParser {
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
                val doc = Jsoup.connect(BimibimiParser.ROOT_URL).timeout(10000)
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
                }
                map.put("播放列表", list)
            }.onFailure {
                it.printStackTrace()
            }
            map
        }
    }

    override suspend fun getBangumiPlayUrl(
        bangumi: Bangumi,
        lineIndex: Int,
        episodes: Int
    ): String {
        return ""
    }
}