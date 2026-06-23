package com.heyanle.easybangumi4.plugin.source

import com.heyanle.easybangumi4.plugin.source.js.SourceMetadata
import com.heyanle.easybangumi4.plugin.source.js.source.JsSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mozilla.javascript.Context
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.ScriptableObject
import java.io.File

class InnerJsSourceAssetTest {

    @Test
    fun innerSourceAssetsHaveSupportedMetadata() {
        val files = innerSourceFiles()

        assertEquals(
            listOf("age.js", "girigirilove.js", "xifandm.js"),
            files.map { it.name }.sorted(),
        )

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
                "${file.name} libVersion should be supported",
                metadata[SourceMetadata.SOURCE_TAG_LIB_VERSION]?.toIntOrNull() in PluginV3.SUPPORTED_LIB_VERSION_RANGE,
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
                val scope = ImporterTopLevel(context)
                context.initStandardObjects(scope)
                putTestInjects(scope, file.nameWithoutExtension)
                context.evaluateString(scope, JsSource.JS_IMPORT, "import", 1, null)
                context.evaluateReader(scope, file.reader(), file.name, 1, null)
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
            if (js.contains("new RenderHelper.VideoStrategy")) {
                assertTrue(
                    "${file.name} should call renderVideoFromJs from JS",
                    js.contains("renderHelper.renderVideoFromJs("),
                )
            }
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
        val folder = File("src/main/assets/inner_source")
        return folder.listFiles()
            ?.filter { it.isFile && it.name.endsWith(PluginV3.JS_SOURCE_SUFFIX) }
            ?.sortedBy { it.name }
            .orEmpty()
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

    private fun putTestInjects(scope: ScriptableObject, sourceKey: String) {
        ScriptableObject.putProperty(scope, "Inject_Source", TestSource(sourceKey))
        ScriptableObject.putProperty(scope, "Inject_NetworkHelper", Any())
        ScriptableObject.putProperty(scope, "Inject_OkhttpHelper", Any())
        ScriptableObject.putProperty(scope, "Inject_PreferenceHelper", Any())
        ScriptableObject.putProperty(scope, "Inject_RenderHelper", Any())
        ScriptableObject.putProperty(scope, "Inject_WebViewHelperV2", Any())
        ScriptableObject.putProperty(scope, "Inject_WebProxyProvider", Any())
    }

    private class TestSource(val key: String)
}
