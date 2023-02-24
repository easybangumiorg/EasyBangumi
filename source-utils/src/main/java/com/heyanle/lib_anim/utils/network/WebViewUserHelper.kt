package com.heyanle.lib_anim.utils.network

import android.webkit.WebView

/**
 * Created by HeYanLe on 2023/2/4 14:13.
 * https://github.com/heyanLE
 */
lateinit var webViewUserHelper: WebViewUserHelper

interface WebViewUserHelper {

    fun start(
        webView: WebView,
        check: (WebView) -> Boolean,
        onStop: (WebView) -> Unit,
    )

}