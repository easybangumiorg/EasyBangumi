package org.easybangumi.next.shared.debug.webview

import org.easybangumi.next.lib.webview.IWebView
import org.easybangumi.next.webkit.WebKitWebViewProxy

actual fun getWebView(): IWebView {
    return WebKitWebViewProxy()
}