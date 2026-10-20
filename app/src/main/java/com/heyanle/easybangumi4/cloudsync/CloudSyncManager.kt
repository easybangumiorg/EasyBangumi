package com.heyanle.easybangumi4.cloudsync

import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.CartoonInfoKey
import com.heyanle.easybangumi4.cartoon.repository.CartoonRepository
import com.heyanle.easybangumi4.cartoon.story.local.source.LocalSource
import com.heyanle.easybangumi4.cloudsync.model.CloudCartoonDisplay
import com.heyanle.easybangumi4.cloudsync.model.CloudFavoriteState
import com.heyanle.easybangumi4.cloudsync.model.CloudHistoryState
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncCodec
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncConflictStrategy
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncDocument
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncMergeReport
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncMerger
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncProvider
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncRecord
import com.heyanle.easybangumi4.cloudsync.model.SyncStamp
import com.heyanle.easybangumi4.cloudsync.model.SyncStampGenerator
import com.heyanle.easybangumi4.cloudsync.remote.CloudSyncRemoteException
import com.heyanle.easybangumi4.cloudsync.remote.CloudSyncRemoteStore
import com.heyanle.easybangumi4.cloudsync.remote.OneDriveRemoteStore
import com.heyanle.easybangumi4.cloudsync.remote.RemoteWriteResult
import com.heyanle.easybangumi4.cloudsync.remote.WebDavConfig
import com.heyanle.easybangumi4.cloudsync.remote.WebDavRemoteStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

enum class CloudSyncStage {
    IDLE,
    PREPARING,
    CONNECTING,
    DOWNLOADING,
    MERGING,
    APPLYING,
    UPLOADING,
    SUCCESS,
    ERROR,
}

data class CloudSyncOperationResult(
    val uploaded: Boolean,
    val downloaded: Boolean,
    val recordCount: Int,
    val report: CloudSyncMergeReport,
)

data class CloudSyncState(
    val stage: CloudSyncStage = CloudSyncStage.IDLE,
    val message: String = "",
    val result: CloudSyncOperationResult? = null,
    val throwable: Throwable? = null,
) {
    val isRunning: Boolean get() = stage in setOf(
        CloudSyncStage.PREPARING,
        CloudSyncStage.CONNECTING,
        CloudSyncStage.DOWNLOADING,
        CloudSyncStage.MERGING,
        CloudSyncStage.APPLYING,
        CloudSyncStage.UPLOADING,
    )
}

