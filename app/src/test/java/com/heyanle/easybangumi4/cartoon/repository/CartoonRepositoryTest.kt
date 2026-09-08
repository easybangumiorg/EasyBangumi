package com.heyanle.easybangumi4.cartoon.repository

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.plugin.api.entity.CartoonImpl
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CartoonRepositoryTest {

    @Test
    fun sharedStatePublishesCacheThenDeduplicatedNetworkResult() = runTest {
        val cached = detailedInfo(name = "缓存标题").copy(starTime = 123L)
        val dao = FakeCartoonInfoDao(cached)
        val allowRemote = CompletableDeferred<Unit>()
        var remoteCalls = 0
        val repository = CartoonRepository(
            cartoonInfoDao = dao,
            remoteLoader = { _, _ ->
                remoteCalls++
                allowRemote.await()
                DataResult.ok(remotePayload("网络标题"))
            },
            sourceName = "测试源",
            remoteMerger = ::mergeRemote,
        )

        val firstState = repository.cartoonInfo(cached.id, cached.source)
        assertSame(firstState, repository.cartoonInfo(cached.id, cached.source))
        val first = async { repository.awaitCartoonInfoWithPlayLines(cached.id, cached.source) }
        yield()
        val second = async {
            repository.awaitCartoonInfoWithPlayLines(
                cached.id,
                cached.source,
                forceRefresh = true,
            )
        }
        yield()

        val cacheResult = firstState.value as DataResult.Ok
        assertEquals("缓存标题", cacheResult.data.name)
        allowRemote.complete(Unit)

        val firstResult = first.await() as DataResult.Ok
        val secondResult = second.await() as DataResult.Ok
        assertEquals(1, remoteCalls)
        assertFalse(firstResult.isCache)
        assertFalse(secondResult.isCache)
        assertFalse((firstState.value as DataResult.Ok).isCache)
        assertEquals("网络标题", firstResult.data.name)
        assertEquals(123L, firstResult.data.starTime)
        assertEquals("网络标题", dao.getByCartoonSummary(cached.id, cached.source)?.name)
    }

    @Test
    fun localMutationUpdatesTheSharedStateWithoutNetwork() = runTest {
        val initial = detailedInfo(name = "初始")
        val repository = CartoonRepository(
            cartoonInfoDao = FakeCartoonInfoDao(initial),
            remoteLoader = { _, _ -> error("network must not be called") },
            sourceName = "测试源",
            remoteMerger = ::mergeRemote,
        )
        val sharedState = repository.cartoonInfo(initial.id, initial.source)
        repository.cachedCartoonInfo(initial.id, initial.source)

        repository.updateCartoonInfo(initial.id, initial.source) {
            it.copy(lastProcessTime = 42_000L)
        }

        val state = sharedState.value as DataResult.Ok
        assertTrue(state.isCache)
        assertEquals(42_000L, state.data.lastProcessTime)
    }

    private fun detailedInfo(name: String) = CartoonInfo(
        id = "cartoon-1",
        source = "source-1",
        url = "https://example.test/cartoon-1",
        name = name,
        coverUrl = "",
        intro = "",
        isDetailed = true,
        isPlayLineLoad = true,
    )

    private fun mergeRemote(
        local: CartoonInfo?,
        cartoon: com.heyanle.easybangumi4.plugin.api.entity.Cartoon,
        sourceName: String,
        @Suppress("UNUSED_PARAMETER") playLines: List<PlayLine>,
    ): CartoonInfo = (local ?: detailedInfo(cartoon.title)).copy(
        name = cartoon.title,
        url = cartoon.url,
        sourceName = sourceName,
        isDetailed = true,
        isPlayLineLoad = true,
    )

    private fun remotePayload(title: String) = CartoonImpl(
        id = "cartoon-1",
        source = "source-1",
        url = "https://example.test/cartoon-1",
        title = title,
    ) to listOf(
        PlayLine(
            id = "line-1",
            label = "线路一",
            episode = arrayListOf(Episode("episode-1", "第 1 集", 1)),
        ),
    )

    private class FakeCartoonInfoDao(vararg initial: CartoonInfo) : CartoonInfoDao {
        private val values = linkedMapOf<Pair<String, String>, CartoonInfo>()
        private val all = MutableStateFlow<List<CartoonInfo>>(emptyList())
        private val perKey = mutableMapOf<Pair<String, String>, MutableStateFlow<CartoonInfo?>>()

        init {
            initial.forEach { values[it.id to it.source] = it }
            publish()
        }

        override suspend fun insert(cartoonInfo: CartoonInfo) {
            values[cartoonInfo.id to cartoonInfo.source] = cartoonInfo
            publish()
        }

        override suspend fun update(cartoonInfo: CartoonInfo) = insert(cartoonInfo)

        override suspend fun delete(cartoonInfo: CartoonInfo) {
            values.remove(cartoonInfo.id to cartoonInfo.source)
            publish()
        }

        override fun flowAll(): Flow<List<CartoonInfo>> = all
        override fun getAll(): List<CartoonInfo> = values.values.toList()

        override suspend fun getByCartoonSummary(id: String, source: String): CartoonInfo? =
            values[id to source]

        override fun flowByCartoonSummary(id: String, source: String): Flow<CartoonInfo?> =
            perKey.getOrPut(id to source) { MutableStateFlow(values[id to source]) }

        override suspend fun getAllBySource(source: String): List<CartoonInfo> =
            values.values.filter { it.source == source }

        override suspend fun deleteByCartoonSummary(id: String, source: String) {
            values.remove(id to source)
            publish()
        }

        override suspend fun clearAll() {
            values.clear()
            publish()
        }

        override fun flowAllHistory(): Flow<List<CartoonInfo>> =
            all.map { list -> list.filter { it.lastHistoryTime > 0L } }

        override fun getAllHistory(): List<CartoonInfo> =
            values.values.filter { it.lastHistoryTime > 0L }

        override fun flowAllStar(): Flow<List<CartoonInfo>> =
            all.map { list -> list.filter { it.starTime > 0L } }

        private fun publish() {
            val snapshot = values.values.toList()
            all.value = snapshot
            perKey.forEach { (key, state) -> state.value = values[key] }
        }
    }
}
