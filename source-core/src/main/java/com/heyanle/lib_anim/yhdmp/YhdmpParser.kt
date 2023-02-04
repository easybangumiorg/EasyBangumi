package com.heyanle.lib_anim.yhdmp

import com.google.gson.JsonParser
import com.heyanle.bangumi_source_api.api.*
import com.heyanle.bangumi_source_api.api.entity.Bangumi
import com.heyanle.bangumi_source_api.api.entity.BangumiDetail
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.lib_anim.bimibimi.BimibimiParser
import com.heyanle.lib_anim.utils.Base64Utils
import com.heyanle.lib_anim.utils.fileHelper
import com.heyanle.lib_anim.utils.getUri
import com.heyanle.lib_anim.utils.network.GET
import com.heyanle.lib_anim.utils.network.networkHelper
import com.heyanle.lib_anim.utils.network.webview_helper.webViewHelper
import com.heyanle.lib_anim.utils.stringHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.internal.userAgent
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
        const val ROOT_URL = "https://m.yhdmp.net"
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
                Jsoup.parse(
                    networkHelper.cloudflareClient.newCall(GET(ROOT_URL)).execute().body?.string()
                        ?: ""
                )
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {
                val children = doc.select("body div.list")[0].children().iterator()
                while (children.hasNext()) {
                    val title = children.next()
                    val child = children.next()
                    val titleString = title.child(0).text()
                    val list = arrayListOf<Bangumi>()
                    child.children().forEach {
                        val coverStyle = it.child(0).child(0).child(0).child(0).attr("style")
                        val coverPattern = Regex("""(?<=url\(').*(?='\))""")
                        var cover = coverPattern.find(coverStyle)?.value ?: ""
                        if (cover.startsWith("//")) {
                            cover = "https:${cover}"
                        }
//                        stringHelper.moeSnackBar(coverStyle)
//                        stringHelper.moeSnackBar(cover)
                        val name = it.child(1).text()
                        val detailUrl = url(it.child(0).child(0).attr("href"))
                        val intro = it.child(0).child(0).child(0).child(1).text()
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
            val url = url("/s_all?ex=$key&kw=$keyword")

            val doc = runCatching {
                Jsoup.connect(url(url)).get()
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            kotlin.runCatching {
                val r = arrayListOf<Bangumi>()
                doc.select("li").forEach {
                    val detailUrl = url(it.child(0).child(0).attr("href"))
                    val coverStyle = it.select("div.imgblock")[0].attr("style")
                    val coverPattern = Regex("""(?<=url\(').*(?='\))""")
                    var cover = coverPattern.find(coverStyle)?.value ?: ""

                    val b = Bangumi(
                        id = "${getLabel()}-$detailUrl",
                        name = it.child(1).text(),
                        detailUrl = detailUrl,
                        intro = it.select("div.itemimgtext")[0].text(),
                        cover = "https:${cover}",
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
                Jsoup.connect(url(bangumi.detailUrl)).get()
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            kotlin.runCatching {
                val id = "${getLabel()}-${bangumi.detailUrl}"
                val name = doc.select("div.show h1")[0].text()
                val intro = doc.select("div.info-sub p")[2].text()
                val cover = doc.select("div.show img")[0].attr("src")
                val description = doc.select("div.info")[0].text()
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

    override suspend fun getPlayMsg(bangumi: BangumiSummary): ISourceParser.ParserResult<LinkedHashMap<String, List<String>>> {
        return withContext(Dispatchers.IO) {
            val map = LinkedHashMap<String, List<String>>()
            val doc = runCatching {
                Jsoup.connect(url(bangumi.detailUrl)).get()
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
                    map["播放列表${index}"] = list
                    index += 1
                }

                return@withContext ISourceParser.ParserResult.Complete(map)
            }.onFailure {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
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

        return withContext(Dispatchers.IO) {
            val playID = Regex("""(?<=showp/).*(?=.html)""").find(bangumi.detailUrl)?.value ?: ""

            if (playID.isEmpty())
                return@withContext ISourceParser.ParserResult.Error(
                    Exception("Unknown Error"),
                    true
                )

            val url = url("/showp/${playID}-${lineIndex}-${episodes}.html")

            val playSecret = runCatching {
                k1 = null
                getPlayInfoRequest(playID, lineIndex, episodes, url)
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

            val vurl = jsonObject.get("vurl").asString

            val result = decodeByteCrypt(vurl)

            if (result.isNotEmpty()) {
                if (result.indexOf(".mp4") != -1)
                    return@withContext ISourceParser.ParserResult.Complete(
                        IPlayerParser.PlayerInfo(
                            type = IPlayerParser.PlayerInfo.TYPE_OTHER,
                            uri = url(result)
                        )
                    )
                return@withContext ISourceParser.ParserResult.Complete(
                    IPlayerParser.PlayerInfo(
                        type = IPlayerParser.PlayerInfo.TYPE_HLS,
                        uri = url(result)
                    )
                )
//                val fp = fileHelper.getFile(getKey(),Base64Utils.getMD5(result)+".m3u8")
//                if (!fp.exists()) {
//                    val req = GET(url(result),Headers.headersOf("User-Agent", networkHelper.defaultLinuxUA))
//                    val res = networkHelper.client.newCall(req).execute().body?.string()?:""
//                    fp.writeText(res,Charsets.UTF_8)
//                }
//                return@withContext ISourceParser.ParserResult.Complete(
//                    IPlayerParser.PlayerInfo(
//                        type = IPlayerParser.PlayerInfo.TYPE_HLS,
//                        uri = fp.getUri()
//                    )
//                )
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    private var t1: Long? = null
    private var t2: Long? = null
    private var k1: Long? = null
    private var k2: Long? = null
    private var errCount = 0

    private fun getPlayInfoRequest(
        playID: String,
        playIndex: Int,
        epIndex: Int,
        referer: String
    ): String {
        val target =
            url("/_getplay?aid=${playID}&playindex=${playIndex}&epindex=${epIndex}&r=${Math.random()}")
        val clint = networkHelper.client
        
        val header =  Headers.Builder().add("Referer", referer)
        if (k1 != null && t1 != null) {
            val t = t1!!.div(0x3e8) shr 5
            k2 = (t * (t % 0x1000) + 0x99d6) * (t % 0x1000) + t
            t2 = Date().time

            header.add("Cookie", "t1=${t1}; k1=${k1}; k2=${k2}; t2=${t2};")
        }

        val request = GET(target, header.build())
        val exReq = clint.newCall(request).execute()

        val body = exReq.body!!.string()

        if (body == "err:timeout" || body.isEmpty()) {
            val cookies: List<String> = exReq.headers.values("Set-Cookie")

            cookies.forEach { session ->
                if (session.isNotEmpty()) {
                    val size = session.length
                    val i = session.indexOf(";")
                    if (i in 0 until size) {
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

            return getPlayInfoRequest(playID, playIndex, epIndex, referer)
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