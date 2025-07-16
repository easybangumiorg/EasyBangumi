import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.easybangumi.next.shared.source.api.utils.WebViewHelper
import org.easybangumi.next.shared.source.core.utils.WebViewHelperImpl

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
class Test {

    @kotlin.test.Test
    fun webViewHelperTest() {
        runBlocking {
            coroutineScope {
                val helper = WebViewHelperImpl(this)
                val strategy1 = WebViewHelper.RenderedStrategy(
                    url = "https://anime.girigirilove.com/playGV26627-2-1/",
                    callBackRegex = "^https://m3u8\\.girigirilove\\.com/addons/aplyer/atom\\.php",
                    needContent = true,
                    needInterceptResource = true
                )
                testRenderedHtml(helper, strategy1)

            }
        }

    }

    private suspend fun testRenderedHtml(
        helper: WebViewHelperImpl,
        strategy: WebViewHelper.RenderedStrategy,
    ) {
        val result = helper.renderedHtml(strategy)
        println()
        println("Rendered HTML")
        println("strategy: $strategy")
        println("result: ${result}")
        println()
    }
}