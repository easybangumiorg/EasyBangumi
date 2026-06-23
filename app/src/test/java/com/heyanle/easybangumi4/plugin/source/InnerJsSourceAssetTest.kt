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
            listOf(
                "age.js",
                "girigirilove.js",
                "kazumi-1ani.js",
                "kazumi-233dm.js",
                "kazumi-295yhw.js",
                "kazumi-7sefun.js",
                "kazumi-80tv.js",
                "kazumi-9ciyuan.js",
                "kazumi-aafun.js",
                "kazumi-akianime.js",
                "kazumi-anfuns.js",
                "kazumi-anime7.js",
                "kazumi-ant.js",
                "kazumi-aowu.js",
                "kazumi-baimao.js",
                "kazumi-bf.js",
                "kazumi-bobodm.js",
                "kazumi-brovod.js",
                "kazumi-ciyuancheng.js",
                "kazumi-clicli.js",
                "kazumi-cyfz.js",
                "kazumi-dlma.js",
                "kazumi-dm84.js",
                "kazumi-dmand.js",
                "kazumi-dmghg.js",
                "kazumi-dms.js",
                "kazumi-eacg.js",
                "kazumi-enlie.js",
                "kazumi-fantuan.js",
                "kazumi-fcdm.js",
                "kazumi-ffdm.js",
                "kazumi-fqdm.js",
                "kazumi-gpjda.js",
                "kazumi-gugu3.js",
                "kazumi-hfkzm.js",
                "kazumi-hzdm.js",
                "kazumi-if.js",
                "kazumi-jzsdm.js",
                "kazumi-k8dm.js",
                "kazumi-kimani.js",
                "kazumi-libvio.js",
                "kazumi-lmm.js",
                "kazumi-mandao.js",
                "kazumi-mcy.js",
                "kazumi-mengfan.js",
                "kazumi-mgnacg.js",
                "kazumi-mifun.js",
                "kazumi-mitang.js",
                "kazumi-mitaodm.js",
                "kazumi-moefan.js",
                "kazumi-mt.js",
                "kazumi-mutefun.js",
                "kazumi-mwcy.js",
                "kazumi-mxdm.js",
                "kazumi-nekodm.js",
                "kazumi-nt.js",
                "kazumi-nyafun.js",
                "kazumi-omofun03.js",
                "kazumi-omofunz.js",
                "kazumi-pekolove.js",
                "kazumi-qdm.js",
                "kazumi-qifun.js",
                "kazumi-qimi.js",
                "kazumi-qkan9.js",
                "kazumi-skr.js",
                "kazumi-tt776b.js",
                "kazumi-wydm.js",
                "kazumi-xfdm.js",
                "kazumi-xfdmneo.js",
                "kazumi-xiaobao.js",
                "kazumi-xiapidm.js",
                "kazumi-xigua.js",
                "kazumi-yinghua.js",
                "kazumi-yishijie.js",
                "kazumi-ylsp.js",
                "kazumi-ziyedm.js",
                "kazumi-zkk79.js",
                "xifandm.js",
            ),
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
            if (js.contains("new JsVideoStrategy")) {
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
