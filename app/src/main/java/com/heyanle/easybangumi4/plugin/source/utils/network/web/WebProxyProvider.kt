package com.heyanle.easybangumi4.plugin.source.utils.network.web

import com.heyanle.easybangumi4.plugin.js.utils.WebViewProxyKtWrapper
import com.heyanle.easybangumi4.utils.WebViewManager

/**
 * Created by heyanle on 2025/10/8
 * https://github.com/heyanLE
 */
class WebProxyProvider(
    private val webProxyManager: WebProxyManager,
    private val webViewManager: WebViewManager,
) {

    fun getWebProxy(): WebViewProxyKtWrapper? {
        val webProxy = WebProxyImpl(webViewManager)
        webProxyManager.addWebProxy(webProxy)
        return WebViewProxyKtWrapper(webProxy)
    }

}