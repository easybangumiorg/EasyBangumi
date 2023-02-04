package com.heyanle.easybangumi.source.utils

import android.webkit.WebView
import com.heyanle.easybangumi.WEB_VIEW_USER
import com.heyanle.easybangumi.navControllerRef
import com.heyanle.lib_anim.utils.network.WebViewUserHelper
import java.lang.ref.WeakReference

/**
 * Created by HeYanLe on 2023/2/4 14:34.
 * https://github.com/heyanLE
 */
object WebViewUserHelperImpl : WebViewUserHelper {

    var webViewRef: WeakReference<WebView>? = null
    var onCheck: WeakReference<(WebView) -> Boolean>? = null
    var onStop: WeakReference<(WebView) -> Unit>? = null

    override fun start(webView: WebView, check: (WebView) -> Boolean, onStop: (WebView) -> Unit) {
        webViewRef = WeakReference(webView)
        onCheck = WeakReference(check)
        this.onStop = WeakReference(onStop)

        navControllerRef?.get()?.navigate(WEB_VIEW_USER)

    }
}