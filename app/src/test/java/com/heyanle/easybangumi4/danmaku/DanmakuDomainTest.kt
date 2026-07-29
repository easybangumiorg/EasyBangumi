package com.heyanle.easybangumi4.danmaku

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DanmakuDomainTest {

    @Test
    fun registryContainsOnlyThePackagedDanDanPlaySource() {
        val registry = InnerDanmakuSourceRegistry.create(
            DanDanPlaySource(DanDanPlayCredentials(appId = "", appSecret = "")),
        )

        assertEquals(listOf(DANDANPLAY_SOURCE_ID), registry.sources.map { it.metadata.id })
        assertFalse(registry.sources.single().metadata.isRemovable)
    }

    @Test
    fun bindingLookupReusesOnlyTheExactPlaybackEpisode() {
        val key = playbackKey("1")
        val binding = bindingFor(key)

        assertEquals(binding, listOf(binding).bindingFor(key))
        assertNull(listOf(binding).bindingFor(playbackKey("2")))
    }

    @Test
    fun cacheExpiryAndPolicyUseExpectedBoundaries() {
        val entry = DanmakuCommentCacheEntry(
            sourceId = DANDANPLAY_SOURCE_ID,
            remoteEpisodeId = 100L,
            createdAtMillis = 100L,
            expiresAtMillis = 200L,
            comments = emptyList(),
        )

        assertTrue(entry.isValid(199L))
        assertFalse(entry.isValid(200L))
        assertEquals(DanmakuCachePolicy.ACTIVE_SHOW_MILLIS, DanmakuCachePolicy.ttlMillis(true, false))
        assertEquals(DanmakuCachePolicy.ARCHIVE_MILLIS, DanmakuCachePolicy.ttlMillis(false, true))
    }

    @Test
    fun stalePlaybackSessionResultIsDiscarded() = runBlocking {
        val pendingResult = CompletableDeferred<DanmakuResult<List<DanmakuBangumi>>>()
        val coordinator = DanmakuRequestCoordinator(
            object : DanmakuPlaybackRepository {
                override suspend fun searchBangumi(
                    sourceId: String,
                    query: String,
                ): DanmakuResult<List<DanmakuBangumi>> = pendingResult.await()

                override suspend fun loadEpisodes(
                    sourceId: String,
                    bangumi: DanmakuBangumi,
                ) = DanmakuResult.Success(emptyList<DanmakuEpisode>())

                override suspend fun loadComments(
                    sourceId: String,
                    remoteEpisodeId: Long,
                    isActiveShow: Boolean,
                    isArchivedShow: Boolean,
                ): DanmakuResult<List<DanmakuComment>> = DanmakuResult.Success(emptyList())
            },
        )
        val firstSession = coordinator.begin(playbackKey("1"))
        val result = async {
            coordinator.searchBangumi(
                session = firstSession,
                sourceId = DANDANPLAY_SOURCE_ID,
                query = "测试番剧",
            )
        }

        coordinator.begin(playbackKey("2"))
        pendingResult.complete(DanmakuResult.Success(listOf(bangumi())))

        assertEquals(DanmakuResult.Stale, result.await())
    }

    @Test
    fun staleCommentResultIsDiscardedAfterEpisodeChange() = runBlocking {
        val pendingResult = CompletableDeferred<DanmakuResult<List<DanmakuComment>>>()
        val repository = object : DanmakuPlaybackRepository {
            override suspend fun searchBangumi(
                sourceId: String,
                query: String,
            ) = DanmakuResult.Success(emptyList<DanmakuBangumi>())

            override suspend fun loadEpisodes(
                sourceId: String,
                bangumi: DanmakuBangumi,
            ) = DanmakuResult.Success(emptyList<DanmakuEpisode>())

            override suspend fun loadComments(
                sourceId: String,
                remoteEpisodeId: Long,
                isActiveShow: Boolean,
                isArchivedShow: Boolean,
            ) = pendingResult.await()
        }
        val coordinator = DanmakuRequestCoordinator(repository)
        val firstSession = coordinator.begin(playbackKey("1"))
        val result = async { coordinator.loadComments(firstSession, bindingFor(playbackKey("1"))) }

        coordinator.begin(playbackKey("2"))
        pendingResult.complete(DanmakuResult.Success(emptyList()))

        assertEquals(DanmakuResult.Stale, result.await())
    }

    @Test
    fun requestCoordinatorReturnsRetryableUnavailableOnTimeout() = runTest {
        val repository = object : DanmakuPlaybackRepository {
            override suspend fun searchBangumi(
                sourceId: String,
                query: String,
            ): DanmakuResult<List<DanmakuBangumi>> {
                delay(5_000L)
                return DanmakuResult.Success(emptyList())
            }

            override suspend fun loadEpisodes(
                sourceId: String,
                bangumi: DanmakuBangumi,
            ) = DanmakuResult.Success(emptyList<DanmakuEpisode>())

            override suspend fun loadComments(
                sourceId: String,
                remoteEpisodeId: Long,
                isActiveShow: Boolean,
                isArchivedShow: Boolean,
            ) = DanmakuResult.Success(emptyList<DanmakuComment>())
        }
        val coordinator = DanmakuRequestCoordinator(repository, requestTimeoutMillis = 1_000L)
        val session = coordinator.begin(playbackKey("1"))

        val result = coordinator.searchBangumi(
            session = session,
            sourceId = DANDANPLAY_SOURCE_ID,
            query = "测试番剧",
        )

        assertEquals(DanmakuResult.Unavailable("弹幕请求超时"), result)
    }

    private fun playbackKey(episodeId: String) = DanmakuPlaybackKey(
        cartoonId = "cartoon",
        cartoonSourceId = "source",
        playLineId = "line",
        episodeId = episodeId,
    )

    private fun bindingFor(key: DanmakuPlaybackKey) = DanmakuBinding(
        playbackKey = key,
        sourceId = DANDANPLAY_SOURCE_ID,
        remoteEpisodeId = 100L,
        remoteAnimeId = 10L,
        remoteBangumiId = "10",
        bangumiTitle = "测试番剧",
        episodeTitle = "第1集",
        timeOffsetMillis = 0L,
        origin = DanmakuMatchOrigin.MANUAL,
        createdAtMillis = 0L,
        updatedAtMillis = 0L,
    )

    private fun episode(id: Long) = DanmakuEpisode(
        remoteEpisodeId = id,
        remoteAnimeId = 10L,
        remoteBangumiId = "10",
        bangumiTitle = "测试番剧",
        episodeTitle = "第1集",
        episodeNumber = "1",
    )

    private fun bangumi() = DanmakuBangumi(
        remoteAnimeId = 10L,
        remoteBangumiId = "10",
        title = "测试番剧",
    )
}
