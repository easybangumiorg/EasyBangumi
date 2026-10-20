package com.heyanle.easybangumi4.cloudsync.model

import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo

enum class CloudSyncProvider {
    NONE,
    WEBDAV,
    ONEDRIVE,
}

enum class CloudSyncConflictStrategy {
    SMART_MERGE,
    LOCAL_WINS,
    REMOTE_WINS,
}

enum class CloudSyncDomain {
    FAVORITE,
    HISTORY,
}

data class SyncStamp(
    val timeMillis: Long,
    val counter: Long,
    val deviceId: String,
) : Comparable<SyncStamp> {
    override fun compareTo(other: SyncStamp): Int =
        compareValuesBy(this, other, SyncStamp::timeMillis, SyncStamp::counter, SyncStamp::deviceId)
}

data class CloudCartoonDisplay(
    val name: String = "",
    val coverUrl: String = "",
    val detailUrl: String = "",
    val intro: String = "",
    val sourceName: String = "",
) {
    companion object {
        fun from(info: CartoonInfo) = CloudCartoonDisplay(
            name = info.name,
            coverUrl = info.coverUrl,
            detailUrl = info.url,
            intro = info.intro,
            sourceName = info.sourceName,
        )
    }
}

data class CloudFavoriteState(
    val present: Boolean,
    val tags: List<String> = emptyList(),
    val starredAt: Long = 0L,
    val pinnedAt: Long = 0L,
    val stamp: SyncStamp,
    /** Local bookkeeping only. Remote documents are always written with this set to false. */
    val dirty: Boolean = false,
)

data class CloudHistoryState(
    val present: Boolean,
    val lineId: String = "",
    val lineIndex: Int = 0,
    val lineLabel: String = "",
    val episodeId: String = "",
    val episodeOrder: Int = 0,
    val episodeIndex: Int = 0,
    val episodeLabel: String = "",
    val positionMillis: Long = 0L,
    val durationMillis: Long = 0L,
    val playedAt: Long = 0L,
    val stamp: SyncStamp,
    /** Local bookkeeping only. Remote documents are always written with this set to false. */
    val dirty: Boolean = false,
)

data class CloudSyncRecord(
    val source: String,
    val cartoonId: String,
    val display: CloudCartoonDisplay = CloudCartoonDisplay(),
    val favorite: CloudFavoriteState? = null,
    val history: CloudHistoryState? = null,
) {
    val key: String get() = "$source\u0000$cartoonId"

    fun cleanForRemote() = copy(
        favorite = favorite?.copy(dirty = false),
        history = history?.copy(dirty = false),
    )
}

data class CloudSyncDocument(
    val format: String = FORMAT,
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val generatedAt: Long = System.currentTimeMillis(),
    val records: List<CloudSyncRecord> = emptyList(),
) {
    fun validate() {
        require(format == FORMAT) { "Unsupported cloud sync document" }
        require(schemaVersion in MIN_SUPPORTED_SCHEMA_VERSION..CURRENT_SCHEMA_VERSION) {
            "Unsupported cloud sync schema: $schemaVersion"
        }
        require(records.size <= MAX_RECORDS) { "Cloud sync document contains too many records" }
        require(records.distinctBy(CloudSyncRecord::key).size == records.size) {
            "Cloud sync document contains duplicate records"
        }
    }

    fun cleanForRemote() = copy(
        generatedAt = System.currentTimeMillis(),
        records = records.map(CloudSyncRecord::cleanForRemote).sortedBy(CloudSyncRecord::key),
    )

    companion object {
        const val FORMAT = "easybangumi-cloud-sync"
        const val CURRENT_SCHEMA_VERSION = 1
        const val MIN_SUPPORTED_SCHEMA_VERSION = 1
        const val MAX_RECORDS = 100_000
    }
}

data class CloudSyncMergeReport(
    val conflictCount: Int = 0,
    val localWinCount: Int = 0,
    val remoteWinCount: Int = 0,
)

data class CloudSyncMergeResult(
    val document: CloudSyncDocument,
    val report: CloudSyncMergeReport,
)
