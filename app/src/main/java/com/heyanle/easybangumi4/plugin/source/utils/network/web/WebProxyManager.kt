package com.heyanle.easybangumi4.plugin.source.utils.network.web

import com.heyanle.easybangumi4.utils.CoroutineProvider
import kotlinx.coroutines.launch

/**
 * Created by heyanle on 2025/10/8
 * https://github.com/heyanLE
 */
class WebProxyManager {

    private val webViewPool = mutableListOf<IWebProxy>()

    fun close() {
        try {
            webViewPool.forEach {
                CoroutineProvider.globalMainScope.launch {
                    it.close()
                }
            }
            webViewPool.clear()
        }catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun addWebProxy(webView: IWebProxy) {
        webViewPool.add(webView)
    }

    fun removeWebProxy(webView: IWebProxy) {
        webViewPool.remove(webView)
    }


}