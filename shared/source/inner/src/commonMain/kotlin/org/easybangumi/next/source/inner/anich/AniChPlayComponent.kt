package org.easybangumi.next.source.inner.anich

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.DataStateException
import org.easybangumi.next.lib.utils.withResult
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.Episode
import org.easybangumi.next.shared.data.cartoon.EpisodeSimple
import org.easybangumi.next.shared.data.cartoon.PlayInfo
import org.easybangumi.next.shared.data.cartoon.PlayLineSimple
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.api.utils.NetworkHelper
import org.easybangumi.next.shared.source.api.utils.PreferenceHelper
import org.easybangumi.next.shared.source.api.utils.WebViewHelper
import org.easybangumi.next.shared.source.api.utils.closeFinally
import org.koin.core.component.inject
import kotlin.getValue

class AniChPlayComponent : PlayComponent, BaseComponent() {

    private val logger = logger()
    private val networkHelper: NetworkHelper by inject()
    private val prefHelper: PreferenceHelper by inject()
    private val webViewHelper: WebViewHelper by inject()

    // 缓存数据结构
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis()
    )

    // 缓存过期时间：15 分钟
    private val CACHE_EXPIRY = 15 * 60 * 1000L

    // 剧集缓存：key = "anich_{bangumiId}"
    private val episodeCache = mutableMapOf<String, CacheEntry<List<EpisodeSimple>>>()

    // 播放源缓存：key = "anich_{bangumiId}_{episodeOrder}"
    private val sitesCache = mutableMapOf<String, CacheEntry<List<PlayLineSimple>>>()

    // 检查缓存是否有效
    private fun <T> isCacheValid(entry: CacheEntry<T>?): Boolean {
        return entry != null && (System.currentTimeMillis() - entry.timestamp) < CACHE_EXPIRY
    }

    // 剧集优先模式
    override suspend fun isEpisodeFirstMode(cartoonIndex: CartoonIndex): Boolean = true

    // 线路优先模式的默认实现（剧集优先模式不使用）
    override suspend fun getPlayLines(cartoonIndex: CartoonIndex): DataState<List<PlayerLine>> {
        return DataState.error("AniCh uses episode-first mode, use getEpisodeList instead")
    }

    override suspend fun getPlayInfo(
        cartoonIndex: CartoonIndex,
        playerLine: PlayerLine,
        episode: Episode
    ): DataState<PlayInfo> {
        return DataState.error("AniCh uses episode-first mode, use getPlayInfoSimple instead")
    }

    override suspend fun getEpisodeList(
        cartoonIndex: CartoonIndex
    ): DataState<List<EpisodeSimple>> {
        return withResult {
            val cacheKey = "anich_${cartoonIndex.id}"

            // 检查缓存
            val cached = episodeCache[cacheKey]
            if (isCacheValid(cached)) {
                logger.info("AniChPlayComponent getEpisodeList cache hit for $cacheKey")
                return@withResult cached!!.data
            }

            val host = prefHelper.get("web_host", "anich.emmmm.eu.org")
            val id = cartoonIndex.id
            val url = "https://$host/b/$id"

            logger.info("AniChPlayComponent getEpisodeList loading url: $url")
            val web = webViewHelper.newWebView()
            web.init(userAgent = networkHelper.defaultLinuxUA, needBlob = false)

            web.loadUrl(url)
            web.waitingForPageLoaded(5000L)

            // 提取 window.$data 中的剧集列表
            val script = """
                (function() {
                    try {
                        var data = window.${'$'}data;
                        var bangumi = data['bangumi-$id'];
                        if (bangumi && bangumi.episodes) {
                            return JSON.stringify(bangumi.episodes);
                        }
                        return JSON.stringify([]);
                    } catch(e) {
                        return JSON.stringify([]);
                    }
                })()
            """.trimIndent()

            val jsonData = web.executeJavaScriptWithCallback(script, 5000L) ?: "[]"
            web.closeFinally()

            val episodes = parseEpisodes(jsonData)

            // 缓存结果
            episodeCache[cacheKey] = CacheEntry(episodes)

            logger.info("AniChPlayComponent getEpisodeList episodes=${episodes.size} for $id")
            episodes
        }
    }

    override suspend fun getPlayLineSimpleForEpisode(
        cartoonIndex: CartoonIndex,
        episode: EpisodeSimple
    ): DataState<List<PlayLineSimple>> {
        return withResult {
            val cacheKey = "anich_${cartoonIndex.id}_${episode.order}"

            // 检查缓存
            val cached = sitesCache[cacheKey]
            if (isCacheValid(cached)) {
                logger.info("AniChPlayComponent getPlayLineSimpleForEpisode cache hit for $cacheKey")
                return@withResult cached!!.data
            }

            val host = prefHelper.get("web_host", "anich.emmmm.eu.org")
            val id = cartoonIndex.id
            val url = "https://$host/b/$id"

            logger.info("AniChPlayComponent getPlayLineSimpleForEpisode loading url: $url for episode: ${episode.order}")
            val web = webViewHelper.newWebView()
            web.init(userAgent = networkHelper.defaultLinuxUA, needBlob = false)

            web.loadUrl(url)
            web.waitingForPageLoaded(5000L)

            // 提取指定剧集的播放源
            val script = """
                (function() {
                    try {
                        var data = window.${'$'}data;
                        var bangumi = data['bangumi-$id'];
                        if (bangumi && bangumi.episodes) {
                            var ep = bangumi.episodes.find(function(e) { return e.sort === ${episode.order}; });
                            if (ep && ep.sites) {
                                return JSON.stringify(ep.sites);
                            }
                        }
                        return JSON.stringify([]);
                    } catch(e) {
                        return JSON.stringify([]);
                    }
                })()
            """.trimIndent()

            val jsonData = web.executeJavaScriptWithCallback(script, 5000L) ?: "[]"
            web.closeFinally()

            val playLines = parsePlayLines(jsonData)

            // 缓存结果
            sitesCache[cacheKey] = CacheEntry(playLines)

            logger.info("AniChPlayComponent getPlayLineSimpleForEpisode playLines=${playLines.size} for $id episode=${episode.order}")
            playLines
        }
    }

    override suspend fun getPlayInfoSimple(
        cartoonIndex: CartoonIndex,
        playLineSimple: PlayLineSimple,
        episodeSimple: EpisodeSimple
    ): DataState<PlayInfo> {
        return withResult {
            val host = prefHelper.get("web_host", "anich.emmmm.eu.org")
            val id = cartoonIndex.id
            val episode = episodeSimple.order
            val url = "https://$host/b/$id/$episode"

            logger.info("AniChPlayComponent getPlayInfoSimple loading url: $url, playLine: ${playLineSimple.label}, episode: ${episodeSimple.label}")

            // 使用 WebView 拦截视频资源
            val result = webViewHelper.use {
                loadUrl(
                    url = url,
                    userAgent = networkHelper.defaultLinuxUA,
                    interceptResRegex = ".*\\.m3u8|.*\\.mp4.*"
                )
                waitingForPageLoaded(5000L)
                waitingForResourceLoaded(".*\\.m3u8|.*\\.mp4.*", true, 10000L)
            }

            val playUrl = result
                ?: throw DataStateException("未找到播放地址")

            logger.info("AniChPlayComponent getPlayInfoSimple intercepted playUrl: $playUrl")

            PlayInfo(
                type = if (playUrl.contains(".m3u8")) PlayInfo.TYPE_HLS else PlayInfo.TYPE_NORMAL,
                url = playUrl
            )
        }
    }

    private fun parseEpisodes(jsonData: String): List<EpisodeSimple> {
        return try {
            val episodes = Json.parseToJsonElement(jsonData).jsonArray
            episodes
                .filter {
                    it.jsonObject["status"]?.jsonPrimitive?.boolean == true
                }
                .map { ep ->
                    val obj = ep.jsonObject
                    val sort = obj["sort"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    val title = obj["title"]?.jsonPrimitive?.content ?: ""
                    EpisodeSimple(
                        id = sort.toString(),
                        label = "第${sort}集 $title",
                        order = sort,
                        sourceName = "AniCh"
                    )
                }
        } catch (e: Exception) {
            logger.error("AniCh parseEpisodes error", e)
            emptyList()
        }
    }

    private fun parsePlayLines(jsonData: String): List<PlayLineSimple> {
        return try {
            val sites = Json.parseToJsonElement(jsonData).jsonArray
            sites.mapIndexed { index, site ->
                val obj = site.jsonObject
                PlayLineSimple(
                    id = obj["id"]?.jsonPrimitive?.content ?: index.toString(),
                    label = obj["site"]?.jsonPrimitive?.content ?: "源${index + 1}",
                    order = index
                )
            }
        } catch (e: Exception) {
            logger.error("AniCh parsePlayLines error", e)
            emptyList()
        }
    }
}
