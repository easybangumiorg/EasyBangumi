import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.binding.JsObject
import com.dokar.quickjs.evaluate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import org.easybangumi.next.lib.logger.KotlinLogger
import org.easybangumi.next.lib.logger.KotlinLoggerProxy
import org.easybangumi.next.lib.logger.debugLoggerProxy
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.source.api.utils.PreferenceHelper
import org.easybangumi.next.shared.source.api.utils.StringHelper
import org.easybangumi.next.shared.source.core.utils.NetworkHelperImpl
import org.easybangumi.next.shared.source.core.utils.WebViewHelperImpl
import org.easybangumi.next.shared.source.quick.utils.QuickWebViewHelper
import org.easybangumi.next.shared.source.quick.utils.callFunctionWithDataState
import org.easybangumi.next.shared.source.quick.utils.logger
import org.easybangumi.next.shared.source.quick.utils.register
import org.easybangumi.next.shared.source.quick.utils.toDataState
import kotlin.test.Test

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
class QuickTest {

    companion object {
        val TEST_CODE_I = """
async function test1() {
    let content = await WebViewHelper.use(async function(web) {
        await web.loadUrl("https://www.easybangumi.org");
        await web.waitingForPageLoaded();
        let content = await web.getContent();
        return content;
    });
    return content;
}
        """.trimIndent()
    }

    @Test
    fun testQuick1() {
        runBlocking {
            debugLoggerProxy = KotlinLoggerProxy()
            val quickJs = QuickJs.create(Dispatchers.IO)
            // 1.register utils
            val webViewHelper = QuickWebViewHelper(WebViewHelperImpl())
            val preferenceHelper = object: PreferenceHelper {
                val map = mutableMapOf<String, String>()
                override suspend fun map(): Map<String, String> {
                    println("PreferenceHelper map called")
                    return map.toMap()
                }

                override suspend fun get(key: String, def: String): String {
                    println("PreferenceHelper get called: key=$key, def=$def")
                    return map[key] ?: def
                }

                override suspend fun put(key: String, value: String) {
                    map[key] = value
                    println("PreferenceHelper put called: key=$key, value=$value")
                }
            }
            val stringHelper = object: StringHelper {
                override fun toast(text: String) {
                    println("StringHelper toast: $text")
                }

                override fun moeSnackBar(text: String) {
                    println("StringHelper moeSnackBar: $text")
                }

                override fun moeDialog(text: String) {
                    println("StringHelper moeDialog: $text")
                }
            }

            val networkHelper = NetworkHelperImpl()

            quickJs.register(preferenceHelper)
            quickJs.register(stringHelper)
            quickJs.register(networkHelper)
            quickJs.register(webViewHelper)

            val logger = logger("QuickTest")
            quickJs.register(logger)

            val path = "/Users/heyanlin/Desktop/EasyBangumi/app/assets/common".toPath()
            val mjsPath = path.resolve("ksoup-js/ksoup.js")
            val ksoupCode = mjsPath.toFile().readText()
            val import = """
                (function() {
                    try {
                        // 动态导入 ES 模块
                        const module = import('${mjsPath}');
                        Log.i(module);
                    } catch (error) {
                        Log.e(error);
                    }
                })();
            """.trimIndent()
            quickJs.evaluate<Unit>(ksoupCode, "ImportKsoup.js", true)


//            quickJs.evaluate<Unit>(TEST_CODE_I, "TestCodeI.js", false)
//            val res1 = quickJs.callFunctionWithDataState("test1")





//            println(res1.toDataState<String>())
        }

    }

}