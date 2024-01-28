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
import com.heyanle.easybangumi4.utils.logi
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

    override fun getGlobalWebView(): WebView {
        return _globalWebView
    }

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
        _globalWebView.clearWeb()
        _globalWebView.settings.apply {
            setUserAgentString(userAgentString)
            setDefaultTextEncodingName(encoding)
        }
        _globalWebView.resumeTimers()
        _globalWebView.loadUrl(url, header.orEmpty())
        _globalWebView.waitUntil(Regex(callBackRegex), timeOut, true)
            .apply {
                ("waitUntil: "+this).logi("WebView.waitUntil")
            }
        _globalWebView.evaluateJavascript(actionJs)
        _globalWebView.getHtml().also {
            _globalWebView.stop()
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
        _globalWebView.clearWeb()
        _globalWebView.settings.userAgentString = userAgentString
        _globalWebView.resumeTimers()
        _globalWebView.loadUrl(url, header.orEmpty())
        val r = _globalWebView.waitUntil(Regex(regex), timeOut, false)
        if(r.isNotEmpty() || actionJs == null){
            return@withContext r
        }
        _globalWebView.evaluateJavascript(actionJs)
        "regex ${regex} ${Regex(regex)}".logi("WebView.waitUntil")
        _globalWebView.waitUntil(Regex(regex), timeOut, true)
    }.apply {
        ("interceptResource: "+this).logi("WebView.waitUntil")
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
        _globalWebView.clearWeb()
        _globalWebView.settings.userAgentString = userAgentString
        _globalWebView.resumeTimers()
        _globalWebView.webViewClient = object : LightweightGettingWebViewClient(targetRegex, false) {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                _globalWebView.evaluateJavascript(blobHookJs)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                _globalWebView.evaluateJavascript(actionJs)
            }
        }
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
            _globalWebView.loadUrl(url, header.orEmpty())
            launch {
                delay(timeOut)
                con.resume("")
            }
        }.also {
            _globalWebView.stop()
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
    private val _globalWebView by lazy(LazyThreadSafetyMode.NONE) {
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
