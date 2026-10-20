package com.heyanle.easybangumi4.plugin.source

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import java.io.ByteArrayInputStream
import java.io.File
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Controlled old-scan/install interleaving; no installed sources or user preferences are changed. */
@RunWith(AndroidJUnit4::class)
class SourceControllerConcurrencyTest {
    @Test
    fun installationWaitsForOlderScanAndPublishesItsOwnSource() = runBlocking {
        withBlockedInitialScan { fixture ->
            // UNDISTPATCHED reaches append's first suspension before returning. The install must
            // suspend at the mutex before copying; it must not start a competing file scan.
            val install = async(start = CoroutineStart.UNDISPATCHED) {
                fixture.controller.appendOrUpdateSource(fixture.input)
            }
            try {
                assertFalse("Install must wait for the in-flight source scan", install.isCompleted)
                assertFalse("File copy belongs inside the reload transaction", fixture.target.exists())
                fixture.assets.release.countDown()
                val result = withTimeout(15_000L) { install.await() }
                assertTrue("Fixture installation should succeed: $result", result is DataResult.Ok)
                assertTrue(fixture.target.exists())
                assertNotNull(fixture.controller.sourceBundle.value?.source(fixture.key))
                assertNotNull(fixture.controller.sourceBundle.value?.page(fixture.key))
            } finally {
                fixture.assets.release.countDown()
                install.cancelAndJoin()
            }
        }
    }

    @Test
    fun cancellingQueuedInstallationPropagatesWithoutCopyingSource() = runBlocking {
        withBlockedInitialScan { fixture ->
            val install = async(start = CoroutineStart.UNDISPATCHED) {
                fixture.controller.appendOrUpdateSource(fixture.input)
            }
            try {
                install.cancel()
                var cancelled = false
                try {
                    install.await()
                } catch (_: CancellationException) {
                    cancelled = true
                }
                assertTrue("Cancellation must not become DataResult.Error", cancelled)
                assertFalse("Cancelled queued install must not copy its file", fixture.target.exists())
            } finally {
                fixture.assets.release.countDown()
                install.cancelAndJoin()
            }
        }
    }

    private data class Fixture(
        val root: File,
        val key: String,
        val input: File,
        val target: File,
        val assets: BlockingEmptyAssets,
        val controller: SourceController,
    )

    private suspend fun withBlockedInitialScan(block: suspend (Fixture) -> Unit) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        require(context.packageName == "com.heyanle.easybangumi4.sourcelab") {
            "Run source concurrency checks only in the isolated source acceptance package"
        }
        val token = UUID.randomUUID().toString().replace("-", "")
        val root = File(context.cacheDir, "source-concurrency-$token").apply { mkdirs() }
        val key = "test.source.concurrent.$token"
        val input = File(root, "input.js").apply {
            writeText("""
                // @key $key
                // @label 并发安装验收
                // @versionName 1.0-test
                // @versionCode 1
                // @libVersion 15
                function PageComponent_getMainTabs() { return new NonLabelMainTab(MainTab.MAIN_TAB_WITH_COVER); }
                function PageComponent_getSubTabs(tab) { return new ArrayList(); }
                function PageComponent_getContent(tab, subTab, page) { return new Pair(null, new ArrayList()); }
            """.trimIndent())
        }
        val assets = BlockingEmptyAssets()
        val folder = File(root, "installed")
        val controller = SourceController(
            sourceFolder = folder,
            sourcePreferences = Inject.get(),
            innerSourceFileProvider = InnerSourceFileProvider(assets, File(root, "inner")),
        )
        try {
            assertTrue("Initial scan should reach its controlled gate", withContext(Dispatchers.IO) {
                assets.entered.await(10, TimeUnit.SECONDS)
            })
            block(Fixture(root, key, input, File(folder, "$key.js"), assets, controller))
        } finally {
            assets.release.countDown()
            withTimeout(15_000L) { controller.sourceBundle.filterNotNull().first() }
            // This directory belongs only to this test. No external source/preferences/DB are used.
            root.deleteRecursively()
        }
    }

    private class BlockingEmptyAssets : InnerSourceFileProvider.AssetReader {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        private val first = AtomicBoolean(true)

        override fun list(path: String): List<String> {
            if (first.compareAndSet(true, false)) {
                entered.countDown()
                check(release.await(15, TimeUnit.SECONDS)) { "Initial source scan was never released" }
            }
            return emptyList()
        }

        override fun open(path: String) = ByteArrayInputStream(ByteArray(0))
    }
}
