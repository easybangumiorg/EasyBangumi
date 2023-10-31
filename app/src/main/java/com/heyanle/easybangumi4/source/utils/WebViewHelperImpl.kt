package com.heyanle.easybangumi4.source.utils

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.source_api.utils.api.WebViewHelper
import com.heyanle.injekt.core.Injekt
import java.lang.ref.WeakReference

/**
 * Created by HeYanLe on 2023/10/29 17:38.
 * https://github.com/heyanLE
 */
@SuppressLint("StaticFieldLeak")
object WebViewHelperImpl: WebViewHelper {

    val globalWebView = WebView(APP)
    val webViewRef : WeakReference<WebView>? = null
    val check: WeakReference<(WebView) -> Boolean>? = null
    val stop: WeakReference<(WebView) -> Unit>? = null

    override suspend fun getRenderedHtmlCode(
        url: String,
        callBackRegex: String,
        encoding: String,
        userAgentString: String?,
        header: Map<String, String>?,
        actionJs: String?,
        timeOut: Long
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun interceptBlob(
        url: String,
        regex: String,
        userAgentString: String?,
        header: Map<String, String>?,
        actionJs: String?,
        timeOut: Long
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun interceptResource(
        url: String,
        regex: String,
        userAgentString: String?,
        header: Map<String, String>?,
        actionJs: String?,
        timeOut: Long
    ): String {
        TODO("Not yet implemented")
    }

    override suspend fun start(webView: WebView, check: (WebView) -> Boolean): String {
        TODO("Not yet implemented")
    }

    override fun start(webView: WebView, check: (WebView) -> Boolean, stop: (WebView) -> Unit) {
        TODO("Not yet implemented")
    }
}