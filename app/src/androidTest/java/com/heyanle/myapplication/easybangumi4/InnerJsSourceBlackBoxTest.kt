package com.heyanle.myapplication.easybangumi4

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.page.SourcePage
import com.heyanle.easybangumi4.plugin.api.component.play.PlayComponent
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.source.ISourceController
import com.heyanle.easybangumi4.plugin.source.SourceController
import com.heyanle.easybangumi4.plugin.source.SourceConfig
import com.heyanle.easybangumi4.plugin.source.SourceInfo
import com.heyanle.easybangumi4.plugin.source.SourcePreferences
import com.heyanle.easybangumi4.plugin.source.bundle.PlayComponentCacheWrapper
import com.heyanle.easybangumi4.plugin.source.bundle.SourceBundle
import com.heyanle.easybangumi4.plugin.source.bundle.getComponentProxy
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class InnerJsSourceBlackBoxTest {

    @Test
    fun kazumiPlayerCacheDoesNotCrossCartoons() = runBlocking {
        assumePackagedInnerSources()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val key = "kazumi.baimao"
        val controller = Inject.get<SourceController>()
        controller.refresh()
        val sourceBundle = waitForSourceBundle(controller, setOf(key))
        val search = sourceBundle.search(key) ?: error("$key search component missing")
        val detailed = sourceBundle.detailed(key) ?: error("$key detailed component missing")
        val sourceInfo = sourceBundle.sourceInfo(key) as? SourceInfo.Loaded
            ?: error("$key source did not load")
        val rawPlay = sourceInfo.componentBundle.getComponentProxy<PlayComponent>()
            ?: error("$key raw play component missing")

        val covers = when (val result = search.search(search.getFirstSearchKey(KEYWORD), KEYWORD)) {
            is SourceResult.Complete -> result.data.second
            is SourceResult.Error -> throw AssertionError("$key search failed: ${result.throwable.message}")
        }
        val firstCover = covers.firstOrNull() ?: throw AssertionError("$key search returned no covers")
        val secondCover = covers.firstOrNull { it.id != firstCover.id }
            ?: throw AssertionError("$key search returned no distinct second cartoon")

        suspend fun firstPlayable(cover: com.heyanle.easybangumi4.plugin.api.entity.CartoonCover): Triple<CartoonSummary, com.heyanle.easybangumi4.plugin.api.entity.PlayLine, com.heyanle.easybangumi4.plugin.api.entity.Episode> {
            val summary = CartoonSummary(cover.id, cover.source)
            val (_, lines) = when (val result = detailed.getAll(summary)) {
                is SourceResult.Complete -> result.data
                is SourceResult.Error -> throw AssertionError("$key detail failed for ${cover.title}: ${result.throwable.message}")
            }
            val line = lines.firstOrNull { it.episode.isNotEmpty() }
                ?: throw AssertionError("$key has no playable line for ${cover.title}")
            return Triple(summary, line, line.episode.first())
        }

        val first = firstPlayable(firstCover)
        val second = firstPlayable(secondCover)
        val cacheFolder = File(context.cacheDir, "kazumi-player-cache-isolation")
        cacheFolder.deleteRecursively()
        try {
            val cache = PlayComponentCacheWrapper(rawPlay, cacheFolder)
            val firstResult = cache.getPlayInfo(first.first, first.second, first.third, canCache = false)
                as? SourceResult.Complete ?: throw AssertionError("$key first cartoon play parsing failed")
            assertFalse("first cartoon should not be a cache result", firstResult.isCache)

            val secondResult = cache.getPlayInfo(second.first, second.second, second.third, canCache = true)
                as? SourceResult.Complete ?: throw AssertionError("$key second cartoon play parsing failed")
            assertFalse(
                "different cartoons must not reuse the first cartoon's player-info cache",
                secondResult.isCache,
            )
            assertNotEquals(
                "different cartoons must not receive the previous render's video URL",
                firstResult.data.uri,
                secondResult.data.uri,
            )

            val firstCached = cache.getPlayInfo(first.first, first.second, first.third, canCache = true)
                as? SourceResult.Complete ?: throw AssertionError("$key first cartoon cached play parsing failed")
            assertTrue("first cartoon should reuse only its own cache entry", firstCached.isCache)
        } finally {
            cacheFolder.deleteRecursively()
        }
    }

    @Test
    fun stableKazumiSourcesPassSearchDetailPlay() = runBlocking {
        assumePackagedInnerSources()
        val controller = Inject.get<SourceController>()
        controller.refresh()
        val sourceBundle = waitForSourceBundle(controller, ACTIVE_KAZUMI_KEYS)

        val loadedKeys = sourceBundle.sources().map { it.key }.toSet()
        assertTrue(loadedKeys.containsAll(ACTIVE_KAZUMI_KEYS))
        assertFalse(
            "search-failed Kazumi sources should not be loaded",
            loadedKeys.any { it in SEARCH_FAILED_KAZUMI_KEYS },
        )

        val reports = STABLE_KAZUMI_KEYS.map { key ->
            withContext(Dispatchers.IO) {
                probeSource(sourceBundle, key)
            }
        }

        val reportLines = reports.map { it.toLine() }
        reportLines.forEach { println(it) }
        writeReport("kazumi_blackbox", reportLines)
        val failures = reports.filterNot { it.ok }
        assertTrue(
            failures.joinToString(separator = "\n") { it.toLine() },
            failures.isEmpty(),
        )
    }

    @Test
    fun activeInnerJsSourcesReportHomeAndSearchQuality() = runBlocking {
        assumePackagedInnerSources()
        val controller = Inject.get<SourceController>()
        controller.refresh()
        val sourceBundle = waitForSourceBundle(controller, ACTIVE_INNER_JS_KEYS)

        val loadedKeys = sourceBundle.sources().map { it.key }.toSet()
        assertTrue(loadedKeys.containsAll(ACTIVE_INNER_JS_KEYS))

        val reports = ACTIVE_INNER_JS_KEYS.map { key ->
            withContext(Dispatchers.IO) {
                probeHomeAndSearch(sourceBundle, key)
            }
        }

        val reportLines = reports.map { it.toLine() }
        reportLines.forEach { println(it) }
        writeReport("inner_home_search", reportLines)
    }

    @Test
    fun allKazumiSourcesReportSearchDetailPlayQuality() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assumePackagedInnerSources(context)
        val assetFiles = kazumiAssetFiles(context)
        val assetFilesByKey = assetFiles.associateBy { keyFromKazumiAssetFile(it) }
        val assetMetaByKey = assetFiles.associate { fileName ->
            keyFromKazumiAssetFile(fileName) to kazumiAssetMeta(context, fileName)
        }
        val expectedKeys = assetFilesByKey.keys.toSortedSet()
        assertTrue("all Kazumi assets should not be empty", expectedKeys.isNotEmpty())
        val controller = createAllKazumiSourceController(context, assetFiles)
        enableKazumiTestSources(expectedKeys)
        controller.refresh()
        val state = waitForSourceInfo(controller, expectedKeys)
        val sourceBundle = controller.sourceBundle.value ?: SourceBundle.NONE
        val loadedKeys = sourceBundle.sources()
            .map { it.key }
            .filter { it.startsWith("kazumi.") }
            .toSet()
        val errors = state.info
            .filterIsInstance<SourceInfo.Error>()
            .filter { it.source.key.startsWith("kazumi.") }
            .associateBy { it.source.key }

        val reports = expectedKeys.map { key ->
            withContext(Dispatchers.IO) {
                val report = when {
                    key in loadedKeys -> probeSource(sourceBundle, key)
                    errors[key] != null -> Report(
                        key = key,
                        home = HomeReport(status = "skip"),
                        search = "skip",
                        detail = "skip",
                        play = "skip",
                        error = "load: ${errors[key]?.msg.orEmpty()}",
                    )
                    else -> Report(
                        key = key,
                        home = HomeReport(status = "skip"),
                        search = "skip",
                        detail = "skip",
                        play = "skip",
                        error = "load: source was not loaded",
                    )
                }
                report.withAssetMeta(assetFilesByKey[key].orEmpty(), assetMetaByKey[key] ?: AssetMeta())
            }
        }

        val reportLines = reports.map { it.toLine() }
        reportLines.forEach { println(it) }
        writeReport("kazumi_all", reportLines)
    }

    private fun kazumiAssetFiles(context: Context): List<String> {
        return context.assets.list(INNER_SOURCE_ASSET_DIR)
            .orEmpty()
            .filter {
                it.endsWith(".js") &&
                    (it.startsWith("kazumi-") || it.startsWith("block-kazumi-"))
            }
            .sortedBy { it.removePrefix("block-") }
    }

    private fun assumePackagedInnerSources(
        context: Context = InstrumentationRegistry.getInstrumentation().targetContext,
    ) {
        assumeTrue(
            "Kazumi asset black-box tests require packaged inner sources",
            context.assets.list(INNER_SOURCE_ASSET_DIR).orEmpty().isNotEmpty(),
        )
    }

    private fun keyFromKazumiAssetFile(fileName: String): String {
        return "kazumi." + fileName
            .removePrefix("block-")
            .removePrefix("kazumi-")
            .removeSuffix(".js")
    }

    private fun createAllKazumiSourceController(
        context: Context,
        assetFiles: List<String>,
    ): SourceController {
        val sourceFolder = File(context.getExternalFilesDir(null), "kazumi_all_blackbox_sources")
        if (sourceFolder.exists()) {
            sourceFolder.deleteRecursively()
        }
        check(sourceFolder.mkdirs() || sourceFolder.exists()) {
            "create all Kazumi source folder failed: ${sourceFolder.absolutePath}"
        }
        assetFiles.forEach { assetFile ->
            val target = File(sourceFolder, assetFile.removePrefix("block-"))
            context.assets.open("$INNER_SOURCE_ASSET_DIR/$assetFile").use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return SourceController(
            sourceFolder = sourceFolder,
            sourcePreferences = Inject.get<SourcePreferences>(),
            innerSourceFileProvider = null,
        )
    }

    private fun enableKazumiTestSources(keys: Set<String>) {
        val sourcePreferences = Inject.get<SourcePreferences>()
        val current = sourcePreferences.configs.getOrDef().toMutableMap()
        keys.forEach { key ->
            val old = current[key]
            current[key] = old?.copy(enable = true) ?: SourceConfig(key, Int.MAX_VALUE, true)
        }
        sourcePreferences.configs.set(current)
    }

    private fun kazumiAssetMeta(context: Context, fileName: String): AssetMeta {
        val text = context.assets.open("$INNER_SOURCE_ASSET_DIR/$fileName").use { input ->
            input.bufferedReader().readText()
        }
        return AssetMeta(
            legacyParser = Regex("""var\s+USE_LEGACY_PARSER\s*=\s*(true|false)""")
                .find(text)
                ?.groupValues
                ?.getOrNull(1)
                .orEmpty(),
            playTimeout = Regex("""var\s+PLAY_TIMEOUT\s*=\s*([0-9]+)""")
                .find(text)
                ?.groupValues
                ?.getOrNull(1)
                .orEmpty(),
        )
    }

    private fun writeReport(prefix: String, reportLines: List<String>) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(context.getExternalFilesDir(null), REPORT_FOLDER)
        check(folder.exists() || folder.mkdirs()) { "create report folder failed: ${folder.absolutePath}" }
        val timestamp = SimpleDateFormat(REPORT_TIMESTAMP_FORMAT, Locale.US).format(Date())
        val file = File(folder, "${prefix}_$timestamp.tsv")
        val content = buildString {
            appendLine("测试报告\t$prefix")
            appendLine("生成时间\t$timestamp")
            appendLine("关键词\t$KEYWORD")
            reportLines.forEach { appendLine(it) }
        }
        file.writeText(content)
        println("报告文件\t${file.absolutePath}")
    }

    private suspend fun waitForSourceBundle(
        controller: ISourceController,
        expectedKeys: Set<String>,
    ): SourceBundle {
        return withTimeout(60_000L) {
            var loadedBundle: SourceBundle? = null
            while (loadedBundle == null) {
                val state = controller.sourceInfo.value
                val bundle = controller.sourceBundle.value
                if (state is ISourceController.SourceInfoState.Info && bundle != null && !bundle.empty()) {
                    val loadedKeys = state.info
                        .filterIsInstance<SourceInfo.Loaded>()
                        .map { it.source.key }
                        .toSet()
                    if (loadedKeys.containsAll(expectedKeys)) {
                        loadedBundle = bundle
                    }
                }
                if (loadedBundle == null) {
                    delay(500L)
                }
            }
            loadedBundle
        }
    }

    private suspend fun waitForSourceInfo(
        controller: ISourceController,
        expectedKeys: Set<String>,
    ): ISourceController.SourceInfoState.Info {
        return withTimeout(60_000L) {
            var loadedState: ISourceController.SourceInfoState.Info? = null
            while (loadedState == null) {
                val state = controller.sourceInfo.value
                if (state is ISourceController.SourceInfoState.Info) {
                    val knownKeys = state.info.map { it.source.key }.toSet()
                    if (knownKeys.containsAll(expectedKeys)) {
                        loadedState = state
                    }
                }
                if (loadedState == null) {
                    delay(500L)
                }
            }
            loadedState
        }
    }

    private suspend fun probeHomeAndSearch(sourceBundle: SourceBundle, key: String): HomeSearchReport {
        val home = probeHome(sourceBundle, key)
        val search = sourceBundle.search(key)
            ?: return HomeSearchReport(key, home, search = "skip", searchNote = "no search component")
        val searchResult = search.search(search.getFirstSearchKey(KEYWORD), KEYWORD)
        return when (searchResult) {
            is SourceResult.Complete -> {
                val covers = searchResult.data.second
                val first = covers.firstOrNull()
                HomeSearchReport(
                    key = key,
                    home = home,
                    search = if (covers.isEmpty()) "empty" else "ok",
                    searchCount = covers.size,
                    searchTitleOk = first?.title?.isNotBlank() == true,
                    searchCoverOk = first?.coverUrl?.isNotBlank() == true,
                    searchFirstTitle = first?.title.orEmpty(),
                    searchFirstCover = first?.coverUrl.orEmpty(),
                    searchNote = if (covers.isEmpty()) "search returned empty" else "",
                )
            }
            is SourceResult.Error -> HomeSearchReport(
                key = key,
                home = home,
                search = "fail",
                searchNote = searchResult.throwable.message.orEmpty(),
            )
        }
    }

    private suspend fun probeSource(sourceBundle: SourceBundle, key: String): Report {
        val pageReport = probeHome(sourceBundle, key)
        val search = sourceBundle.search(key)
        val detailed = sourceBundle.detailed(key)
        val play = sourceBundle.play(key)
        assertNotNull("$key search component", search)
        assertNotNull("$key detailed component", detailed)
        assertNotNull("$key play component", play)

        val searchResult = search!!.search(search.getFirstSearchKey(KEYWORD), KEYWORD)
        val covers = when (searchResult) {
            is SourceResult.Complete -> searchResult.data.second
            is SourceResult.Error -> return Report(
                key,
                home = pageReport,
                search = "fail",
                error = "search: ${searchResult.throwable.message}",
            )
        }
        if (covers.isEmpty()) {
            return Report(key, home = pageReport, search = "empty", error = "search returned empty")
        }
        val cartoonIdFailure = validateCartoonIds(covers)
        if (cartoonIdFailure != null) {
            return Report(key, home = pageReport, search = "invalid", error = "cartoon id: $cartoonIdFailure")
        }
        val first = covers.first()
        if (first.id.isBlank()) {
            return Report(key, home = pageReport, search = "ok", error = "first search id empty")
        }

        val summary = CartoonSummary(first.id, first.source)
        val allResult = detailed!!.getAll(summary)
        val all = when (allResult) {
            is SourceResult.Complete -> allResult.data
            is SourceResult.Error -> return Report(
                key,
                home = pageReport,
                search = "ok",
                detail = "fail",
                searchTitle = first.title,
                searchCover = first.coverUrl.orEmpty(),
                error = "detail: ${allResult.throwable.message}",
            )
        }
        val cartoon = all.first
        val playLines = all.second
        val titleMatch = titleSimilar(first.title, cartoon.title)
        val coverMatch = coverSimilar(first.coverUrl.orEmpty(), cartoon.coverUrl.orEmpty())
        if (playLines.isEmpty() || playLines.first().episode.isEmpty()) {
            return Report(
                key,
                home = pageReport,
                search = "ok",
                detail = "empty",
                titleMatch = titleMatch,
                coverMatch = coverMatch,
                searchTitle = first.title,
                detailTitle = cartoon.title,
                searchCover = first.coverUrl.orEmpty(),
                detailCover = cartoon.coverUrl.orEmpty(),
                error = "no play lines or episodes",
            )
        }

        val playlistFailure = validatePlayLines(playLines)
        if (playlistFailure != null) {
            return Report(
                key = key,
                home = pageReport,
                search = "ok",
                detail = "invalid",
                titleMatch = titleMatch,
                coverMatch = coverMatch,
                searchTitle = first.title,
                detailTitle = cartoon.title,
                searchCover = first.coverUrl.orEmpty(),
                detailCover = cartoon.coverUrl.orEmpty(),
                playlistLines = playLines.size,
                playlistEpisodeCounts = playLines.joinToString(",") { it.episode.size.toString() },
                error = "playlist: $playlistFailure",
            )
        }

        val firstLine = playLines.first()
        val firstEpisode = firstLine.episode.first()
        val playPageUrl = decodeSourceIdUrl(firstEpisode.id)
        val playResult = play!!.getPlayInfo(summary, firstLine, firstEpisode, canCache = false)
        val playerInfo = when (playResult) {
            is SourceResult.Complete -> playResult.data
            is SourceResult.Error -> return Report(
                key,
                home = pageReport,
                search = "ok",
                detail = "ok",
                play = "fail",
                titleMatch = titleMatch,
                coverMatch = coverMatch,
                searchTitle = first.title,
                detailTitle = cartoon.title,
                searchCover = first.coverUrl.orEmpty(),
                detailCover = cartoon.coverUrl.orEmpty(),
                playLine = firstLine.label,
                episode = firstEpisode.label,
                episodeId = firstEpisode.id,
                playPageUrl = playPageUrl,
                error = "play: ${playResult.throwable.message}",
            )
        }

        val videoVerdict = verifyVideoUrl(playerInfo.uri, playerInfo.header.orEmpty())
        assertEquals("source should be preserved for $key", key, first.source)
        return Report(
            key = key,
            home = pageReport,
            search = "ok",
            detail = "ok",
            play = when {
                playerInfo.uri.isBlank() -> "empty"
                videoVerdict.accepted -> "ok"
                else -> "invalid"
            },
            titleMatch = titleMatch,
            coverMatch = coverMatch,
            searchTitle = first.title,
            detailTitle = cartoon.title,
            searchCover = first.coverUrl.orEmpty(),
            detailCover = cartoon.coverUrl.orEmpty(),
            playLine = firstLine.label,
            episode = firstEpisode.label,
            episodeId = firstEpisode.id,
            playPageUrl = playPageUrl,
            playlistLines = playLines.size,
            playlistEpisodeCounts = playLines.joinToString(",") { it.episode.size.toString() },
            playUrl = playerInfo.uri,
            videoVerdict = videoVerdict,
            error = when {
                playerInfo.uri.isBlank() -> "play uri empty"
                !videoVerdict.accepted -> "play url is not a real video: ${videoVerdict.reason}"
                else -> ""
            },
        )
    }

    private fun validatePlayLines(playLines: List<com.heyanle.easybangumi4.plugin.api.entity.PlayLine>): String? {
        playLines.forEachIndexed { lineIndex, line ->
            if (line.episode.isEmpty()) return "line ${lineIndex + 1} has no episodes"
            val episodeIds = hashSetOf<String>()
            line.episode.forEachIndexed { episodeIndex, episode ->
                if (episode.label.isBlank()) return "line ${lineIndex + 1} episode ${episodeIndex + 1} label is blank"
                val pageUrl = decodeSourceIdUrl(episode.id)
                if (!pageUrl.startsWith("http://") && !pageUrl.startsWith("https://")) {
                    return "line ${lineIndex + 1} episode ${episodeIndex + 1} has non-http page url: $pageUrl"
                }
                if (!episodeIds.add(pageUrl)) {
                    return "line ${lineIndex + 1} repeats episode url: $pageUrl"
                }
            }
        }
        return null
    }

    private fun validateCartoonIds(
        covers: List<com.heyanle.easybangumi4.plugin.api.entity.CartoonCover>,
    ): String? {
        val idsByDetailUrl = hashMapOf<String, String>()
        covers.forEachIndexed { index, cover ->
            if (cover.id.isBlank()) return "result ${index + 1} id is blank"
            val existingDetailUrl = idsByDetailUrl.put(cover.id, cover.url)
            if (existingDetailUrl != null && existingDetailUrl != cover.url) {
                return "id ${cover.id} maps to both $existingDetailUrl and ${cover.url}"
            }
        }
        return null
    }

    private suspend fun probeHome(sourceBundle: SourceBundle, key: String): HomeReport {
        val pageComponent = sourceBundle.page(key) ?: return HomeReport(status = "skip", note = "no page component")
        val pages = runCatching { pageComponent.getPages() }.getOrElse {
            return HomeReport(status = "fail", note = it.message.orEmpty())
        }
        if (pages.isEmpty()) {
            return HomeReport(status = "empty", note = "no pages")
        }
        return loadFirstHomePage(pages.first())
    }

    private suspend fun loadFirstHomePage(page: SourcePage): HomeReport {
        return when (page) {
            is SourcePage.SingleCartoonPage -> loadSingleHomePage(page)
            is SourcePage.SingleAsyncPage -> when (val loaded = page.load()) {
                is SourceResult.Complete -> loadSingleHomePage(loaded.data)
                is SourceResult.Error -> HomeReport("fail", page.label, note = loaded.throwable.message.orEmpty())
            }
            is SourcePage.Group -> when (val loaded = page.loadPage()) {
                is SourceResult.Complete -> {
                    val first = loaded.data.firstOrNull()
                        ?: return HomeReport("empty", page.label, note = "group has no sub pages")
                    loadSingleHomePage(first)
                }
                is SourceResult.Error -> HomeReport("fail", page.label, note = loaded.throwable.message.orEmpty())
            }
        }
    }

    private fun decodeSourceIdUrl(value: String): String {
        return runCatching {
            URLDecoder.decode(value.substringBefore("|"), "utf-8")
        }.getOrElse {
            value
        }
    }

    private suspend fun loadSingleHomePage(page: SourcePage.SingleCartoonPage): HomeReport {
        return when (val loaded = page.load(page.firstKey())) {
            is SourceResult.Complete -> {
                val covers = loaded.data.second
                val first = covers.firstOrNull()
                HomeReport(
                    status = if (covers.isEmpty()) "empty" else "ok",
                    label = page.label,
                    count = covers.size,
                    titleOk = first?.title?.isNotBlank() == true,
                    coverOk = first?.coverUrl?.isNotBlank() == true,
                    firstTitle = first?.title.orEmpty(),
                    firstCover = first?.coverUrl.orEmpty(),
                    note = if (covers.isEmpty()) "home page returned empty" else "",
                )
            }
            is SourceResult.Error -> HomeReport("fail", page.label, note = loaded.throwable.message.orEmpty())
        }
    }

    private fun titleSimilar(search: String, detail: String): Boolean {
        if (search.isBlank() || detail.isBlank()) return false
        val s = normalizeTitle(search)
        val d = normalizeTitle(detail)
        return d.contains(s) || s.contains(d)
    }

    private fun normalizeTitle(value: String): String {
        return value.replace(Regex("\\s+"), "")
            .replace("在线观看", "")
            .replace("全集", "")
            .replace("高清", "")
            .replace("完结", "")
            .replace("连载", "")
            .replace("更新至", "")
            .replace("剧场版", "")
            .replace("《", "")
            .replace("》", "")
    }

    private fun coverSimilar(search: String, detail: String): Boolean {
        if (search.isBlank() || detail.isBlank()) return false
        return search.substringBefore("?").substringBefore("#") ==
            detail.substringBefore("?").substringBefore("#")
    }

    private fun verifyVideoUrl(url: String, headers: Map<String, String>): VideoVerdict {
        if (url.isBlank()) return VideoVerdict("empty", accepted = false, reason = "blank url")
        val lower = url.lowercase()
        if (SCRIPT_EXTENSIONS.any { lower.substringBefore("?").endsWith(it) }) {
            return VideoVerdict("script", accepted = false, reason = "script resource")
        }
        if (HTML_EXTENSIONS.any { lower.substringBefore("?").endsWith(it) }) {
            return VideoVerdict("html", accepted = false, reason = "html resource")
        }
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return VideoVerdict("unsupported", accepted = false, reason = "non-http url")
        }
        val directM3u8 = lower.substringBefore("?").endsWith(".m3u8")
        val directMp4 = lower.substringBefore("?").endsWith(".mp4")
        if (!directM3u8 && !directMp4 && QUERY_VIDEO_REGEX.find(url) != null) {
            return VideoVerdict("wrapper", accepted = false, reason = "video url is embedded in wrapper query")
        }
        return probeDirectVideoUrl(url, directM3u8, directM3u8 || directMp4, headers)
    }

    private fun probeDirectVideoUrl(
        url: String,
        directM3u8: Boolean,
        allowUnverified: Boolean,
        headers: Map<String, String>,
    ): VideoVerdict {
        val request = Request.Builder()
            .url(url)
            .header("Range", "bytes=0-4095")
            .apply { headers.forEach { (key, value) -> header(key, value) } }
            .build()
        return runCatching {
            HTTP_CLIENT.newCall(request).execute().use { response ->
                val contentType = response.header("Content-Type").orEmpty().lowercase()
                val body = response.body?.source()?.use { source ->
                    source.request(VIDEO_PROBE_BYTES)
                    source.buffer.clone().readUtf8()
                }.orEmpty().trimStart()
                if (!response.isSuccessful && response.code != 206) {
                    return@runCatching if (!allowUnverified) {
                        VideoVerdict("unknown", accepted = false, reason = "http ${response.code}")
                    } else if (directM3u8) {
                        VideoVerdict("direct_m3u8_unverified", accepted = true, reason = "http ${response.code}")
                    } else {
                        VideoVerdict("direct_video_unverified", accepted = true, reason = "http ${response.code}")
                    }
                }
                when {
                    body.startsWith("#EXTM3U") -> classifyM3u8Body(body)
                    contentType.contains("mpegurl") || contentType.contains("vnd.apple.mpegurl") -> {
                        VideoVerdict("confirmed_hls", accepted = true, reason = contentType)
                    }
                    contentType.startsWith("video/") || contentType.contains("octet-stream") -> {
                        VideoVerdict("confirmed_video", accepted = true, reason = contentType)
                    }
                    directM3u8 -> VideoVerdict("direct_m3u8_unverified", accepted = true, reason = contentType)
                    else -> VideoVerdict("unknown_content", accepted = false, reason = contentType)
                }
            }
        }.getOrElse {
            if (!allowUnverified) {
                VideoVerdict("unknown", accepted = false, reason = it.message.orEmpty())
            } else if (directM3u8) {
                VideoVerdict("direct_m3u8_unverified", accepted = true, reason = it.message.orEmpty())
            } else {
                VideoVerdict("direct_video_unverified", accepted = true, reason = it.message.orEmpty())
            }
        }
    }

    private fun classifyM3u8Body(body: String): VideoVerdict {
        val mediaLines = body.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .take(8)
            .toList()
        val first = mediaLines.firstOrNull().orEmpty()
        val lower = first.lowercase()
        return when {
            first.isBlank() -> VideoVerdict("confirmed_hls", accepted = true, reason = "playlist header only")
            IMAGE_EXTENSIONS.any { lower.substringBefore("?").endsWith(it) } -> {
                VideoVerdict("snapshot_playlist", accepted = true, reason = "first media segment is image-like: $first")
            }
            lower.substringBefore("?").endsWith(".m3u8") ||
                lower.substringBefore("?").endsWith(".ts") ||
                lower.substringBefore("?").endsWith(".m4s") ||
                lower.substringBefore("?").endsWith(".mp4") -> {
                VideoVerdict("confirmed_hls", accepted = true, reason = "media segment: $first")
            }
            else -> VideoVerdict("confirmed_hls", accepted = true, reason = "playlist media: $first")
        }
    }

    private data class HomeReport(
        val status: String,
        val label: String = "",
        val count: Int = 0,
        val titleOk: Boolean = false,
        val coverOk: Boolean = false,
        val firstTitle: String = "",
        val firstCover: String = "",
        val note: String = "",
    ) {
        fun toFields(): List<String> {
            return listOf(
                "首页=${statusText(status)}",
                "首页标签=$label",
                "首页数量=$count",
                "首页标题有效=${booleanText(titleOk)}",
                "首页封面有效=${booleanText(coverOk)}",
                "首页首个标题=$firstTitle",
                "首页首个封面=$firstCover",
                "首页备注=${noteText(note)}",
            )
        }
    }

    private data class VideoVerdict(
        val kind: String = "skip",
        val accepted: Boolean = false,
        val reason: String = "",
    )

    private data class AssetMeta(
        val legacyParser: String = "",
        val playTimeout: String = "",
    )

    private data class HomeSearchReport(
        val key: String,
        val home: HomeReport,
        val search: String,
        val searchCount: Int = 0,
        val searchTitleOk: Boolean = false,
        val searchCoverOk: Boolean = false,
        val searchFirstTitle: String = "",
        val searchFirstCover: String = "",
        val searchNote: String = "",
    ) {
        fun toLine(): String {
            return listOf(
                "首页搜索报告",
                key,
                *home.toFields().toTypedArray(),
                "搜索=${statusText(search)}",
                "搜索数量=$searchCount",
                "搜索标题有效=${booleanText(searchTitleOk)}",
                "搜索封面有效=${booleanText(searchCoverOk)}",
                "搜索首个标题=$searchFirstTitle",
                "搜索首个封面=$searchFirstCover",
                "搜索备注=${noteText(searchNote)}",
            ).joinToString("\t")
        }
    }

    private data class Report(
        val key: String,
        val assetFile: String = "",
        val assetStatus: String = "",
        val home: HomeReport = HomeReport(status = "skip"),
        val search: String = "skip",
        val detail: String = "skip",
        val play: String = "skip",
        val titleMatch: Boolean = false,
        val coverMatch: Boolean = false,
        val searchTitle: String = "",
        val detailTitle: String = "",
        val searchCover: String = "",
        val detailCover: String = "",
        val playLine: String = "",
        val episode: String = "",
        val episodeId: String = "",
        val playPageUrl: String = "",
        val playlistLines: Int = 0,
        val playlistEpisodeCounts: String = "",
        val legacyParser: String = "",
        val playTimeout: String = "",
        val playUrl: String = "",
        val videoVerdict: VideoVerdict = VideoVerdict(),
        val error: String = "",
    ) {
        val ok: Boolean = search == "ok" && detail == "ok" && play == "ok"

        fun toLine(): String {
            return listOf(
                "黑盒报告",
                key,
                "源文件=$assetFile",
                "资产状态=${assetStatusText(assetStatus)}",
                *home.toFields().toTypedArray(),
                "搜索=${statusText(search)}",
                "详情=${statusText(detail)}",
                "播放=${statusText(play)}",
                "标题匹配=${booleanText(titleMatch)}",
                "封面匹配=${booleanText(coverMatch)}",
                "搜索标题=$searchTitle",
                "详情标题=$detailTitle",
                "搜索封面=$searchCover",
                "详情封面=$detailCover",
                "播放线路=$playLine",
                "剧集=$episode",
                "剧集ID=$episodeId",
                "播放页=$playPageUrl",
                "播放线路数=$playlistLines",
                "每线路剧集数=$playlistEpisodeCounts",
                "Legacy解析=$legacyParser",
                "播放超时=$playTimeout",
                "播放地址=$playUrl",
                "视频=${videoKindText(videoVerdict.kind)}",
                "视频说明=${noteText(videoVerdict.reason)}",
                "错误=${noteText(error)}",
            ).joinToString("\t")
        }

        fun withAssetMeta(fileName: String, meta: AssetMeta): Report {
            return copy(
                assetFile = fileName,
                assetStatus = if (fileName.startsWith("block-")) "blocked" else "active",
                legacyParser = meta.legacyParser,
                playTimeout = meta.playTimeout,
            )
        }
    }

    private companion object {
        const val KEYWORD = "孤独摇滚"

        const val VIDEO_PROBE_BYTES = 4096L
        const val REPORT_FOLDER = "inner_source_reports"
        const val REPORT_TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss_SSS"
        const val INNER_SOURCE_ASSET_DIR = "inner_source"

        val HTTP_CLIENT: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .build()
        val QUERY_VIDEO_REGEX = Regex("""[?&][^=]+=(https?%3A%2F%2F|https?://).+?\.(m3u8|mp4)""", RegexOption.IGNORE_CASE)
        val SCRIPT_EXTENSIONS = setOf(".js", ".css", ".wasm")
        val HTML_EXTENSIONS = setOf(".html", ".htm", ".php", ".asp", ".aspx", ".jsp")
        val IMAGE_EXTENSIONS = setOf(".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg")

        val ACTIVE_KAZUMI_KEYS = setOf(
            "kazumi.9ciyuan",
            "kazumi.anime7",
            "kazumi.ant",
            "kazumi.baimao",
            "kazumi.mxdm",
            "kazumi.omofun03",
            "kazumi.ylsp",
        )

        val STABLE_KAZUMI_KEYS = setOf(
            "kazumi.9ciyuan",
            "kazumi.anime7",
            "kazumi.ant",
            "kazumi.baimao",
            "kazumi.mxdm",
            "kazumi.omofun03",
            "kazumi.ylsp",
        )

        val SEARCH_FAILED_KAZUMI_KEYS = setOf(
            "kazumi.1ani",
            "kazumi.233dm",
            "kazumi.295yhw",
            "kazumi.80tv",
            "kazumi.aafun",
            "kazumi.akianime",
            "kazumi.anfuns",
            "kazumi.aowu",
            "kazumi.bf",
            "kazumi.bobodm",
            "kazumi.brovod",
            "kazumi.ciyuancheng",
            "kazumi.clicli",
            "kazumi.cyfz",
            "kazumi.dlma",
            "kazumi.dm84",
            "kazumi.dmand",
            "kazumi.dmghg",
            "kazumi.dms",
            "kazumi.eacg",
            "kazumi.enlie",
            "kazumi.fantuan",
            "kazumi.fcdm",
            "kazumi.ffdm",
            "kazumi.fqdm",
            "kazumi.gugu3",
            "kazumi.hfkzm",
            "kazumi.hzdm",
            "kazumi.if",
            "kazumi.jzsdm",
            "kazumi.k8dm",
            "kazumi.kimani",
            "kazumi.libvio",
            "kazumi.lmm",
            "kazumi.mandao",
            "kazumi.mcy",
            "kazumi.mengfan",
            "kazumi.mgnacg",
            "kazumi.mifun",
            "kazumi.mitang",
            "kazumi.mitaodm",
            "kazumi.moefan",
            "kazumi.mt",
            "kazumi.mutefun",
            "kazumi.mwcy",
            "kazumi.nekodm",
            "kazumi.nt",
            "kazumi.nyafun",
            "kazumi.omofunz",
            "kazumi.pekolove",
            "kazumi.qdm",
            "kazumi.qifun",
            "kazumi.qimi",
            "kazumi.qkan9",
            "kazumi.skr",
            "kazumi.tt776b",
            "kazumi.wydm",
            "kazumi.xfdm",
            "kazumi.xfdmneo",
            "kazumi.xiaobao",
            "kazumi.xiapidm",
            "kazumi.xigua",
            "kazumi.yinghua",
            "kazumi.yishijie",
            "kazumi.ziyedm",
            "kazumi.zkk79",
        )

        val ACTIVE_INNER_JS_KEYS = ACTIVE_KAZUMI_KEYS + setOf(
            "heyanle.age",
            "heyanle.ggl",
            "heyanle.xifan",
        )

        private fun statusText(value: String): String {
            return when (value) {
                "ok" -> "通过"
                "fail" -> "失败"
                "empty" -> "空"
                "skip" -> "跳过"
                "invalid" -> "无效"
                else -> value
            }
        }

        private fun booleanText(value: Boolean): String {
            return if (value) "是" else "否"
        }

        private fun videoKindText(value: String): String {
            return when (value) {
                "confirmed_hls" -> "确认HLS"
                "confirmed_video" -> "确认视频"
                "snapshot_playlist" -> "图片分片播放列表"
                "direct_m3u8_unverified" -> "直接M3U8待验证"
                "direct_video_unverified" -> "直接视频待验证"
                "script" -> "脚本资源"
                "html" -> "网页资源"
                "wrapper" -> "播放器包装页"
                "unsupported" -> "不支持的地址"
                "unknown" -> "未知地址"
                "unknown_content" -> "未知内容"
                "empty" -> "空"
                "skip" -> "跳过"
                else -> value
            }
        }

        private fun assetStatusText(value: String): String {
            return when (value) {
                "active" -> "启用"
                "blocked" -> "block"
                else -> value
            }
        }

        private fun noteText(value: String): String {
            return when {
                value.isBlank() -> ""
                value == "no page component" -> "无首页组件"
                value == "no pages" -> "无首页页面"
                value == "home page returned empty" -> "首页返回空"
                value == "search returned empty" -> "搜索返回空"
                value == "blank url" -> "播放地址为空"
                value == "script resource" -> "脚本资源"
                value == "html resource" -> "网页资源"
                value == "non-http url" -> "非 HTTP 地址"
                value == "video url is embedded in wrapper query" -> "视频地址嵌在播放器包装页参数中"
                value == "not direct m3u8/mp4" -> "不是直接 m3u8/mp4 地址"
                value == "playlist header only" -> "仅检测到播放列表头"
                value.startsWith("first media segment is image-like: ") ->
                    "首个媒体分片疑似图片：" + value.removePrefix("first media segment is image-like: ")
                value.startsWith("media segment: ") ->
                    "媒体分片：" + value.removePrefix("media segment: ")
                value.startsWith("playlist media: ") ->
                    "播放列表媒体：" + value.removePrefix("playlist media: ")
                value.startsWith("play url is not a real video: ") ->
                    "播放地址不是真实视频：" + value.removePrefix("play url is not a real video: ")
                value == "play uri empty" -> "播放地址为空"
                value == "first search id empty" -> "首个搜索结果 ID 为空"
                value == "no play lines or episodes" -> "无播放线路或剧集"
                else -> value
            }
        }
    }
}
