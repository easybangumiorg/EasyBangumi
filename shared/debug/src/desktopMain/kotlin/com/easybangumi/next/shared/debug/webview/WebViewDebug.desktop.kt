package com.easybangumi.next.shared.debug.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.easybangumi.next.shared.debug.DebugScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.shared.source.api.utils.WebViewHelper
import org.easybangumi.next.shared.source.core.utils.WebViewHelperImpl

private val logger = logger("WebViewDebug")
@Composable
actual fun DebugScope.WebViewDebug() {
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Button(onClick = {
            val helper = WebViewHelperImpl(scope)
            scope.launch {
                val strategy1 = WebViewHelper.RenderedStrategy(
                    url = "https://anime.girigirilove.com/playGV26627-2-1/",
                    callBackRegex = "(https://anime.girigirilove.com/addons/dp/player/index.php?.*|https://m3u8.girigirilove.com/addons/aplyer/atom.php?.*)",
                    needContent = true,
                    needInterceptResource = false,
                    userAgentString = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36 OPR/26.0.1656.60"
                )
                testRenderedHtml(helper, strategy1)
            }


        }) {
            Text("test1")
        }
    }

}

private suspend fun testRenderedHtml(
    helper: WebViewHelperImpl,
    strategy: WebViewHelper.RenderedStrategy,
) {
    val result = helper.renderedHtml(strategy)
    logger.info("Rendered HTML")
    logger.info("strategy: $strategy")
    logger.info("result: ${result}")
}