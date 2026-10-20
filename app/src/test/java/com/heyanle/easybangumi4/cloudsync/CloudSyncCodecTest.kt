package com.heyanle.easybangumi4.cloudsync

import com.heyanle.easybangumi4.cloudsync.model.CloudFavoriteState
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncCodec
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncDocument
import com.heyanle.easybangumi4.cloudsync.model.CloudSyncRecord
import com.heyanle.easybangumi4.cloudsync.model.SyncStamp
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CloudSyncCodecTest {
    private val codec = CloudSyncCodec(
        Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build(),
    )

    @Test
    fun roundTripValidatesAndRemovesDirtyFlags() {
        val source = CloudSyncDocument(
            records = listOf(
                CloudSyncRecord(
                    source = "source",
                    cartoonId = "id",
                    favorite = CloudFavoriteState(
                        present = true,
                        tags = listOf("A"),
                        stamp = SyncStamp(10, 2, "device"),
                        dirty = true,
                    ),
                )
            ),
        )

        val decoded = codec.decode(codec.encode(source))

        assertEquals("id", decoded.records.single().cartoonId)
        assertEquals(listOf("A"), decoded.records.single().favorite!!.tags)
        assertFalse(decoded.records.single().favorite!!.dirty)
    }
}
