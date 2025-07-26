package com.easybangumi.next.shared.debug.webview

import org.easybangumi.next.jcef.JcefWebViewProxy
import org.easybangumi.next.lib.webview.IWebView


actual fun getWebView(): IWebView {
    return JcefWebViewProxy()
}