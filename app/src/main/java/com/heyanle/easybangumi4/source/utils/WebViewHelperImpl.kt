package com.heyanle.easybangumi4.source.utils

import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.WEB_VIEW_USER
import com.heyanle.easybangumi4.navControllerRef
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelper
import com.heyanle.easybangumi4.source_api.utils.core.setDefaultSettings
import com.heyanle.easybangumi4.utils.clearWeb
import com.heyanle.easybangumi4.utils.evaluateJavascript
import com.heyanle.easybangumi4.utils.getHtml
import com.heyanle.easybangumi4.utils.stop
import com.heyanle.easybangumi4.utils.waitUntil
import com.heyanle.easybangumi4.utils.waitUntilLoadFinish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by HeYanLe on 2023/2/3 22:39.
 * https://github.com/heyanLE
 */
object WebViewHelperImpl : WebViewHelper {

    var webViewRef: WeakReference<WebView>? = null
    var check: WeakReference<(WebView) -> Boolean>? = null
    var stop: WeakReference<(WebView) -> Unit>? = null

    override fun start(webView: WebView, check: (WebView) -> Boolean, stop: (WebView) -> Unit) {
        webViewRef = WeakReference(webView)
        WebViewHelperImpl.check = WeakReference(check)
        WebViewHelperImpl.stop = WeakReference(stop)
        navControllerRef?.get()?.navigate(WEB_VIEW_USER)
    }

    override suspend fun start(webView: WebView, check: (WebView) -> Boolean): String {
        return withContext(Dispatchers.Main) {
            suspendCoroutine { con ->
                start(webView, check) {
                    launch(Dispatchers.Main) {
                        con.resume(webView.getHtml())
                        webView.stop()
                    }
                }
            }
        }
    }


    override suspend fun getRenderedHtmlCode(
        url: String,
        callBackRegex: String,
        encoding: String,
        userAgentString: String?,
        header: Map<String, String>?,
        actionJs: String?,
        timeOut: Long
    ): String = withContext(Dispatchers.Main) {
        globalWebView.clearWeb()
        globalWebView.settings.apply {
            setUserAgentString(userAgentString)
            setDefaultTextEncodingName(encoding)
        }
        globalWebView.resumeTimers()
        globalWebView.loadUrl(url, header.orEmpty())
        globalWebView.waitUntil(Regex(callBackRegex), timeOut)
        globalWebView.evaluateJavascript(actionJs)
        globalWebView.getHtml().also {
            globalWebView.stop()
        }
    }

    override suspend fun interceptResource(
        url: String,
        regex: String,
        userAgentString: String?,
        header: Map<String, String>?,
        actionJs: String?,
        timeOut: Long
    ): String = withContext(Dispatchers.Main) {
        globalWebView.clearWeb()
        globalWebView.settings.userAgentString = userAgentString
        globalWebView.resumeTimers()
        globalWebView.loadUrl(url, header.orEmpty())
        globalWebView.waitUntilLoadFinish(timeOut)
        globalWebView.evaluateJavascript(actionJs)
        globalWebView.waitUntil(Regex(regex), timeOut).also {
            globalWebView.stop()
        }
    }

    override suspend fun interceptBlob(
        url: String,
        regex: String,
        userAgentString: String?,
        header: Map<String, String>?,
        actionJs: String?,
        timeOut: Long
    ): String = withContext(Dispatchers.Main) {
        val targetRegex = Regex(regex)
        globalWebView.clearWeb()
        globalWebView.settings.userAgentString = userAgentString
        globalWebView.resumeTimers()
        globalWebView.webViewClient = object : LightweightGettingWebViewClient(targetRegex, false) {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                globalWebView.evaluateJavascript(blobHookJs)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                globalWebView.evaluateJavascript(actionJs)
            }
        }
        suspendCoroutine { con ->
            globalWebView.addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun handleWrapper(blobTextData: String) {
                    if (targetRegex.containsMatchIn(blobTextData)) {
                        globalWebView.removeJavascriptInterface("blobHook")
                        con.resume(blobTextData)
                    }
                }
            }, "blobHook")
            globalWebView.loadUrl(url, header.orEmpty())
            launch {
                delay(timeOut)
                con.resume("")
            }
        }.also {
            globalWebView.stop()
        }
    }


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
    val globalWebView by lazy(LazyThreadSafetyMode.NONE) {
        WebView(APP).apply {
            setDefaultSettings()
            cookieManager.also {
                it.setAcceptCookie(true)
                it.acceptCookie()
                it.setAcceptThirdPartyCookies(this, true) // 跨域cookie读取
            }
        }
    }

}
