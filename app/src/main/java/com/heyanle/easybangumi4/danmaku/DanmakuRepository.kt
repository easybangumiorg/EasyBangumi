package com.heyanle.easybangumi4.danmaku

import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicLong

interface DanmakuPlaybackRepository {
    suspend fun searchBangumi(
        sourceId: String,
        query: String,
    ): DanmakuResult<List<DanmakuBangumi>>

    suspend fun loadEpisodes(
        sourceId: String,
        bangumi: DanmakuBangumi,
    ): DanmakuResult<List<DanmakuEpisode>>

    suspend fun loadComments(
        sourceId: String,
        remoteEpisodeId: Long,
        isActiveShow: Boolean = false,
        isArchivedShow: Boolean = false,
    ): DanmakuResult<List<DanmakuComment>>
}

interface DanmakuPlaybackStore : DanmakuPlaybackRepository {
    fun defaultSource(): DanmakuSource?

    fun binding(playbackKey: DanmakuPlaybackKey): DanmakuBinding?

    fun saveBinding(binding: DanmakuBinding)

}

class DanmakuRepository(
    private val sourceRegistry: InnerDanmakuSourceRegistry,
    private val preferences: DanmakuPreferences,
    private val storage: DanmakuStorage,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : DanmakuPlaybackStore {
    override fun defaultSource(): DanmakuSource? {
        val enabled = preferences.enabledSourceIds.get()
        val preferred = preferences.defaultSourceId.get()
        return sourceRegistry.source(preferred)
            ?.takeIf { it.metadata.id in enabled }
            ?: sourceRegistry.enabledAndAvailable(enabled).firstOrNull()
    }

    fun source(id: String): DanmakuSource? = sourceRegistry.source(id)

    override fun binding(playbackKey: DanmakuPlaybackKey): DanmakuBinding? = storage.binding(playbackKey)

    override fun saveBinding(binding: DanmakuBinding) = storage.saveBinding(binding)

    override suspend fun searchBangumi(
        sourceId: String,
        query: String,
    ): DanmakuResult<List<DanmakuBangumi>> {
        val source = source(id = sourceId) ?: return DanmakuResult.Unavailable("弹幕源不可用")
        val now = nowMillis()
        storage.bangumiCache(sourceId, query, now)?.let {
            return DanmakuResult.Success(it.bangumis, fromCache = true)
        }
        return source.searchBangumi(query).also { result ->
            if (result is DanmakuResult.Success) {
                storage.saveBangumiCache(
                    DanmakuBangumiCacheEntry(
                        sourceId = sourceId,
                        query = query,
                        createdAtMillis = now,
                        expiresAtMillis = now + DanmakuCachePolicy.DEFAULT_MILLIS,
                        bangumis = result.value,
                    ),
                )
            }
        }
    }

    override suspend fun loadEpisodes(
        sourceId: String,
        bangumi: DanmakuBangumi,
    ): DanmakuResult<List<DanmakuEpisode>> {
        val source = source(id = sourceId) ?: return DanmakuResult.Unavailable("弹幕源不可用")
        val now = nowMillis()
        storage.episodeCache(sourceId, bangumi.remoteAnimeId, bangumi.remoteBangumiId, now)?.let {
            return DanmakuResult.Success(it.episodes, fromCache = true)
        }
        return source.loadEpisodes(bangumi).also { result ->
            if (result is DanmakuResult.Success) {
                storage.saveEpisodeCache(
                    DanmakuEpisodeCacheEntry(
                        sourceId = sourceId,
                        remoteAnimeId = bangumi.remoteAnimeId,
                        remoteBangumiId = bangumi.remoteBangumiId,
                        createdAtMillis = now,
                        expiresAtMillis = now + DanmakuCachePolicy.DEFAULT_MILLIS,
                        episodes = result.value,
                    ),
                )
            }
        }
    }

    override suspend fun loadComments(
        sourceId: String,
        remoteEpisodeId: Long,
        isActiveShow: Boolean,
        isArchivedShow: Boolean,
    ): DanmakuResult<List<DanmakuComment>> {
        val source = source(id = sourceId) ?: return DanmakuResult.Unavailable("弹幕源不可用")
        val now = nowMillis()
        storage.commentCache(sourceId, remoteEpisodeId, now)?.let {
            return DanmakuResult.Success(it.comments, fromCache = true)
        }
        return source.loadComments(remoteEpisodeId).also { result ->
            if (result is DanmakuResult.Success) {
                storage.saveCommentCache(
                    DanmakuCommentCacheEntry(
                        sourceId = sourceId,
                        remoteEpisodeId = remoteEpisodeId,
                        createdAtMillis = now,
                        expiresAtMillis = now + DanmakuCachePolicy.ttlMillis(isActiveShow, isArchivedShow),
                        comments = result.value,
                    ),
                )
            }
        }
    }

    suspend fun loadComments(
        sourceId: String,
        remoteEpisodeId: Long,
    ): DanmakuResult<List<DanmakuComment>> {
        return loadComments(sourceId, remoteEpisodeId, isActiveShow = false, isArchivedShow = false)
    }
}

/**
 * Binds bounded source work to the active playback episode. Calls are cancellable by their caller
 * and any result that completes after [begin] was called for another episode becomes [Stale].
 */
class DanmakuRequestCoordinator(
    private val repository: DanmakuPlaybackRepository,
    private val requestTimeoutMillis: Long = REQUEST_TIMEOUT_MILLIS,
) {
    private val nextGeneration = AtomicLong(0L)

    @Volatile
    private var activeSession: DanmakuRequestSession? = null

    fun begin(playbackKey: DanmakuPlaybackKey): DanmakuRequestSession {
        return DanmakuRequestSession(playbackKey, nextGeneration.incrementAndGet()).also {
            activeSession = it
        }
    }

    fun invalidate() {
        activeSession = null
        nextGeneration.incrementAndGet()
    }

    fun isCurrent(session: DanmakuRequestSession): Boolean = activeSession == session

    suspend fun loadComments(
        session: DanmakuRequestSession,
        binding: DanmakuBinding,
    ): DanmakuResult<List<DanmakuComment>> {
        return protect(session) {
            repository.loadComments(binding.sourceId, binding.remoteEpisodeId)
        }
    }

    suspend fun searchBangumi(
        session: DanmakuRequestSession,
        sourceId: String,
        query: String,
    ): DanmakuResult<List<DanmakuBangumi>> {
        return protect(session) {
            repository.searchBangumi(sourceId, query)
        }
    }

    suspend fun loadEpisodes(
        session: DanmakuRequestSession,
        sourceId: String,
        bangumi: DanmakuBangumi,
    ): DanmakuResult<List<DanmakuEpisode>> {
        return protect(session) {
            repository.loadEpisodes(sourceId, bangumi)
        }
    }

    private suspend fun <T> protect(
        session: DanmakuRequestSession,
        request: suspend () -> DanmakuResult<T>,
    ): DanmakuResult<T> {
        val result = withTimeoutOrNull(requestTimeoutMillis) { request() }
            ?: DanmakuResult.Unavailable("弹幕请求超时")
        return if (isCurrent(session)) result else DanmakuResult.Stale
    }

    private companion object {
        const val REQUEST_TIMEOUT_MILLIS = 15_000L
    }
}
