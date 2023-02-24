package com.heyanle.lib_anim.old.agefans

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.heyanle.bangumi_source_api.api.*
import com.heyanle.bangumi_source_api.api.entity.Bangumi
import com.heyanle.bangumi_source_api.api.entity.BangumiDetail
import com.heyanle.bangumi_source_api.api.entity.BangumiSummary
import com.heyanle.lib_anim.utils.network.GET
import com.heyanle.lib_anim.utils.network.networkHelper
import com.heyanle.lib_anim.utils.network.webview_helper.webViewHelper
import com.heyanle.lib_anim.utils.stringHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response

/**
 * Created by AyalaKaguya on 2023/2/8 15:50.
 * https://github.com/AyalaKaguya
 */
class AgefansParser : ISourceParser, IHomeParser, IDetailParser, IPlayerParser, ISearchParser {

    companion object {
        const val SOURCE_KEY = "agefans"
        const val ROOT_URL = "https://www.agemys.net"
        const val BASE_URL = "https://api.agefans.app"
        const val WEBVIEW_DETAIL_ROOT = "https://web.age-spa.com:8443/#"
    }

    private object R {
        val clint = networkHelper.client

        fun webViewDetailUrl(aid: String): String = "$WEBVIEW_DETAIL_ROOT/detail/$aid"

        // 等一波API更新，先用脏办法实现了
        fun homeList(update: Int, recommend: Int): String =
            "$BASE_URL/v2/home-list?update=$update&recommend=$recommend"

        fun search(title: String, page: Int): String = "$BASE_URL/v2/search?query=$title&page=$page"
        fun detail(aid: String): String = "$BASE_URL/v2/detail/$aid"
        fun recommend(size: Int): String = "$BASE_URL/v2/recommend?size=$size"
        fun rank(value: String, page: Int, size: Int): String =
            "$BASE_URL/v2/rank?value=$value&page=$page&size=$size"

        fun catalog(): String = "$BASE_URL/v2/catalog"
        fun slipic(): String = "$BASE_URL/v2/slipic"
    }

    private object Parse {

        fun url2id(url: String): String = url.split("/").last()

        fun home(jObject: JsonObject, key: String): List<Bangumi> {
            val list = arrayListOf<Bangumi>()
            val targetList = jObject.get(key).asJsonArray
            targetList.forEach {
                val ele = it.asJsonObject
                val bgm = Bangumi(
                    id = ele.get("AID").asString,
                    name = ele.get("Title").asString,
                    cover = ele.get("PicSmall").asString,
                    intro = ele.get("NewTitle").asString,
                    detailUrl = R.webViewDetailUrl(ele.get("AID").asString),
                    visitTime = System.currentTimeMillis(),
                    source = SOURCE_KEY
                )
                list.add(bgm)
            }
            return list
        }

        fun search(jObject: JsonObject, key: String): List<Bangumi> {
            val list = arrayListOf<Bangumi>()
            val bgList = jObject.get(key).asJsonArray
            bgList.forEach {
                val ele = it.asJsonObject
                val bgm = Bangumi(
                    id = ele.get("AID").asString,
                    name = ele.get("R动画名称").asString,
                    cover = ele.get("R封面图小").asString,
                    intro = ele.get("R新番标题").asString,
                    detailUrl = R.webViewDetailUrl(ele.get("AID").asString),
                    visitTime = System.currentTimeMillis(),
                    source = SOURCE_KEY
                )
                list.add(bgm)
            }
            return list
        }

        fun detail(jObject: JsonObject, key: String): BangumiDetail {
            val ele = jObject.get(key).asJsonObject

            return BangumiDetail(
                id = ele.get("AID").asString,
                name = ele.get("R动画名称").asString,
                cover = ele.get("R封面图").asString,
                intro = ele.get("R新番标题").asString,
                detailUrl = R.webViewDetailUrl(ele.get("AID").asString),
                description = ele.get("R简介").asString,
                source = SOURCE_KEY
            )
        }

        fun playList(jObject: JsonObject, key: String): LinkedHashMap<String, List<String>> {
            val ele = jObject.get(key).asJsonArray
            val map = java.util.LinkedHashMap<String, List<String>>()
            var index = 1

            fun unpackPlayList(JArray: JsonArray): List<String> {
                val list = arrayListOf<String>()

                JArray.forEach {
                    list.add(it.asJsonObject.get("Title_l").asString)
                }
                return list
            }

            ele.forEach {
                map["播放列表${index}"] = unpackPlayList(it.asJsonArray)
                index += 1
            }
            return map
        }
    }

    private fun get(target: String): Response = R.clint.newCall(GET(target)).execute()

