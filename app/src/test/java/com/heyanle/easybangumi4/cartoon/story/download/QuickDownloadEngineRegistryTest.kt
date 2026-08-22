package com.heyanle.easybangumi4.cartoon.story.download

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadEngine
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadEngineContext
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadEngineDescriptor
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadEngineRegistry
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadMediaType
import com.heyanle.easybangumi4.cartoon.story.download.engine.QuickDownloadToggleResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuickDownloadEngineRegistryTest {

    @Test
    fun `registry resolves stable ids without implicit fallback`() {
        val aria = FakeEngine("aria")
        val registry = QuickDownloadEngineRegistry.create(aria)

        assertEquals(aria, registry.find("aria"))
        assertNull(registry.find("removed-engine"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `registry rejects duplicate ids`() {
        QuickDownloadEngineRegistry.create(
            FakeEngine("duplicate"),
            FakeEngine("duplicate"),
        )
    }

    private class FakeEngine(id: String) : QuickDownloadEngine {
        override val descriptor = QuickDownloadEngineDescriptor(
            id = id,
            displayName = id,
            supportedMediaTypes = setOf(QuickDownloadMediaType.DIRECT),
        )

        override suspend fun canResume(request: CartoonDownloadReq) = false
        override suspend fun toggle(taskId: String) = QuickDownloadToggleResult.UNSUPPORTED
        override fun start(context: QuickDownloadEngineContext) = Unit
        override fun cancel(taskId: String) = Unit
    }
}
