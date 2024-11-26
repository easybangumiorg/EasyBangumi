package com.heyanle.easybangumi4.plugin.source.utils.network

import android.webkit.WebView
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelper
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.utils.getHtml
import com.heyanle.easybangumi4.utils.stop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by HeYanLe on 2023/2/3 22:39.
 * https://github.com/heyanLE
 */
class WebViewHelperImpl(
    private val webViewHelperV2: WebViewHelperV2
) : WebViewHelper {

    override fun getGlobalWebView(): WebView {
        return webViewHelperV2.getGlobalWebView()
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
        return webViewHelperV2.renderedHtml(
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
        return webViewHelperV2.renderedHtml(
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
        return webViewHelperV2.renderedHtml(
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
        webViewHelperV2.openWebPage(check, stop)
    }
}
