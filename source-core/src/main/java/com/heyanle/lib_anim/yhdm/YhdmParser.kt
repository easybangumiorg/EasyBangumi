package com.heyanle.lib_anim.yhdm


import com.heyanle.bangumi_source_api.api.*
import com.heyanle.bangumi_source_api.api.IPlayerParser.PlayerInfo.Companion.TYPE_HLS
import com.heyanle.bangumi_source_api.api.IPlayerParser.PlayerInfo.Companion.TYPE_OTHER
import com.heyanle.bangumi_source_api.api.entity.Bangumi
import com.heyanle.bangumi_source_api.api.entity.BangumiDetail
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.lib_anim.utils.network.networkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.lang.Exception
import java.lang.IndexOutOfBoundsException

/**
 * Created by HeYanLe on 2021/10/21 15:31.
 * https://github.com/heyanLE
 */
class YhdmParser : ISourceParser, IHomeParser, IDetailParser, IPlayerParser, ISearchParser {
    override fun getKey(): String {
        return "yhdm"
    }

    override fun getLabel(): String {
        return "樱花动漫"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun getVersionCode(): Int {
        return 0
    }

    companion object {
        const val ROOT_URL = "http://www.yinghuacd.com"
    }

    private fun url(source: String): String {
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

    override suspend fun home(): ISourceParser.ParserResult<LinkedHashMap<String, List<Bangumi>>> {
        return withContext(Dispatchers.IO) {
            val map = LinkedHashMap<String, List<Bangumi>>()

            val doc = runCatching {
                Jsoup.connect(url(ROOT_URL)).get()
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {

                val children = doc.select("div.firs.l")[0].children().iterator()
                while (children.hasNext()) {
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
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }.onSuccess {
                return@withContext ISourceParser.ParserResult.Complete(map)
            }

            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    override fun firstKey(): Int {
        return 1
    }

    override suspend fun search(
        keyword: String,
        key: Int
    ): ISourceParser.ParserResult<Pair<Int?, List<Bangumi>>> {
        return withContext(Dispatchers.IO) {
            val url = url("/search/$keyword?page=$key")

            val doc = runCatching {
                Jsoup.connect(url(url)).get()
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            kotlin.runCatching {
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
                if (pages.isEmpty()) {
                    return@withContext ISourceParser.ParserResult.Complete(Pair(null, r))
                } else {
                    val p = pages.select("a#lastn")
                    if (p.isEmpty()) {
                        return@withContext ISourceParser.ParserResult.Complete(Pair(null, r))
                    } else {
                        return@withContext ISourceParser.ParserResult.Complete(Pair(key + 1, r))
                    }
                }
            }.onFailure {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    override suspend fun detail(bangumi: BangumiSummary): ISourceParser.ParserResult<BangumiDetail> {
        return withContext(Dispatchers.IO) {
            val doc = runCatching {
                Jsoup.connect(url(bangumi.detailUrl)).get()
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            kotlin.runCatching {
                val id = "${getLabel()}-${bangumi.detailUrl}"
                val name = doc.select("div.fire div.rate h1")[0].text()
                val intro = doc.select("div.fire div.rate div.sinfo p")[0].text()
                val cover = url(doc.select("div.fire.l div.thumb.l img")[0].attr("src"))
                val description = doc.getElementsByClass("info")[0].text()
                return@withContext ISourceParser.ParserResult.Complete(
                    BangumiDetail(
                        id = id,
                        source = getKey(),
                        name = name,
                        cover = cover,
                        intro = intro,
                        detailUrl = bangumi.detailUrl,
                        description = description
                    )
                )
            }.onFailure {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    private var bangumi: BangumiSummary? = null
    private val temp: ArrayList<String> = arrayListOf()
    override suspend fun getPlayMsg(bangumi: BangumiSummary): ISourceParser.ParserResult<LinkedHashMap<String, List<String>>> {
        temp.clear()
        return withContext(Dispatchers.IO) {
            val map = LinkedHashMap<String, List<String>>()
            val doc = runCatching {
                Jsoup.connect(url(bangumi.detailUrl)).get()
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            kotlin.runCatching {
                val sourceDiv = doc.getElementsByClass("movurl")[0].child(0)
                val list = arrayListOf<String>()
                val tt = arrayListOf<String>()
                sourceDiv.children().forEach {
                    list.add(it.text())
                    tt.add(it.child(0).attr("href"))
                }
                temp.addAll(tt.reversed())
                map["播放列表"] = list.reversed()
                this@YhdmParser.bangumi = bangumi
                return@withContext ISourceParser.ParserResult.Complete(map)
            }.onFailure {
                this@YhdmParser.bangumi = null
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            this@YhdmParser.bangumi = null
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    override suspend fun getPlayUrl(
        bangumi: BangumiSummary,
        lineIndex: Int,
        episodes: Int
    ): ISourceParser.ParserResult<IPlayerParser.PlayerInfo> {
        if (lineIndex < 0 || episodes < 0) {
            return ISourceParser.ParserResult.Error(IndexOutOfBoundsException(), false)
        }
        var url = ""
        if (bangumi != this.bangumi
            || episodes >= temp.size
            || temp[episodes] == ""
        ) {
            getPlayMsg(bangumi).error {
                return@getPlayUrl ISourceParser.ParserResult.Error(it.throwable, it.isParserError)
            }.complete {
                runCatching {
                    url = temp[episodes]
                }.onFailure {
                    return@getPlayUrl ISourceParser.ParserResult.Error(it, true)
                }
            }
        } else {
            url = temp[episodes]
        }

        if (url.isEmpty()) {
            return ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
        return withContext(Dispatchers.IO) {
            val doc = runCatching {
                Jsoup.connect(url(url)).get()
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            val result = runCatching {
                doc.select("div.area div.bofang div#playbox")[0].attr("data-vid").split("$")[0]
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            if (result.isNotEmpty()) {
                if (result.indexOf(".m3u8") != -1)
                    return@withContext ISourceParser.ParserResult.Complete(
                        IPlayerParser.PlayerInfo(
                            type = TYPE_HLS,
                            uri = result
                        )
                    )
                return@withContext ISourceParser.ParserResult.Complete(
                    IPlayerParser.PlayerInfo(
                        type = TYPE_OTHER,
                        uri = result
                    )
                )
            }

            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }
}