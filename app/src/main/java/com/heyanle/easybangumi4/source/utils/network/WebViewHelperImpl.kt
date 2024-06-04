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
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.utils.clearWeb
import com.heyanle.easybangumi4.utils.evaluateJavascript
import com.heyanle.easybangumi4.utils.getHtml
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stop
import com.heyanle.easybangumi4.utils.waitUntil
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
class WebViewHelperImpl(
    private val webViewHelperV2Impl: WebViewHelperV2Impl
) : WebViewHelper {

    override fun getGlobalWebView(): WebView {
        return webViewHelperV2Impl.getGlobalWebView()
    }

    override suspend fun getRenderedHtmlCode(
        url: String,
        callBackRegex: String,
        encoding: String,
        userAgentString: String?,
        header: Map<String, String>?,
        actionJs: String?,
        timeOut: Long
    ): String {
        return webViewHelperV2Impl.renderedHtml(
            WebViewHelperV2.RenderedStrategy(
                url, callBackRegex, encoding, userAgentString, header, actionJs, false, timeOut
            )
        ).content
    }

    override suspend fun interceptBlob(
        url: String,
        regex: String,
        userAgentString: String?,
        header: Map<String, String>?,
        actionJs: String?,
        timeOut: Long
    ): String {
        return webViewHelperV2Impl.renderedHtml(
            WebViewHelperV2.RenderedStrategy(
                url, regex, "utf-8", userAgentString, header, actionJs, true, timeOut
            )
        ).interceptResource
    }

    override suspend fun interceptResource(
        url: String,
        regex: String,
        userAgentString: String?,
        header: Map<String, String>?,
        actionJs: String?,
        timeOut: Long
    ): String {
        return webViewHelperV2Impl.renderedHtml(
            WebViewHelperV2.RenderedStrategy(
                url, regex, "utf-8", userAgentString, header, actionJs, false, timeOut
            )
        ).interceptResource
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

    override fun start(webView: WebView, check: (WebView) -> Boolean, stop: (WebView) -> Unit) {
        webViewHelperV2Impl.openWebPage(check, stop)
    }
}
