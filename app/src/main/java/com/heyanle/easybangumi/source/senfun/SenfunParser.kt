package com.heyanle.easybangumi.source.senfun

import android.content.Context
import android.os.Build
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.heyanle.easybangumi.EasyApplication
import com.heyanle.easybangumi.entity.Bangumi
import com.heyanle.easybangumi.entity.BangumiDetail
import com.heyanle.easybangumi.source.*
import com.heyanle.easybangumi.source.agefans.AgefansParser
import com.heyanle.easybangumi.ui.detailplay.DetailPlayWebViewActivity
import com.heyanle.easybangumi.utils.OkHttpUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.lang.ref.WeakReference
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch
import kotlin.Exception

/**
 * Created by HeYanLe on 2022/6/11 20:31.
 * https://github.com/heyanLE
 */
class SenfunParser: ISourceParser, IHomeParser, IDetailParser, IPlayerParser, ISearchParser {

    companion object{
        const val ROOT_URL = "https://www.senfun.net"
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
        return "senfun"
    }

    override fun getLabel(): String {
        return "森之屋动漫"
    }

    override fun getVersion(): String {
        return "1.0"
    }

    override fun getVersionCode(): Int {
        return 1
    }

    override suspend fun home(): ISourceParser.ParserResult<LinkedHashMap<String, List<Bangumi>>> {
        return withContext(Dispatchers.IO){
            val map = LinkedHashMap<String, List<Bangumi>>()
            val doc = runCatching {
                Jsoup.parse(OkHttpUtils.get(url(ROOT_URL)))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            kotlin.runCatching {

                val elements = doc.select("div.main div.content div.module")

                fun load(element: Element){

                    val titleEle = element.select("div.module-heading h2.module-title a")[0]


                    val columnTitle = titleEle.ownText()
                    val uls = element.select("div.module-main div.module-items")
                    val list = arrayListOf<Bangumi>()
                    val ul = uls[0]
                    ul.children().forEach { ele ->
                        val detailUrl =  url(ele.attr("href"))
                        val imgUrl = url(ele.select("div.module-item-cover div.module-item-pic img")[0].attr("data-original"))
                        val title = ele.child(1).child(0).text()
                        val intro = ele.child(0).child(0).text()
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

                load(elements[1])
                load(elements[2])
                load(elements[3])
                load(elements[4])
                load(elements[5])

            }.onFailure {
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
        return withContext(Dispatchers.IO){
            val doc = runCatching {
                val url = url("/search/wd/${keyword}/page/${key}.html")
                Jsoup.parse(OkHttpUtils.get(url(url)))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            runCatching {
                val r = arrayListOf<Bangumi>()
                doc.select("div.main div.content div.module div.module-main.module-page div.module-items div.module-item").forEach {
                    val detailUrl = url(it.child(1).attr("href"))
                    val itt = it.child(2)
                    val b = Bangumi(
                        id = "${getLabel()}-$detailUrl",
                        name = itt.child(0).child(0).text(),
                        detailUrl = detailUrl,
                        intro = it.child(1).child(0).child(0).text(),
                        cover = url(it.child(1).child(0).child(1).child(0).attr("data-original")),
                        visitTime = System.currentTimeMillis(),
                        source = getKey(),
                    )
                    r.add(b)
                }
                val pages = doc.select("div.main div.content div.module div.module-main.module-page div#page a.page-next")
                if(pages.size < 2){
                    return@withContext ISourceParser.ParserResult.Complete(Pair(null, r))
                }else{
                    if(pages[0].attr("href") == pages[1].attr("href"))
                        return@withContext ISourceParser.ParserResult.Complete(Pair(null, r))
                    return@withContext ISourceParser.ParserResult.Complete(Pair(key + 1, r))
                }
            }.onFailure {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }

            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }



    override suspend fun detail(bangumi: Bangumi): ISourceParser.ParserResult<BangumiDetail> {
        return withContext(Dispatchers.IO) {
            if(bangumi.detailUrl.endsWith("707.html")){
                withContext(Dispatchers.Main){
                    Toast.makeText(EasyApplication.INSTANCE, "暂不支持 [需要登录] 的番剧", Toast.LENGTH_SHORT).show()
                }
                return@withContext ISourceParser.ParserResult.Error(Exception("不支持钛合金"), false)
            }
            val doc = runCatching {
                Jsoup.parse(OkHttpUtils.get(url(bangumi.detailUrl)))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            kotlin.runCatching {
                val description = doc.select("div.main div.content div.module div.module-info-introduction-content")[0].text()
                return@withContext  ISourceParser.ParserResult.Complete(BangumiDetail(
                    id = bangumi.id,
                    source = getKey(),
                    name = bangumi.name,
                    cover = bangumi.cover,
                    intro = bangumi.intro,
                    detailUrl = bangumi.detailUrl,
                    description = description
                ))
            }.onFailure {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    private var bangumi:Bangumi? = null
    private val temp: ArrayList<ArrayList<String>> = arrayListOf()

    override suspend fun getPlayMsg(bangumi: Bangumi): ISourceParser.ParserResult<LinkedHashMap<String, List<String>>> {
        this@SenfunParser.temp.clear()
        return withContext(Dispatchers.IO) {
            val map = LinkedHashMap<String, List<String>>()
            val doc = runCatching {
                Jsoup.parse(OkHttpUtils.get(url(bangumi.detailUrl)))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {

                //val sourceDiv = doc.getElementsByClass("play_source_tab")[0]

                val ite = doc.select("div.main div.content div.module-tab-items div.module-tab-item span").iterator()
                val playBoxIte = doc.select("div.main div.content div.module div.module-list.sort-list").iterator()
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
                    this@SenfunParser.temp.add(urlList)
                }
                this@SenfunParser.bangumi = bangumi
                return@withContext ISourceParser.ParserResult.Complete(map)
            }.onFailure {
                this@SenfunParser.bangumi = null
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            this@SenfunParser.bangumi = null
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    override suspend fun getPlayUrl(
        bangumi: Bangumi,
        lineIndex: Int,
        episodes: Int
    ): ISourceParser.ParserResult<String> {
        if(lineIndex < 0 || episodes < 0){
            return ISourceParser.ParserResult.Error(IndexOutOfBoundsException(), false)
        }
        var url = ""
        if(bangumi != this.bangumi
            || lineIndex >= temp.size
            || episodes >= temp[lineIndex].size
            || temp[lineIndex][episodes] == ""){
            getPlayMsg(bangumi).error {
                return@getPlayUrl ISourceParser.ParserResult.Error(it.throwable, it.isParserError)
            }.complete {
                runCatching {
                    url = temp[lineIndex][episodes]
                }.onFailure {
                    return@getPlayUrl ISourceParser.ParserResult.Error(it, true)
                }
            }
        }else{
            url = temp[lineIndex][episodes]
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

            val result = runCatching {
                val jsonSource = doc.select("div.main div.content div.module-player div.module-main script")[0].toString().run {
                    substring(indexOf("{"), lastIndexOf("}") + 1)
                }
                val jsonObject = JSONObject(jsonSource)
                val jsonUrl = jsonObject.getString("url")
                URLDecoder.decode(jsonUrl, "UTF-8");
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }

            if(result.isNotEmpty())
                return@withContext ISourceParser.ParserResult.Complete(result)

            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }


}