/** Global, manually-triggered cloud sync coordinator. No background scheduling is performed. */
class CloudSyncManager(
    private val cartoonRepository: CartoonRepository,
    private val preferences: CloudSyncPreferences,
    private val secretStore: CloudSyncSecretStore,
    private val eventBatcher: CloudSyncEventBatcher,
    private val localStore: CloudSyncLocalStore,
    private val codec: CloudSyncCodec,
    private val merger: CloudSyncMerger,
    private val oneDriveAuthController: OneDriveAuthController,
) {
    private val operationMutex = Mutex()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()
    private val stampGenerator = SyncStampGenerator(secretStore.deviceId())
    private val _state = MutableStateFlow(CloudSyncState())
    val state = _state.asStateFlow()
    val pendingChangeCount = eventBatcher.pendingCount

    suspend fun testConnection() = operationMutex.withLock {
        runOperation(CloudSyncStage.CONNECTING, "正在测试连接") {
            remoteStore().probe()
            _state.value = CloudSyncState(
                stage = CloudSyncStage.SUCCESS,
                message = "连接成功",
            )
        }
    }

    suspend fun upload(
        strategy: CloudSyncConflictStrategy = preferences.conflictStrategy.get(),
    ) = operationMutex.withLock {
        runOperation(CloudSyncStage.PREPARING, "正在整理本机数据") {
            val pendingSnapshot = eventBatcher.flush()
            var local = prepareLocalDocument(pendingSnapshot)
            val remoteStore = remoteStore()
            var lastReport = CloudSyncMergeReport()

            repeat(MAX_CONFLICT_RETRIES) { attempt ->
                updateStage(CloudSyncStage.DOWNLOADING, "正在检查云端数据")
                val remoteFile = remoteStore.read()
                val remote = remoteFile?.let { codec.decode(it.bytes).cleanForRemote() }
                    ?: CloudSyncDocument()
                updateStage(CloudSyncStage.MERGING, "正在处理数据冲突")
                val merged = merger.merge(local, remote, strategy)
                local = merged.document
                lastReport = merged.report

                updateStage(CloudSyncStage.APPLYING, "正在更新本机数据")
                applyDocument(local)
                localStore.write(local)

                updateStage(CloudSyncStage.UPLOADING, "正在上传云端数据")
                when (val write = remoteStore.write(codec.encode(local), remoteFile?.version)) {
                    RemoteWriteResult.Conflict -> {
                        if (attempt == MAX_CONFLICT_RETRIES - 1) {
                            throw CloudSyncRemoteException(
                                "云端数据持续发生变化，请稍后重试",
                                CloudSyncRemoteException.Kind.SERVER,
                            )
                        }
                    }
                    is RemoteWriteResult.Success -> {
                        val clean = local.cleanForRemote()
                        localStore.write(clean)
                        eventBatcher.markUploaded(pendingSnapshot)
                        preferences.lastRemoteVersion.set(write.version.orEmpty())
                        preferences.lastSuccessTime.set(System.currentTimeMillis())
                        val result = CloudSyncOperationResult(
                            uploaded = true,
                            downloaded = remoteFile != null,
                            recordCount = clean.records.size,
                            report = lastReport,
                        )
                        _state.value = CloudSyncState(
                            stage = CloudSyncStage.SUCCESS,
                            message = buildSuccessMessage("上传完成", result),
                            result = result,
                        )
                        return@runOperation
                    }
                }
            }
        }
    }

    suspend fun download(
        strategy: CloudSyncConflictStrategy = preferences.conflictStrategy.get(),
    ) = operationMutex.withLock {
        runOperation(CloudSyncStage.PREPARING, "正在整理本机数据") {
            val local = prepareLocalDocument()
            val remoteStore = remoteStore()
            updateStage(CloudSyncStage.DOWNLOADING, "正在下载云端数据")
            val remoteFile = remoteStore.read()
                ?: throw CloudSyncRemoteException(
                    "云端还没有同步数据",
                    CloudSyncRemoteException.Kind.INVALID_RESPONSE,
                )
            val remote = codec.decode(remoteFile.bytes).cleanForRemote()
            updateStage(CloudSyncStage.MERGING, "正在处理数据冲突")
            val merged = merger.merge(local, remote, strategy)
            updateStage(CloudSyncStage.APPLYING, "正在更新本机数据")
            applyDocument(merged.document)
            localStore.write(merged.document)
            eventBatcher.submit(
                merged.document.records.filter {
                    it.favorite?.dirty == true || it.history?.dirty == true
                }.map { CartoonInfoKey(it.cartoonId, it.source) }
            )
            preferences.lastRemoteVersion.set(remoteFile.version.orEmpty())
            preferences.lastSuccessTime.set(System.currentTimeMillis())
            val result = CloudSyncOperationResult(
                uploaded = false,
                downloaded = true,
                recordCount = merged.document.records.size,
                report = merged.report,
            )
            _state.value = CloudSyncState(
                stage = CloudSyncStage.SUCCESS,
                message = buildSuccessMessage("下载完成", result),
                result = result,
            )
        }
    }

    fun resetStatus() {
        if (!_state.value.isRunning) _state.value = CloudSyncState()
    }

    private suspend fun prepareLocalDocument(
        pendingSnapshot: Map<CartoonInfoKey, CloudSyncPendingChange>? = null,
    ): CloudSyncDocument {
        val pendingChanges = pendingSnapshot ?: eventBatcher.flush()
        val stored = localStore.read()
        val infoByKey = cartoonRepository.flowAll().first()
            .asSequence()
            .filterNot { it.source == LocalSource.LOCAL_SOURCE_KEY }
            .associateBy { "${it.source}\u0000${it.id}" }
        val allStamps = stored.records.asSequence().flatMap {
            listOfNotNull(it.favorite?.stamp, it.history?.stamp).asSequence()
        }.toMutableList()

        val records = linkedMapOf<String, CloudSyncRecord>()
        stored.records.forEach { records[it.key] = it }
        infoByKey.forEach { (key, info) ->
            val old = records[key]
            val eventTime = pendingChanges[CartoonInfoKey(info.id, info.source)]?.occurredAt
            var favorite = old?.favorite
            var history = old?.history
            val favoriteValue = favoriteFrom(
                info,
                favorite?.stamp ?: nextStamp(allStamps, eventTime ?: info.starTime.takeIf { it > 0L }),
                dirty = true,
            )
            if (favorite == null && !favoriteValue.present) {
                favorite = null
            } else if (favorite == null || !sameFavorite(favorite, favoriteValue)) {
                val stamp = nextStamp(allStamps, eventTime)
                favorite = favoriteValue.copy(stamp = stamp, dirty = true)
            }
            val historyValue = historyFrom(
                info,
                history?.stamp ?: nextStamp(
                    allStamps,
                    eventTime ?: info.lastHistoryTime.takeIf { it > 0L },
                ),
                dirty = true,
            )
            if (history == null && !historyValue.present) {
                history = null
            } else if (history == null || !sameHistory(history, historyValue)) {
                val stamp = nextStamp(allStamps, eventTime)
                history = historyValue.copy(stamp = stamp, dirty = true)
            }
            if (favorite != null || history != null) {
                records[key] = CloudSyncRecord(
                    source = info.source,
                    cartoonId = info.id,
                    display = CloudCartoonDisplay.from(info),
                    favorite = favorite,
                    history = history,
                )
            }
        }
        return CloudSyncDocument(records = records.values.sortedBy(CloudSyncRecord::key)).also {
            localStore.write(it)
        }
    }

    private fun nextStamp(
        existing: MutableList<SyncStamp>,
        eventTimeMillis: Long? = null,
    ): SyncStamp = stampGenerator.next(
        existing = existing.asSequence(),
        eventTimeMillis = eventTimeMillis ?: System.currentTimeMillis(),
    ).also(existing::add)

    private suspend fun applyDocument(document: CloudSyncDocument) = withContext(Dispatchers.IO) {
        val targets = document.records.map { record ->
            val fallback = CartoonInfo(
                id = record.cartoonId,
                source = record.source,
                name = record.display.name.ifBlank { "未知番剧" },
                coverUrl = record.display.coverUrl,
                intro = record.display.intro,
                url = record.display.detailUrl,
                sourceName = record.display.sourceName,
            )
            CartoonRepository.MutationTarget(record.cartoonId, record.source, fallback)
        }
        val byKey = document.records.associateBy(CloudSyncRecord::key)
        cartoonRepository.mutateCartoonInfo(
            targets = targets,
            notifyCloudSync = false,
        ) { target, current ->
            val old = current ?: target.fallback ?: return@mutateCartoonInfo null
            val record = byKey["${target.source}\u0000${target.id}"]
                ?: return@mutateCartoonInfo old
            old.applyFavorite(record.favorite).applyHistory(record.history)
        }
    }

    private fun CartoonInfo.applyFavorite(value: CloudFavoriteState?): CartoonInfo {
        value ?: return this
        return if (value.present) {
            copy(
                starTime = value.starredAt.takeIf { it > 0L } ?: value.stamp.timeMillis,
                upTime = value.pinnedAt,
                tags = value.tags.distinct().joinToString(", "),
            )
        } else {
            copy(starTime = 0L, upTime = 0L, tags = "")
        }
    }

    private fun CartoonInfo.applyHistory(value: CloudHistoryState?): CartoonInfo {
        value ?: return this
        return if (value.present) {
            copy(
                lastHistoryTime = value.playedAt.takeIf { it > 0L } ?: value.stamp.timeMillis,
                lastLineId = value.lineId,
                lastLinesIndex = value.lineIndex,
                lastLineLabel = value.lineLabel,
                lastEpisodeId = value.episodeId,
                lastEpisodeOrder = value.episodeOrder,
                lastEpisodeIndex = value.episodeIndex,
                lastEpisodeLabel = value.episodeLabel,
                lastProcessTime = value.positionMillis,
                lastTotalTile = value.durationMillis,
            )
        } else {
            copy(
                lastHistoryTime = 0L,
                lastLineId = "",
                lastLinesIndex = 0,
                lastLineLabel = "",
                lastEpisodeId = "",
                lastEpisodeOrder = 0,
                lastEpisodeIndex = 0,
                lastEpisodeLabel = "",
                lastProcessTime = 0L,
                lastTotalTile = 0L,
            )
        }
    }

    private fun favoriteFrom(info: CartoonInfo, stamp: SyncStamp, dirty: Boolean) = CloudFavoriteState(
        present = info.starTime > 0L,
        tags = info.tagList.sorted(),
        starredAt = info.starTime,
        pinnedAt = info.upTime,
        stamp = stamp,
        dirty = dirty,
    )

    private fun historyFrom(info: CartoonInfo, stamp: SyncStamp, dirty: Boolean) = CloudHistoryState(
        present = info.lastHistoryTime > 0L,
        lineId = info.lastLineId,
        lineIndex = info.lastLinesIndex,
        lineLabel = info.lastLineLabel,
        episodeId = info.lastEpisodeId,
        episodeOrder = info.lastEpisodeOrder,
        episodeIndex = info.lastEpisodeIndex,
        episodeLabel = info.lastEpisodeLabel,
        positionMillis = info.lastProcessTime,
        durationMillis = info.lastTotalTile,
        playedAt = info.lastHistoryTime,
        stamp = stamp,
        dirty = dirty,
    )

    private fun sameFavorite(a: CloudFavoriteState, b: CloudFavoriteState): Boolean =
        a.copy(stamp = b.stamp, dirty = b.dirty) == b

    private fun sameHistory(a: CloudHistoryState, b: CloudHistoryState): Boolean =
        a.copy(stamp = b.stamp, dirty = b.dirty) == b

    private fun remoteStore(): CloudSyncRemoteStore = when (preferences.provider.get()) {
        CloudSyncProvider.NONE -> throw CloudSyncRemoteException(
            "请先选择云同步服务",
            CloudSyncRemoteException.Kind.NOT_CONFIGURED,
        )
        CloudSyncProvider.WEBDAV -> {
            val url = preferences.webDavUrl.get().trim()
            val username = preferences.webDavUsername.get().trim()
            val password = secretStore.webDavPassword()
            if (url.isBlank() || username.isBlank() || password.isBlank()) {
                throw CloudSyncRemoteException(
                    "请先完善 WebDAV 配置",
                    CloudSyncRemoteException.Kind.NOT_CONFIGURED,
                )
            }
            WebDavRemoteStore(client, WebDavConfig(url, username, password))
        }
        CloudSyncProvider.ONEDRIVE -> OneDriveRemoteStore(client, oneDriveAuthController)
    }

    private suspend fun runOperation(
        initialStage: CloudSyncStage,
        initialMessage: String,
        block: suspend () -> Unit,
    ) {
        _state.value = CloudSyncState(initialStage, initialMessage)
        runCatching { block() }.onFailure { throwable ->
            _state.value = CloudSyncState(
                stage = CloudSyncStage.ERROR,
                message = throwable.message ?: "云同步失败",
                throwable = throwable,
            )
        }
    }

    private fun updateStage(stage: CloudSyncStage, message: String) {
        _state.value = CloudSyncState(stage = stage, message = message)
    }

    private fun buildSuccessMessage(prefix: String, result: CloudSyncOperationResult): String = buildString {
        append(prefix).append("，共 ").append(result.recordCount).append(" 条")
        if (result.report.conflictCount > 0) {
            append("，已处理 ").append(result.report.conflictCount).append(" 个冲突")
        }
    }

    private companion object {
        const val MAX_CONFLICT_RETRIES = 3
    }
}
