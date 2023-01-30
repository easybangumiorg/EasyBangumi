package com.heyanle.lib_anim.yhdmp

import com.google.gson.JsonParser
import com.heyanle.lib_anim.*
import com.heyanle.lib_anim.entity.Bangumi
import com.heyanle.lib_anim.entity.BangumiDetail
import com.heyanle.lib_anim.entity.BangumiSummary
import com.heyanle.lib_anim.utils.OkHttpUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLDecoder
import java.util.Date


/**
 * Created by AyalaKaguya on 2023/1/29 21:38.
 * https://github.com/AyalaKaguya
 */
class YhdmpParser : ISourceParser, IHomeParser, IDetailParser, IPlayerParser, ISearchParser {

    override fun getKey(): String {
        return "yhdmp"
    }

    override fun getLabel(): String {
        return "樱花动漫P"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun getVersionCode(): Int {
        return 0
    }

    companion object {
        const val ROOT_URL = "https://www.yhdmp.net"
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
                Jsoup.parse(OkHttpUtils.get(url(ROOT_URL)))
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
                        val cover = it.child(0).child(0).attr("src")
                        val name = it.child(1).text()
                        val detailUrl = url(it.child(0).attr("href"))
                        val intro = it.child(2).text()
                        val bangumi = Bangumi(
                            id = "${getLabel()}-$detailUrl",
                            source = getKey(),
                            name = name,
                            cover = "https:${cover}",
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
            val url = url("/s_all?ex=$key&kw=$keyword")

            val doc = runCatching {
                Jsoup.parse(OkHttpUtils.get(url(url)))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            kotlin.runCatching {
                val r = arrayListOf<Bangumi>()
                doc.select("div.fire.l div.lpic ul li").forEach {
                    val detailUrl = url(it.child(0).attr("href"))
                    val coverUrl = it.child(0).child(0).attr("src")
                    val b = Bangumi(
                        id = "${getLabel()}-$detailUrl",
                        name = it.child(1).text(),
                        detailUrl = detailUrl,
                        intro = it.child(2).text(),
                        cover = "https:${coverUrl}",
                        visitTime = System.currentTimeMillis(),
                        source = getKey(),
                    )
                    r.add(b)
                }
                val pages = doc.select("div.pages")
                if (pages.isEmpty()) {
                    return@withContext ISourceParser.ParserResult.Complete(Pair(null, r))
                } else {
                    val p = pages.text()
                    val f = p.contains((key + 1).toString())
                    if (f) {
                        return@withContext ISourceParser.ParserResult.Complete(Pair(key + 1, r))
                    } else {
                        return@withContext ISourceParser.ParserResult.Complete(Pair(null, r))
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
                Jsoup.parse(OkHttpUtils.get(url(bangumi.detailUrl)))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            kotlin.runCatching {
                val id = "${getLabel()}-${bangumi.detailUrl}"
                val name = doc.select("div.fire.l div.rate.r h1")[0].text()
                val intro = doc.select("div.fire.l div.rate.r div.sinfo p")[0].text()
                val cover = doc.select("div.fire.l div.thumb.l img")[0].attr("src")
                val description = doc.getElementsByClass("info")[0].text()
                return@withContext ISourceParser.ParserResult.Complete(
                    BangumiDetail(
                        id = id,
                        source = getKey(),
                        name = name,
                        cover = "https:${cover}",
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
                Jsoup.parse(OkHttpUtils.get(url(bangumi.detailUrl)))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            kotlin.runCatching {
                val sourceDiv = doc.getElementsByClass("movurl")
                var index = 1

                sourceDiv.forEach { element ->
                    val list = arrayListOf<String>()
                    val tt = arrayListOf<String>()

                    element.child(0).children().forEach {
                        list.add(it.text())
                        tt.add(it.child(0).attr("href"))
                    }
                    temp.addAll(tt)
                    map["播放列表${index}"] = list
                    index += 1
                }

                this@YhdmpParser.bangumi = bangumi
                return@withContext ISourceParser.ParserResult.Complete(map)
            }.onFailure {
                this@YhdmpParser.bangumi = null
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            this@YhdmpParser.bangumi = null
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
            val playID = Regex("""(?<=showp/).*(?=.html)""").find(bangumi.detailUrl)?.value?:""

            if (playID.isEmpty())
                return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)

            val playSecret = runCatching {
                k1 = null
                getPlayInfoRequest(bangumi.detailUrl, lineIndex, episodes, url(url))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            val jsonObject = runCatching {
                JsonParser.parseString(playSecret).asJsonObject
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }

            val vurl =  jsonObject.get("vurl").asString

            val result = decodeByteCrypt(vurl)

            if (result.isNotEmpty()) {
                if (result.indexOf(".mp4") == -1)
                    return@withContext ISourceParser.ParserResult.Complete(
                        IPlayerParser.PlayerInfo(
                            type = IPlayerParser.PlayerInfo.TYPE_HLS,
                            uri = result
                        )
                    )
                return@withContext ISourceParser.ParserResult.Complete(
                    IPlayerParser.PlayerInfo(
                        type = IPlayerParser.PlayerInfo.TYPE_OTHER,
                        uri = result
                    )
                )
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    private var t1: Long?  = null
    private var t2: Long?  = null
    private var k1: Long?  = null
    private var k2: Long?  = null
    private var errCount = 0

    private fun getPlayInfoRequest(playURL:String, playIndex:Int, epIndex:Int, referer:String): String {
        val playID = Regex("""(?<=showp/).*(?=.html)""").find(playURL)?.value?:""

        val target = url("/_getplay?aid=${playID}&playindex=${playIndex}&epindex=${epIndex}&r=${Math.random()}")
        val clint = OkHttpUtils.client()
        val request = OkHttpUtils.request(target)
            .addHeader("Referer", referer)
            .get()

        if (k1 != null) {
            val t = t1!!.div(0x3e8) shr 5
            k2 = (t * (t % 0x1000) + 0x99d6) * (t % 0x1000) + t
            t2 = Date().time

            request.addHeader("Cookie", "t1=${t1}; k1=${k1}; k2=${k2}; t2=${t2};")
        }

        val exReq = clint.newCall(request.build()).execute()

        val body = exReq.body!!.string()

        if (body == "err:timeout" || body.isEmpty()) {
            val cookies: List<String> = exReq.headers.values("Set-Cookie")

            cookies.forEach {session ->
                if (session.isNotEmpty()) {
                    val size = session.length
                    val i = session.indexOf(";")
                    if (i in 0 until size) {
                        //最终获取到的cookie
                        val cookie = session.substring(0, i).split("=")
                        when (cookie[0]) {
                            "k1" -> k1 = cookie[1].toLong()
                            "t1" -> t1 = cookie[1].toLong()
                        }
                    }
                }
            }

            if (errCount == 4) {
                errCount = 0
                throw Error("Too many failures")
            }
            errCount++

            return getPlayInfoRequest(playURL, playIndex, epIndex, referer)
        }

        return body
    }

    private fun decodeByteCrypt(rawData: String): String {
        if (rawData.indexOf('{') < 0) {
            var hfPanurl = ""
            val keyMP = 1048576
            val panurlLen = rawData.length

            for (i in 0 until panurlLen step 2) {
                val byte = rawData.substring(i, i + 2)
                var mn = byte.toInt(16)
                mn = (mn + keyMP - (panurlLen / 2 - 1 - i / 2)) % 256
                hfPanurl = Char(mn) + hfPanurl
            }
            return URLDecoder.decode(hfPanurl, "utf-8")
        }
        return rawData
    }

}