package com.heyanle.lib_anim.cycplus

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.heyanle.bangumi_source_api.api.*
import com.heyanle.bangumi_source_api.api.entity.Bangumi
import com.heyanle.bangumi_source_api.api.entity.BangumiDetail
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.lib_anim.utils.network.GET
import com.heyanle.lib_anim.utils.network.networkHelper
import com.heyanle.lib_anim.utils.stringHelper
import kotlinx.coroutines.*

/**
 * Created by AyalaKaguya on 2023/2/11 11:54.
 * https://github.com/AyalaKaguya
 */
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
        fun webviewUrl(id: String) = "$WEBVIEW_ROOT#$id"
        fun indexVideo() = "$BASE_URL/ciyuancheng.php/v$VERSION_CODES/index_video"
        fun search(title: String, page: Int) =
            "$BASE_URL/ciyuancheng.php/v$VERSION_CODES/search?pg=$page&text=$title"

        fun videoDetail(id: String) =
            "$BASE_URL/ciyuancheng.php/v$VERSION_CODES/video_detail?id=$id"
    }

    private fun getJson(target: String): Result<JsonElement> {
        return runCatching {
            val req = R.clint.newCall(GET(target)).execute()
            val body = req.body!!.string()
            JsonParser.parseString(body)
        }
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            getJson(ROOT_URL).onFailure {
                it.printStackTrace()
                R.BASE_URL = "https://app.95189371.cn"
            }.onSuccess {
                kotlin.runCatching {
                    R.BASE_URL = it.asJsonArray[0].asString
                }
            }
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
                    val id = bgObject.get("vod_id").asString
                    val bgm = Bangumi(
                        id = id,
                        name = bgObject.get("vod_name").asString,
                        cover = bgObject.get("vod_pic").asString,
                        intro = bgObject.get("vod_remarks").asString,
                        detailUrl = R.webviewUrl(id),
                        visitTime = System.currentTimeMillis(),
                        source = SOURSE_KEY
                    )
                    list.add(bgm)
                }

                map[ele.get("name").asString] = list
            }
            return map
        }

        fun search(jObject: JsonObject, key: String): List<Bangumi> {
            val list = arrayListOf<Bangumi>()
            val playList = jObject.getAsJsonArray(key)

            playList.forEach {
                val ele = it.asJsonObject
                val id = ele.get("vod_id").asString
                val bgm = Bangumi(
                    id = id,
                    name = ele.get("vod_name").asString,
                    cover = ele.get("vod_pic").asString,
                    intro = ele.get("vod_remarks").asString,
                    detailUrl = R.webviewUrl(id),
                    visitTime = System.currentTimeMillis(),
                    source = SOURSE_KEY
                )
                list.add(bgm)
            }

            return list
        }

        fun detail(jObject: JsonObject, key: String): BangumiDetail {
            val jDetail = jObject.getAsJsonObject(key)
            val id = jDetail.get("vod_id").asString
            return BangumiDetail(
                id = id,
                name = jDetail.get("vod_name").asString,
                cover = jDetail.get("vod_pic").asString,
                intro = jDetail.get("vod_remarks").asString,
                detailUrl = R.webviewUrl(id),
                description = jDetail.get("vod_content").asString,
                source = SOURSE_KEY
            )
        }

        fun playList(jObject: JsonObject, key: String): LinkedHashMap<String, List<String>> {
            val jList = jObject.getAsJsonArray(key)
            val map = LinkedHashMap<String, List<String>>()

            fun unpackPlayList(strArray: List<String>): List<String> {
                val list = arrayListOf<String>()

                strArray.forEach {
                    list.add(it.split("$")[0])
                }

                return list
            }

            jList.forEach {
                map[it.asJsonObject.get("name").asString] = unpackPlayList(
                    it.asJsonObject
                        .get("url").asString
                        .split("#")
                )
            }
            return map
        }
    }

    override suspend fun home(): ISourceParser.ParserResult<LinkedHashMap<String, List<Bangumi>>> {
        return withContext(Dispatchers.IO) {
            val index = getJson(R.indexVideo()).getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {
                val map = Parse.home(index.asJsonObject, "data")
                return@withContext ISourceParser.ParserResult.Complete(map)
            }.onFailure {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    override fun firstKey(): Int = 1

    override suspend fun search(
        keyword: String,
        key: Int
    ): ISourceParser.ParserResult<Pair<Int?, List<Bangumi>>> {
        return withContext(Dispatchers.IO) {
            val search = getJson(R.search(keyword, key)).getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {
                val total = search.asJsonObject.get("total").asInt
                val limit = search.asJsonObject.get("limit").asInt
                val list = Parse.search(search.asJsonObject, "data")

                val maxP = total / limit + 1

                if (maxP > key)
                    return@withContext ISourceParser.ParserResult.Complete(Pair(key + 1, list))

                return@withContext ISourceParser.ParserResult.Complete(Pair(null, list))
            }.onFailure {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    private val bangumiCache = LinkedHashMap<String, JsonObject>()

    override suspend fun detail(bangumi: BangumiSummary): ISourceParser.ParserResult<BangumiDetail> {
        return withContext(Dispatchers.IO) {
            val detail = getJson(R.videoDetail(bangumi.id)).getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {
                val bangumiInfo =
                    Parse.detail(detail.asJsonObject.getAsJsonObject("data"), "vod_info")
                return@withContext ISourceParser.ParserResult.Complete(bangumiInfo)
            }.onFailure {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }

    override suspend fun getPlayMsg(bangumi: BangumiSummary): ISourceParser.ParserResult<LinkedHashMap<String, List<String>>> {
        return withContext(Dispatchers.IO) {
            val detail = getJson(R.videoDetail(bangumi.id)).getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {
                stringHelper.moeSnackBar("次元城+来自于次元城APP，如果没有必要，还请点击下方的’打开原网站‘下载次元城APP使用")
                val bgmInfo = detail.asJsonObject
                    .getAsJsonObject("data")
                    .getAsJsonObject("vod_info")
                bangumiCache[bgmInfo.get("vod_id").asString] = bgmInfo
                val playlist = Parse.playList(bgmInfo, "vod_url_with_player")
                return@withContext ISourceParser.ParserResult.Complete(playlist)

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
            kotlin.runCatching {
                val bgmInfo = bangumiCache[bangumi.id]!!
                val playSourse = bgmInfo
                    .getAsJsonArray("vod_url_with_player")[lineIndex]
                    .asJsonObject
                val saltPerfix = playSourse.get("un_link_features").asString
                val playUrl = playSourse.get("url").asString
                    .split("#")[episodes]
                    .split("$")[1]
                val saltParse = playSourse.get("parse_api").asString

                if (playUrl.startsWith(saltPerfix)) {
                    // 这里不使用var playUrl是因为下面可以得到确切类型，而上面的是不确定的
                    val reLink = getJson(saltParse + playUrl).getOrElse {
                        it.printStackTrace()
                        return@withContext ISourceParser.ParserResult.Error(it, false)
                    }.asJsonObject
                    val type = reLink.get("type").asString
                    val result = reLink.get("url").asString
                    if (type == "m3u8")
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

                if (playUrl.indexOf(".m3u8") != -1)
                    return@withContext ISourceParser.ParserResult.Complete(
                        IPlayerParser.PlayerInfo(
                            type = IPlayerParser.PlayerInfo.TYPE_HLS,
                            uri = playUrl
                        )
                    )

                return@withContext ISourceParser.ParserResult.Complete(
                    IPlayerParser.PlayerInfo(
                        type = IPlayerParser.PlayerInfo.TYPE_OTHER,
                        uri = playUrl
                    )
                )

            }.onFailure {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }
}