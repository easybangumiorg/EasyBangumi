package com.easybangumi.next.shared.debug.webview

import androidx.compose.runtime.Composable
import com.easybangumi.next.shared.debug.DebugScope
import org.easybangumi.next.lib.webview.IWebView
import org.easybangumi.next.webkit.WebKitWebViewProxy

actual fun getWebView(): IWebView {
    return WebKitWebViewProxy()
}