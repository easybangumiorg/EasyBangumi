package com.heyanle.easybangumi4.plugin.api.utils.api

import android.webkit.WebView

interface WebViewHelperV2 {

    fun getGlobalWebView(): WebView

    fun openWebPage(
        onCheck: (WebView) -> Boolean,
        onStop: (WebView) -> Unit,
    )

    fun openWebPage(
        webView: WebView,
        onCheck: (WebView) -> Boolean,
        onStop: (WebView) -> Unit,
    )
}
