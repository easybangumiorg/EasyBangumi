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
import org.easybangumi.next.source.inner.anich.AniChManager
import kotlin.time.Clock


class AniChPlayComponent : PlayComponent, BaseComponent() {

    private val logger = logger()
    private val networkHelper: NetworkHelper by inject()
    private val prefHelper: PreferenceHelper by inject()
    private val webViewHelper: WebViewHelper by inject()
    private val aniChManager: AniChManager by inject()

    // 缓存数据结构
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    )

    // 缓存过期时间：15 分钟
    private val CACHE_EXPIRY = 15 * 60 * 1000L

    // 缓存最大条目数
    private val MAX_CACHE_SIZE = 50

    // 剧集缓存：key = "anich_{bangumiId}"
    private val episodeCache = mutableMapOf<String, CacheEntry<List<EpisodeSimple>>>()

    // 播放源缓存：key = "anich_{bangumiId}_{episodeOrder}"
    private val sitesCache = mutableMapOf<String, CacheEntry<List<PlayLineSimple>>>()

    // 检查缓存是否有效
    private fun <T> isCacheValid(entry: CacheEntry<T>?): Boolean {
        return entry != null && (Clock.System.now().toEpochMilliseconds() - entry.timestamp) < CACHE_EXPIRY
    }

    // 清理过期缓存并限制大小
    private fun cleanupCache() {
        val now = Clock.System.now().toEpochMilliseconds()
        episodeCache.entries.removeIf { (now - it.value.timestamp) >= CACHE_EXPIRY }
        sitesCache.entries.removeIf { (now - it.value.timestamp) >= CACHE_EXPIRY }

        // 如果仍然超过限制，移除最旧的条目
        while (episodeCache.size > MAX_CACHE_SIZE) {
            val oldestKey = episodeCache.minByOrNull { it.value.timestamp }?.key
            oldestKey?.let { episodeCache.remove(it) }
        }
        while (sitesCache.size > MAX_CACHE_SIZE) {
            val oldestKey = sitesCache.minByOrNull { it.value.timestamp }?.key
            oldestKey?.let { sitesCache.remove(it) }
        }
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

            val result = aniChManager.getEpisodes(cartoonIndex.id)
            when (result) {
                is DataState.Ok -> {
                    val episodes = result.data
                    // 缓存结果（清理过期条目后）
                    cleanupCache()
                    episodeCache[cacheKey] = CacheEntry(episodes)
                    logger.info("AniChPlayComponent getEpisodeList episodes=${episodes.size} for ${cartoonIndex.id}")
                    episodes
                }
                is DataState.Error -> {
                    throw DataStateException(result.errorMsg ?: "获取剧集列表失败")
                }
                else -> {
                    throw DataStateException("获取剧集列表请求未完成")
                }
            }
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

            val result = aniChManager.getPlayLines(cartoonIndex.id, episode.order)
            when (result) {
                is DataState.Ok -> {
                    val playLines = result.data
                    // 缓存结果（清理过期条目后）
                    cleanupCache()
                    sitesCache[cacheKey] = CacheEntry(playLines)
                    logger.info("AniChPlayComponent getPlayLineSimpleForEpisode playLines=${playLines.size} for ${cartoonIndex.id} episode=${episode.order}")
                    playLines
                }
                is DataState.Error -> {
                    throw DataStateException(result.errorMsg ?: "获取播放线路失败")
                }
                else -> {
                    throw DataStateException("获取播放线路请求未完成")
                }
            }
        }
    }

    override suspend fun getPlayInfoSimple(
        cartoonIndex: CartoonIndex,
        playLineSimple: PlayLineSimple,
        episodeSimple: EpisodeSimple
    ): DataState<PlayInfo> {
        return withResult {
            val result = aniChManager.getPlayUrl(cartoonIndex.id, episodeSimple.order, playLineSimple)
            when (result) {
                is DataState.Ok -> {
                    val playUrl = result.data
                    logger.info("AniChPlayComponent getPlayInfoSimple intercepted playUrl: $playUrl")
                    PlayInfo(
                        type = if (playUrl.contains(".m3u8")) PlayInfo.TYPE_HLS else PlayInfo.TYPE_NORMAL,
                        url = playUrl
                    )
                }
                is DataState.Error -> {
                    throw DataStateException(result.errorMsg ?: "获取播放地址失败")
                }
                else -> {
                    throw DataStateException("获取播放地址请求未完成")
                }
            }
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
