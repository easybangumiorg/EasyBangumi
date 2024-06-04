package com.heyanle.easybangumi4.source.utils.network

import android.webkit.WebSettings
import android.webkit.WebView
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.source.utils.network.WebViewHelperImpl.cookieManager
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.utils.clearWeb
import com.heyanle.easybangumi4.utils.evaluateJavascript
import com.heyanle.easybangumi4.utils.getHtml
import com.heyanle.easybangumi4.utils.stop
import com.heyanle.easybangumi4.utils.waitUntil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

/**
 * Created by heyanlin on 2024/6/4.
 */
class WebViewHelperV2Impl: WebViewHelperV2 {

    companion object {

        var webViewRef: WeakReference<WebView>? = null
        var check: WeakReference<(WebView) -> Boolean>? = null
        var stop: WeakReference<(WebView) -> Unit>? = null

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
    }

    override fun getGlobalWebView(): WebView {
        return _globalWebView
    }

    override fun openWebPage(onCheck: (WebView) -> Boolean, onStop: (WebView) -> Unit) {
        webViewRef = WeakReference(_globalWebView)
        check = WeakReference(onCheck)
        stop = WeakReference(onStop)
    }

    override fun openWevPage(
        webView: WebView,
        onCheck: (WebView) -> Boolean,
        onStop: (WebView) -> Unit
    ) {
        webViewRef = WeakReference(webView)
        check = WeakReference(onCheck)
        stop = WeakReference(onStop)
    }

    override suspend fun renderedHtml(strategy: WebViewHelperV2.RenderedStrategy): WebViewHelperV2.RenderedResult {
        return withContext(Dispatchers.Main){
            _globalWebView.clearWeb()
            _globalWebView.settings.apply {
                setUserAgentString(userAgentString)
                defaultTextEncodingName = strategy.encoding
            }
            _globalWebView.resumeTimers()


            // 拦截普通资源模式
            _globalWebView.loadUrl(strategy.url, strategy.header.orEmpty())
            var r = _globalWebView.waitUntil(Regex(strategy.callBackRegex), strategy.timeOut, true, ignoreTimeoutExt = true)
            if(r.isNotEmpty() || strategy.actionJs == null){
                val content =  _globalWebView.getHtml().also {
                    _globalWebView.stop()
                }
                return@withContext WebViewHelperV2.RenderedResult(strategy, strategy.url, false, content, r)
            }
            _globalWebView.evaluateJavascript(strategy.actionJs)
            r = try {
                _globalWebView.waitUntil(Regex(strategy.callBackRegex), strategy.timeOut, true, ignoreTimeoutExt = false)
            }catch (e: TimeoutCancellationException){
                return@withContext WebViewHelperV2.RenderedResult(strategy, strategy.url, true, "", "")
            }
            val content =  _globalWebView.getHtml().also {
                _globalWebView.stop()
            }
            return@withContext WebViewHelperV2.RenderedResult(strategy, strategy.url, false, content, r)

            // 拦截 Blob 模式
        }
    }
}