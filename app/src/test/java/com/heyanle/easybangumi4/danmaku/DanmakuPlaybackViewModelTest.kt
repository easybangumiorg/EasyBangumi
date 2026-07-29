package com.heyanle.easybangumi4.danmaku

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DanmakuPlaybackViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun detailReadyMatchesBangumiOnceThenMatchesCurrentEpisodePosition() = runTest {
        val store = FakeStore()
        val model = model(store)

        start(model, episodeId = "local-2", sortedPosition = 2)
        advanceUntilIdle()

        assertEquals(1, store.searchBangumiCalls)
        assertEquals(1, store.loadEpisodeCalls)
        assertEquals(102L, store.lastSavedBinding?.remoteEpisodeId)
        assertTrue(model.state.value.status is DanmakuPlaybackStatus.Matched)

        model.onBangumiDetailAvailable(bangumiContext())
        advanceUntilIdle()
        assertEquals(1, store.searchBangumiCalls)
    }

    @Test
    fun failedBangumiMatchDoesNotLoadOrRematchEpisodesOnPlaybackChange() = runTest {
        val store = FakeStore().apply {
            searchResult = DanmakuResult.Success(emptyList())
        }
        val model = model(store)

        start(model, "local-1", 1)
        advanceUntilIdle()
        model.onPlaybackTargetChanged(episodeContext("local-2", 2))
        advanceUntilIdle()

        assertEquals(1, store.searchBangumiCalls)
        assertEquals(0, store.loadEpisodeCalls)
        assertTrue(model.state.value.status is DanmakuPlaybackStatus.Unmatched)

        model.beginManualMatch()
        assertEquals(DanmakuMatchPage.BANGUMI, model.state.value.manualMatch?.page)
        assertNull(model.state.value.manualMatch?.selectedBangumi)
        assertTrue(model.state.value.manualMatch?.episodes.isNullOrEmpty())
    }

    @Test
    fun sourceCanBeConfiguredAndRetriedWithoutChangingPlaybackTarget() = runTest {
        val store = FakeStore().apply { sourceAvailable = false }
        val model = model(store)
        start(model, "local-1", 1)
        advanceUntilIdle()
        assertTrue(model.state.value.status is DanmakuPlaybackStatus.Unavailable)

        store.sourceAvailable = true
        model.retry()
        advanceUntilIdle()

        assertTrue(model.state.value.status is DanmakuPlaybackStatus.Matched)
        assertEquals(101L, store.lastSavedBinding?.remoteEpisodeId)
    }

    @Test
    fun existingBindingRestoresBangumiWithoutSearchingAndRematchesByPosition() = runTest {
        val store = FakeStore().apply {
            bindings[episodeContext("local-1", 2).playbackKey] = binding(
                episodeContext("local-1", 2).playbackKey,
                origin = DanmakuMatchOrigin.MANUAL,
            )
        }
        val model = model(store)

        start(model, "local-1", 2)
        advanceUntilIdle()

        assertEquals(0, store.searchBangumiCalls)
        assertEquals(1, store.loadEpisodeCalls)
        assertEquals(DanmakuMatchOrigin.MANUAL, model.state.value.bangumiSelection?.origin)
        assertEquals(102L, store.lastSavedBinding?.remoteEpisodeId)
    }

    @Test
    fun manualFlowRequiresExplicitBangumiAndEpisodeSelection() = runTest {
        val manualBangumi = bangumi(id = 20L, title = "人工番剧")
        val manualEpisode = episode(id = 201L, bangumi = manualBangumi)
        val store = FakeStore().apply {
            searchResult = DanmakuResult.Success(emptyList())
            searchHandler = { query ->
                if (query == "人工") DanmakuResult.Success(listOf(manualBangumi))
                else DanmakuResult.Success(emptyList())
            }
            episodeHandler = { selected ->
                DanmakuResult.Success(listOf(episode(999L, selected), manualEpisode))
            }
        }
        val model = model(store)

        start(model, "local-2", 2)
        advanceUntilIdle()
        model.beginManualMatch()
        assertEquals(DanmakuMatchPage.BANGUMI, model.state.value.manualMatch?.page)

        // An episode cannot be committed until this draft explicitly selected its bangumi.
        model.selectManualEpisode(manualEpisode)
        assertNull(store.lastSavedBinding)

        model.updateManualQuery("人工")
        model.searchManualMatch()
        advanceUntilIdle()
        model.selectManualBangumi(manualBangumi)
        advanceUntilIdle()
        assertEquals(DanmakuMatchPage.EPISODE, model.state.value.manualMatch?.page)
        model.selectManualEpisode(manualEpisode)
        advanceUntilIdle()

        assertEquals(DanmakuMatchOrigin.MANUAL, store.lastSavedBinding?.origin)
        assertEquals(201L, store.lastSavedBinding?.remoteEpisodeId)
        assertEquals(manualBangumi, model.state.value.bangumiSelection?.bangumi)
    }

    @Test
    fun manualBangumiSurvivesPlaybackChangesAndOnlyEpisodePositionIsRematched() = runTest {
        val manualBangumi = bangumi(id = 20L, title = "人工番剧")
        val remoteEpisodes = listOf(
            episode(201L, manualBangumi),
            episode(202L, manualBangumi),
            episode(203L, manualBangumi),
        )
        val store = FakeStore().apply {
            searchHandler = { query ->
                if (query == "人工") DanmakuResult.Success(listOf(manualBangumi))
                else DanmakuResult.Success(emptyList())
            }
            episodeHandler = { DanmakuResult.Success(remoteEpisodes) }
        }
        val model = model(store)
        start(model, "local-1", 1)
        advanceUntilIdle()
        commitManual(model, manualBangumi, remoteEpisodes[0])
        val searchCallsAfterManual = store.searchBangumiCalls
        val episodeLoadsAfterManual = store.loadEpisodeCalls

        model.onPlaybackTargetChanged(episodeContext("local-3", 3, lineId = "other-line"))
        advanceUntilIdle()

        assertEquals(searchCallsAfterManual, store.searchBangumiCalls)
        assertEquals(episodeLoadsAfterManual, store.loadEpisodeCalls)
        assertEquals(203L, store.lastSavedBinding?.remoteEpisodeId)
        assertEquals(20L, store.lastSavedBinding?.remoteAnimeId)
        assertEquals(DanmakuMatchOrigin.MANUAL, model.state.value.bangumiSelection?.origin)

        model.beginManualMatch()
        assertEquals(DanmakuMatchPage.EPISODE, model.state.value.manualMatch?.page)
        assertEquals(manualBangumi, model.state.value.manualMatch?.selectedBangumi)
        assertEquals(remoteEpisodes, model.state.value.manualMatch?.episodes)
    }

    @Test
    fun automaticBangumiIsReusedAndCurrentSortPositionAloneSelectsEpisode() = runTest {
        val store = FakeStore()
        val model = model(store)
        start(model, "same-episode-object", 1)
        advanceUntilIdle()
        assertEquals(101L, store.lastSavedBinding?.remoteEpisodeId)

        model.onPlaybackTargetChanged(episodeContext("same-episode-object", 2, lineId = "line-2"))
        advanceUntilIdle()

        assertEquals(1, store.searchBangumiCalls)
        assertEquals(102L, store.lastSavedBinding?.remoteEpisodeId)
    }

    @Test
    fun automaticMatchOpensManualPanelAtEpisodeWithCommittedSelection() = runTest {
        val store = FakeStore()
        val model = model(store)
        start(model, "local-1", 1)
        advanceUntilIdle()
        val searchCalls = store.searchBangumiCalls
        val episodeLoads = store.loadEpisodeCalls

        model.beginManualMatch()

        val selection = model.state.value.bangumiSelection
        assertEquals(DanmakuMatchPage.EPISODE, model.state.value.manualMatch?.page)
        assertEquals(selection?.bangumi, model.state.value.manualMatch?.selectedBangumi)
        assertEquals(selection?.episodes, model.state.value.manualMatch?.episodes)
        assertEquals(101L, model.state.value.manualMatch?.selectedEpisode?.remoteEpisodeId)
        assertEquals(searchCalls, store.searchBangumiCalls)
        assertEquals(episodeLoads, store.loadEpisodeCalls)
    }

    @Test
    fun automaticBangumiSelectionStillOpensEpisodeStepWhenPositionIsUnmatched() = runTest {
        val store = FakeStore()
        val model = model(store)
        start(model, "local-99", 99)
        advanceUntilIdle()

        assertTrue(model.state.value.status is DanmakuPlaybackStatus.Unmatched)
        model.beginManualMatch()

        assertEquals(DanmakuMatchPage.EPISODE, model.state.value.manualMatch?.page)
        assertEquals(
            model.state.value.bangumiSelection?.bangumi,
            model.state.value.manualMatch?.selectedBangumi,
        )
        assertTrue(model.state.value.manualMatch?.episodes?.isNotEmpty() == true)
        assertNull(model.state.value.manualMatch?.selectedEpisode)
    }

    @Test
    fun lateAutomaticSelectionHydratesPristineOpenManualPanel() = runTest {
        val pendingAutomatic = CompletableDeferred<DanmakuResult<List<DanmakuBangumi>>>()
        val automaticBangumi = bangumi()
        val store = FakeStore().apply {
            searchHandler = {
                withContext(NonCancellable) { pendingAutomatic.await() }
            }
        }
        val model = model(store)
        start(model, "local-2", 2)
        runCurrent()

        model.beginManualMatch()
        assertEquals(DanmakuMatchPage.BANGUMI, model.state.value.manualMatch?.page)

        pendingAutomatic.complete(DanmakuResult.Success(listOf(automaticBangumi)))
        advanceUntilIdle()

        assertEquals(DanmakuMatchPage.EPISODE, model.state.value.manualMatch?.page)
        assertEquals(automaticBangumi, model.state.value.manualMatch?.selectedBangumi)
        assertEquals(
            model.state.value.bangumiSelection?.episodes,
            model.state.value.manualMatch?.episodes,
        )
        assertEquals(102L, model.state.value.manualMatch?.selectedEpisode?.remoteEpisodeId)
    }

    @Test
    fun lateAutomaticBangumiCannotOverwriteCommittedManualBangumi() = runTest {
        val pendingAutomatic = CompletableDeferred<DanmakuResult<List<DanmakuBangumi>>>()
        val automaticBangumi = bangumi()
        val manualBangumi = bangumi(20L, "人工番剧")
        val manualEpisode = episode(201L, manualBangumi)
        val store = FakeStore().apply {
            searchHandler = { query ->
                if (query == "测试番剧") {
                    withContext(NonCancellable) { pendingAutomatic.await() }
                } else {
                    DanmakuResult.Success(listOf(manualBangumi))
                }
            }
            episodeHandler = { selected ->
                DanmakuResult.Success(listOf(episode(201L, selected)))
            }
        }
        val model = model(store)
        start(model, "local-1", 1)
        runCurrent()

        model.beginManualMatch()
        model.updateManualQuery("人工")
        model.searchManualMatch()
        runCurrent()
        model.selectManualBangumi(manualBangumi)
        advanceUntilIdle()
        model.selectManualEpisode(manualEpisode)
        runCurrent()

        pendingAutomatic.complete(DanmakuResult.Success(listOf(automaticBangumi)))
        advanceUntilIdle()

        assertEquals(manualBangumi, model.state.value.bangumiSelection?.bangumi)
        assertEquals(DanmakuMatchOrigin.MANUAL, model.state.value.bangumiSelection?.origin)
    }

    @Test
    fun outOfBoundsPositionDoesNotCreateBindingOrLoadComments() = runTest {
        val store = FakeStore()
        val model = model(store)

        start(model, "local-99", 99)
        advanceUntilIdle()

        assertNull(store.lastSavedBinding)
        assertEquals(0, store.commentCalls)
        assertTrue(model.state.value.status is DanmakuPlaybackStatus.Unmatched)
    }

    private fun TestScope.commitManual(
        model: DanmakuPlaybackViewModel,
        manualBangumi: DanmakuBangumi,
        manualEpisode: DanmakuEpisode,
    ) {
        model.beginManualMatch()
        model.updateManualQuery("人工")
        model.searchManualMatch()
        advanceUntilIdle()
        model.selectManualBangumi(manualBangumi)
        advanceUntilIdle()
        model.selectManualEpisode(manualEpisode)
        advanceUntilIdle()
    }

    private fun start(
        model: DanmakuPlaybackViewModel,
        episodeId: String,
        sortedPosition: Int,
    ) {
        // Exercise the intentionally order-independent Compose integration.
        model.onPlaybackTargetChanged(episodeContext(episodeId, sortedPosition))
        model.onBangumiDetailAvailable(bangumiContext())
    }

    private fun model(store: FakeStore) = DanmakuPlaybackViewModel(
        repositoryOverride = store,
        requestCoordinatorOverride = DanmakuRequestCoordinator(store),
    )

    private fun bangumiContext() = DanmakuBangumiContext(
        cartoonId = "cartoon",
        cartoonSourceId = "source",
        title = "测试番剧",
    )

    private fun episodeContext(
        episodeId: String,
        sortedPosition: Int,
        lineId: String = "line",
    ) = DanmakuEpisodeContext(
        playbackKey = DanmakuPlaybackKey("cartoon", "source", lineId, episodeId),
        sortedEpisodePosition = sortedPosition,
    )

    private fun bangumi(id: Long = 10L, title: String = "测试番剧") = DanmakuBangumi(
        remoteAnimeId = id,
        remoteBangumiId = id.toString(),
        title = title,
    )

    private fun episode(id: Long, bangumi: DanmakuBangumi = bangumi()) = DanmakuEpisode(
        remoteEpisodeId = id,
        remoteAnimeId = bangumi.remoteAnimeId,
        remoteBangumiId = bangumi.remoteBangumiId,
        bangumiTitle = bangumi.title,
        episodeTitle = "选集 $id",
        episodeNumber = "误导字段",
    )

    private fun binding(
        key: DanmakuPlaybackKey,
        origin: DanmakuMatchOrigin,
    ) = DanmakuBinding(
        playbackKey = key,
        sourceId = DANDANPLAY_SOURCE_ID,
        remoteEpisodeId = 999L,
        remoteAnimeId = 10L,
        remoteBangumiId = "10",
        bangumiTitle = "测试番剧",
        episodeTitle = "旧绑定",
        timeOffsetMillis = 0L,
        origin = origin,
        createdAtMillis = 0L,
        updatedAtMillis = 0L,
    )

    private inner class FakeStore : DanmakuPlaybackStore {
        private val defaultBangumi = bangumi()
        private val defaultEpisodes = listOf(episode(101L), episode(102L), episode(103L))
        private val source = object : DanmakuSource {
            override val metadata = DanmakuSourceMetadata(
                DANDANPLAY_SOURCE_ID,
                "弹弹play",
                "测试",
                "https://example.com",
            )

            override fun isAvailable() = sourceAvailable
            override suspend fun searchBangumi(query: String) =
                DanmakuResult.Success(emptyList<DanmakuBangumi>())
            override suspend fun loadEpisodes(bangumi: DanmakuBangumi) =
                DanmakuResult.Success(emptyList<DanmakuEpisode>())
            override suspend fun loadComments(remoteEpisodeId: Long) =
                DanmakuResult.Success(emptyList<DanmakuComment>())
        }

        val bindings = linkedMapOf<DanmakuPlaybackKey, DanmakuBinding>()
        var lastSavedBinding: DanmakuBinding? = null
        var searchBangumiCalls = 0
        var loadEpisodeCalls = 0
        var commentCalls = 0
        var sourceAvailable = true
        var searchResult: DanmakuResult<List<DanmakuBangumi>> =
            DanmakuResult.Success(listOf(defaultBangumi))
        var searchHandler: (suspend (String) -> DanmakuResult<List<DanmakuBangumi>>)? = null
        var episodeHandler: (suspend (DanmakuBangumi) -> DanmakuResult<List<DanmakuEpisode>>)? = null

        override fun defaultSource(): DanmakuSource = source
        override fun binding(playbackKey: DanmakuPlaybackKey) = bindings[playbackKey]
        override fun saveBinding(binding: DanmakuBinding) {
            bindings[binding.playbackKey] = binding
            lastSavedBinding = binding
        }

        override suspend fun searchBangumi(
            sourceId: String,
            query: String,
        ): DanmakuResult<List<DanmakuBangumi>> {
            searchBangumiCalls++
            return searchHandler?.invoke(query) ?: searchResult
        }

        override suspend fun loadEpisodes(
            sourceId: String,
            bangumi: DanmakuBangumi,
        ): DanmakuResult<List<DanmakuEpisode>> {
            loadEpisodeCalls++
            return episodeHandler?.invoke(bangumi) ?: DanmakuResult.Success(defaultEpisodes)
        }

        override suspend fun loadComments(
            sourceId: String,
            remoteEpisodeId: Long,
            isActiveShow: Boolean,
            isArchivedShow: Boolean,
        ): DanmakuResult<List<DanmakuComment>> {
            commentCalls++
            return DanmakuResult.Success(
                listOf(
                    DanmakuComment(
                        id = 1L,
                        timeMillis = 1_000L,
                        mode = DanmakuDisplayMode.SCROLL,
                        colorArgb = 0xFFFFFF,
                        userId = null,
                        text = "测试",
                    ),
                ),
            )
        }
    }
}