    override fun getKey(): String = SOURCE_KEY
    override fun getLabel(): String = "Age动漫"
    override fun getVersion(): String = "1.0.0"
    override fun getVersionCode(): Int = 0

    override suspend fun home(): ISourceParser.ParserResult<LinkedHashMap<String, List<Bangumi>>> {
        return withContext(Dispatchers.IO) {
            val map = java.util.LinkedHashMap<String, List<Bangumi>>()
            val homeList = kotlin.runCatching {
                val rHomeList = get(R.homeList(12, 12))
                JsonParser.parseString(rHomeList.body!!.string()).asJsonObject
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {
                map["每日推荐"] = Parse.home(homeList, "AniPreEvDay")
                map["最近更新"] = Parse.home(homeList, "AniPreUP")

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

    override fun firstKey(): Int = 1

    override suspend fun search(
        keyword: String,
        key: Int
    ): ISourceParser.ParserResult<Pair<Int?, List<Bangumi>>> {
        return withContext(Dispatchers.IO) {
            val searchList = kotlin.runCatching {
                val rSearchList = get(R.search(keyword, key))
                JsonParser.parseString(rSearchList.body!!.string()).asJsonObject
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {

                val list = Parse.search(searchList, "AniPreL")
                val pgSize = searchList.get("PageCtrl").asJsonArray.size()

                if (pgSize == 3)
                    return@withContext ISourceParser.ParserResult.Complete(Pair(null, list))

                if (pgSize - 3 > key)
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
            val bangumiDetail = kotlin.runCatching {
                val id = Parse.url2id(bangumi.detailUrl)
                val rBgDetail = get(R.detail(id))
                JsonParser.parseString(rBgDetail.body!!.string()).asJsonObject
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {
                val bangumiInfo = Parse.detail(bangumiDetail, "AniInfo")
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
            val bangumiDetail = kotlin.runCatching {
                val id = Parse.url2id(bangumi.detailUrl)
                val rBgDetail = get(R.detail(id))
                JsonParser.parseString(rBgDetail.body!!.string()).asJsonObject
            }.getOrElse {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, false)
            }

            kotlin.runCatching {
                val bangumiInfo = bangumiDetail.get("AniInfo").asJsonObject
                bangumiCache[bangumiInfo.get("AID").asString] = bangumiInfo
                val playlist = Parse.playList(bangumiInfo, "R在线播放All")
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
                val id = Parse.url2id(bangumi.detailUrl)
                val trueBangumi = bangumiCache[id]!!.asJsonObject
                val playTarget = trueBangumi
                    .get("R在线播放All")
                    .asJsonArray[lineIndex]
                    .asJsonArray[episodes].asJsonObject
                val playUrl = playTarget.get("PlayVid").asString

                when (playTarget.get("PlayId").asString) {
                    "<play>88jx</play>" -> {
                        stringHelper.moeSnackBar("番剧源存在跨域解析，请耐心等待")

                        val blobUrl = webViewHelper.interceptResource(
                            url = "$WEBVIEW_DETAIL_ROOT/play/${bangumi.id}/${lineIndex + 1}/$episodes",
                            regex = """(?=http).*(?=\.mp4)""",
                            timeOut = 8000
                        )

                        if (blobUrl.isNotEmpty())
                            return@withContext ISourceParser.ParserResult.Complete(
                                IPlayerParser.PlayerInfo(
                                    type = IPlayerParser.PlayerInfo.TYPE_OTHER,
                                    uri = blobUrl
                                )
                            )

                        stringHelper.moeSnackBar("解析失败，请打开原网站播放")
                    }

                    "<play>web_m3u8</play>" -> {
                        return@withContext ISourceParser.ParserResult.Complete(
                            IPlayerParser.PlayerInfo(
                                type = IPlayerParser.PlayerInfo.TYPE_HLS,
                                uri = playUrl
                            )
                        )
                    }

                    "<play>zjm3u8</play>" -> {
                        return@withContext ISourceParser.ParserResult.Complete(
                            IPlayerParser.PlayerInfo(
                                type = IPlayerParser.PlayerInfo.TYPE_HLS,
                                uri = playUrl
                            )
                        )
                    }

                    else -> {
                        return@withContext ISourceParser.ParserResult.Complete(
                            IPlayerParser.PlayerInfo(
                                type = IPlayerParser.PlayerInfo.TYPE_OTHER,
                                uri = playUrl
                            )
                        )
                    }
                }
            }.onFailure {
                it.printStackTrace()
                return@withContext ISourceParser.ParserResult.Error(it, true)
            }
            return@withContext ISourceParser.ParserResult.Error(Exception("Unknown Error"), true)
        }
    }
}