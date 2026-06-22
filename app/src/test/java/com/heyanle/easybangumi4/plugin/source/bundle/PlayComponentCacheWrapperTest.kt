package com.heyanle.easybangumi4.plugin.source.bundle

import com.heyanle.easybangumi4.plugin.api.Source
import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.play.PlayComponent
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.reflect.KClass

class PlayComponentCacheWrapperTest {

    @Test
    fun wrapperPersistsPlayerInfoAndHonorsCanCache() = runBlocking {
        val cacheFolder = Files.createTempDirectory("play-info-cache").toFile()
        try {
            val delegate = FakePlayComponent()
            val wrapper = PlayComponentCacheWrapper(delegate, cacheFolder)
            val summary = CartoonSummary("cartoon-1", "fake.source")
            val playLine = PlayLine("line-1", "Line 1", arrayListOf())
            val episode = Episode("episode-1", "Episode 1", 1)

            val first = wrapper.getPlayInfo(summary, playLine, episode) as SourceResult.Complete
            assertFalse(first.isCache)
            assertEquals("https://cdn.example.test/video-1.m3u8", first.data.uri)
            assertEquals(1, delegate.callCount)
            assertEquals(false, delegate.lastCanCache)

            val second = wrapper.getPlayInfo(summary, playLine, episode) as SourceResult.Complete
            assertTrue(second.isCache)
            assertEquals("https://cdn.example.test/video-1.m3u8", second.data.uri)
            assertEquals(mapOf("Referer" to "https://example.test"), second.data.header)
            assertEquals(1, delegate.callCount)

            val third = wrapper.getPlayInfo(summary, playLine, episode, canCache = false) as SourceResult.Complete
            assertFalse(third.isCache)
            assertEquals("https://cdn.example.test/video-2.m3u8", third.data.uri)
            assertEquals(2, delegate.callCount)
            assertEquals(false, delegate.lastCanCache)
        } finally {
            cacheFolder.deleteRecursively()
        }
    }

    private class FakePlayComponent : PlayComponent {
        override val source: Source = FakeSource
        var callCount = 0
        var lastCanCache: Boolean? = null

        override suspend fun getPlayInfo(
            summary: CartoonSummary,
            playLine: PlayLine,
            episode: Episode,
            canCache: Boolean,
        ): SourceResult<PlayerInfo> {
            callCount++
            lastCanCache = canCache
            return SourceResult.Complete(
                PlayerInfo(
                    decodeType = PlayerInfo.DECODE_TYPE_HLS,
                    uri = "https://cdn.example.test/video-$callCount.m3u8",
                ).apply {
                    header = mapOf("Referer" to "https://example.test")
                }
            )
        }
    }

    private object FakeSource : Source {
        override val key: String = "fake.source"
        override val label: String = "Fake Source"
        override val version: String = "1.0"
        override val versionCode: Int = 1
        override val describe: String? = null
        override fun register(): List<KClass<*>> = emptyList()
    }
}
