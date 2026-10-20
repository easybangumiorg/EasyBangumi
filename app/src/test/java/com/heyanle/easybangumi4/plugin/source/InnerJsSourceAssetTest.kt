package com.heyanle.easybangumi4.plugin.source

import com.heyanle.easybangumi4.plugin.source.js.SourceMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mozilla.javascript.Context
import org.mozilla.javascript.ImporterTopLevel
import java.io.File
import java.net.URI

class InnerJsSourceAssetTest {

    @Test
    fun repositorySourcesAreDebugOnlyAndExcludedFromReleaseAssets() {
        val buildFile = File("build.gradle.kts")
        val buildScript = buildFile.readText()
        assertTrue(
            "debug builds should retain repository sources for development and tests",
            buildScript.contains("getByName(\"debug\").assets.srcDir(rootProject.file(\"inner_source\"))"),
        )
        assertFalse(
            "main assets would leak repository sources into release APKs",
            buildScript.contains("getByName(\"main\").assets.srcDir(rootProject.file(\"inner_source\"))"),
        )
    }

    @Test
    fun innerSourceAssetsHaveSupportedMetadata() {
        val files = innerSourceFiles()

        // Development inputs include unverified candidates and older source APIs. The stable
        // regression set must remain supported, while candidates are classified by admission.
        val stable = stableRegressionFiles().map { it.name }.toSet()
        assertTrue("source candidates must not be empty", files.isNotEmpty())
        val keys = files.map { readMetadata(it)[SourceMetadata.SOURCE_TAG_KEY] }
        assertEquals("candidate keys must be unique", keys.size, keys.toSet().size)

        files.forEach { file ->
            val metadata = readMetadata(file)
            assertTrue("${file.name} key should not be blank", metadata[SourceMetadata.SOURCE_TAG_KEY].isNotNullOrBlank())
            assertTrue("${file.name} label should not be blank", metadata[SourceMetadata.SOURCE_TAG_LABEL].isNotNullOrBlank())
            assertTrue("${file.name} version should not be blank", metadata[SourceMetadata.SOURCE_TAG_VERSION_NAME].isNotNullOrBlank())
            assertTrue(
                "${file.name} versionCode should be valid",
                metadata[SourceMetadata.SOURCE_TAG_VERSION_CODE]?.toLongOrNull() != null,
            )
            assertTrue(
                "${file.name} libVersion should be declared",
                (metadata[SourceMetadata.SOURCE_TAG_LIB_VERSION]?.toIntOrNull() ?: 0) > 0,
            )
            if (file.name in stable) {
                assertTrue("${file.name} stable source API should be supported",
                    metadata[SourceMetadata.SOURCE_TAG_LIB_VERSION]?.toIntOrNull() in PluginV3.SUPPORTED_LIB_VERSION_RANGE)
            }
            val cover = metadata[SourceMetadata.SOURCE_TAG_COVER]
            assertTrue("${file.name} cover should not be blank", cover.isNotNullOrBlank())
            assertTrue(
                "${file.name} cover should be an HTTP(S) URL or the built-in default",
                cover == "default" || cover?.let {
                    runCatching { URI(it).scheme in setOf("http", "https") }.getOrDefault(false)
                } == true,
            )
        }
    }

    @Test
    fun innerSourceAssetsHaveValidRhinoSyntax() {
        innerSourceFiles().forEach { file ->
            Context.enter().apply {
                optimizationLevel = -1
                languageVersion = Context.VERSION_ES6
            }.use { context ->
                // Syntax validation must not execute a candidate's top-level network or config
                // code; runtime capabilities are checked through the Android acceptance path.
                file.reader().use { reader -> context.compileReader(reader, file.name, 1, null) }
            }
        }
    }

    @Test
    fun innerSourceAssetsDoNotCallKotlinStaticWrappersDirectly() {
        innerSourceFiles().forEach { file ->
            val js = file.readText()
            assertTrue(
                "${file.name} should call Java utility facades instead of Kotlin wrappers",
                !js.contains("KtWrapper"),
            )
        }
    }

