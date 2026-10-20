package com.heyanle.easybangumi4.cloudsync

import com.heyanle.easybangumi4.cloudsync.model.CloudFavoriteState
import com.heyanle.easybangumi4.cloudsync.model.CloudHistoryState
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncConflictStrategy
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncDocument
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncMerger
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncRecord
import com.heyanle.easybangumi4.cloudsync.model.SyncStamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudSyncMergerTest {
    private val merger = CloudSyncMerger()

    @Test
    fun smartMergeResolvesFavoriteAndHistoryIndependently() {
        val local = document(
            record(
                favorite = favorite(true, stamp(20, "local"), dirty = true),
                history = history("ep-1", stamp(10, "local"), dirty = true),
            )
        )
        val remote = document(
            record(
                favorite = favorite(false, stamp(15, "remote")),
                history = history("ep-2", stamp(30, "remote")),
            )
        )

        val result = merger.merge(local, remote, CloudSyncConflictStrategy.SMART_MERGE)
        val merged = result.document.records.single()

        assertTrue(merged.favorite!!.present)
        assertEquals("ep-2", merged.history!!.episodeId)
        assertEquals(2, result.report.conflictCount)
        assertEquals(1, result.report.localWinCount)
        assertEquals(1, result.report.remoteWinCount)
    }

    @Test
    fun remoteWinsStrategyHonorsFavoriteTombstone() {
        val local = document(record(favorite = favorite(true, stamp(30, "local"), dirty = true)))
        val remote = document(record(favorite = favorite(false, stamp(40, "remote"))))

        val merged = merger.merge(local, remote, CloudSyncConflictStrategy.REMOTE_WINS)
            .document.records.single()

        assertFalse(merged.favorite!!.present)
        assertEquals("remote", merged.favorite!!.stamp.deviceId)
    }

    @Test
    fun cleanLocalStateNeverBlocksANewerRemoteChange() {
        val local = document(record(favorite = favorite(true, stamp(30, "local"), dirty = false)))
        val remote = document(record(favorite = favorite(false, stamp(40, "remote"))))

        val result = merger.merge(local, remote, CloudSyncConflictStrategy.LOCAL_WINS)

        assertFalse(result.document.records.single().favorite!!.present)
        assertEquals(0, result.report.conflictCount)
    }

    @Test
    fun remoteDocumentNeverContainsLocalDirtyFlags() {
        val document = document(
            record(
                favorite = favorite(true, stamp(10, "local"), dirty = true),
                history = history("ep", stamp(11, "local"), dirty = true),
            )
        ).cleanForRemote()

        assertFalse(document.records.single().favorite!!.dirty)
        assertFalse(document.records.single().history!!.dirty)
    }

    private fun document(vararg records: CloudSyncRecord) = CloudSyncDocument(records = records.toList())

    private fun record(
        favorite: CloudFavoriteState? = null,
        history: CloudHistoryState? = null,
    ) = CloudSyncRecord(source = "source", cartoonId = "cartoon", favorite = favorite, history = history)

    private fun favorite(present: Boolean, stamp: SyncStamp, dirty: Boolean = false) =
        CloudFavoriteState(present = present, stamp = stamp, dirty = dirty)

    private fun history(episode: String, stamp: SyncStamp, dirty: Boolean = false) =
        CloudHistoryState(present = true, episodeId = episode, stamp = stamp, dirty = dirty)

    private fun stamp(time: Long, device: String) = SyncStamp(time, 0, device)
}
