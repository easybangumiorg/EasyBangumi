package org.easybangumi.next.shared.debug.webview

import org.easybangumi.next.lib.webview.IWebView
import org.easybangumi.next.webkit.WebKitWebViewProxy
import org.easybangumi.next.webkit.WebViewManager
import org.koin.compose.koinInject
import org.koin.core.Koin
import org.koin.mp.KoinPlatform

actual fun getWebView(): IWebView {
    val webViewManager = KoinPlatform.getKoin().get<WebViewManager>()
    return WebKitWebViewProxy(webViewManager)
}