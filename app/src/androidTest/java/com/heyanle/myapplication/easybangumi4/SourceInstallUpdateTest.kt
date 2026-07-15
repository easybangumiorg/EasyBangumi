package com.heyanle.myapplication.easybangumi4

import android.content.Context
import androidx.paging.PagingSource
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.heyanle.easybangumi4.plugin.source.ISourceController
import com.heyanle.easybangumi4.plugin.source.SourceController
import com.heyanle.easybangumi4.plugin.source.SourceInfo
import com.heyanle.easybangumi4.plugin.source.push.SourcePushController
import com.heyanle.easybangumi4.plugin.source.push.SourcePushTask
import com.heyanle.easybangumi4.ui.search_migrate.PagingSearchSource
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SourceInstallUpdateTest {

    @Test
    fun apkDoesNotPackageInnerSources() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val assetFiles = context.assets.list(INNER_SOURCE_ASSET_DIR)
            .orEmpty()
            .filter { it.endsWith(".js") }
        assertTrue("APK must not package inner_source JS files", assetFiles.isEmpty())
    }

    @Test
    fun pushFromCodeInstallsAssetSourceAndSearchPagingWorks() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val controller = Inject.get<SourceController>()
        val pushController = Inject.get<SourcePushController>()
        val key = "test.install.age"
        val versionCode = 101
        val versionName = "101.0"

        cleanupInstalledSource(context, key)
        val installResult = pushSourceFromAsset(
            pushController = pushController,
            assetFileName = ASSET_FILE_NAME,
            key = key,
            versionName = versionName,
            versionCode = versionCode,
        )
        assertTrue(
            pushController.state.value.completelyMsg.ifBlank { pushController.state.value.errorMsg },
            installResult,
        )

        val loaded = waitForLoadedSource(controller, key, versionCode)
        assertEquals(versionName, loaded.source.version)
        assertEquals(versionCode, loaded.source.versionCode)

        val installedFile = installedSourceFile(context, key)
        assertTrue(installedFile.exists())
        assertTrue(installedFile.readText().contains("// @key $key"))
        assertTrue(installedFile.readText().contains("// @versionCode $versionCode"))

        val searchPage = loadFirstSearchPage(controller, key)
        assertFalse("installed source search page should not be empty", searchPage.data.isEmpty())
        assertTrue(
            "installed source first search result should have title",
            searchPage.data.first().title.isNotBlank(),
        )
    }

    @Test
    fun pushFromCodeUpdatesInstalledSourceAndSearchPagingStillWorks() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val controller = Inject.get<SourceController>()
        val pushController = Inject.get<SourcePushController>()
        val key = "test.update.age"

        cleanupInstalledSource(context, key)

        val firstInstall = pushSourceFromAsset(
            pushController = pushController,
            assetFileName = ASSET_FILE_NAME,
            key = key,
            versionName = "1.0-test",
            versionCode = 11,
        )
        assertTrue(
            pushController.state.value.completelyMsg.ifBlank { pushController.state.value.errorMsg },
            firstInstall,
        )
        waitForLoadedSource(controller, key, 11)

        val secondInstall = pushSourceFromAsset(
            pushController = pushController,
            assetFileName = ASSET_FILE_NAME,
            key = key,
            versionName = "2.0-test",
            versionCode = 22,
        )
        assertTrue(
            pushController.state.value.completelyMsg.ifBlank { pushController.state.value.errorMsg },
            secondInstall,
        )

        val loaded = waitForLoadedSource(controller, key, 22)
        assertEquals("2.0-test", loaded.source.version)
        assertEquals(22, loaded.source.versionCode)
        assertEquals(
            1,
            controller.sourceBundle.value
                ?.sources()
                ?.count { it.key == key },
        )

        val installedFile = installedSourceFile(context, key)
        assertTrue(installedFile.exists())
        val installedText = installedFile.readText()
        assertTrue(installedText.contains("// @versionName 2.0-test"))
        assertTrue(installedText.contains("// @versionCode 22"))

        val searchPage = loadFirstSearchPage(controller, key)
        assertFalse("updated source search page should not be empty", searchPage.data.isEmpty())
        assertTrue(
            "updated source first search result should belong to the updated source key",
            searchPage.data.first().source == key,
        )
    }

    private suspend fun pushSourceFromAsset(
        pushController: SourcePushController,
        assetFileName: String,
        key: String,
        versionName: String,
        versionCode: Int,
    ): Boolean {
        pushController.cleanErrorOrCompletely()
        pushController.push(
            SourcePushTask.Param(
                identify = SourcePushTask.SOURCE_PUSH_TASK_IDENTIFY_FROM_CODE,
                str1 = buildSourceCodeFromAsset(assetFileName, key, versionName, versionCode),
            )
        )
        return withTimeout(60_000L) {
            while (true) {
                val state = pushController.state.value
                if (state.isCompletely) {
                    return@withTimeout true
                }
                if (state.isError) {
                    return@withTimeout false
                }
                delay(300L)
            }
            false
        }
    }

    private suspend fun waitForLoadedSource(
        controller: ISourceController,
        key: String,
        versionCode: Int,
    ): SourceInfo.Loaded {
        return withTimeout(60_000L) {
            while (true) {
                val state = controller.sourceInfo.value
                if (state is ISourceController.SourceInfoState.Info) {
                    val loaded = state.info
                        .filterIsInstance<SourceInfo.Loaded>()
                        .firstOrNull { it.source.key == key && it.source.versionCode == versionCode }
                    if (loaded != null) {
                        return@withTimeout loaded
                    }
                    val error = state.info
                        .filterIsInstance<SourceInfo.Error>()
                        .firstOrNull { it.source.key == key }
                    if (error != null) {
                        throw AssertionError("source $key failed to load: ${error.msg}").apply {
                            error.exception?.let { initCause(it) }
                        }
                    }
                }
                delay(300L)
            }
            throw AssertionError("source $key did not load")
        }
    }

    private suspend fun loadFirstSearchPage(
        controller: SourceController,
        key: String,
    ): PagingSource.LoadResult.Page<Int, com.heyanle.easybangumi4.plugin.api.entity.CartoonCover> {
        return withTimeout(60_000L) {
            val searchComponent = controller.sourceBundle.value?.search(key)
                ?: throw AssertionError("search component missing for $key")
            val pagingSource = PagingSearchSource(searchComponent, SEARCH_KEYWORD)
            when (
                val result = pagingSource.load(
                    PagingSource.LoadParams.Refresh(
                        key = searchComponent.getFirstSearchKey(SEARCH_KEYWORD),
                        loadSize = 10,
                        placeholdersEnabled = false,
                    )
                )
            ) {
                is PagingSource.LoadResult.Page -> result
                is PagingSource.LoadResult.Error -> throw AssertionError(
                    "search page load failed for $key: ${result.throwable.message}",
                ).apply {
                    initCause(result.throwable)
                }
                is PagingSource.LoadResult.Invalid -> throw AssertionError("search page invalid for $key")
            }
        }
    }

    private fun buildSourceCodeFromAsset(
        assetFileName: String,
        key: String,
        versionName: String,
        versionCode: Int,
    ): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val text = context.assets.open(assetFileName).use { input ->
            input.bufferedReader().readText()
        }
        return text
            .replace(Regex("""(?m)^// @key .*$"""), "// @key $key")
            .replace(Regex("""(?m)^// @versionName .*$"""), "// @versionName $versionName")
            .replace(Regex("""(?m)^// @versionCode .*$"""), "// @versionCode $versionCode")
    }

    private fun cleanupInstalledSource(context: Context, key: String) {
        installedSourceFile(context, key).delete()
    }

    private fun installedSourceFile(context: Context, key: String): File {
        return File(context.getFilePath("source_v3"), "$key.js")
    }

    private companion object {
        const val INNER_SOURCE_ASSET_DIR = "inner_source"
        const val ASSET_FILE_NAME = "extension_test.js"
        const val SEARCH_KEYWORD = "孤独摇滚"
    }
}