    @Test
    fun innerSourceAssetsUseInjectedUtilityInstances() {
        val forbidden = listOf(
            "OkhttpHelper.client",
            "OkhttpHelper.cloudflareClient",
            "OkhttpHelper.cloudflareWebViewClient",
            "NetworkHelper.defaultLinuxUA",
            "RenderHelper.renderVideo",
            "renderHelper.renderVideo(",
            "WebProxyProvider.getWebProxy",
            "webViewHelperV2.renderHtmlFromJs",
        )
        innerSourceFiles().forEach { file ->
            val js = file.readText()
            forbidden.forEach { pattern ->
                assertTrue(
                    "${file.name} should use Inject_* instance instead of $pattern",
                    !js.contains(pattern),
                )
            }
        }
    }

    @Test
    fun innerSourceAssetsUseNonSuspendRenderVideoBridge() {
        innerSourceFiles().forEach { file ->
            val js = file.readText()
            if (js.contains("new JsVideoStrategy")) {
                assertTrue(
                    "${file.name} should call renderVideoFromJs from JS",
                    js.contains("renderHelper.renderVideoFromJs("),
                )
            }
        }
    }

    @Test
    fun activeKazumiAssetsExposePageComponent() {
        stableRegressionFiles()
            .filter { it.name.startsWith("kazumi-") }
            .forEach { file ->
                val js = file.readText()
                assertTrue(
                    "${file.name} should expose PageComponent_getMainTabs",
                    js.contains("function PageComponent_getMainTabs()"),
                )
                assertTrue(
                    "${file.name} should expose PageComponent_getSubTabs",
                    js.contains("function PageComponent_getSubTabs(mainTab)"),
                )
                assertTrue(
                    "${file.name} should expose PageComponent_getContent",
                    js.contains("function PageComponent_getContent(mainTab, subTab, pageKey)"),
                )
            }
    }

    @Test
    fun searchWithCheckSourcesThrowVerificationRequest() {
        innerSourceFiles().forEach { file ->
            val js = file.readText()
            if (js.contains("SearchComponent_searchWithCheck")) {
                assertTrue(
                    "${file.name} should call webProxy.needUserCheck when verification is required",
                    js.contains(".needUserCheck("),
                )
            }
        }
    }

    @Test
    fun rhinoRuntimeSupportsCompatibleSyntax() {
        Context.enter().apply {
            optimizationLevel = -1
            languageVersion = Context.VERSION_ES6
        }.use { context ->
            val scope = ImporterTopLevel(context)
            context.initStandardObjects(scope)
            val result = context.evaluateString(
                scope,
                """
                var total = 1;
                var values = [2, 3];
                for (var i = 0; i < values.length; i++) {
                    total += values[i];
                }
                total;
                """.trimIndent(),
                "es6-smoke",
                1,
                null,
            )
            assertEquals("6", Context.toString(result))
        }
    }

    private fun innerSourceFiles(): List<File> {
        val folder = File("../inner_source")
        return folder.listFiles()
            ?.filter { it.isFile && it.name.endsWith(PluginV3.JS_SOURCE_SUFFIX) }
            ?.sortedBy { it.name }
            .orEmpty()
    }

    private fun stableRegressionFiles(): List<File> {
        val names = setOf("age.js", "girigirilove.js", "kazumi-9ciyuan.js", "kazumi-anime7.js",
            "kazumi-ant.js", "kazumi-baimao.js", "kazumi-mxdm.js", "kazumi-omofun03.js",
            "kazumi-ylsp.js", "xifandm.js")
        val files = innerSourceFiles().filter { it.name in names }
        assertEquals("stable regression inputs must remain present", names, files.map { it.name }.toSet())
        return files
    }

    private inline fun <T> Context.use(block: (Context) -> T): T {
        return try {
            block(this)
        } finally {
            Context.exit()
        }
    }

    private fun readMetadata(file: File): Map<String, String> {
        val metadata = LinkedHashMap<String, String>()
        file.bufferedReader().useLines { lines ->
            lines.takeWhile { it.isNotEmpty() && it.startsWith("//") }
                .forEach { line ->
                    val body = line.removePrefix("//").trimStart()
                    if (body.startsWith("@")) {
                        val split = body.indexOf(' ')
                        if (split > 1) {
                            metadata[body.substring(1, split)] = body.substring(split + 1)
                        }
                    }
                }
        }
        return metadata
    }

    private fun String?.isNotNullOrBlank(): Boolean {
        return !this.isNullOrBlank()
    }

}
