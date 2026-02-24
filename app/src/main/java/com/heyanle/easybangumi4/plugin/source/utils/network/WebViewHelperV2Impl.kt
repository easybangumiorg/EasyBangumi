package com.heyanle.easybangumi4.plugin.source.utils.network

import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.heyanle.easybangumi4.WEB_VIEW_USER
import com.heyanle.easybangumi4.navControllerRef
import com.heyanle.easybangumi4.plugin.source.utils.LightweightGettingWebViewClient
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2.RenderedResult
import com.heyanle.easybangumi4.utils.WebViewManager
import com.heyanle.easybangumi4.utils.clearWeb
import com.heyanle.easybangumi4.utils.evaluateJavascript
import com.heyanle.easybangumi4.utils.getHtml
import com.heyanle.easybangumi4.utils.stop
import com.heyanle.easybangumi4.utils.waitUntil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by heyanlin on 2024/6/4.
 */
class WebViewHelperV2Impl(
    private val webViewManager: WebViewManager
): WebViewHelperV2 {

    companion object {

        // 创建三次还失败那就寄
        const val MAX_TRY_COUNT = 3

        var webViewRef: WeakReference<WebView>? = null
        var check: WeakReference<(WebView) -> Boolean>? = null
        var stop: WeakReference<(WebView) -> Unit>? = null

        var webPageShowing = false


        private const val blobHookJs = """
        let origin = window.URL.createObjectURL
        window.URL.createObjectURL = function (t) {
            let blobUrl = origin(t)
            let xhr = new XMLHttpRequest()
            xhr.onload = function () {
                 window.blobHook.handleWrapper(xhr.responseText)
            }
            xhr.open('get', blobUrl)
            xhr.send();
            return blobUrl
        }
    """
    }


    private val scope = MainScope()

    override fun getGlobalWebView(): WebView {
        return webViewManager.getWebViewOrNull() ?: throw WebViewCreatedException()
    }

    fun getGlobalWebViewOrNull(): WebView? {
        return webViewManager.getWebViewOrNull()
    }

    fun recyclerWebView(webView: WebView) {
        webViewManager.recycle(webView)
    }



    override fun openWebPage(onCheck: (WebView) -> Boolean, onStop: (WebView) -> Unit) {
        scope.launch {
            if (webPageShowing) {
                return@launch
            }
            webPageShowing = true
            webViewRef = WeakReference(getGlobalWebView())
            check = WeakReference(onCheck)
            stop = WeakReference(onStop)
            navControllerRef?.get()?.navigate(WEB_VIEW_USER)
        }
    }

    override fun openWebPage(
        webView: WebView,
        onCheck: (WebView) -> Boolean,
        onStop: (WebView) -> Unit
    ) {
        scope.launch {
            if (webPageShowing) {
                return@launch
            }
            webPageShowing = true
            webViewRef = WeakReference(webView)
            check = WeakReference(onCheck)
            stop = WeakReference(onStop)
            navControllerRef?.get()?.navigate(WEB_VIEW_USER)
        }
    }

    fun openWebPage(
        webView: WebView,
        tips: String,
        onCheck: (WebView) -> Boolean,
        onStop: (WebView) -> Unit
    ) {
        scope.launch {
            if (webPageShowing) {
                return@launch
            }
            webPageShowing = true
            webViewRef = WeakReference(webView)
            check = WeakReference(onCheck)
            stop = WeakReference(onStop)
            navControllerRef?.get()?.navigate("$WEB_VIEW_USER?tips=$tips")
        }
    }

    fun renderHtmlFromJs(strategy: WebViewHelperV2.RenderedStrategy): WebViewHelperV2.RenderedResult {
        var res: RenderedResult? = null
        val countDownLatch = CountDownLatch(1)
        scope.launch {
            res = renderedHtml(strategy)
            countDownLatch.countDown()
        }
        countDownLatch.await(10, TimeUnit.SECONDS)
        return res ?: RenderedResult(
            strategy = strategy,
            url = "",
            isTimeout = true,
            content = "",
            interceptResource = ""
        )
    }

    override suspend fun renderedHtml(strategy: WebViewHelperV2.RenderedStrategy): WebViewHelperV2.RenderedResult {
        val webview = getGlobalWebViewOrNull() ?: throw WebViewCreatedException()
        return withContext(Dispatchers.Main){
            webview.clearWeb()
            webview.settings.apply {
                setUserAgentString(strategy.userAgentString ?: userAgentString)
                defaultTextEncodingName = strategy.encoding
            }
            webview.resumeTimers()


            if (!strategy.isBlockBlob) {
                // 拦截普通资源模式
                webview.loadUrl(strategy.url, strategy.header.orEmpty())
                var r = webview.waitUntil(
                    if (strategy.callBackRegex.isEmpty()) null else Regex(strategy.callBackRegex),
                    strategy.timeOut,
                    true,
                    ignoreTimeoutExt = true
                )
                if (r.isNotEmpty() || strategy.actionJs == null) {
                    val content = webview.getHtml().also {
                        webview.stop()
                        recyclerWebView(webview)
                    }
                    return@withContext WebViewHelperV2.RenderedResult(
                        strategy,
                        strategy.url,
                        false,
                        content,
                        r
                    )
                }
                webview.evaluateJavascript(strategy.actionJs)
                r = try {
                    webview.waitUntil(
                        if (strategy.callBackRegex.isEmpty()) null else Regex(strategy.callBackRegex),
                        strategy.timeOut,
                        true,
                        ignoreTimeoutExt = false
                    )
                } catch (e: CancellationException) {
                    e.printStackTrace()
                    recyclerWebView(webview)
                    return@withContext WebViewHelperV2.RenderedResult(
                        strategy,
                        strategy.url,
                        true,
                        "",
                        ""
                    )
                }
                val content = webview.getHtml().also {
                    webview.stop()
                    recyclerWebView(webview)
                }
                return@withContext WebViewHelperV2.RenderedResult(
                    strategy,
                    strategy.url,
                    false,
                    content,
                    r
                )
            }else {
                // 拦截 Blob 模式
                val targetRegex = Regex(strategy.callBackRegex)
                webview.webViewClient =
                    object : LightweightGettingWebViewClient(targetRegex, false) {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            webview.evaluateJavascript(blobHookJs)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            webview.evaluateJavascript(strategy.actionJs)
                        }
                    }
                val blobResource = withTimeoutOrNull(strategy.timeOut) {
                    suspendCoroutine { con ->
                        webview.addJavascriptInterface(object : Any() {
                            @JavascriptInterface
                            fun handleWrapper(blobTextData: String) {
                                if (targetRegex.containsMatchIn(blobTextData)) {
                                    webview.removeJavascriptInterface("blobHook")
                                    con.resume(blobTextData)
                                }
                            }
                        }, "blobHook")
                        webview.loadUrl(strategy.url, strategy.header.orEmpty())
                    }
                }
                if (blobResource == null) {
                    webview.stop()
                    recyclerWebView(webview)
                    return@withContext WebViewHelperV2.RenderedResult(
                        strategy,
                        strategy.url,
                        true,
                        "",
                        ""
                    )
                } else {
                    val content = webview.getHtml().also {
                        webview.stop()
                        recyclerWebView(webview)
                    }
                    return@withContext WebViewHelperV2.RenderedResult(
                        strategy,
                        strategy.url,
                        false,
                        content,
                        blobResource
                    )
                }
            }
        }
    }

}

class WebViewCreatedException : Exception("WebView create error")