package com.heyanle.easybangumi4.cloudsync.model

class CloudSyncMerger {

    fun merge(
        local: CloudSyncDocument,
        remote: CloudSyncDocument,
        strategy: CloudSyncConflictStrategy,
    ): CloudSyncMergeResult {
        local.validate()
        remote.validate()
        val localByKey = local.records.associateBy(CloudSyncRecord::key)
        val remoteByKey = remote.records.associateBy(CloudSyncRecord::key)
        var conflicts = 0
        var localWins = 0
        var remoteWins = 0

        val records = (localByKey.keys + remoteByKey.keys).sorted().map { key ->
            val localRecord = localByKey[key]
            val remoteRecord = remoteByKey[key]
            if (localRecord == null) {
                remoteWins++
                return@map checkNotNull(remoteRecord).cleanForRemote()
            }
            if (remoteRecord == null) {
                localWins++
                return@map localRecord
            }

            val favorite = chooseDomain(
                local = localRecord.favorite,
                remote = remoteRecord.favorite,
                localDirty = localRecord.favorite?.dirty == true,
                strategy = strategy,
                sameValue = ::sameFavoriteValue,
            ).also {
                conflicts += it.conflicts
                localWins += it.localWins
                remoteWins += it.remoteWins
            }.value
            val history = chooseDomain(
                local = localRecord.history,
                remote = remoteRecord.history,
                localDirty = localRecord.history?.dirty == true,
                strategy = strategy,
                sameValue = ::sameHistoryValue,
            ).also {
                conflicts += it.conflicts
                localWins += it.localWins
                remoteWins += it.remoteWins
            }.value

            CloudSyncRecord(
                source = localRecord.source,
                cartoonId = localRecord.cartoonId,
                display = mergeDisplay(localRecord.display, remoteRecord.display, favorite, history),
                favorite = favorite,
                history = history,
            )
        }

        return CloudSyncMergeResult(
            document = CloudSyncDocument(records = records),
            report = CloudSyncMergeReport(conflicts, localWins, remoteWins),
        )
    }

    private data class DomainChoice<T>(
        val value: T?,
        val conflicts: Int = 0,
        val localWins: Int = 0,
        val remoteWins: Int = 0,
    )

    private fun <T> chooseDomain(
        local: T?,
        remote: T?,
        localDirty: Boolean,
        strategy: CloudSyncConflictStrategy,
        sameValue: (T, T) -> Boolean,
    ): DomainChoice<T> where T : Any {
        if (local == null) return DomainChoice(remote, remoteWins = if (remote == null) 0 else 1)
        if (remote == null) return DomainChoice(local, localWins = 1)

        if (sameValue(local, remote)) {
            val selected = if (stampOf(local) >= stampOf(remote)) local else remote
            return if (selected === local) {
                DomainChoice(local, localWins = 1)
            } else {
                DomainChoice(remote, remoteWins = 1)
            }
        }

        val conflict = if (localDirty) 1 else 0
        val localWins = if (!localDirty) {
            false
        } else when (strategy) {
            CloudSyncConflictStrategy.LOCAL_WINS -> true
            CloudSyncConflictStrategy.REMOTE_WINS -> false
            CloudSyncConflictStrategy.SMART_MERGE -> stampOf(local) >= stampOf(remote)
        }
        return if (localWins) {
            DomainChoice(local, conflicts = conflict, localWins = 1)
        } else {
            DomainChoice(remote, conflicts = conflict, remoteWins = 1)
        }
    }

    private fun stampOf(value: Any): SyncStamp = when (value) {
        is CloudFavoriteState -> value.stamp
        is CloudHistoryState -> value.stamp
        else -> error("Unsupported sync domain")
    }

    private fun sameFavoriteValue(a: CloudFavoriteState, b: CloudFavoriteState): Boolean =
        a.copy(stamp = b.stamp, dirty = b.dirty) == b

    private fun sameHistoryValue(a: CloudHistoryState, b: CloudHistoryState): Boolean =
        a.copy(stamp = b.stamp, dirty = b.dirty) == b

    private fun mergeDisplay(
        local: CloudCartoonDisplay,
        remote: CloudCartoonDisplay,
        favorite: CloudFavoriteState?,
        history: CloudHistoryState?,
    ): CloudCartoonDisplay {
        val preferLocal = sequenceOf(favorite?.dirty, history?.dirty).any { it == true }
        val primary = if (preferLocal) local else remote
        val fallback = if (preferLocal) remote else local
        return CloudCartoonDisplay(
            name = primary.name.ifBlank { fallback.name },
            coverUrl = primary.coverUrl.ifBlank { fallback.coverUrl },
            detailUrl = primary.detailUrl.ifBlank { fallback.detailUrl },
            intro = primary.intro.ifBlank { fallback.intro },
            sourceName = primary.sourceName.ifBlank { fallback.sourceName },
        )
    }
}

class SyncStampGenerator(
    private val deviceId: String,
) {
    @Synchronized
    fun next(
        existing: Sequence<SyncStamp> = emptySequence(),
        eventTimeMillis: Long = System.currentTimeMillis(),
    ): SyncStamp {
        val largest = existing.maxOrNull()
        val time = maxOf(eventTimeMillis, largest?.timeMillis ?: 0L)
        val counter = if (largest != null && largest.timeMillis >= eventTimeMillis) {
            largest.counter + 1
        } else {
            0L
        }
        return SyncStamp(time, counter, deviceId)
    }
}
