package com.heyanle.easybangumi4.source.utils.network

import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.WEB_VIEW_USER
import com.heyanle.easybangumi4.navControllerRef
import com.heyanle.easybangumi4.source.utils.LightweightGettingWebViewClient
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.utils.clearWeb
import com.heyanle.easybangumi4.utils.evaluateJavascript
import com.heyanle.easybangumi4.utils.getHtml
import com.heyanle.easybangumi4.utils.stop
import com.heyanle.easybangumi4.utils.waitUntil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by heyanlin on 2024/6/4.
 */
class WebViewHelperV2Impl: WebViewHelperV2 {

    companion object {

        var webViewRef: WeakReference<WebView>? = null
        var check: WeakReference<(WebView) -> Boolean>? = null
        var stop: WeakReference<(WebView) -> Unit>? = null


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

        val cookieManager = CookieManager.getInstance()
        private val _globalWebView by lazy(LazyThreadSafetyMode.NONE) {
            WebView(APP).apply {
                // setDefaultSettings()
                with(settings){
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    cacheMode = WebSettings.LOAD_DEFAULT

                    // Allow zooming
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                cookieManager.also {
                    it.setAcceptCookie(true)
                    it.acceptCookie()
                    it.setAcceptThirdPartyCookies(this, true) // 跨域cookie读取
                }
            }
        }
        val globalWebView: WebView get() = _globalWebView
    }

    override fun getGlobalWebView(): WebView {
        return _globalWebView
    }

    override fun openWebPage(onCheck: (WebView) -> Boolean, onStop: (WebView) -> Unit) {
        webViewRef = WeakReference(_globalWebView)
        check = WeakReference(onCheck)
        stop = WeakReference(onStop)
        navControllerRef?.get()?.navigate(WEB_VIEW_USER)
    }

    override fun openWebPage(
        webView: WebView,
        onCheck: (WebView) -> Boolean,
        onStop: (WebView) -> Unit
    ) {
        webViewRef = WeakReference(webView)
        check = WeakReference(onCheck)
        stop = WeakReference(onStop)
        navControllerRef?.get()?.navigate(WEB_VIEW_USER)
    }

    override suspend fun renderedHtml(strategy: WebViewHelperV2.RenderedStrategy): WebViewHelperV2.RenderedResult {
        return withContext(Dispatchers.Main){
            _globalWebView.clearWeb()
            _globalWebView.settings.apply {
                setUserAgentString(strategy.userAgentString ?: userAgentString)
                defaultTextEncodingName = strategy.encoding
            }
            _globalWebView.resumeTimers()


            if (!strategy.isBlockBlob) {
                // 拦截普通资源模式
                _globalWebView.loadUrl(strategy.url, strategy.header.orEmpty())
                var r = _globalWebView.waitUntil(
                    if (strategy.callBackRegex.isEmpty()) null else Regex(strategy.callBackRegex),
                    strategy.timeOut,
                    true,
                    ignoreTimeoutExt = true
                )
                if (r.isNotEmpty() || strategy.actionJs == null) {
                    val content = _globalWebView.getHtml().also {
                        _globalWebView.stop()
                    }
                    return@withContext WebViewHelperV2.RenderedResult(
                        strategy,
                        strategy.url,
                        false,
                        content,
                        r
                    )
                }
                _globalWebView.evaluateJavascript(strategy.actionJs)
                r = try {
                    _globalWebView.waitUntil(
                        if (strategy.callBackRegex.isEmpty()) null else Regex(strategy.callBackRegex),
                        strategy.timeOut,
                        true,
                        ignoreTimeoutExt = false
                    )
                } catch (e: CancellationException) {
                    return@withContext WebViewHelperV2.RenderedResult(
                        strategy,
                        strategy.url,
                        true,
                        "",
                        ""
                    )
                }
                val content = _globalWebView.getHtml().also {
                    _globalWebView.stop()
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
                _globalWebView.webViewClient =
                    object : LightweightGettingWebViewClient(targetRegex, false) {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            _globalWebView.evaluateJavascript(blobHookJs)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            _globalWebView.evaluateJavascript(strategy.actionJs)
                        }
                    }
                val blobResource = withTimeoutOrNull(strategy.timeOut) {
                    suspendCoroutine { con ->
                        _globalWebView.addJavascriptInterface(object : Any() {
                            @JavascriptInterface
                            fun handleWrapper(blobTextData: String) {
                                if (targetRegex.containsMatchIn(blobTextData)) {
                                    _globalWebView.removeJavascriptInterface("blobHook")
                                    con.resume(blobTextData)
                                }
                            }
                        }, "blobHook")
                        _globalWebView.loadUrl(strategy.url, strategy.header.orEmpty())
                    }
                }
                if (blobResource == null) {
                    _globalWebView.stop()
                    return@withContext WebViewHelperV2.RenderedResult(
                        strategy,
                        strategy.url,
                        true,
                        "",
                        ""
                    )
                } else {
                    val content = _globalWebView.getHtml().also {
                        _globalWebView.stop()
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