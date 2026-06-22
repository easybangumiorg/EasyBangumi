package com.heyanle.easybangumi4.plugin.source.utils.network

import android.webkit.WebView
import com.heyanle.easybangumi4.WEB_VIEW_USER
import com.heyanle.easybangumi4.navControllerRef
import com.heyanle.easybangumi4.plugin.api.utils.api.WebViewHelperV2
import com.heyanle.easybangumi4.utils.WebViewManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class WebViewHelperV2Impl(
    private val webViewManager: WebViewManager,
) : WebViewHelperV2 {

    companion object {
        const val MAX_TRY_COUNT = 3

        var webViewRef: WeakReference<WebView>? = null
        var check: WeakReference<(WebView) -> Boolean>? = null
        var stop: WeakReference<(WebView) -> Unit>? = null

        var webPageShowing = false
    }

    private val scope = MainScope()

    override fun getGlobalWebView(): WebView {
        return getGlobalWebViewOrNull() ?: throw WebViewCreatedException()
    }

    fun getGlobalWebViewOrNull(): WebView? {
        return webViewManager.getWebViewOrNull()
    }

    fun recyclerWebView(webView: WebView) {
        webViewManager.recycle(webView)
    }

    override fun openWebPage(onCheck: (WebView) -> Boolean, onStop: (WebView) -> Unit) {
        scope.launch {
            if (webPageShowing) {
                return@launch
            }
            webPageShowing = true
            webViewRef = WeakReference(getGlobalWebView())
            check = WeakReference(onCheck)
            stop = WeakReference(onStop)
            navControllerRef?.get()?.navigate(WEB_VIEW_USER)
        }
    }

    override fun openWebPage(
        webView: WebView,
        onCheck: (WebView) -> Boolean,
        onStop: (WebView) -> Unit,
    ) {
        scope.launch {
            if (webPageShowing) {
                return@launch
            }
            webPageShowing = true
            webViewRef = WeakReference(webView)
            check = WeakReference(onCheck)
            stop = WeakReference(onStop)
            navControllerRef?.get()?.navigate(WEB_VIEW_USER)
        }
    }

    fun openWebPage(
        webView: WebView,
        tips: String,
        onCheck: (WebView) -> Boolean,
        onStop: (WebView) -> Unit,
    ) {
        scope.launch {
            if (webPageShowing) {
                return@launch
            }
            webPageShowing = true
            webViewRef = WeakReference(webView)
            check = WeakReference(onCheck)
            stop = WeakReference(onStop)
            navControllerRef?.get()?.navigate("$WEB_VIEW_USER?tips=$tips")
        }
    }
}

class WebViewCreatedException : Exception("WebView create error")
