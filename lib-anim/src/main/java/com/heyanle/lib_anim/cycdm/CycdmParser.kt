package com.heyanle.lib_anim.cycdm

import com.heyanle.lib_anim.*
import com.heyanle.lib_anim.entity.Bangumi
import com.heyanle.lib_anim.entity.BangumiDetail
import com.heyanle.lib_anim.entity.BangumiSummary
import com.heyanle.lib_anim.utils.OkHttpUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.net.URLDecoder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

/**
 * Created by AyalaKaguya on 2023/1/24 21:22.
 * https://github.com/AyalaKaguya
 */
class CycdmParser : ISourceParser, IHomeParser, IDetailParser, IPlayerParser, ISearchParser  {

    override fun getKey(): String {
        return "cycdm"
    }

    override fun getLabel(): String {
        return "次元城动漫"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun getVersionCode(): Int {
        return 0
    }

    companion object{
        const val ROOT_URL = "https://www.cycdm01.top"
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

    override suspend fun home(): ISourceParser.ParserResult<LinkedHashMap<String, List<Bangumi>>> {
        return withContext(Dispatchers.IO){
            val map = LinkedHashMap<String, List<Bangumi>>()

            val doc = runCatching {
                Jsoup.parse(OkHttpUtils.get(url(CycdmParser.ROOT_URL)))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {
                val docMain = doc.select("div.slid-e-list.swiper-wrapper")[0].children().iterator()
                val listMain = arrayListOf<Bangumi>()

                docMain.forEach {

                    val imgBox = it.child(0)
                    val detailBox = it.child(1).child(0)
                    val coverStyle = imgBox.child(3).attr("style")
                    val coverPattern = Regex("""(?<=url\().*(?=\))""")
                    val cover = coverPattern.find(coverStyle)?.value?:""
                    val name = detailBox.child(1).text()
                    val detailUrl = url(detailBox.child(3).child(0).child(1).attr("href"))
                    val intro = detailBox.child(2).text()
                    val id = detailBox.child(3).child(0).child(2).attr("data-id")
                    val bangumi = Bangumi(
                        id = "${getLabel()}-$detailUrl",
                        source = getKey(),
                        name = name,
                        cover = cover,
                        intro = intro,
                        detailUrl = detailUrl,
                        visitTime = System.currentTimeMillis()
                    )
                    listMain.add(bangumi)

                }
                map["首页推荐"] = listMain

                val docCol = doc.select("div.box-width.wow.fadeInUp.animated").iterator()
                while (docCol.hasNext()){
                    val children = docCol.next()
                    val columnName = children.child(0).child(0).child(0).text()
                    val list = arrayListOf<Bangumi>()

                    if (columnName == "系列推荐") continue;

                    children.child(1).children().forEach {
                        val cover = it.child(0).child(0).child(0).attr("data-original")
                        val name = it.child(0).child(0).attr("title")
                        val detailUrl = url(it.child(0).child(0).attr("href"))
                        val bangumi = Bangumi(
                            id = "${getLabel()}-$detailUrl",
                            source = getKey(),
                            name = name,
                            cover = cover,
                            intro = name,
                            detailUrl = detailUrl,
                            visitTime = System.currentTimeMillis()
                        )
                        list.add(bangumi)
                    }

                    map[columnName] = list
                }
//                val children_week = doc.select("div.public-r.list-swiper.rel.overflow.none").iterator()

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
            val urlSearch = url("/search/wd/$keyword/page/$key.html")

            val doc = runCatching {
                Jsoup.parse(OkHttpUtils.get(url(urlSearch)))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {
                val r = arrayListOf<Bangumi>()
                doc.select("div.row-right.hide div.search-box.flex.rel").forEach {
                    val detailUrl = url(it.child(1).child(0).attr("href"))
                    val b = Bangumi(
                        id = "${getLabel()}-$detailUrl",
                        name = it.child(2).child(0).child(0).text(),
                        detailUrl = detailUrl,
                        intro = it.child(1).child(0).child(1).text(),
                        cover = it.child(1).child(0).child(0).attr("data-original"),
                        visitTime = System.currentTimeMillis(),
                        source = getKey(),
                    )
                    r.add(b)
                }
                val pages = doc.select("div.page-info")
                if(pages.isEmpty()){
                    return@withContext ISourceParser.ParserResult.Complete(Pair(null, r))
                }else{
                    val p = pages.select("a.page-link.bj2.cor7.ho").next().text()
                    if(p == "下一页"){
                        return@withContext ISourceParser.ParserResult.Complete(Pair(null, r))
                    } else {
                        return@withContext ISourceParser.ParserResult.Complete(Pair(key+1, r))
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
                val name = doc.select("div.detail-info.rel.flex-auto h3")[0].text()
                val intro = doc.select("div.detail-info.rel.flex-auto div.slide-info.hide span.slide-info-remarks")[0].text()
                val cover = doc.select("a.detail-pic.lazy.mask-0")[0].attr("data-original")
                val description = doc.select("div.check.text.selected.cor3")[0].text()
                return@withContext ISourceParser.ParserResult.Complete( BangumiDetail(
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
                Jsoup.parse(OkHttpUtils.get(url(bangumi.detailUrl)))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {
                val sourceListDiv = doc.select("ul.anthology-list-play.size")[0]
                val listTarget = arrayListOf<String>()
                val listLink = arrayListOf<String>()
                sourceListDiv.children().forEach {
                    listTarget.add(it.text())
                    listLink.add(it.child(0).attr("href"))
                }
                temp.addAll(listLink.reversed())
                map["播放列表"] = listTarget.reversed()
                this@CycdmParser.bangumi = bangumi
                return@withContext  ISourceParser.ParserResult.Complete(map)
            }.onFailure {
                this@CycdmParser.bangumi = bangumi
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            this@CycdmParser.bangumi = bangumi
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    override suspend fun getPlayUrl(
        bangumi: BangumiSummary,
        lineIndex: Int,
        episodes: Int
    ): ISourceParser.ParserResult<IPlayerParser.PlayerInfo> {
        if(lineIndex < 0 || episodes < 0){
            return ISourceParser.ParserResult.Error(IndexOutOfBoundsException(), false)
        }
        var url = ""
        if(bangumi != this.bangumi
            || episodes >= temp.size
            || temp[episodes] == ""){
            getPlayMsg(bangumi).error {
                return@getPlayUrl ISourceParser.ParserResult.Error(it.throwable, it.isParserError)
            }.complete {
                runCatching {
                    url = temp[episodes]
                }.onFailure {
                    return@getPlayUrl ISourceParser.ParserResult.Error(it, true)
                }
            }
        }else{
            url = temp[episodes]
        }

        if(url.isEmpty()){
            return ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
        return withContext(Dispatchers.IO){
            val doc = runCatching {
                Jsoup.parse(OkHttpUtils.get(url(url)))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            runCatching {
                val playInfo = doc.select("div.player-left script")[0].data()
                val playPattern = Regex("""(?<="url":").*(?=","u)""")
                val playSecret = playPattern.find(playInfo)?.value?:""

                var result = playSecret.base64Decoded
                result = URLDecoder.decode(result)

                if (result.isNotEmpty())
                    return@withContext ISourceParser.ParserResult.Complete(IPlayerParser.PlayerInfo(type = IPlayerParser.PlayerInfo.TYPE_HLS,uri = result))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }

            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }
}