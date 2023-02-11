package com.heyanle.lib_anim.cycplus

import android.os.Build.VERSION_CODES
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.heyanle.bangumi_source_api.api.*
import com.heyanle.bangumi_source_api.api.entity.Bangumi
import com.heyanle.bangumi_source_api.api.entity.BangumiDetail
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.lib_anim.utils.network.GET
import com.heyanle.lib_anim.utils.network.networkHelper
import kotlinx.coroutines.*

class CycplusParser : ISourceParser, IHomeParser, IDetailParser, IPlayerParser, ISearchParser {

    companion object {
        const val SOURSE_KEY = "cycplus"
        const val ROOT_URL = "https://cycdm-1303090324.cos.ap-guangzhou.myqcloud.com/dtym.json"
        const val WEBVIEW_ROOT = "https://www.cycity.top/"
        const val VERSION_CODES = 6
    }

    private object R {
        val clint = networkHelper.client
        var BASE_URL = ""

        // 等一波API更新，先用脏办法实现了
        fun indexVedio() = "$BASE_URL/ciyuancheng.php/v$VERSION_CODES/index_video"
        fun search(title: String,page: Int) = "$BASE_URL/ciyuancheng.php/v$VERSION_CODES/search?pg=$page&text=$title"
        fun vedioDetail(id: String) = "$BASE_URL/ciyuancheng.php/v$VERSION_CODES/video_detail?id=$id"
    }

    private fun getJson(target: String): Result<JsonElement> {
        return runCatching {
            val req = R.clint.newCall(GET(target)).execute()
            val body = req.body!!.string()
            JsonParser.parseString(body)
        }
    }

    init {
        GlobalScope.launch {
            val rt = getJson(ROOT_URL).getOrElse {
                it.printStackTrace()
                JsonParser.parseString("[\"https://app.95189371.cn\"]")
            }
            R.BASE_URL = rt.asJsonArray[0].asString
        }
    }

    override fun getKey(): String = SOURSE_KEY
    override fun getLabel(): String = "次元城+"
    override fun getVersion(): String = "1.0.0"
    override fun getVersionCode(): Int = VERSION_CODES

    private object Parse {
        fun home(jObject: JsonObject, key: String): LinkedHashMap<String, List<Bangumi>> {
            val map = LinkedHashMap<String, List<Bangumi>>()
            val playList = jObject.getAsJsonArray(key)
            playList.forEach { jsonElement ->
                val list = arrayListOf<Bangumi>()
                val ele = jsonElement.asJsonObject

                ele.getAsJsonArray("vlist").forEach {
                    val bgObject = it.asJsonObject
                    val bgm = Bangumi(
                        id = bgObject.get("vod_id").asString,
                        name = bgObject.get("vod_name").asString,
                        cover = bgObject.get("vod_pic").asString,
                        intro = bgObject.get("vod_remarks").asString,
                        detailUrl = WEBVIEW_ROOT,
                        visitTime = System.currentTimeMillis(),
                        source = SOURSE_KEY
                    )
                    list.add(bgm)
                }

                map[ele.get("name").asString] = list
            }
            return map
        }
    }

    override suspend fun home(): ISourceParser.ParserResult<LinkedHashMap<String, List<Bangumi>>> {
        return withContext(Dispatchers.IO) {
            val index = getJson(R.indexVedio()).getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it,false)
            }

            kotlin.runCatching {
                val map = Parse.home(index.asJsonObject,"data")
                return@withContext ISourceParser.ParserResult.Complete(map)
            }.onFailure {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it,true)
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    override fun firstKey(): Int = 1

    override suspend fun search(
        keyword: String,
        key: Int
    ): ISourceParser.ParserResult<Pair<Int?, List<Bangumi>>> {
        TODO("Not yet implemented")
    }

    override suspend fun detail(bangumi: BangumiSummary): ISourceParser.ParserResult<BangumiDetail> {
        TODO("Not yet implemented")
    }

    override suspend fun getPlayMsg(bangumi: BangumiSummary): ISourceParser.ParserResult<LinkedHashMap<String, List<String>>> {
        TODO("Not yet implemented")
    }

}