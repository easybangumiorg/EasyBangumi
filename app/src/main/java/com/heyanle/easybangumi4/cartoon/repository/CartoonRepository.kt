package com.heyanle.easybangumi4.cartoon.repository

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.repository.db.dao.StarMigrationResult
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.plugin.api.entity.Cartoon
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Stable identity for one globally shared [CartoonInfo] state. */
data class CartoonInfoKey(
    val id: String,
    val source: String,
)

/**
 * Application-scoped source of truth for [CartoonInfo].
 *
 * Every consumer asking for the same `(source, id)` observes the same state. Room is loaded first
 * and published with `isCache = true`; a successful source refresh is merged with local metadata,
 * persisted, then published with `isCache = false`. Per-key mutexes collapse concurrent detail
 * requests while [databaseMutationMutex] keeps read-modify-write operations lossless.
 */
class CartoonRepository private constructor(
    private val cartoonInfoDao: CartoonInfoDao,
    private val remoteLoader: suspend (String, String) -> DataResult<Pair<Cartoon, List<PlayLine>>>,
    private val sourceNameLoader: suspend (String) -> String,
    private val remoteMerger: (CartoonInfo?, Cartoon, String, List<PlayLine>) -> CartoonInfo,
) {

    constructor(
        cartoonInfoDao: CartoonInfoDao,
        cartoonNetworkDataSource: CartoonNetworkDataSource,
        sourceStateCase: SourceStateCase,
    ) : this(
        cartoonInfoDao = cartoonInfoDao,
        remoteLoader = cartoonNetworkDataSource::awaitCartoonWithPlayLines,
        sourceNameLoader = { source ->
            sourceStateCase.awaitBundle().source(source)?.label.orEmpty()
        },
        remoteMerger = { local, cartoon, sourceName, playLines ->
            local?.copyFromCartoon(cartoon, sourceName, playLines)
                ?: CartoonInfo.fromCartoon(cartoon, sourceName, playLines)
        },
    )

    internal constructor(
        cartoonInfoDao: CartoonInfoDao,
        remoteLoader: suspend (String, String) -> DataResult<Pair<Cartoon, List<PlayLine>>>,
        sourceName: String,
        remoteMerger: (CartoonInfo?, Cartoon, String, List<PlayLine>) -> CartoonInfo,
    ) : this(
        cartoonInfoDao,
        remoteLoader,
        sourceNameLoader = { sourceName },
        remoteMerger = remoteMerger,
    )

    data class MutationTarget(
        val id: String,
        val source: String,
        val fallback: CartoonInfo? = null,
    ) {
        val key: CartoonInfoKey get() = CartoonInfoKey(id, source)
    }

    private class Entry {
        val mutableCartoonInfo = MutableStateFlow<DataResult<CartoonInfo>>(DataResult.Loading())
        val cartoonInfo = mutableCartoonInfo.asStateFlow()
        val loadMutex = Mutex()
        @Volatile
        var networkGeneration = 0L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val entries = mutableMapOf<CartoonInfoKey, Entry>()
    private val databaseMutationMutex = Mutex()

    /** Shared observable state for one cartoon. Callers cannot mutate it directly. */
    fun cartoonInfo(id: String, source: String): StateFlow<DataResult<CartoonInfo>> =
        entry(CartoonInfoKey(id, source)).cartoonInfo

    fun cartoonInfo(summary: CartoonSummary): StateFlow<DataResult<CartoonInfo>> =
        cartoonInfo(summary.id, summary.source)

    /**
     * Loads complete detail/play-line data using cache-then-network semantics.
     *
     * An in-memory non-cache result is reused unless [forceRefresh] is true. A Room value is
     * published immediately while the network refresh runs. If refresh fails, a usable detailed
     * cache remains the public state and is returned to the caller.
     */
    suspend fun awaitCartoonInfoWithPlayLines(
        id: String,
        source: String,
        forceRefresh: Boolean = false,
    ): DataResult<CartoonInfo> {
        val key = CartoonInfoKey(id, source)
        val entry = entry(key)
        val generationAtRequest = entry.networkGeneration
        return entry.loadMutex.withLock {
            val current = entry.mutableCartoonInfo.value
            val currentInfo = current.okOrNull()
            if (entry.networkGeneration != generationAtRequest) {
                return@withLock current
            }
            if (
                !forceRefresh && current is DataResult.Ok && !current.isCache &&
                currentInfo?.isDetailed == true && currentInfo.isPlayLineLoad
            ) {
                return@withLock current
            }

            val stored = cartoonInfoDao.getByCartoonSummary(id, source)
            val cachedDetail = sequenceOf(currentInfo, stored).firstOrNull {
                it?.isDetailed == true && it.isPlayLineLoad
            }
            if (cachedDetail != null && currentInfo != cachedDetail) {
                entry.mutableCartoonInfo.value = DataResult.ok(cachedDetail, isCache = true)
            }

            val result = when (val remote = remoteLoader(id, source)) {
                is DataResult.Ok -> {
                    val sourceName = sourceNameLoader(source)
                    val merged = databaseMutationMutex.withLock {
                        val latestLocal = cartoonInfoDao.getByCartoonSummary(id, source)
                            ?: stored
                            ?: currentInfo
                        val value = remoteMerger(
                            latestLocal,
                            remote.data.first,
                            sourceName,
                            remote.data.second,
                        )
                        cartoonInfoDao.modify(value)
                        cartoonInfoDao.getByCartoonSummary(id, source) ?: value
                    }
                    DataResult.ok(merged, isCache = remote.isCache).also {
                        entry.mutableCartoonInfo.value = it
                    }
                }

                is DataResult.Error -> {
                    if (cachedDetail != null) {
                        DataResult.ok(cachedDetail, isCache = true).also {
                            entry.mutableCartoonInfo.value = it
                        }
                    } else {
                        DataResult.error<CartoonInfo>(remote.errorMsg, remote.throwable).also {
                            entry.mutableCartoonInfo.value = it
                        }
                    }
                }

                is DataResult.Loading -> entry.mutableCartoonInfo.value
            }
            entry.networkGeneration++
            result
        }
    }

    suspend fun awaitCartoonInfoWithPlayLines(
        summary: CartoonSummary,
        forceRefresh: Boolean = false,
    ): DataResult<CartoonInfo> = awaitCartoonInfoWithPlayLines(
        summary.id,
        summary.source,
        forceRefresh,
    )

    /** Returns local/in-memory data only and never starts a network request. */
    suspend fun cachedCartoonInfo(id: String, source: String): CartoonInfo? {
        val key = CartoonInfoKey(id, source)
        val activeEntry = activeEntry(key)
        activeEntry?.mutableCartoonInfo?.value?.okOrNull()?.let { return it }
        return cartoonInfoDao.getByCartoonSummary(id, source)?.also { stored ->
            activeEntry?.mutableCartoonInfo?.value = DataResult.ok(stored, isCache = true)
        }
    }

    suspend fun cachedCartoonInfo(summary: CartoonSummary): CartoonInfo? =
        cachedCartoonInfo(summary.id, summary.source)

    /** Atomically updates one row and its shared state. */
    suspend fun updateCartoonInfo(
        id: String,
        source: String,
        fallback: CartoonInfo? = null,
        transform: (CartoonInfo) -> CartoonInfo,
    ): CartoonInfo? = mutateCartoonInfo(
        targets = listOf(MutationTarget(id, source, fallback)),
    ) { _, current -> current?.let(transform) }.firstOrNull()

    suspend fun updateCartoonInfo(
        summary: CartoonSummary,
        fallback: CartoonInfo? = null,
        transform: (CartoonInfo) -> CartoonInfo,
    ): CartoonInfo? = updateCartoonInfo(summary.id, summary.source, fallback, transform)

    suspend fun upsertCartoonInfo(cartoonInfo: CartoonInfo, isCache: Boolean = true) {
        val stored = databaseMutationMutex.withLock {
            cartoonInfoDao.modify(cartoonInfo)
            cartoonInfoDao.getByCartoonSummary(cartoonInfo.id, cartoonInfo.source) ?: cartoonInfo
        }
        activeEntry(CartoonInfoKey(cartoonInfo.id, cartoonInfo.source))
            ?.mutableCartoonInfo
            ?.value = DataResult.ok(stored, isCache)
    }

    /**
     * Applies a batch mutation in one Room transaction and publishes every committed value.
     * Returning null from [transform] skips that target.
     */
    suspend fun mutateCartoonInfo(
        targets: Collection<MutationTarget>,
        transform: (MutationTarget, CartoonInfo?) -> CartoonInfo?,
    ): List<CartoonInfo> {
        if (targets.isEmpty()) return emptyList()
        val distinctTargets = targets.distinctBy { it.key }
        val updated = mutableListOf<CartoonInfo>()
        databaseMutationMutex.withLock {
            cartoonInfoDao.transaction {
                distinctTargets.forEach { target ->
                    val current = cartoonInfoDao.getByCartoonSummary(target.id, target.source)
                        ?: target.fallback
                    val next = transform(target, current) ?: return@forEach
                    require(next.id == target.id && next.source == target.source) {
                        "CartoonInfo identity cannot change during repository mutation"
                    }
                    cartoonInfoDao.modify(next)
                    updated += cartoonInfoDao.getByCartoonSummary(target.id, target.source) ?: next
                }
            }
        }
        updated.forEach { value ->
            val entry = activeEntry(CartoonInfoKey(value.id, value.source))
                ?: return@forEach
            val retainedCacheFlag = (entry.mutableCartoonInfo.value as? DataResult.Ok)?.isCache
                ?: true
            entry.mutableCartoonInfo.value = DataResult.ok(value, retainedCacheFlag)
        }
        return updated
    }

    suspend fun deleteHistory(cartoonInfo: Collection<CartoonInfo>) {
        mutateCartoonInfo(
            cartoonInfo.map { MutationTarget(it.id, it.source, it) },
        ) { _, current -> current?.copy(lastHistoryTime = 0L) }
    }

    suspend fun clearHistory() {
        deleteHistory(flowAllHistory().first())
    }

    suspend fun clearStar(cartoonInfo: Collection<CartoonInfo>) {
        mutateCartoonInfo(
            cartoonInfo.map { MutationTarget(it.id, it.source, it) },
        ) { _, current -> current?.copy(starTime = 0L, tags = "", upTime = 0L) }
    }

    suspend fun updateTags(cartoonInfo: Collection<CartoonInfo>, tags: String) {
        mutateCartoonInfo(
            cartoonInfo.map { MutationTarget(it.id, it.source, it) },
        ) { _, current -> current?.copy(tags = tags) }
    }

    suspend fun renameTag(oldTag: String, newTag: String) {
        mutateCartoonInfo(
            flowAllStar().first().map { MutationTarget(it.id, it.source, it) },
        ) { _, current -> current?.renameTag(oldTag, newTag) }
    }

    suspend fun migrateStar(
        sourceId: String,
        sourceKey: String,
        targetDraft: CartoonInfo,
    ): StarMigrationResult {
        val result = databaseMutationMutex.withLock {
            cartoonInfoDao.migrateStar(sourceId, sourceKey, targetDraft)
        }
        if (result == StarMigrationResult.SUCCESS) {
            publishDatabaseValue(CartoonInfoKey(sourceId, sourceKey))
            publishDatabaseValue(CartoonInfoKey(targetDraft.id, targetDraft.source))
        }
        return result
    }

    fun flowAll() = cartoonInfoDao.flowAll()
    fun flowAllHistory() = cartoonInfoDao.flowAllHistory()
    fun flowAllStar() = cartoonInfoDao.flowAllStar()

    private fun entry(key: CartoonInfoKey): Entry {
        synchronized(entries) {
            entries[key]?.let { return it }
            val created = Entry()
            entries[key] = created
            scope.launch {
                cartoonInfoDao.flowByCartoonSummary(key.id, key.source).collectLatest { stored ->
                    stored ?: return@collectLatest
                    created.mutableCartoonInfo.update { current ->
                        if (current.okOrNull() == stored) {
                            current
                        } else {
                            DataResult.ok(stored, isCache = true)
                        }
                    }
                }
            }
            return created
        }
    }

    private suspend fun publishDatabaseValue(key: CartoonInfoKey) {
        val stored = cartoonInfoDao.getByCartoonSummary(key.id, key.source) ?: return
        val activeEntry = activeEntry(key) ?: return
        activeEntry.mutableCartoonInfo.value = DataResult.ok(stored, isCache = true)
    }

    private fun activeEntry(key: CartoonInfoKey): Entry? = synchronized(entries) { entries[key] }
}
