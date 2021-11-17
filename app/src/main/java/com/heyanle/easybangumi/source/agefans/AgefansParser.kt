package com.heyanle.easybangumi.source.agefans

import android.content.Context
import android.os.Build
import android.util.Log
import android.webkit.*
import com.heyanle.easybangumi.EasyApplication
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.entity.Bangumi
import com.heyanle.easybangumi.entity.BangumiDetail
import com.heyanle.easybangumi.source.*
import com.heyanle.easybangumi.source.bimibimi.BimibimiParser
import com.heyanle.easybangumi.ui.detailplay.DetailPlayWebViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.util.concurrent.CountDownLatch
import android.webkit.JavascriptInterface
import com.heyanle.easybangumi.utils.OkHttpUtils
import java.net.URLDecoder


/**
 * Created by HeYanLe on 2021/10/21 16:14.
 * https://github.com/heyanLE
 */
class AgefansParser: ISourceParser, IHomeParser, IDetailParser, IPlayerParser, ISearchParser {

    companion object{
        const val ROOT_URL = "https://www.agefans.vip"
    }

    private fun url(source: String): String{
        return when {
            source.startsWith("http") -> {
                source
            }
            source.startsWith("//") -> {
                "https:$source"
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
        return "agefans"
    }

    override fun getLabel(): String {
        return "AGE 动漫"
    }

    override fun getVersion(): String {
        return "1.0"
    }

    override fun getVersionCode(): Int {
        return 0
    }

    override suspend fun home(): ISourceParser.ParserResult<LinkedHashMap<String, List<Bangumi>>> {
        return withContext(Dispatchers.IO){
            val map = LinkedHashMap<String, List<Bangumi>>()
            val doc = runCatching {
                Log.i("AgefansParser", ROOT_URL)
                Jsoup.parse(OkHttpUtils.get(ROOT_URL))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            runCatching {
                val titleIt = doc.select("div.div_left.baseblock div.blocktitle").iterator()
                val contentIt = doc.select("div.div_left.baseblock div.blockcontent").iterator()
                while(titleIt.hasNext() && contentIt.hasNext()){
                    val title = titleIt.next().text()
                    val list = arrayListOf<Bangumi>()
                    contentIt.next().getElementsByTag("li").forEach {
                        val detailUrl =  url(it.child(0).attr("href"))
                        val imgUrl = url(it.child(0).child(0).attr("src"))
                        val name = it.child(1).text()
                        val intro = it.child(0).child(1).text()
                        val bangumi = Bangumi(
                            id = "${getLabel()}-$detailUrl",
                            source = getKey(),
                            name = name,
                            cover = url(imgUrl),
                            intro = intro,
                            detailUrl = detailUrl,
                            visitTime = System.currentTimeMillis()
                        )
                        list.add(bangumi)
                    }
                    map[title] = list
                }
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
            val url = "https://www.agefans.cc/search?query=$keyword&page=$key"
            val doc = runCatching {
                Jsoup.parse(OkHttpUtils.get(url))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            runCatching {
                val ans = arrayListOf<Bangumi>()
                doc.select("div#container div.baseblock div.cell").forEach {
                    val detailUrl = url(it.child(0).attr("href"))
                    val imgUrl = url(it.child(0).child(0).attr("src"))
                    val intro = it.child(0).child(1).text()
                    val title = it.child(1).child(0).text()
                    val bangumi = Bangumi(
                        id = "${getLabel()}-$detailUrl",
                        source = getKey(),
                        name = title,
                        cover = url(imgUrl),
                        intro = intro,
                        detailUrl = detailUrl,
                        visitTime = System.currentTimeMillis()
                    )
                    ans.add(bangumi)
                }
                doc.select("div#container div.baseblock div.blockcontent li").forEach {
                    if(it.text() == "下一页"){
                        return@withContext ISourceParser.ParserResult.Complete(Pair(key+1, ans))
                    }
                }
                return@withContext ISourceParser.ParserResult.Complete(Pair(null, ans))
            }.onFailure {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    override suspend fun detail(bangumi: Bangumi): ISourceParser.ParserResult<BangumiDetail> {
        return withContext(Dispatchers.IO){
            val doc = runCatching {
                Jsoup.parse(OkHttpUtils.get(bangumi.detailUrl))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            kotlin.runCatching {
                val description = doc.select("div.baseblock div.blockcontent div.detail_imform_desc_pre").text()
                return@withContext ISourceParser.ParserResult.Complete( BangumiDetail(
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
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    private var bangumi:Bangumi? = null
    private val temp: ArrayList<ArrayList<String>> = arrayListOf()
    override suspend fun getPlayMsg(bangumi: Bangumi): ISourceParser.ParserResult<LinkedHashMap<String, List<String>>> {
        temp.clear()
        return withContext(Dispatchers.IO){
            val map = LinkedHashMap<String, List<String>>()
            val doc = runCatching {
                Jsoup.parse(OkHttpUtils.get(bangumi.detailUrl))
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }
            runCatching {
                val li = doc.select("div.div_right div#playlist-div ul li").iterator()
                val mov = doc.select("div.div_right div#playlist-div div#main0 div.movurl").iterator()
                while(li.hasNext() && mov.hasNext()){
                    val title = li.next().text()
                    val list = arrayListOf<String>()
                    val urlList = arrayListOf<String>()
                    mov.next().select("ul li").forEach {
                        list.add(it.text())
                        urlList.add(url(it.child(0).attr("href")))
                    }
                    if(list.isEmpty() || urlList.isEmpty()){
                        continue
                    }
                    map[title] = list
                    temp.add(urlList)
                }
                this@AgefansParser.bangumi = bangumi
                return@withContext ISourceParser.ParserResult.Complete(map)
            }.onFailure {
                this@AgefansParser.bangumi = null
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    class AgeWebViewClient : WebViewClient(){

        inner class JavaJs {
            @JavascriptInterface
            fun showSource(html: String) {
                onComplete?.let {
                    it(html)
                }
            }
        }

        val javaJs = JavaJs()

        var onComplete : ((String) -> Unit)? = null
        var onError : (() -> Unit)? = null
        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            request?.let {
                //Log.i("AgefansParser", it.url.toString()?:"")
            }
            return super.shouldInterceptRequest(view, request)
        }

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
            Log.i("agefans", "finish")
            val js = "javascript:window.java_obj.showSource(document.getElementsByTagName('iframe')[0].getAttribute('src'));"
            view.loadUrl(js)
        }

        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            onError?.let {
                it()
            }
            onComplete = null
            onError = null
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                onError?.let {
                    it()
                }
                onComplete = null
                onError = null
            }

        }
    }

    private val client : AgeWebViewClient by lazy {
        AgeWebViewClient()
    }

    override suspend fun getPlayUrl(
        bangumi: Bangumi,
        lineIndex: Int,
        episodes: Int,
        webView: WebView
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
        val countDownLatch = CountDownLatch(1)
        var result:ISourceParser.ParserResult<String> = ISourceParser.ParserResult.Error<String>(Exception("Unknown Error"), true)
        withContext(Dispatchers.Main){
            webView.addJavascriptInterface(client.javaJs, "java_obj")
            client.onComplete = {
                runCatching {
                    Log.i("AgefansParser", "onComplete $it")
                    val res = runCatching {
                        Log.i("AgefansParser", it)
                        val index = it.indexOf("?")
                        val u = it.subSequence(index+1, it.length)
                        var res = ""
                        u.split("&").forEach { pa ->
                            val kv = pa.split("=")
                            if(kv[0] == "url"){
                                res = kv[1]
                            }
                        }
                        res
                    }.getOrElse {
                        it.printStackTrace()
                        result = ISourceParser.ParserResult.Error<String>(it, true)
                        countDownLatch.countDown()
                        null
                    }
                    if(res != null){
                        if(res.isEmpty()){
                            countDownLatch.countDown()
                        }else{
                            result = ISourceParser.ParserResult.Complete(URLDecoder.decode(res, "UTF-8"))
                            countDownLatch.countDown()
                        }
                    }
                }
            }
            client.onError = {
                runCatching {
                    Log.i("AgefansParser", "onError")
                    result = ISourceParser.ParserResult.Error(Exception("net error"), false)
                    countDownLatch.countDown()
                }

            }
            webView.webViewClient = client
            webView.loadUrl(url)
        }

        countDownLatch.await()
        withContext(Dispatchers.Main){
            webView.removeJavascriptInterface("java_obj")
            client.onError = null
            client.onComplete = null
            webView.loadUrl("")
        }
        return result
    }

    override fun startPlay(context: Context, bangumi: Bangumi) {
        DetailPlayWebViewActivity.start(context, bangumi)
    }
}